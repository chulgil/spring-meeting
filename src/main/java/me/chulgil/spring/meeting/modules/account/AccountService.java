package me.chulgil.spring.meeting.modules.account;

import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.account.domain.UserAccount;
import me.chulgil.spring.meeting.modules.account.form.SignUp;
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
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final JavaMailSender mail;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Account processNewAccount(SignUp signUp) {
        Account newAccount = saveNewAccount(signUp);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(SignUp signUp) {
        Account account = Account.builder()
                .email(signUp.getEmail())
                .nickname(signUp.getNickname())
                .password(passwordEncoder.encode(signUp.getPassword()))
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

    @Override
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
}
