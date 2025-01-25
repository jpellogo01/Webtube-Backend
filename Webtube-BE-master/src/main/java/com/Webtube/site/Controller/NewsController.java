package com.Webtube.site.Controller;


import com.Webtube.site.Exception.NewsNotFoundException;
import com.Webtube.site.Model.News;
import com.Webtube.site.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.254.144:3000"})
@RestController
//@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AUTHOR')")
@RequestMapping("/api/v1")
public class NewsController<NewsService> {

    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ViewRepository viewRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/news")
    public List<News> getAllNews() {
        return newsRepository.findAll();
    }

    @GetMapping("/news/{id}")
    public News getNewsById(@PathVariable Long id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new NewsNotFoundException("News not found with id " + id));
    }

    @PostMapping("/news")
    public ResponseEntity<String> createOrUpdateNews(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "thumbnailUrl", required = false) MultipartFile thumbnailUrl,  // For the main thumbnail
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "additionalPhotos", required = false) MultipartFile[] additionalPhotos,  // Optional additional photos
            @RequestParam(value = "embedYoutubeUrl", required = false) String embedYoutubeUrl) {  // Optional YouTube URL

        try {
            News newNews = new News();

            // Set fields if provided
            if (title != null) {
                newNews.setTitle(title);
            }
            if (description != null) {
                newNews.setDescription(description);
            }
            if (author != null) {
                newNews.setAuthor(author);
            }
            if (content != null) {
                newNews.setContent(content);
            }
            if (category != null) {
                newNews.setCategory(category);
            }
            if (status != null) {
                newNews.setStatus("Pending");
            }
            if (embedYoutubeUrl != null) {
                newNews.setEmbedYouTubeUrl(embedYoutubeUrl);
            }

            // Handle thumbnail if provided
            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                if (thumbnailUrl.getSize() > 5 * 1024 * 1024) { // 5MB limit
                    return ResponseEntity.badRequest().body("File size exceeds the 5MB limit.");
                }
                newNews.setThumbnailUrl(thumbnailUrl.getBytes());
            }

            // Handle additional photos if provided
            List<byte[]> additionalPhotosList = new ArrayList<>();
            if (additionalPhotos != null) {
                for (MultipartFile photo : additionalPhotos) {
                    if (!photo.isEmpty()) {
                        additionalPhotosList.add(photo.getBytes());
                    }
                }
            }

            // Set the additional photos if any
            newNews.setAdditionalPhotos(additionalPhotosList);

            // Save the news item
            newsRepository.save(newNews);

            return ResponseEntity.status(201).body("News created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error while creating news: " + e.getMessage());
        }
    }



    @PutMapping("/news/{id}")
    public ResponseEntity<String> updateNews(
            @PathVariable("id") long newsId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "thumbnailUrl", required = false) MultipartFile thumbnailUrl,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "embedYoutubeUrl", required = false) String embedYoutubeUrl,
            @RequestParam(value = "additionalPhotos", required = false) MultipartFile[] additionalPhotos,
            @RequestParam(value = "removedPhotos", required = false) List<String> removedPhotos // Accept IDs or identifiers
    ) throws NewsNotFoundException {

        // Find the existing news by ID or throw an exception if not found
        News existingNews = newsRepository.findById(newsId)
                .orElseThrow(() -> new NewsNotFoundException("News content not found for this ID: " + newsId));

        try {
            // Update fields only if new values are provided
            if (title != null) {
                existingNews.setTitle(title);
            }
            if (description != null) {
                existingNews.setDescription(description);
            }
            if (author != null) {
                existingNews.setAuthor(author);
            }
            if (content != null) {
                existingNews.setContent(content);
            }
            if (category != null) {
                existingNews.setCategory(category);
            }
            if (status != null) {
                existingNews.setStatus(status);
            }
            if (embedYoutubeUrl != null) {
                existingNews.setEmbedYouTubeUrl(embedYoutubeUrl);
            }

            // Update the thumbnail if a new file is provided
            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                if (thumbnailUrl.getSize() > 5 * 1024 * 1024) { // 5MB limit
                    return ResponseEntity.badRequest().body("File size exceeds the 5MB limit.");
                }
                existingNews.setThumbnailUrl(thumbnailUrl.getBytes());
            }

            // Handle additional photos
            List<byte[]> updatedAdditionalPhotos = new ArrayList<>();

            // Remove specified photos from the existing list
            if (removedPhotos != null && !removedPhotos.isEmpty()) {
                for (byte[] existingPhoto : existingNews.getAdditionalPhotos()) {
                    String encodedPhoto = Base64.getEncoder().encodeToString(existingPhoto);
                    if (!removedPhotos.contains(encodedPhoto)) { // Keep photos not marked for removal
                        updatedAdditionalPhotos.add(existingPhoto);
                    }
                }
            } else {
                // If no photos are to be removed, keep all existing photos
                updatedAdditionalPhotos.addAll(existingNews.getAdditionalPhotos());
            }

            // Append new photos if provided
            if (additionalPhotos != null) {
                for (MultipartFile photo : additionalPhotos) {
                    if (!photo.isEmpty()) {
                        updatedAdditionalPhotos.add(photo.getBytes());
                    }
                }
            }

            // Update the news object with the updated list of photos
            existingNews.setAdditionalPhotos(updatedAdditionalPhotos);

            // Save the updated news
            newsRepository.save(existingNews);

            return ResponseEntity.ok("News updated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing the file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("An error occurred while updating the news: " + e.getMessage());
        }
    }

    //Delete News Content by id
    @Transactional
    @DeleteMapping("/news/{id}")
    public ResponseEntity<?> deleteNews(@PathVariable(value = "id") long newsId) throws NewsNotFoundException {
        // Find the news item
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new NewsNotFoundException("News content not found for this ID :: " + newsId));

        try {
            // Delete associated comments
            commentRepository.deleteByNewsId(newsId);

            // Delete associated views
            viewRepository.deleteByNewsId(newsId);

            // Delete the news item
            newsRepository.delete(news);

            return ResponseEntity.ok("News, along with its comments and views, has been deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("An error occurred while deleting the news: " + e.getMessage());
        }
    }









}
