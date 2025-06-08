package com.Webtube.site.Controller;

import com.Webtube.site.Model.News;
import com.Webtube.site.Repository.NewsRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.HttpClientErrorException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facebook")
public class FacebookPostController {

    private final NewsRepository newsRepository;
    private final String PAGE_ACCESS_TOKEN = "EAASfeBNuklsBOZBcqGU8HpoDnsajkotCiHXnAZCb8Akf6HtyZBsOZAIvsWVO8Wh79bRCVjyZCzAE5hZBgljtTAIKy2wwH9c6WzYP1UhZBGdv9mSn2NZBw5YwZCzeZCusLNLmAsElkE3TSHx7PohIJQy0ekPmJRILmhojUjEOVQ9DqyzHFhlAm0jlz0ipDBIt6TfjSCmLUafuxkX7v5SliGIvZBLb40aXAZDZD";
    private final String PAGE_ID = "592216443966075";

    public FacebookPostController(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @PostMapping("/post/{id}")
    public ResponseEntity<String> postNewsToFacebook(@PathVariable Long id) {
        try {
            News news = newsRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("News not found"));
            String facebookResponse = postToFacebook(news);
            return ResponseEntity.ok("News posted to Facebook successfully! Response: " + facebookResponse);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error posting to Facebook: " + e.getMessage());
        }
    }

    private String postToFacebook(News news) throws UnsupportedEncodingException {
        String facebookApiUrl = "https://graph.facebook.com/v18.0/" + PAGE_ID + "/feed";
        RestTemplate restTemplate = new RestTemplate();

        // Decode the URL-encoded content from the news (so we have human-readable text)
        String decodedContent = URLDecoder.decode(news.getContent(), StandardCharsets.UTF_8.toString());
        // Build your caption using the decoded content
        String caption = "**" + news.getTitle() + "**\n\n" + decodedContent;

        // Build the URI with query parameters (do NOT tell the builder that values are already encoded)
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(facebookApiUrl)
                .queryParam("message", caption)
                .queryParam("access_token", PAGE_ACCESS_TOKEN);

        // Build without assuming pre-encoded values, then encode the URI
        UriComponents uriComponents = builder.build(false).encode(StandardCharsets.UTF_8);
        String uriString = uriComponents.toUriString();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uriString, null, String.class);
            System.out.println("Facebook Response: " + response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.out.println("Error: " + e.getResponseBodyAsString());
            return "Error: " + e.getResponseBodyAsString();
        }
    }

    private String postImageToFacebook(String imageUrl, String caption) {
        String url = "https://graph.facebook.com/v18.0/" + PAGE_ID + "/photos";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("url", imageUrl);  // Must be a publicly accessible URL
        requestBody.put("caption", caption);
        requestBody.put("access_token", PAGE_ACCESS_TOKEN);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            System.out.println("Facebook Response: " + response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.out.println("Error: " + e.getResponseBodyAsString());
            return "Error: " + e.getResponseBodyAsString();
        }
    }

    private String postMultipleImagesToFacebook(List<byte[]> imageUrls, String caption) {
        String albumUrl = "https://graph.facebook.com/v18.0/" + PAGE_ID + "/photos";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        for (byte[] imageUrl : imageUrls) {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("url", imageUrl);  // Ensure the URL is publicly accessible
            requestBody.put("caption", caption);
            requestBody.put("access_token", PAGE_ACCESS_TOKEN);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            try {
                restTemplate.postForEntity(albumUrl, requestEntity, String.class);
            } catch (HttpClientErrorException e) {
                System.out.println("Error posting image: " + e.getResponseBodyAsString());
                return "Error posting image: " + e.getResponseBodyAsString();
            }
        }
        return "Multiple images posted successfully!";
    }
}
