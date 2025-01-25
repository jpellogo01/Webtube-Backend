package com.Webtube.site.Controller;


import com.Webtube.site.Exception.NewsNotFoundException;
import com.Webtube.site.Model.News;
import com.Webtube.site.Repository.NewsRepository;
import com.Webtube.site.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.254.144:3000"})
@RestController
@RequestMapping("/api/v1")
public class PublicNewsController<NewsService> {

    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private UsersRepository userRepository;


    @GetMapping("/public-news")
    public List<News> getAllNews() {
        return newsRepository.findByStatusAndPublicationDateBefore("Approved", new Date());
    }

    @GetMapping("/public-news/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable Long id) {
        Optional<News> news = newsRepository.findById(id);

        if (news.isPresent() && news.get().getStatus().equals("Approved")) {
            return ResponseEntity.ok(news.get());
        } else {
            throw new NewsNotFoundException("News with ID " + id + " not found or not approved.");
        }
    }

}