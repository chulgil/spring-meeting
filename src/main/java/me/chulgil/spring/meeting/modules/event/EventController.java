package me.chulgil.spring.meeting.modules.event;

import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.event.form.EventForm;
import me.chulgil.spring.meeting.modules.main.CurrentAccount;
import me.chulgil.spring.meeting.modules.meeting.Meeting;
import me.chulgil.spring.meeting.modules.meeting.MeetingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/meeting/{path}/event")
@RequiredArgsConstructor
public class EventController {

    static final String EVENT = "/event";
    static final String FORM = "/form";

    private final MeetingService meetingService;

    @GetMapping("/new")
    public String newEventForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Meeting meeting = meetingService.getMeetingWithManager(account, path);
        model.addAttribute(account);
        model.addAttribute(meeting);
        model.addAttribute(new EventForm());
        return EVENT + FORM;
    }


}
