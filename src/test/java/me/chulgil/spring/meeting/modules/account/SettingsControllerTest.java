package me.chulgil.spring.meeting.modules.account;

import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.account.domain.WithAccount;
import me.chulgil.spring.meeting.modules.account.form.SignUp;
import me.chulgil.spring.meeting.modules.account.validator.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static me.chulgil.spring.meeting.modules.account.SettingsController.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;


    @BeforeEach
    void beforeEach() {
        SignUp signUp = new SignUp();
        signUp.setNickname("testAccount");
        signUp.setEmail("test@test.com");
        signUp.setPassword("password");
        //accountService.processNewAccount(signUp);
        System.out.println("========================Before");
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    //@WithUserDetails("cglee") // @BeforeEach보다 먼저 실행됨 (사용할수없음)
    //@WithUserDetails(value="cglee", setupBefore = TestExecutionEvent.TEST_EXECUTION) //(사용할수없음)버그가 있음
    @WithAccount("testAccount")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        String bio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PROFILE))
                .andExpect(flash().attributeExists("message"));

        Account keesun = accountRepository.findByNickname("testAccount");
        assertEquals(bio, keesun.getBio());
    }

}