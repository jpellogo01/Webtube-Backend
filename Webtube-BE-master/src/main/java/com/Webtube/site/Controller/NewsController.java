package com.Webtube.site.Controller;


import com.Webtube.site.Exception.NewsNotFoundException;
import com.Webtube.site.Model.News;
import com.Webtube.site.Repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Logger logger = LoggerFactory.getLogger(NewsController.class);

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @GetMapping("/news")
    public List<News> getAllNews() {
        return newsRepository.findAll();
    }

    @GetMapping("/news/{id}")
    public News getNewsById(@PathVariable Long id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new NewsNotFoundException("News not found with id " + id));
    }

    //public endpoint to contribute content
    @PostMapping("/news-contribute")
    public ResponseEntity<String> createNewsWithContentAndPhotos(
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "additionalPhotos", required = false) MultipartFile[] additionalPhotos) {

        try {
            News newNews = new News();

            // Set content if provided
            if (content != null) {
                newNews.setContent(content);
            }

            // Handle additional photos
            List<byte[]> additionalPhotosList = new ArrayList<>();
            if (additionalPhotos != null) {
                for (MultipartFile photo : additionalPhotos) {
                    if (!photo.isEmpty()) {
                        additionalPhotosList.add(photo.getBytes());
                    }
                }
            }

            newNews.setAdditionalPhotos(additionalPhotosList);

            // Save to the database
            newsRepository.save(newNews);

            return ResponseEntity.status(201).body("News content and photos saved successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error while saving news: " + e.getMessage());
        }
    }

    // AI Integration Method
    public String callOpenAI(String prompt) {
        String apiUrl = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey); // GOOD ✅

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

        Object choicesObj = response.getBody().get("choices");

        if (choicesObj instanceof List) {
            List<?> choices = (List<?>) choicesObj;
            if (!choices.isEmpty() && choices.get(0) instanceof Map) {
                Map<?, ?> choiceMap = (Map<?, ?>) choices.get(0);
                Object messageObj = choiceMap.get("message");
                if (messageObj instanceof Map) {
                    Map<?, ?> messageMap = (Map<?, ?>) messageObj;
                    Object content = messageMap.get("content");
                    if (content != null) {
                        return content.toString();
                    }
                }
            }
        }

        return "No response from OpenAI.";
    }

    // Create or Update AI News
    @PostMapping("/AInews")
    public ResponseEntity<String> createOrUpdateNewsAi(

            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "thumbnailUrl", required = false) MultipartFile thumbnailUrl,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "content") String content, // REQUIRED
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "thumbnaillink", required = false) String thumbnaillink,
            @RequestParam(value = "additionalPhotos", required = false) MultipartFile[] additionalPhotos,
            @RequestParam(value = "embedYoutubeUrl", required = false) String embedYoutubeUrl) {

        try {
            News newNews = new News();

            // AI-generated title and description if missing
            if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
                String prompt = """
                         Generate a catchy news title and a short 10-15 words introductory summary for the following content.\s
                        The summary should sound like the first paragraph of a news article and must not use 5Ws formatting.
                                      
                        Content:
                        %s
                                      
                        Respond only in this format:
                        Title: <title>
                        Description: <description>
                                      
                """.formatted(content.trim());
                System.out.println("Calling OpenAI...");

                String aiResult = callOpenAI(prompt);

                // DEBUG — see what AI returned
                logger.info("AI Result: {}", aiResult);

                // Use regex to extract title and description
                Pattern titlePattern = Pattern.compile("(?i)title:\\s*(.*)");
                Pattern descriptionPattern = Pattern.compile("(?i)description:\\s*(.*)");

                Matcher titleMatcher = titlePattern.matcher(aiResult);
                Matcher descriptionMatcher = descriptionPattern.matcher(aiResult);

                if ((title == null || title.trim().isEmpty()) && titleMatcher.find()) {
                    newNews.setTitle(titleMatcher.group(1).trim());
                }
                if ((description == null || description.trim().isEmpty()) && descriptionMatcher.find()) {
                    newNews.setDescription(descriptionMatcher.group(1).trim());
                }
                logger.info("Final Title: {}", newNews.getTitle());
                logger.info("Final Description: {}", newNews.getDescription());

            } else {
                newNews.setTitle(title);
                newNews.setDescription(description);
            }

            newNews.setContent(content);
            if (author != null) newNews.setAuthor(author);
            if (category != null) newNews.setCategory(category);
            if (thumbnaillink != null) newNews.setThumbnaillink(thumbnaillink);
            if (status != null) newNews.setStatus("Pending"); // Always default to Pending
            if (embedYoutubeUrl != null) newNews.setEmbedYouTubeUrl(embedYoutubeUrl);

            // Process thumbnail
            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                if (thumbnailUrl.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.badRequest().body("Thumbnail exceeds 5MB limit.");
                }
                newNews.setThumbnailUrl(thumbnailUrl.getBytes());
            }

            // Process additional photos
            List<byte[]> additionalPhotosList = new ArrayList<>();
            if (additionalPhotos != null) {
                for (MultipartFile photo : additionalPhotos) {
                    if (!photo.isEmpty()) {
                        additionalPhotosList.add(photo.getBytes());
                    }
                }
            }
            newNews.setAdditionalPhotos(additionalPhotosList);

            // Save to DB
            newsRepository.save(newNews);
            return ResponseEntity.status(201).body("News created successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error while creating news: " + e.getMessage());
        }
    }




