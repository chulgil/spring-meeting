package me.chulgil.spring.meeting.modules.meeting.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.meeting.Meeting;

@Getter
@RequiredArgsConstructor
public class MeetingCreatedEvent {

    private final Meeting meeting;

}
