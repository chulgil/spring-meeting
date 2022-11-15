package me.chulgil.spring.meeting.modules.meeting;

import me.chulgil.spring.meeting.infra.AbstractContainerBaseTest;
import me.chulgil.spring.meeting.infra.MockMvcTest;
import me.chulgil.spring.meeting.modules.account.AccountFactory;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.account.domain.WithAccount;
import me.chulgil.spring.meeting.modules.account.validator.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@MockMvcTest
class MeetingControllerTest extends AbstractContainerBaseTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    MeetingService meetingService;
    @Autowired
    MeetingRepository meetingRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    AccountFactory accountFactory;
    @Autowired
    MeetingFactory meetingFactory;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }


    final static String TEST_ACCOUNT = "test_account";

    @Test
    @WithAccount(TEST_ACCOUNT)
    @DisplayName("아젠다 개설 폼 조회")
    void createMeetingForm() throws Exception {
        mockMvc.perform(get("/new-meeting"))
                .andExpect(status().isOk())
                .andExpect(view().name("meeting/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("meetingForm"));
    }


    @Test
    @WithAccount(TEST_ACCOUNT)
    @DisplayName("아젠다 개설 - 완료")
    void createMeeting_success() throws Exception {
        mockMvc.perform(post("/new-meeting")
                        .param("path", "test-path")
                        .param("title", "meeting title")
                        .param("shortDescription", "short description of a meeting")
                        .param("fullDescription", "full description of a meeting")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/meeting/test-path"));

        Meeting meeting = meetingRepository.findByPath("test-path");
        assertNotNull(meeting);
        Account account = accountRepository.findByNickname(TEST_ACCOUNT);
        assertTrue(meeting.getManagers().contains(account));
    }

    @Test
    @WithAccount(TEST_ACCOUNT)
    @DisplayName("아젠다 개설 - 실패")
    void createMeeting_fail() throws Exception {
        mockMvc.perform(post("/new-meeting")
                        .param("path", "wrong path")
                        .param("title", "meeting title")
                        .param("shortDescription", "short description of a meeting")
                        .param("fullDescription", "full description of a meeting")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("meeting/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("meetingForm"))
                .andExpect(model().attributeExists("account"));

        Meeting meeting = meetingRepository.findByPath("test-path");
        assertNull(meeting);
    }

    @Test
    @WithAccount(TEST_ACCOUNT)
    @DisplayName("아젠다 조회")
    void viewMeeting() throws Exception {
        Meeting meeting = new Meeting();
        meeting.setPath("test-path");
        meeting.setTitle("test meeting");
        meeting.setShortDescription("short description");
        meeting.setFullDescription("<p>full description</p>");

        Account account = accountRepository.findByNickname(TEST_ACCOUNT);
        meetingService.createNewMeeting(meeting, account);

        mockMvc.perform(get("/meeting/test-path"))
                .andExpect(view().name("meeting/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("meeting"));
    }

    @Test
    @WithAccount(TEST_ACCOUNT)
    @DisplayName("아젠다 가입")
    void joinMeeting() throws Exception {
        Account whiteship = accountFactory.createAccount(TEST_ACCOUNT);
        Meeting meeting = meetingFactory.createMeeting("test-meeting", whiteship);

        mockMvc.perform(get("/meeting/" + meeting.getPath() + "/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/meeting/" + meeting.getPath() + "/members"));

        Account account = accountRepository.findByNickname(TEST_ACCOUNT);
        assertTrue(meeting.getMembers().contains(account));
    }

    @Test
    @WithAccount(TEST_ACCOUNT)
    @DisplayName("아젠다 탈퇴")
    void leaveMeeting() throws Exception {
        Account newAccount = accountFactory.createAccount(TEST_ACCOUNT);
        Meeting meeting = meetingFactory.createMeeting("test-meeting", newAccount);
        Account account = accountRepository.findByNickname(TEST_ACCOUNT);
        meetingService.addMember(meeting, account);

        mockMvc.perform(get("/meeting/" + meeting.getPath() + "/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/meeting/" + meeting.getPath() + "/members"));

        assertFalse(meeting.getMembers().contains(account));
    }


}