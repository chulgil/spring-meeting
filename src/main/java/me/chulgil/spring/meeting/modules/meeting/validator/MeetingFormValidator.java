package me.chulgil.spring.meeting.modules.meeting.validator;

import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.meeting.MeetingRepository;
import me.chulgil.spring.meeting.modules.meeting.form.MeetingForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class MeetingFormValidator implements Validator {

    private final MeetingRepository meetingRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return MeetingForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MeetingForm form = (MeetingForm) target;
        if (meetingRepository.existsByPath(form.getPath())) {
            errors.rejectValue("path", "wrong.path", "아젠다 경로를 사용할 수 없습니다.");
        }
    }
}
