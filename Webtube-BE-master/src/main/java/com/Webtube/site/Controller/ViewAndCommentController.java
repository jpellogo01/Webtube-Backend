package com.Webtube.site.Controller;

import com.Webtube.site.Model.Comment;
import com.Webtube.site.Model.View;
import com.Webtube.site.Repository.CommentRepository;
import com.Webtube.site.Repository.ViewRepository;
import com.Webtube.site.Repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:3000")

@RestController
@RequestMapping("/api/v1")
public class ViewAndCommentController {

    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private com.Webtube.site.Service.ProfanityCheckerService profanityCheckerService;

    // Endpoint to get all comments for a specific news item
    @GetMapping("news/approved/comments/{newsId}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long newsId) {
        // Find the news item by ID
        var news = newsRepository.findById(newsId).orElseThrow(() -> new RuntimeException("News not found"));

        // Get all comments for the news item
        List<Comment> comments = commentRepository.findByNewsIdAndStatus(newsId, "Approved");
        return ResponseEntity.ok(comments);
    }

    // Endpoint to add a new comment for a specific news item
    @PostMapping("/comment-news/{newsId}")
    public ResponseEntity<?> addComment(
            @PathVariable Long newsId,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestHeader("Visitor-Id") String visitorId,
            @RequestBody Comment comment) {

        // Find the news item
        var news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News not found"));

        // 🔍 Check comment content using external API
        String analysisResult = profanityCheckerService.checkCommentForProfanity(comment.getContent());

        if (analysisResult == null) {
            // ✅ If API failed, mark comment AND news as "pending"
            comment.setStatus("pending");
            news.setStatus("pending");
            newsRepository.save(news); // Make sure to persist the change
        } else {
            // 🧠 Analyze the response
            if (analysisResult.equalsIgnoreCase("no profanity") || analysisResult.trim().isEmpty()) {
                comment.setStatus("Approved"); // ✅ No bad words
            } else {
                String[] pairs = analysisResult.split(",");
                for (String pair : pairs) {
                    String[] parts = pair.split(":");
                    if (parts.length == 2) {
                        try {
                            double accuracy = Double.parseDouble(parts[1]);
                            if (accuracy >= 0.90) {
                                return ResponseEntity.badRequest().body("Comment contains bad words and was not saved.");
                            } else if (accuracy >= 0.70) {
                                comment.setStatus("pending"); // ⚠️ Possibly bad
                                break;
                            }
                        } catch (NumberFormatException e) {
                            // Log or ignore invalid accuracy format
                        }
                    }
                }
                // If none are serious, approve
                if (comment.getStatus() == null) {
                    comment.setStatus("Approved");
                }
            }
        }

        // Set other values
        comment.setNews(news);
        comment.setVisitorId(visitorId);

        if (parentId != null) {
            Comment parentComment = commentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parentComment);
        }

        commentRepository.save(comment);
        return ResponseEntity.ok("Comment added with status: " + comment.getStatus());
    }


//    @PostMapping("/comment-reply/{parentCommentId}")
//    public ResponseEntity<Comment> replyToComment(
//            @PathVariable Long parentCommentId,
//            @RequestBody Comment replyData) {
//
//        Comment parentComment = commentRepository.findById(parentCommentId)
//                .orElseThrow(() -> new RuntimeException("Parent comment not found"));
//
//        replyData.setParentComment(parentComment);
//        replyData.setNews(parentComment.getNews()); // use same news
//        if (replyData.getStatus() == null) replyData.setStatus("Pending");
//
//        Comment savedReply = commentRepository.save(replyData);
//        return ResponseEntity.ok(savedReply);
//    }
//
//
//    // Endpoint to approve a comment
//    @PutMapping("/approve-comment/{commentId}")
//    public ResponseEntity<?> approveComment(@PathVariable Long commentId) {
//        Comment comment = commentRepository.findById(commentId)
//                .orElseThrow(() -> new RuntimeException("Comment not found"));
//
//        comment.setStatus("Approved");
//        commentRepository.save(comment);
//
//        return ResponseEntity.ok("Comment approved successfully");
//    }

    // Endpoint to delete a comment
    @DeleteMapping("/delete-comment/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        commentRepository.delete(comment);

        return ResponseEntity.ok("Comment deleted successfully");
    }

    @DeleteMapping("/user-delete-comment/{commentId}")
    public ResponseEntity<?> deleteOwnComment(
            @PathVariable Long commentId,
            @RequestHeader("Visitor-Id") String visitorId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getVisitorId().equals(visitorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not allowed to delete this comment.");
        }

        commentRepository.delete(comment);
        return ResponseEntity.ok("Comment deleted successfully");
    }


    // Endpoint to add a new view for a specific news item
    @PostMapping("view-news/{newsId}")
    public ResponseEntity<?> addView(@PathVariable Long newsId, @RequestParam String viewerIp) {
        // Find the news item by ID
        var news = newsRepository.findById(newsId).orElseThrow(() -> new RuntimeException("News not found"));

        // Create a new view
        View view = new View();
        view.setViewerIp(viewerIp);
        view.setNews(news);
        long viewCount = viewRepository.countByNews(news);

        // Save the view to the database
        viewRepository.save(view);

        return ResponseEntity.ok("View added successfully");
    }
    // Endpoint to get all views for a specific news item
    @GetMapping("news/views/{newsId}")
    public ResponseEntity<Long> getViews(@PathVariable Long newsId) {
        // Find the news item by ID
        var news = newsRepository.findById(newsId).orElseThrow(() -> new RuntimeException("News not found"));

        // Get all views for the news item
        List<View> views = viewRepository.findByNews(news);
        Long viewCount = viewRepository.countByNews(news);

        return ResponseEntity.ok(viewCount);
    }
}
