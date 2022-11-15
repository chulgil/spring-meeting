package me.chulgil.spring.meeting.modules.meeting;


import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingService {

    private final MeetingRepository meetingRepository;

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

    public void addMember(Meeting meeting, Account account) {
        meeting.addMember(account);
    }

    public void removeMember(Meeting meeting, Account account) {
        meeting.removeMember(account);
    }
}
