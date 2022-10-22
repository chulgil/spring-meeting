package me.chulgil.spring.meeting.modules.account.form;

import lombok.Data;

@Data
public class Notifications {

    private boolean createdByEmail;

    private boolean createdByWeb;

    private boolean enrollmentByEmail;

    private boolean enrollmentByWeb;

    private boolean updatedByEmail;

    private boolean updatedByWeb;

}
