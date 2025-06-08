package com.Webtube.site.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Entity
@Table(name = "News")
public class News {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "thumbnail", columnDefinition = "MEDIUMBLOB")
    private byte[] thumbnailUrl;

    @Column(name = "description")
    private String description;

    @Column(name = "author")
    private String author;

    // Getter for content
    @Getter
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "category")
    private String category;

    @Column(name = "status")
    private String status = "Pending"; // Default to "Pending"

    @Temporal(TemporalType.TIMESTAMP)
    private Date publicationDate;

    // New field for additional photos
    @Getter
    @Lob
    @ElementCollection
    @CollectionTable(name = "News_Additional_Photos", joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "photo", columnDefinition = "MEDIUMBLOB")
    private List<byte[]> additionalPhotos;

    // Field for YouTube embed URL
    @Setter
    @Getter
    @Column(name = "embed_youtube_url")
    private String embedYouTubeUrl;

    // Field for YouTube embed URL
    @Setter
    @Getter
    @Column(name = "thumbnaillink", columnDefinition = "TEXT")
    private String thumbnaillink;

    // Custom formatter for date/time display
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy hh:mm a");

    public String getFormattedCreatedAt() {
        return createdAt.format(formatter);
    }

    public String getFormattedUpdatedAt() {
        return updatedAt.format(formatter);
    }
}
