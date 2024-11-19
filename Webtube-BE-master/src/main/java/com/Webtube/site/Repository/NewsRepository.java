package com.Webtube.site.Repository;

import com.Webtube.site.Model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findByStatus(String approved);
// Custom queries if needed
}
