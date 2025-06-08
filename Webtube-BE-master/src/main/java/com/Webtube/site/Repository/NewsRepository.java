package com.Webtube.site.Repository;

import com.Webtube.site.Model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findByStatus(String status);
    @Query("SELECT n FROM News n WHERE n.status = :status AND n.publicationDate <= :currentTime")
    List<News> findByStatusAndPublicationDateBefore(@Param("status") String status, @Param("currentTime") Date currentTime);

    Optional<Object> findByIdAndStatus(Long id, String approved);
}
