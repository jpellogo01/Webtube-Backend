package com.Webtube.site.payload.response;

public class CommentWithNewsDTO {
    private Long commentId;
    private String commentText;
    private String commentStatus;
    private String newsTitle;

    // Constructor
    public CommentWithNewsDTO(Long commentId, String commentText, String commentStatus, String newsTitle) {
        this.commentId = commentId;
        this.commentText = commentText;
        this.commentStatus = commentStatus;
        this.newsTitle = newsTitle;
    }

    // Getters and Setters
    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getCommentStatus() {
        return commentStatus;
    }

    public void setCommentStatus(String commentStatus) {
        this.commentStatus = commentStatus;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }
}

