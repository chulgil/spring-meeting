package me.chulgil.spring.meeting.modules.account;

import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.account.validator.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFactory {
    @Autowired
    AccountRepository accountRepository;

    public Account createAccount(String nickname) {
        Account account = new Account();
        account.setNickname(nickname);
        account.setEmail(nickname + "@email.com");
        accountRepository.save(account);
        return account;
    }

}
