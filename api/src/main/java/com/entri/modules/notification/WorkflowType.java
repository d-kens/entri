package com.entri.modules.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkflowType {
    PASSWORD_RESET("RESET_PASSWORD"),
    UPDATED_PASSWORD("UPDATED_PASSWORD");

    private final String workflowId;
}
