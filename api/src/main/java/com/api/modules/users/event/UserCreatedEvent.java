package com.api.modules.users.event;

import com.api.modules.users.entity.User;

public record UserCreatedEvent(User user) {}
