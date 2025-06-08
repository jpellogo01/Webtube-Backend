package com.Webtube.site.Model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatCompletionResponse {

    private List<Choice> choices;

       @Getter
       public static class Choice{
           private int index;
           private ChatMessage message;

       }
 }
