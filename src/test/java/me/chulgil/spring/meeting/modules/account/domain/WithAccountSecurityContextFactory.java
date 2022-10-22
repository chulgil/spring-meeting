package me.chulgil.spring.meeting.modules.account.domain;

import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.AccountService;
import me.chulgil.spring.meeting.modules.account.form.SignUp;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor
public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount> {

    private final AccountService accountService;

    @Override
    public SecurityContext createSecurityContext(WithAccount withAccount) {
        String nickname = withAccount.value();

        accountService.processNewAccount(this.createSignUp(nickname));


        UserDetails principal = accountService.loadUserByUsername(nickname);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }

    private SignUp createSignUp(String nickname) {
        SignUp signUp = new SignUp();
        signUp.setNickname(nickname);
        signUp.setEmail(nickname + "@test.com");
        signUp.setPassword("password");
        return signUp;
    }
}
