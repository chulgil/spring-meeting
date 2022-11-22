package me.chulgil.spring.meeting.modules.meeting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.main.CurrentAccount;
import me.chulgil.spring.meeting.modules.meeting.form.MeetingDescriptionForm;
import me.chulgil.spring.meeting.modules.meeting.validator.MeetingFormValidator;
import me.chulgil.spring.meeting.modules.tag.Tag;
import me.chulgil.spring.meeting.modules.tag.TagForm;
import me.chulgil.spring.meeting.modules.tag.TagRepository;
import me.chulgil.spring.meeting.modules.tag.TagService;
import me.chulgil.spring.meeting.modules.zone.Zone;
import me.chulgil.spring.meeting.modules.zone.ZoneForm;
import me.chulgil.spring.meeting.modules.zone.ZoneRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/meeting/{path}/settings")
@RequiredArgsConstructor
public class MeetingSettingController {

    static final String SETTINGS = "/settings";
    static final String TAGS = "/tags";
    static final String ZONES = "/zones";
    static final String BANNER = "/banner";
    static final String MEETING = "/meeting";
    static final String ADD = "/add";
    static final String REMOVE = "/remove";
    static final String DISABLE = "/disable";
    static final String ENABLE = "/enable";
    static final String DESCRIPTION = "/description";
    static final String RECRUIT = "/recruit";
    static final String DESCRIPTION_URL = MEETING + SETTINGS + DESCRIPTION;
    static final String BANNER_URL = MEETING + SETTINGS + BANNER;
    static final String TAGS_URL = MEETING + SETTINGS + TAGS;
    static final String ZONES_URL = MEETING + SETTINGS + ZONES;
    static final String MEETING_URL = MEETING + SETTINGS + MEETING;

    private final MeetingRepository meetingRepository;
    private final MeetingService meetingService;
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;
    private final TagService tagService;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    private final MeetingFormValidator meetingFormValidator;

