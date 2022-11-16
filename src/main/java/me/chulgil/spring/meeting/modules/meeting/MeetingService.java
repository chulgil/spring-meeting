package me.chulgil.spring.meeting.modules.meeting;


import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.meeting.form.MeetingDescriptionForm;
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
    private final MeetingRepository meetingRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Meeting createNewMeeting(Meeting meeting, Account account) {

        Meeting newMeeting = meetingRepository.save(meeting);
        newMeeting.addManager(account);
        return newMeeting;
    }

    public Meeting getMeeting(String path) {
        Meeting meeting = this.meetingRepository.findByPath(path);
        checkIfExistingMeeting(path, meeting);
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
        Meeting meeting = this.meetingRepository.findMeetingWithZonesByPath(path);
        checkIfExistingMeeting(path, meeting);
        checkIfManager(account, meeting);
        return meeting;
    }

    public void updateMeetingDescription(Meeting meeting, MeetingDescriptionForm descriptionForm) {
        modelMapper.map(descriptionForm, meeting);
        //eventPublisher.publishEvent(new MeetingUpdateEvent(meeting, "아젠다 소개를 수정했습니다."));
    }
}
