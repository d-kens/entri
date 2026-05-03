package com.puuul.api.modules.users.event;

import com.puuul.api.modules.users.entity.User;

public record UserCreatedEvent(User user) {}
