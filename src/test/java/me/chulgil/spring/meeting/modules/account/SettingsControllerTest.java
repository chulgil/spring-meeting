package me.chulgil.spring.meeting.modules.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.account.domain.WithAccount;
import me.chulgil.spring.meeting.modules.account.form.SignUpForm;
import me.chulgil.spring.meeting.modules.account.validator.AccountRepository;
import me.chulgil.spring.meeting.modules.tag.Tag;
import me.chulgil.spring.meeting.modules.tag.TagForm;
import me.chulgil.spring.meeting.modules.tag.TagRepository;
import me.chulgil.spring.meeting.modules.zone.Zone;
import me.chulgil.spring.meeting.modules.zone.ZoneForm;
import me.chulgil.spring.meeting.modules.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static me.chulgil.spring.meeting.modules.account.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountService accountService;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ZoneRepository zoneRepository;

    @Autowired
    ObjectMapper objectMapper;

    final static String TEST_ACCOUNT = "testAccount";
    final static String TEST_PASS = "password";

    private Zone testZone = Zone.builder().city("testCity").localNameOfCity("테스트시").province("테스트도").build();

    @BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("testAccount");
        signUpForm.setEmail("test@test.com");
        signUpForm.setPassword("password");
        //accountService.processNewAccount(signUp);
        System.out.println("========================Before");
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    //@WithUserDetails("cglee") // @BeforeEach보다 먼저 실행됨 (사용할수없음)
    //@WithUserDetails(value="cglee", setupBefore = TestExecutionEvent.TEST_EXECUTION) //(사용할수없음)버그가 있음
    @WithAccount(TEST_ACCOUNT)
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

    @WithAccount(TEST_ACCOUNT)
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_error() throws Exception {
        String bio = "길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 너무나도 길게 소개를 수정하는 경우. ";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PROFILE))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account account = accountRepository.findByNickname("testAccount");
        assertNull(account.getBio());
    }


    @WithAccount(TEST_ACCOUNT)
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_form() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
        ;
    }

    @WithAccount(TEST_ACCOUNT)
    @DisplayName("패스워드 수정 - 입력값 정상 ")
    @Test
    void updatePassword_success() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", TEST_PASS)
                .param("newPasswordConfirm", TEST_PASS)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PASSWORD))
                .andExpect(flash().attributeExists("message"))
        ;

        Account account = accountRepository.findByNickname(TEST_ACCOUNT);
        assertTrue(passwordEncoder.matches(TEST_PASS, account.getPassword()));
    }

    @WithAccount(TEST_ACCOUNT)
    @DisplayName("패스워드 수정 - 입력값 패스워드 불일치 ")
    @Test
    void updatePassword_fail() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                        .param("newPassword", TEST_PASS)
                        .param("newPasswordConfirm", "TEST_PASS")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PASSWORD))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"))
        ;
    }


    @WithAccount(TEST_ACCOUNT)
    @DisplayName("태그 수정 폼")
    @Test
    void updateTagForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + TAGS))
                .andExpect(view().name(SETTINGS + TAGS))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whiteList"))
                .andExpect(model().attributeExists("tags"))
        ;
    }

    @WithAccount(TEST_ACCOUNT)
    @DisplayName("계정에 태그 추가")
    @Test
    void addTag() throws Exception{
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");
        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk())
        ;

        // 태그가 계정안에 존재하는지 확인
        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname(TEST_ACCOUNT).getTags().contains(newTag));

    }

    @WithAccount(TEST_ACCOUNT)
    @DisplayName("계정의 지역 정보 수정 폼")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES))
                .andExpect(view().name(SETTINGS + ZONES))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"))
        ;
    }

    @WithAccount(TEST_ACCOUNT)
    @DisplayName("계정의 지역 정보 추가 폼")
    @Test
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk())
        ;

        Account account = accountRepository.findByNickname(TEST_ACCOUNT);
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(account.getZones().contains(zone));
    }


    @WithAccount(TEST_ACCOUNT)
    @DisplayName("계정의 지역 정보 삭제 폼")
    @Test
    void removeZone() throws Exception {
        Account account = accountRepository.findByNickname(TEST_ACCOUNT);
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(account, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk())
        ;

        assertFalse(account.getZones().contains(zone));
    }


}