package com.Webtube.site.Repository;

import com.Webtube.site.Model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByAuthor(String author);
}
