package me.chulgil.spring.meeting.modules.meeting;

import lombok.AllArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.main.CurrentAccount;
import me.chulgil.spring.meeting.modules.meeting.form.MeetingForm;
import me.chulgil.spring.meeting.modules.meeting.validator.MeetingFormValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

import static me.chulgil.spring.meeting.modules.meeting.MeetingSettingController.MEETING_URL;
import static me.chulgil.spring.meeting.modules.meeting.MeetingSettingController.SETTINGS;

@Controller
@AllArgsConstructor
@RequestMapping("/meeting")
public class MeetingController {

    private final MeetingRepository meetingRepository;
    private final MeetingService meetingService;
    private final ModelMapper modelMapper;
    private final MeetingFormValidator meetingFormValidator;

    static final String ROOT = "/";
    static final String PATH = "/{path}";
    static final String MEMBERS = "/members";
    static final String MEETING = "/meeting";
    static final String VIEW = "/view";
    static final String FORM = "/form";
    static final String NEW = "/new";


    /**
     * input 스트링으로 들어오는 String 데이터들의 white space를 trim해주는 역할을 한다.
     * 모든 요청이 들어올때마다 해당 method를 거침 (node의 middleware 같은 것 )
     *
     * @param dataBinder
     */
    @InitBinder("meetingForm")
    public void InitBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    /**
     * MeetingForm 을 받을때 아젠다 추가시 Path값이 중복되는지 체크
     *
     * @param dataBinder
     */
    @InitBinder("meetingForm")
    public void InitBinderMeetingForm(WebDataBinder dataBinder) {
        dataBinder.addValidators(meetingFormValidator);
    }

    @GetMapping(ROOT)
    public String meetingSettingForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Meeting meeting = meetingService.getMeetingToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(meeting);
        return MEETING_URL;
    }

    @GetMapping(PATH)
    public String viewMeeting(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Meeting meeting = meetingService.getMeeting(path);
        model.addAttribute(account);
        model.addAttribute(meeting);
        return MEETING + VIEW;
    }

    @GetMapping(PATH + MEMBERS)
    public String viewMeetingMembers(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Meeting meeting = meetingService.getMeeting(path);
        model.addAttribute(account);
        model.addAttribute(meeting);
        return MEETING + MEMBERS;
    }


    @GetMapping(NEW)
    public String viewNewMeeting(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new MeetingForm());
        return MEETING + FORM;
    }

    @PostMapping(NEW)
    public String submitNewMeeting(@CurrentAccount Account account, @Valid MeetingForm meetingForm, Errors errors, Model model) {
        if (errors.hasErrors()) {
            return MEETING + FORM;
        }

        Meeting newMeeting = meetingService.createNewMeeting(modelMapper.map(meetingForm, Meeting.class), account);
        return "redirect:" + MEETING + newMeeting.getEncodedURL();
    }

    @GetMapping(PATH + "/join")
    public String joinMeeting(@CurrentAccount Account account, @PathVariable String path) {
        Meeting meeting = meetingRepository.findMeetingWithMembersByPath(path);
        meetingService.addMember(meeting, account);
        return "redirect:"+ MEETING + meeting.getEncodedURL() + MEMBERS;
    }

    @GetMapping(PATH + "/leave")
    public String leaveMeeting(@CurrentAccount Account account, @PathVariable String path) {
        Meeting meeting = meetingRepository.findMeetingWithMembersByPath(path);
        meetingService.removeMember(meeting, account);
        return "redirect:"+ MEETING + meeting.getEncodedURL() + MEMBERS;
    }

}
