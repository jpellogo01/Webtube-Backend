package com.Webtube.site.Controller;

import com.Webtube.site.Exception.NewsNotFoundException;
import com.Webtube.site.Model.Comment;
import com.Webtube.site.Model.News;
import com.Webtube.site.Model.Notification;
import com.Webtube.site.Repository.CommentRepository;
import com.Webtube.site.Repository.NewsRepository;
import com.Webtube.site.Repository.NotificationRepository;
import com.Webtube.site.payload.request.NewsApprovalRequest;
import com.Webtube.site.payload.response.CommentWithNewsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    private CommentRepository commentRepository;


    @GetMapping("pending/news")
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


    // Modify to use @RequestBody for accepting JSON payload
    @PostMapping("approve/news/{id}")
    public News approveNews(@PathVariable Long id,
                            @RequestBody NewsApprovalRequest request) {

        News news = newsRepository.findById(id).orElseThrow(() -> new RuntimeException("News not found"));

        if ("Approved".equals(news.getStatus())) {
            throw new RuntimeException("News is already approved.");
        }

        // If publicationDate is provided, check if it's in the future
        if (request.getPublicationDate() != null) {
            ZonedDateTime requestedPublicationDate = request.getPublicationDate();  // Use the provided publication date directly
            ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("Asia/Manila"));

            // Check if the publicationDate is in the future
            if (requestedPublicationDate.isBefore(currentTime)) {
                throw new RuntimeException("The publication date must be in the future.");
            }

            // If it's valid, set the publicationDate
            news.setPublicationDate(Date.from(requestedPublicationDate.toInstant()));
        } else {
            // If no publicationDate is provided, publish immediately in PHT
            ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("Asia/Manila"));
            news.setPublicationDate(Date.from(zdt.toInstant()));
        }

        news.setStatus("Approved");
        newsRepository.save(news);

        // Create and save a notification for the author
        Notification notification = new Notification();
        notification.setAuthor(news.getAuthor());
        notification.setMessage("Your content titled '" + news.getTitle() + "' has been approved and will be published at "
                + (request.getPublicationDate() != null ? request.getPublicationDate() : "now") + ".");
        notificationRepository.save(notification);

        return news;
    }


    @PostMapping("reject/news/{id}")
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


    @GetMapping("news/comments/pending")
    public List<CommentWithNewsDTO> getAllComments() {
        // Fetch all pending comments
        List<Comment> comments = commentRepository.findByStatus("pending");

        // Convert to DTO list
        List<CommentWithNewsDTO> result = new ArrayList<>();
        for (Comment comment : comments) {
            // Get the news title associated with the comment
            String newsTitle = comment.getNews() != null ? comment.getNews().getTitle() : null;

            // Add the comment data along with the news title to the result list
            result.add(new CommentWithNewsDTO(
                    comment.getId(),
                    comment.getContent(),
                    comment.getStatus(),
                    newsTitle
            ));
        }

        return result;
    }

    @GetMapping("news/comments/pending/{id}")
    public ResponseEntity<CommentWithNewsDTO> getPendingCommentById(@PathVariable Long id) {
        // Fetch the pending comment by ID
        Optional<Comment> commentOptional = commentRepository.findByIdAndStatus(id, "pending");

        if (commentOptional.isEmpty()) {
            // Return a 404 Not Found response if the comment is not found
            return ResponseEntity.notFound().build();
        }

        Comment comment = commentOptional.get();

        // Get the news title associated with the comment
        String newsTitle = comment.getNews() != null ? comment.getNews().getTitle() : null;

        // Convert the comment to a DTO
        CommentWithNewsDTO result = new CommentWithNewsDTO(
                comment.getId(),
                comment.getContent(),
                comment.getStatus(),
                newsTitle
        );

        // Return the DTO in the response
        return ResponseEntity.ok(result);
    }




    @PostMapping("news/comment/{action}/{commentID}")
    public ResponseEntity<?> handleCommentAction(@PathVariable Long commentID, @PathVariable String action) {
        try {
            // Find the comment by ID
            Comment comment = commentRepository.findById(commentID)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));

            switch (action.toLowerCase()) {
                case "approve":
                    // Approve the comment
                    comment.setStatus("approved");
                    commentRepository.save(comment);
                    return ResponseEntity.ok("Comment approved successfully");

                case "reject":
                    // Reject the comment (automatically delete it)
                    commentRepository.delete(comment);
                    return ResponseEntity.ok("Comment rejected and deleted successfully");

                case "delete":
                    // Delete the comment manually
                    commentRepository.delete(comment);
                    return ResponseEntity.ok("Comment deleted successfully");

                default:
                    return ResponseEntity.badRequest().body("Invalid action specified");
            }

        } catch (Exception e) {
            // Handle any exceptions that occur
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error while processing comment action: " + e.getMessage());
        }
    }

}
