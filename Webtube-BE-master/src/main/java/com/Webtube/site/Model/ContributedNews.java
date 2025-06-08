package com.Webtube.site.Model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Contributed_News")
public class ContributedNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "author_name", nullable = false)
    private String author;

    @Column(name = "author_email", nullable = false)
    private String authorEmail;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "category", nullable = false)
    private String category;

    @Temporal(TemporalType.TIMESTAMP)
    private Date publicationDate;

    // Additional photos uploaded by contributor
    @Lob
    @ElementCollection
    @CollectionTable(name = "ContributedNews_Additional_Photos", joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "photo", columnDefinition = "MEDIUMBLOB")
    private List<byte[]> additionalPhotos;

    // Optional: A field to indicate review status
    @Column(name = "review_status")
    private String reviewStatus = "Pending";
}
