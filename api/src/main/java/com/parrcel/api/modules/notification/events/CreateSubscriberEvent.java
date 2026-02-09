package com.parrcel.api.modules.notification.events;


import com.parrcel.api.modules.users.model.User;

public record CreateSubscriberEvent(User user) {}