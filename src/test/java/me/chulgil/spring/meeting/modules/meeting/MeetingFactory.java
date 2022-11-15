package me.chulgil.spring.meeting.modules.meeting;

import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetingFactory {

    @Autowired
    MeetingService meetingService;
    @Autowired MeetingRepository meetingRepository;

    public Meeting createMeeting(String path, Account manager) {
        Meeting meeting = new Meeting();
        meeting.setPath(path);
        meetingService.createNewMeeting(meeting, manager);
        return meeting;
    }

}
