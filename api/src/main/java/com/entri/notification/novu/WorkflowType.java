package com.entri.notification.novu;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkflowType {
    PASSWORD_RESET("RESET_PASSWORD"),
    UPDATED_PASSWORD("UPDATED_PASSWORD"),
    TICKET_CONFIRMATION("TICKET_CONFIRMATION");

    private final String workflowId;
}
