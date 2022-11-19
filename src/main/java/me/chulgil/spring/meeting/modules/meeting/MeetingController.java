package me.chulgil.spring.meeting.modules.meeting;

import lombok.AllArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.main.CurrentUser;
import me.chulgil.spring.meeting.modules.meeting.form.MeetingForm;
import me.chulgil.spring.meeting.modules.meeting.validator.MeetingFormValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@AllArgsConstructor
public class MeetingController {

    private final MeetingRepository meetingRepository;
    private final MeetingService meetingService;
    private final ModelMapper modelMapper;
    private final MeetingFormValidator meetingFormValidator;

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

    @GetMapping("meeting")
    public String meetingSettingForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Meeting meeting = meetingService.getMeetingToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(meeting);
        return "meeting/settings/meeting";
    }

    @PostMapping("meeting")
    public String publishMeeting(@CurrentUser Account account, @PathVariable String path,
                                 RedirectAttributes attributes) {
        Meeting meeting = meetingService.getMeetingToUpdateStatus(account, path);
        meetingService.publish(meeting);
        attributes.addFlashAttribute("message", "모임을 공개 했습니다.");
        return "redirect:/meeting/" + meeting.getEncodedPath() + "/settings/meeting";
    }


    @GetMapping("meeting/{path}")
    public String viewMeeting(@CurrentUser Account account, @PathVariable String path, Model model) {
        Meeting meeting = meetingService.getMeeting(path);
        model.addAttribute(account);
        model.addAttribute(meeting);
        return "meeting/view";
    }

    @GetMapping("meeting/{path}/members")
    public String viewMeetingMembers(@CurrentUser Account account, @PathVariable String path, Model model) {
        Meeting meeting = meetingService.getMeeting(path);
        model.addAttribute(account);
        model.addAttribute(meeting);
        return "meeting/members";
    }


    @GetMapping("new-meeting")
    public String viewNewMeeting(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new MeetingForm());
        return "meeting/form";
    }

    @PostMapping("new-meeting")
    public String submitNewMeeting(@CurrentUser Account account, @Valid MeetingForm meetingForm, Errors errors, Model model) {
        if (errors.hasErrors()) {
            return "meeting/form";
        }

        Meeting newMeeting = meetingService.createNewMeeting(modelMapper.map(meetingForm, Meeting.class), account);
        return "redirect:/meeting/" + newMeeting.getEncodedPath();
    }

}
