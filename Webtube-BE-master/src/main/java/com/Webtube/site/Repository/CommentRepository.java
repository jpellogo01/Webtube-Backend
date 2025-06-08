package com.Webtube.site.Repository;

import com.Webtube.site.Model.Comment;
import com.Webtube.site.Model.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Find all comments associated with a specific news item
    List<Comment> findByNews(News news);
    List<Comment> findByStatus(String status);
    List<Comment> findByNewsIdAndStatus(Long newsId, String status);
    List<Comment> findByStatusIn(List<String> statuses);


    Optional<Comment> findByIdAndStatus(Long id, String pending);

    void deleteByNewsId(long newsId);
    List<Comment> findByParentCommentId(Long parentId);


}
