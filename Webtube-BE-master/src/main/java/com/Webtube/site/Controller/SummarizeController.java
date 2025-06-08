package com.Webtube.site.Controller;

import com.Webtube.site.Model.ChatCompletionRequest;
import com.Webtube.site.Model.ChatCompletionResponse;
import com.Webtube.site.Model.News;
import com.Webtube.site.Repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.254.144:3000"})

public class SummarizeController {


    @Autowired
    NewsRepository newsRepository;
    @Autowired
    RestTemplate restTemplate;

    @PostMapping("/hitOpenaiApi/{id}")
    public String getOpenaiResponse(@PathVariable Long id) {
        try {
            // Retrieve the approved news item from the database by its ID and status
            News news = (News) newsRepository.findByIdAndStatus(id, "Approved")
                    .orElseThrow(() -> new RuntimeException("Approved news not found"));

            // Get the content of the news
            String newsContent = news.getContent();

            // Construct the prompt with the news content
            String prompt = "Summarize this content(make it short but easy to get the context)  using the What, When, Who, Where, Why and additional information structure: " + newsContent;

            // Create a request object for the OpenAI API
            ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(
                    "gpt-4o-mini", prompt
            );

            // Send the request to the OpenAI API
            ChatCompletionResponse response = restTemplate.postForObject(
                    "https://api.openai.com/v1/chat/completions",
                    chatCompletionRequest,
                    ChatCompletionResponse.class
            );

            // Return the summarized content
            assert response != null;
            return response.getChoices().get(0).getMessage().getContent();

        } catch (HttpClientErrorException e) {
            // Handle specific HTTP errors
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return "Error: Unauthorized access. Please check your API key.";
            } else if (e.getStatusCode() == HttpStatus.PAYMENT_REQUIRED) {
                return "Error: Your API credits have been exhausted. Please top up your credits to continue.";
            } else {
                return "Error: Unable to process the request. " + e.getMessage();
            }
        } catch (Exception e) {
            // Handle other errors
            return "An unexpected error occurred: " + e.getMessage();
        }
    }


}
