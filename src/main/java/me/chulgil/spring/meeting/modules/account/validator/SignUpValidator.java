package me.chulgil.spring.meeting.modules.account.validator;


import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.form.SignUp;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUp.class);
    }

    @Override
    public void validate(Object target, Errors errors) {

        SignUp signUp = (SignUp) target;
        if (accountRepository.existsByEmail(signUp.getEmail())) {
            errors.rejectValue("email", "invalid.email", new Object[]{signUp.getEmail()}, "이미 사용중인 Email 입니다.");
        }

        if (accountRepository.existsByNickname(signUp.getNickname())) {
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{signUp.getEmail()}, "이미 사용중인 닉테임 입니다.");
        }

    }





}
