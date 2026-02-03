package com.icuxika.markdown.stream.render.demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

public class DeepSeekClient {

    public record ChatMessage(String role, String content) {
    }

    private final String apiKey;
    private final HttpClient client;
    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    public DeepSeekClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void streamChat(List<ChatMessage> messages, Consumer<String> onToken, Runnable onComplete, Consumer<Throwable> onError) {
        StringBuilder messagesJson = new StringBuilder("[");
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            messagesJson.append("""
                    {"role": "%s", "content": "%s"}
                    """.formatted(msg.role(), escapeJson(msg.content())));
            if (i < messages.size() - 1) {
                messagesJson.append(",");
            }
        }
        messagesJson.append("]");

        String requestBody = """
                {
                  "model": "deepseek-chat",
                  "messages": %s,
                  "stream": true
                }
                """.formatted(messagesJson.toString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.fromLineSubscriber(new Flow.Subscriber<>() {
                    private Flow.Subscription subscription;

                    @Override
                    public void onSubscribe(Flow.Subscription subscription) {
                        this.subscription = subscription;
                        subscription.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(String line) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6).trim();
                            if ("[DONE]".equals(data)) {
                                return; // Stream finished
                            }

                            String content = extractContent(data);
                            if (content != null && !content.isEmpty()) {
                                onToken.accept(content);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        onError.accept(throwable);
                    }

                    @Override
                    public void onComplete() {
                        onComplete.run();
                    }
                }))
                .exceptionally(e -> {
                    onError.accept(e);
                    return null;
                });
    }

    // Simple JSON escaping for the request body
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Regex to extract "content": "..." from JSON
    private String extractContent(String json) {
        // Look for "content":
        int contentIndex = json.indexOf("\"content\"");
        if (contentIndex == -1) return null;

        // Find the colon after "content"
        int colonIndex = json.indexOf(":", contentIndex);
        if (colonIndex == -1) return null;

        // Find the opening quote
        int startQuote = json.indexOf("\"", colonIndex + 1);
        if (startQuote == -1) return null;

        // Find the closing quote, skipping escaped quotes
        StringBuilder content = new StringBuilder();
        boolean escape = false;
        for (int i = startQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escape) {
                // Add the escaped character
                if (c == 'n') content.append('\n');
                else if (c == 'r') content.append('\r');
                else if (c == 't') content.append('\t');
                else content.append(c); // includes \" and \\
                escape = false;
            } else {
                if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    // Found closing quote
                    return content.toString();
                } else {
                    content.append(c);
                }
            }
        }
        return null; // Malformed JSON or incomplete
    }
}
