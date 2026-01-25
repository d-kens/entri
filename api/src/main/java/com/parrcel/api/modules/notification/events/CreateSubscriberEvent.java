package com.parrcel.api.modules.notification.events;


import com.parrcel.api.modules.user.entity.User;

public record CreateSubscriberEvent(User user) {}