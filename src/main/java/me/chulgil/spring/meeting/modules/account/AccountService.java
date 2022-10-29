package me.chulgil.spring.meeting.modules.account;

import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.infra.config.AppProperties;
import me.chulgil.spring.meeting.infra.mail.EmailMessage;
import me.chulgil.spring.meeting.infra.mail.EmailService;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.account.domain.UserAccount;
import me.chulgil.spring.meeting.modules.account.form.Notifications;
import me.chulgil.spring.meeting.modules.account.form.Profile;
import me.chulgil.spring.meeting.modules.account.form.SignUpForm;
import me.chulgil.spring.meeting.modules.account.validator.AccountRepository;
import me.chulgil.spring.meeting.modules.tag.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final JavaMailSender mail;
    private final PasswordEncoder passwordEncoder;

    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final EmailService emailService;

    @Transactional
    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .emailVerified(false)
                .createdByWeb(true)
                .updatedByWeb(true)
                .enrollmentByWeb(true)
                .build();

        Account newAccount = accountRepository.save(account);

        return newAccount;
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("회원 가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }


    public void login(Account account) {
        // 인코딩한 패스워드접근해야 하기때문에 이방법을 사용
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);

        // 정석적인 방법
//        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                account.getEmail(), account.getPassword());
//        Authentication authentication = authenticationManager.authenticate(authToken);
//        context.setAuthentication(authentication);
    }


    /**
     * 앍기 전용 트랜잭션을 사용하면 WriteLock을 사용하지 않아서 성능 향상이 된다.
     * @param emailOrNickname
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);
        if (account == null) {
            account = accountRepository.findByNickname(emailOrNickname);
        }

        if (account == null) {
            throw new UsernameNotFoundException(emailOrNickname);
        }
        return new UserAccount(account);
    }

    public Account getAccount(String nickname) {
        Account account = accountRepository.findByNickname(nickname);
        if (account == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }
        return account;
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        this.login(account);
    }

    /**
     * repository에 save를 하지 않을경우 저장이 되지 않음
     * 이유 : Settings Controller의 updateProfile(@CurrentUser Account)객체이기때문에
     * 이 Account 객체의 상태는 (Persist, Transient(신규), Detached(ID값이 있음), Deleted)중 무엇일까?
     * Authentication안에 저장된 Principal 정보의 객체이다.
     * 따라서 이미 트랜잭션이 끝난 객체로 Detached상태의 영속성 컨테이너에서 관리되지 않은 객체이다.
     * 이 경우 Sync하는 방법은 리포지토리에서 save한다.
     * 이 save구현체 안에서 id값이 있는지 체크해서 merge를 시킨다.
     * @param account
     * @param profile
     */
    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile, account);
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications, account);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        this.login(account);
    }

    public void sendLoginLink(Account account) {
        Context context = new Context();
        context.setVariable("link", "/login-by-email?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "로그인하기");
        context.setVariable("message", "로그인 하려면 아래 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("로그인 링크")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag));
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(t -> t.getTags().remove(tag));
    }
}
