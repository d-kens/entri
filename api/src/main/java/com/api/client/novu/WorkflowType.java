package com.api.client.novu;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkflowType {
    PASSWORD_RESET("RESET_PASSWORD"),
    PASSWORD_UPDATED("PASSWORD_CHANGE");

    private final String workflowId;
}
