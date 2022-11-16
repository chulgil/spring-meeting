package me.chulgil.spring.meeting.modules.meeting.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chulgil.spring.meeting.infra.config.AppProperties;
import me.chulgil.spring.meeting.infra.mail.EmailMessage;
import me.chulgil.spring.meeting.infra.mail.EmailService;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.account.validator.AccountRepository;
import me.chulgil.spring.meeting.modules.meeting.Meeting;
import me.chulgil.spring.meeting.modules.meeting.MeetingRepository;
import me.chulgil.spring.meeting.modules.notification.Notification;
import me.chulgil.spring.meeting.modules.notification.NotificationRepository;
import me.chulgil.spring.meeting.modules.notification.NotificationType;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class MeetingEventListener {

    private final MeetingRepository meetingRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;

//    @EventListener
//    public void handleMeetingCreatedEvent(MeetingCreatedEvent meetingCreatedEvent) {
//        Meeting meeting = meetingRepository.findWithTagsAndZonesById(meetingCreatedEvent.getMeeting().getId());
//        Iterable<Account> accounts = accountRepository.findWithTagsAndZonesById(meeting.getTags(), meeting.getZones());
//        accounts.forEach(account -> {
//            if (account.isMeetingCreatedByEmail()) {
//                sendMeetingCreatedEmail(meeting, account, "새로운 아젠다가 생겼습니다",
//                        "모임, '" + meeting.getTitle() + "' 아젠다가 생겼습니다.");
//            }
//
//            if (account.isMeetingCreatedByWeb()) {
//                createNotification(meeting, account, meeting.getShortDescription(), NotificationType.MEETING_CREATED);
//            }
//        });
//    }
//
//    @EventListener
//    public void handleMeetingUpdateEvent(MeetingUpdateEvent meetingUpdateEvent) {
//        Meeting meeting = meetingRepository.findMeetingWithManagersAndMemebersById(meetingUpdateEvent.getMeeting().getId());
//        Set<Account> accounts = new HashSet<>();
//        accounts.addAll(meeting.getManagers());
//        accounts.addAll(meeting.getMembers());
//
//        accounts.forEach(account -> {
//            if (account.isMeetingUpdatedByEmail()) {
//                sendMeetingCreatedEmail(meeting, account, meetingUpdateEvent.getMessage(),
//                        "모임, '" + meeting.getTitle() + "' 모임에 새소식이 있습니다.");
//            }
//
//            if (account.isMeetingUpdatedByWeb()) {
//                createNotification(meeting, account, meetingUpdateEvent.getMessage(), NotificationType.MEETING_UPDATED);
//            }
//        });
//    }

    private void createNotification(Meeting meeting, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(meeting.getTitle());
        notification.setLink("/meeting/" + meeting.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    private void sendMeetingCreatedEmail(Meeting meeting, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/meeting/" + meeting.getEncodedPath());
        context.setVariable("linkName", meeting.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

}