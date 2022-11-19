package me.chulgil.spring.meeting.modules.meeting;


import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.meeting.event.MeetingCreatedEvent;
import me.chulgil.spring.meeting.modules.meeting.form.MeetingDescriptionForm;
import me.chulgil.spring.meeting.modules.tag.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingService {

    private final ModelMapper modelMapper;
    private final MeetingRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public Meeting createNewMeeting(Meeting meeting, Account account) {

        Meeting newMeeting = repository.save(meeting);
        newMeeting.addManager(account);
        return newMeeting;
    }

    public Meeting getMeeting(String path) {
        Meeting meeting = this.repository.findByPath(path);
        checkIfExistingMeeting(path, meeting);
        return meeting;
    }

    public Meeting getMeetingToUpdate(Account account, String path) {
        Meeting meeting = this.getMeeting(path);
        checkIfManager(account, meeting);
        return meeting;
    }

    private void checkIfExistingMeeting(String path, Meeting meeting) {
        if (meeting == null) {
            throw new IllegalArgumentException(path + "에 해당하는 아젠다가 없습니다.");
        }
    }

    private void checkIfManager(Account account, Meeting meeting) {
        if (!meeting.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    public void addMember(Meeting meeting, Account account) {
        meeting.addMember(account);
    }

    public void removeMember(Meeting meeting, Account account) {
        meeting.removeMember(account);
    }

    public Meeting getMeetingToUpdateZone(Account account, String path) {
        Meeting meeting = this.repository.findMeetingWithZonesByPath(path);
        checkIfExistingMeeting(path, meeting);
        checkIfManager(account, meeting);
        return meeting;
    }

    public void updateMeetingDescription(Meeting meeting, MeetingDescriptionForm descriptionForm) {
        modelMapper.map(descriptionForm, meeting);
        //eventPublisher.publishEvent(new MeetingUpdateEvent(meeting, "아젠다 소개를 수정했습니다."));
    }

    public Meeting getMeetingToUpdateStatus(Account account, String path) {
        Meeting meeting = this.repository.findMeetingWithManagersByPath(path);
        checkIfExistingMeeting(path, meeting);
        checkIfManager(account, meeting);
        return meeting;
    }

    public void publish(Meeting meeting) {
        meeting.publish();
        this.eventPublisher.publishEvent(new MeetingCreatedEvent(meeting));
    }

    public void disableMeetingBanner(Meeting meeting) {
        meeting.setUseBanner(false);
    }

    public void enableMeetingBanner(Meeting meeting) {
        meeting.setUseBanner(true);
    }

    public void updateMeetingImage(Meeting meeting, String image) {
        meeting.setImage(image);
    }

    public Meeting getMeetingToUpdateTag(Account account, String path) {
        Meeting meeting = repository.findMeetingWithTagsByPath(path);
        checkIfExistingMeeting(path, meeting);
        checkIfManager(account, meeting);
        return meeting;
    }

    public void addTag(Meeting meeting, Tag tag) {
        meeting.getTags().add(tag);
    }

    public void removeTag(Meeting meeting, Tag tag) {
        meeting.getTags().add(tag);
    }
{}
}
