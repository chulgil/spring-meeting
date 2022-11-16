package me.chulgil.spring.meeting.modules.meeting;

import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.main.CurrentUser;
import me.chulgil.spring.meeting.modules.meeting.form.MeetingDescriptionForm;
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

@Controller
@RequestMapping("/meeting/{path}/settings")
@RequiredArgsConstructor
public class MeetingSettingController {

    private final MeetingRepository meetingRepository;
    private final MeetingService meetingService;
    private final ModelMapper modelMapper;
    private final MeetingFormValidator meetingFormValidator;

    /**
     * input 스트링으로 들어오는 String 데이터들의 white space를 trim해주는 역할을 한다.
     * 모든 요청이 들어올때마다 해당 method를 거침 (node의 middleware 같은 것 )
     * @param dataBinder
     */
    @InitBinder("meetingSettingForm")
    public void InitBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }


    @GetMapping("/description")
    public String viewMeetingSetting(@CurrentUser Account account, @PathVariable String path, Model model) {
        Meeting meeting = meetingService.getMeetingToUpdateZone(account, path);
        model.addAttribute(account);
        model.addAttribute(meeting);
        model.addAttribute(modelMapper.map(meeting, MeetingDescriptionForm.class));
        return "meeting/settings/description";
    }

    @PostMapping("/description")
    public String updateMeetingInfo(@CurrentUser Account account, @PathVariable String path,
                                  @Valid MeetingDescriptionForm meetingDescriptionForm, Errors errors,
                                  Model model, RedirectAttributes attributes) {
        Meeting meeting = meetingService.getMeetingToUpdateZone(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(meeting);
            return "meeting/settings/description";
        }

        meetingService.updateMeetingDescription(meeting, meetingDescriptionForm);
        attributes.addFlashAttribute("message", "스터디 소개를 수정했습니다.");
        return "redirect:/meeting/" + meeting.getEncodedPath() + "/settings/description";
    }

}
