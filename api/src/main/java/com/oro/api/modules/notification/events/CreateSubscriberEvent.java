package com.oro.api.modules.notification.events;


import com.oro.api.modules.user.entity.User;

public record CreateSubscriberEvent(User user) {}