//    @PostMapping("/news")
//    public ResponseEntity<String> createOrUpdateNews(
//            @RequestParam(value = "title", required = false) String title,
//            @RequestParam(value = "thumbnailUrl", required = false) MultipartFile thumbnailUrl,  // For the main thumbnail
//            @RequestParam(value = "description", required = false) String description,
//            @RequestParam(value = "author", required = false) String author,
//            @RequestParam(value = "content", required = false) String content,
//            @RequestParam(value = "category", required = false) String category,
//            @RequestParam(value = "status", required = false) String status,
//            @RequestParam(value = "thumbnaillink", required = false) String thumbnaillink,
//            @RequestParam(value = "additionalPhotos", required = false) MultipartFile[] additionalPhotos,  // Optional additional photos
//            @RequestParam(value = "embedYoutubeUrl", required = false) String embedYoutubeUrl) {  // Optional YouTube URL
//
//        try {
//            News newNews = new News();
//
//            // Set fields if provided
//            if (title != null) {
//                newNews.setTitle(title);
//            }
//            if (description != null) {
//                newNews.setDescription(description);
//            }
//            if (author != null) {
//                newNews.setAuthor(author);
//            }
//            if (content != null) {
//                newNews.setContent(content);
//            }
//            if (category != null) {
//                newNews.setCategory(category);
//            }
//            if (thumbnaillink != null) {
//                newNews.setThumbnaillink(thumbnaillink);
//            }
//            if (status != null) {
//                newNews.setStatus("Pending");
//            }
//            if (embedYoutubeUrl != null) {
//                newNews.setEmbedYouTubeUrl(embedYoutubeUrl);
//            }
//
//            // Handle thumbnail if provided
//            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
//                if (thumbnailUrl.getSize() > 5 * 1024 * 1024) { // 5MB limit
//                    return ResponseEntity.badRequest().body("File size exceeds the 5MB limit.");
//                }
//                newNews.setThumbnailUrl(thumbnailUrl.getBytes());
//            }
//
//            // Handle additional photos if provided
//            List<byte[]> additionalPhotosList = new ArrayList<>();
//            if (additionalPhotos != null) {
//                for (MultipartFile photo : additionalPhotos) {
//                    if (!photo.isEmpty()) {
//                        additionalPhotosList.add(photo.getBytes());
//                    }
//                }
//            }
//
//            // Set the additional photos if any
//            newNews.setAdditionalPhotos(additionalPhotosList);
//
//            // Save the news item
//            newsRepository.save(newNews);
//
//            return ResponseEntity.status(201).body("News created successfully.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(500).body("Error while creating news: " + e.getMessage());
//        }
//    }



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
