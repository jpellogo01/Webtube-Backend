package com.Webtube.site.Controller;


import com.Webtube.site.Exception.NewsNotFoundException;
import com.Webtube.site.Model.News;
import com.Webtube.site.Repository.NewsRepository;
import com.Webtube.site.Repository.NotificationRepository;
import com.Webtube.site.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000/")
@RestController
//@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AUTHOR')")
@RequestMapping("/api/v1")
public class NewsController<NewsService> {

    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/news")
    public List<News> getAllNews(){
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
                newNews.setStatus(status);
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
            @RequestParam(value = "embedYoutubeUrl", required = false) String embedYoutubeUrl) throws NewsNotFoundException {

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
    @DeleteMapping("/news/{id}")
    public ResponseEntity<?> deleteNews(@PathVariable(value = "id") long newsId) throws NewsNotFoundException{
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new NewsNotFoundException("News Content not found for this Id :: " + newsId));

        newsRepository.deleteById(newsId);
        return ResponseEntity.ok().build();
    }








}
