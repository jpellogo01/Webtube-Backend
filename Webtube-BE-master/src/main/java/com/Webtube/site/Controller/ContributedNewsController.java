package com.Webtube.site.Controller;

import com.Webtube.site.Model.ContributedNews;
import com.Webtube.site.Repository.ContributedNewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000/")
@RequestMapping("/api/v1")
public class ContributedNewsController {

    @Autowired
    private ContributedNewsRepository contributedNewsRepository;

    // POST: Submit a new contribution
    @PostMapping(value = "/contribute-news", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContributedNews> createContribution(
            @RequestParam("author") String author,
            @RequestParam("authorEmail") String authorEmail,
            @RequestParam("content") String content,
            @RequestParam("category") String category,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos
    ) {
        if (author == null || author.trim().isEmpty()
                || authorEmail == null || !authorEmail.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")
                || content == null || content.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        ContributedNews news = new ContributedNews();
        news.setAuthor(author);
        news.setAuthorEmail(authorEmail);
        news.setContent(content);
        news.setCategory(category);
        news.setPublicationDate(new Date());
        news.setReviewStatus("Pending");

        // Process and store photos
        if (photos != null && !photos.isEmpty()) {
            List<byte[]> photoBytes = photos.stream().map(photo -> {
                try {
                    return photo.getBytes();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to read photo", e);
                }
            }).toList();

            news.setAdditionalPhotos(photoBytes);
        }

        ContributedNews saved = contributedNewsRepository.save(news);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    // GET: All contributions
    @GetMapping("/contribute-news")
    public ResponseEntity<List<ContributedNews>> getAllContributions() {
        List<ContributedNews> contributions = contributedNewsRepository.findAll();
        return ResponseEntity.ok(contributions);
    }

    // GET: One contribution by ID
    @GetMapping("/contribute-news/{id}")
    public ResponseEntity<ContributedNews> getContribution(@PathVariable Long id) {
        Optional<ContributedNews> news = contributedNewsRepository.findById(id);
        return news.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE: Remove a contribution by ID
    @DeleteMapping("/contribute-news/{id}")
    public ResponseEntity<Void> deleteContribution(@PathVariable Long id) {
        if (contributedNewsRepository.existsById(id)) {
            contributedNewsRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
