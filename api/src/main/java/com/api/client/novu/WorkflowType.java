package com.api.client.novu;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkflowType {
    PASSWORD_RESET("RESET_PASSWORD");

    private final String workflowId;
}
