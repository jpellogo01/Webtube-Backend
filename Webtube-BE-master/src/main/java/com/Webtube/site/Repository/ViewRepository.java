package com.Webtube.site.Repository;

import com.Webtube.site.Model.View;
import com.Webtube.site.Model.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ViewRepository extends JpaRepository<View, Long> {
    // Find all views associated with a specific news item
    List<View> findByNews(News news);

    long countByNews(News news);

    void deleteByNewsId(long newsId);
}
