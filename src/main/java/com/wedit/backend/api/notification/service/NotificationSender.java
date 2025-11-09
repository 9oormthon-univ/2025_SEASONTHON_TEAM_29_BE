package com.wedit.backend.api.notification.service;

import com.wedit.backend.api.notification.entity.Notification;


public interface NotificationSender {

    void send(Notification notification);
}
