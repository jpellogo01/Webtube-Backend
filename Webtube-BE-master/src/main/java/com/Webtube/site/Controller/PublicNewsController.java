package com.Webtube.site.Controller;


import com.Webtube.site.Model.News;
import com.Webtube.site.Repository.NewsRepository;
import com.Webtube.site.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000/")
@RestController
@RequestMapping("/api/v1")
public class PublicNewsController<NewsService> {

    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private UsersRepository userRepository;

    @GetMapping("/public-news")
    public List<News> getAllNews(){
        return newsRepository.findByStatus("Approved");
    }

}
