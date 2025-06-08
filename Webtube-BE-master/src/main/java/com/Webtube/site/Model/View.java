package com.Webtube.site.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Views")
public class View {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "viewer_ip")
    private String viewerIp;

    @CreationTimestamp
    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    @ManyToOne
    @JoinColumn(name = "news_id", nullable = false)
    private News news;
}
