package me.chulgil.spring.meeting.modules.meeting;

import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.main.CurrentUser;
import me.chulgil.spring.meeting.modules.meeting.form.MeetingForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MeetingController {

    @GetMapping("/new-meeting")
    public String newMeetingForm(@CurrentUser Account account, Model model) {
        model.addAttribute(new MeetingForm());
        return "meeting/form";
    }
}
