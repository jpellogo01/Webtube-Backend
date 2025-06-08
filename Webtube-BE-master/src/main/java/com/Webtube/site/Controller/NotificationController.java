package com.Webtube.site.Controller;

import com.Webtube.site.Model.Notification;
import com.Webtube.site.Repository.NotificationRepository;
import com.Webtube.site.Repository.UsersRepository;
import com.Webtube.site.Security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:3000/")
@RestController
//@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AUTHOR')")
@RequestMapping("/api/v1")
public class NotificationController {

    @Autowired
    private UsersRepository userRepository;


    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("news/notifications")
    public List<Notification> getNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String loggedInFullName = userDetails.getFullname();
            return notificationRepository.findByAuthor(loggedInFullName);
        } else {
            throw new AccessDeniedException("Authentication details not found.");
        }
    }

    @PostMapping("news/notifications/mark-read/{id}")
    public Notification markNotificationAsRead(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }   

    @DeleteMapping("news/notification/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            Notification notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Comment Not Found"));

            notificationRepository.delete((notification));

            return  ResponseEntity.ok("Deleted");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error while processing the action" + e.getMessage());
        }
    }
}
