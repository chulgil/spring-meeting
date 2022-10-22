package me.chulgil.spring.meeting.modules.account;

import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.account.form.Profile;
import me.chulgil.spring.meeting.modules.main.CurrentUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class SettingsController {


    static final String ROOT = "/";
    static final String SETTINGS = "settings";
    static final String PROFILE = "/profile";

    static final String SETTINGS_PROFILE_VIEW_NAME = SETTINGS + PROFILE;
    static final String SETTINGS_PROFILE_URL = ROOT + SETTINGS + PROFILE;

    private final AccountService accountService;

    @GetMapping(SETTINGS_PROFILE_URL)
    public String profileUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new Profile(account));
        return SETTINGS_PROFILE_VIEW_NAME;
    }

    /**
     * 2가지 문제점
     * 1. Profile을 참조할 때 nullpointexception발생 (기본생성자가 없기때문 )
     * 스프링MVC가 Model Attribute 로 데이터를 받아오려고 할때 Profile의 인스턴스를 자동으로 생성 후 setter 로 주입
     * 이때 파라미터의 Account가 없기 때문에 발생 따라서 디폴트 생성자를 만들어 줘야한다.
     * 2.
     *
     * @param account
     * @param profile
     * @param errors
     * @param model
     * @return
     */
    @PostMapping(SETTINGS_PROFILE_URL)
    public String updateProfile(@CurrentUser Account account, @Valid @ModelAttribute Profile profile, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_PROFILE_VIEW_NAME;
        }
        accountService.updateProfile(account, profile);
        // 1회성 데이터
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:" + SETTINGS_PROFILE_URL;
    }

}
