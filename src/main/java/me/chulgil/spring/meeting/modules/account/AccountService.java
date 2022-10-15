package me.chulgil.spring.meeting.modules.account;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.account.form.SignUp;
import me.chulgil.spring.meeting.modules.account.validator.AccountRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository repository;
    private final JavaMailSender mail;
    private final PasswordEncoder passwordEncoder;

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

        Account newAccount = repository.save(account);

        return newAccount;
    }

    private void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("회원 가입 인증");
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());

        mail.send(mailMessage);
    }

    @Transactional
    public void processNewAccount(SignUp signUp) {
        Account newAccount = saveNewAccount(signUp);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);
    }
}
