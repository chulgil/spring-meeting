package me.chulgil.spring.meeting.modules.account.validator;

import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.account.form.NicknameForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return NicknameForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nameForm = (NicknameForm) target;
        Account byNickname = accountRepository.findByNickname(nameForm.getNickname());
        if(byNickname != null) {
            errors.rejectValue("nickname", "wrong.value", "닉네임 사용불가");
        }
    }
}
