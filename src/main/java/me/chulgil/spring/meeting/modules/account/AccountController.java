package me.chulgil.spring.meeting.modules.account;

import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.account.form.SignUp;
import me.chulgil.spring.meeting.modules.account.validator.AccountRepository;
import me.chulgil.spring.meeting.modules.account.validator.SignUpValidator;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpValidator validator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;


    @InitBinder("signUp")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(validator);
    }

    @GetMapping("sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUp());
        return "account/sign-up";
    }

    @PostMapping("sign-up")
    public String signUpSubmit(@Valid SignUp signUp, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }
        accountService.processNewAccount(signUp);


        return "redirect:/";
    }

    @GetMapping("check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";
        if (account == null) {
            model.addAttribute("error", "invalid.email");
            return view;
        }

        if(!account.getEmailCheckToken().equals(token)) {
            model.addAttribute("error", "invalid.token");
            return view;
        }

        account.completeSignUp();
        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return view;
    }
}
