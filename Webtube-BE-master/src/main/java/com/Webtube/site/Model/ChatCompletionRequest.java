package com.Webtube.site.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatCompletionRequest {

   private String model;
   private List<ChatMessage> messages;

    public ChatCompletionRequest(String model, String prompt) {
        this.model = model;
        this.messages = new ArrayList<ChatMessage>();
        this.messages.add(new ChatMessage("user", prompt));
    }
}
