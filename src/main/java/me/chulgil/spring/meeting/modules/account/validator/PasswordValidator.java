package me.chulgil.spring.meeting.modules.account.validator;


import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.form.PasswordForm;
import me.chulgil.spring.meeting.modules.account.form.SignUpForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class PasswordValidator implements Validator {


    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(PasswordForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {

        PasswordForm form = (PasswordForm) target;
        if (!form.getNewPassword().equals(form.getNewPasswordConfirm())) {
            errors.rejectValue("newPassword", "wrong.value", "패스워드 불일치");
        }
    }


}
