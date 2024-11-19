package com.Webtube.site.Controller;

import com.Webtube.site.Model.Comment;
import com.Webtube.site.Model.View;
import com.Webtube.site.Repository.CommentRepository;
import com.Webtube.site.Repository.ViewRepository;
import com.Webtube.site.Repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:3000/")

@RestController
@RequestMapping("/api/v1")
public class ViewAndCommentController {

    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NewsRepository newsRepository;


    // Endpoint to add a comment to a specific news item
    // Endpoint to get all comments for a specific news item
    @GetMapping("news/comments/{newsId}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long newsId) {
        // Find the news item by ID
        var news = newsRepository.findById(newsId).orElseThrow(() -> new RuntimeException("News not found"));

        // Get all comments for the news item
        List<Comment> comments = commentRepository.findByNews(news);

        return ResponseEntity.ok(comments);
    }

    // Endpoint to add a new comment for a specific news item
    @PostMapping("news/comment/{newsId}")
    public ResponseEntity<?> addComment(@PathVariable Long newsId, @RequestBody Comment comment) {
        // Find the news item by ID
        var news = newsRepository.findById(newsId).orElseThrow(() -> new RuntimeException("News not found"));

        // Set the news item to the comment
        comment.setNews(news);

        // Save the comment to the database
        commentRepository.save(comment);

        return ResponseEntity.ok("Comment added successfully");
    }



    // Endpoint to add a new view for a specific news item
    @PostMapping("news/view/{newsId}")
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
