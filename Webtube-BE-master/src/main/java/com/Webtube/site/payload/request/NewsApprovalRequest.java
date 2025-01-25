package com.Webtube.site.payload.request;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Setter
@Getter
public class NewsApprovalRequest {
    // Getters and Setters
    private ZonedDateTime publicationDate;

}

