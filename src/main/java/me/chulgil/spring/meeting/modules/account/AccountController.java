package me.chulgil.spring.meeting.modules.account;

import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.account.form.SignUpForm;
import me.chulgil.spring.meeting.modules.account.validator.AccountRepository;
import me.chulgil.spring.meeting.modules.account.validator.SignUpValidator;
import me.chulgil.spring.meeting.modules.main.CurrentUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

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
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);

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

        if (!account.isValidToken(token)) {
            model.addAttribute("error", "invalid.token");
            return view;
        }

        accountService.completeSignUp(account);
        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return view;
    }

    @GetMapping("check-email")
    public String checkEmail(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    @GetMapping("resend-confirm-email")
    public String resendConfirmEmail(@CurrentUser Account account, Model model) {
        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 1분에 한번만 전송할 수 있습니다.");
            model.addAttribute(account);
            return "account/check-email";
        }
        accountService.sendSignUpConfirmEmail(account);

        // 동일 URL 남아있으면 계속해서 메일을 보내는 문제를 해결하기위해 리다이렉트함
        return "redirect:/";
    }

    @GetMapping("profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account
    ) throws IllegalAccessException {
        Account accountToView = accountService.getAccount(nickname);
        if (nickname == null) {
            throw new IllegalAccessException(nickname + "에 해당하는 사용자가 없습니다.");
        }
        model.addAttribute("account", accountToView);
        model.addAttribute("isOwner", accountToView.equals(account));

        return "account/profile";
    }


}
