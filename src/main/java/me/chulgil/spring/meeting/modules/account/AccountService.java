package me.chulgil.spring.meeting.modules.account;

import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.account.domain.UserAccount;
import me.chulgil.spring.meeting.modules.account.form.Profile;
import me.chulgil.spring.meeting.modules.account.form.SignUpForm;
import me.chulgil.spring.meeting.modules.account.validator.AccountRepository;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final JavaMailSender mail;
    private final PasswordEncoder passwordEncoder;

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
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("회원 가입 인증");
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());

        mail.send(mailMessage);
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
        account.setUrl(profile.getUrl());
        account.setOccupation(profile.getOccupation());
        account.setBio(profile.getBio());
        account.setLocation(profile.getLocation());
        account.setProfileImage(profile.getProfileImage());

        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }


}
