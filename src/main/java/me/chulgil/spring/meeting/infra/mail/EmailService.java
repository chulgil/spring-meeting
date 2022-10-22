package me.chulgil.spring.meeting.infra.mail;

public interface EmailService {

    void sendEmail(EmailMessage emailMessage);
}
