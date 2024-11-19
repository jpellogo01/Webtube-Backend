package com.Webtube.site.Controller;

import com.Webtube.site.Model.News;
import com.Webtube.site.Model.Notification;
import com.Webtube.site.Repository.NewsRepository;
import com.Webtube.site.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:3000/")
@RestController
//@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AUTHOR')")
@RequestMapping("/api/v1")
public class AdminApprovalController {
    // Get all pending news articles
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private NotificationRepository notificationRepository;


    @GetMapping("news/pending")
    public List<News> getAllPendingNews() {
        return newsRepository.findByStatus("Pending");
    }

    // Get all approved news articles
    @GetMapping("news/approved")
    public List<News> getAllApprovedNews() {
        return newsRepository.findByStatus("Approved");
    }

    // Get all rejected news articles
    @GetMapping("news/rejected")
    public List<News> getAllRejectedNews() {
        return newsRepository.findByStatus("Rejected");
    }


    //ADMIN DASHBOARD CONTROLLER
    @PostMapping("news/approve/{id}")
    public News approveNews(@PathVariable Long id) {
        News news = newsRepository.findById(id).orElseThrow(() -> new RuntimeException("News not found"));
        news.setStatus("Approved");
        newsRepository.save(news);

        // Create and save a notification for the author
        Notification notification = new Notification();
        notification.setAuthor(news.getAuthor());
        notification.setMessage("Your content titled '" + news.getTitle() + "' has been approved.");
        notificationRepository.save(notification);

        return news;
    }

    @PostMapping("news/reject/{id}")
    public News rejectNews(@PathVariable Long id) {
        News news = newsRepository.findById(id).orElseThrow(() -> new RuntimeException("News not found"));
        news.setStatus("Rejected");
        newsRepository.save(news);
        // Create and save a notification for the author
        Notification notification = new Notification();
        notification.setAuthor(news.getAuthor());
        notification.setMessage("Your content titled '" + news.getTitle() + "' has been rejected.");
        notificationRepository.save(notification);

        return news;
    }
}
