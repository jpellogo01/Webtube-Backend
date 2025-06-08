package com.Webtube.site.Service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Service
public class ProfanityCheckerService {

    public String checkCommentForProfanity(String content) {
        try {
            URI uri = URI.create("https://scvpapi.pythonanywhere.com/chat");
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInput = String.format(
                    "{ \"question\": \"Strictly return only a plain text response with no markdown, no code fences, no JSON wrapping, no explanation. Output the bad words and accuracies in the format: word:accuracy,word:accuracy,... (accuracy with two decimals, no spaces). For example: papatayin:0.95,otherword:0.87. Analyze this comment: '%s'\", \"api_key\": \"47BF544A-53C3-4215-A213-18E7377F5A67\" }",
                    content.replace("\"", "\\\"")
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes("utf-8"));
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            String responseStr = response.toString();
            if (responseStr.startsWith("{\"answer\":\"") && responseStr.endsWith("\"}")) {
                return responseStr.substring(11, responseStr.length() - 2);
            }

            return responseStr;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
