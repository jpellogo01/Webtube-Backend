package com.Webtube.site.Repository;

import com.Webtube.site.Model.Comment;
import com.Webtube.site.Model.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Find all comments associated with a specific news item
    List<Comment> findByNews(News news);
    List<Comment> findByStatus(String approved);


}
