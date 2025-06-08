package com.Webtube.site.Repository;

import com.Webtube.site.Model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n WHERE n.author = :author ORDER BY n.isRead ASC, n.createdAt DESC")
    List<Notification> findByAuthor(String author);
}