    /**
     * input 스트링으로 들어오는 String 데이터들의 white space를 trim해주는 역할을 한다.
     * 모든 요청이 들어올때마다 해당 method를 거침 (node의 middleware 같은 것 )
     *
     * @param dataBinder
     */
    @InitBinder("meetingSettingForm")
    public void InitBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }


    @GetMapping(DESCRIPTION)
    public String viewMeetingSetting(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Meeting meeting = meetingService.getMeetingToUpdateZone(account, path);
        model.addAttribute(account);
        model.addAttribute(meeting);
        model.addAttribute(modelMapper.map(meeting, MeetingDescriptionForm.class));
        return DESCRIPTION_URL;
    }

    @PostMapping(DESCRIPTION)
    public String updateMeetingInfo(@CurrentAccount Account account, @PathVariable String path,
                                    @Valid MeetingDescriptionForm meetingDescriptionForm, Errors errors,
                                    Model model, RedirectAttributes attributes) {
        Meeting meeting = meetingService.getMeetingToUpdateZone(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(meeting);
            return DESCRIPTION_URL;
        }

        meetingService.updateMeetingDescription(meeting, meetingDescriptionForm);
        attributes.addFlashAttribute("message", "스터디 소개를 수정했습니다.");
        return "redirect:" + MEETING + meeting.getEncodedURL() + SETTINGS + DESCRIPTION;
    }

    @GetMapping(BANNER)
    public String viewMeetingBanner(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Meeting meeting = meetingService.getMeetingToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(meeting);
        return BANNER_URL;
    }

    @PostMapping(BANNER)
    public String submitMeetingImage(@CurrentAccount Account account, @PathVariable String path,
                                     String image, RedirectAttributes attributes) {
        Meeting meeting = meetingService.getMeetingToUpdate(account, path);
        meetingService.updateMeetingImage(meeting, image);
        attributes.addFlashAttribute("message", "배너 이미지를 수정하였습니다.");
        return "redirect:" + MEETING + meeting.getEncodedURL() + SETTINGS + BANNER;
    }

    @PostMapping(BANNER + DISABLE)
    public String disableMeetingBanner(@CurrentAccount Account account, @PathVariable String path) {
        Meeting meeting = meetingService.getMeetingToUpdate(account, path);
        meetingService.disableMeetingBanner(meeting);
        return "redirect:" + MEETING + meeting.getEncodedURL() + SETTINGS + BANNER;
    }

    @PostMapping(BANNER + ENABLE)
    public String enableMeetingBanner(@CurrentAccount Account account, @PathVariable String path) {
        Meeting meeting = meetingService.getMeetingToUpdate(account, path);
        meetingService.enableMeetingBanner(meeting);
        return "redirect:" + MEETING + meeting.getEncodedURL() + SETTINGS + BANNER;
    }

    @GetMapping(TAGS)
    public String viewMeetingTags(@CurrentAccount Account account, @PathVariable String path, Model model)
            throws JsonProcessingException {
        Meeting meeting = meetingService.getMeetingToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(meeting);
        model.addAttribute("tags", meeting.getTags().stream()
                .map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTagsTitle = tagRepository.findAll().stream()
                .map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTagsTitle));
        return TAGS_URL;
    }

    @PostMapping(TAGS + ADD)
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @PathVariable String path,
                                 @RequestBody TagForm tagForm) {
        Meeting meeting = meetingService.getMeetingToUpdateTag(account, path);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        meetingService.addTag(meeting, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping(TAGS + REMOVE)
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @PathVariable String path,
                                    @RequestBody TagForm tagForm) {
        Meeting meeting = meetingService.getMeetingToUpdateTag(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        meetingService.removeTag(meeting, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping(ZONES)
    public String viewMeetingZones(@CurrentAccount Account account, @PathVariable String path, Model model)
            throws JsonProcessingException {

        Meeting meeting = meetingService.getMeetingToUpdateZone(account, path);
        model.addAttribute(account);
        model.addAttribute(meeting);
        model.addAttribute("zones", meeting.getZones().stream()
                .map(Zone::toString).collect(Collectors.toList()));

        List<String> allZones = zoneRepository.findAll().stream()
                .map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));
        return ZONES_URL;
    }

    @PostMapping(ZONES + ADD)
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @PathVariable String path,
                                  @RequestBody ZoneForm zoneForm) {
        Meeting meeting = meetingService.getMeetingToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }
        meetingService.addZone(meeting, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping(ZONES + REMOVE)
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @PathVariable String path,
                                     @RequestBody ZoneForm zoneForm) {
        Meeting meeting = meetingService.getMeetingToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }
        meetingService.removeZone(meeting, zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping(MEETING)
    public String meetingSettingForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Meeting meeting = meetingService.getMeetingToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(meeting);
        return MEETING_URL;
    }

    @PostMapping(MEETING + ENABLE)
    public String enableMeeting(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        Meeting meeting = meetingService.getMeetingWithManager(account, path);
        meetingService.enable(meeting);
        attributes.addFlashAttribute("message", "모임을 활성화 했습니다.");
        return "redirect:" + MEETING + meeting.getEncodedURL() + SETTINGS + MEETING;
    }

    @PostMapping(MEETING + DISABLE)
    public String disableMeeting(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        Meeting meeting = meetingService.getMeetingWithManager(account, path);
        meetingService.disable(meeting);
        attributes.addFlashAttribute("message", "모임을 종료 했습니다.");
        return "redirect:" + MEETING + meeting.getEncodedURL() + SETTINGS + MEETING;
    }

    @PostMapping(RECRUIT + ENABLE)
    public String enableRecruit(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        Meeting meeting = meetingService.getMeetingWithManager(account, path);
        if (!meeting.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:" + MEETING + meeting.getEncodedURL() + SETTINGS + MEETING;
        }
        meetingService.enableRecruit(meeting);
        attributes.addFlashAttribute("message", "인원 모집을 활성화 했습니다.");
        return "redirect:" + MEETING + meeting.getEncodedURL() + SETTINGS + MEETING;
    }

    @PostMapping(RECRUIT + DISABLE)
    public String disableRecruit(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        Meeting meeting = meetingService.getMeetingWithManager(account, path);
        if (!meeting.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:" + MEETING + meeting.getEncodedURL() + SETTINGS + MEETING;
        }

        meetingService.disableRecruit(meeting);
        attributes.addFlashAttribute("message", "인원 모집을 해제 했습니다.");
        return "redirect:" + MEETING + meeting.getEncodedURL() + SETTINGS + MEETING;
    }

    @PostMapping(MEETING + "/path")
    public String updateMeetingPath(@CurrentAccount Account account, @PathVariable String path, String newPath,
                                    Model model, RedirectAttributes attributes) {
        Meeting meeting = meetingService.getMeetingWithManager(account, path);
        if (!meetingService.isValidPath(newPath)) {
            model.addAttribute(account);
            model.addAttribute(meeting);
            model.addAttribute("meetingPathError", "해당 경로는 사용할 수 없습니다. 다른 값을 입력하세요.");
            return MEETING_URL;
        }

        meetingService.updateMeetingPath(meeting, newPath);
        attributes.addFlashAttribute("message", "모임 경로를 수정했습니다.");
        return "redirect:" + MEETING + meeting.getEncodedURL() + SETTINGS + MEETING;
    }

    @PostMapping(MEETING + "/title")
    public String updateMeetingTitle(@CurrentAccount Account account, @PathVariable String path, String newTitle,
                                     Model model, RedirectAttributes attributes) {
        Meeting meeting = meetingService.getMeetingWithManager(account, path);
        if (!meetingService.isValidTitle(newTitle)) {
            model.addAttribute(account);
            model.addAttribute(meeting);
            model.addAttribute("meetingTitleError", "모임 이름을 다시 입력하세요.");
            return MEETING_URL;
        }

        meetingService.updateMeetingTitle(meeting, newTitle);
        attributes.addFlashAttribute("message", "수정했습니다.");
        return "redirect:" + MEETING + meeting.getEncodedURL() + SETTINGS + MEETING;
    }

    @PostMapping(MEETING + REMOVE)
    public String removeMeeting(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Meeting meeting = meetingService.getMeetingWithManager(account, path);
        meetingService.remove(meeting);
        return "redirect:/";
    }
}
