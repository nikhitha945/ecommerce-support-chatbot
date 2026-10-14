package com.support.chatbot.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.support.chatbot.tools.OrderTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final OrderTools orderTools;

    public ChatController(ChatClient.Builder builder, VectorStore vectorStore, OrderTools orderTools) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.orderTools = orderTools;
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> body) {
        String question = body.get(body.get("message"));
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("question must not be blank");
        }
        String reply = chatClient.prompt()
                .user(question)
                .call()
                .content();
        return Map.of("reply", reply);
    }

    @PostMapping("/ask")
    public Map<String, Object> ask(@RequestBody Map<String, String> body) {
        String question = body.get("question");

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("question must not be blank");
        }

        // 1. Retrieve: embed the question, find the 4 most similar chunks
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(4)
                        .similarityThreshold(0.5)
                        .build());

        // 2. Build context, tagging each chunk with its source file
        String context = docs.stream()
                .map(d -> "[Source: " + d.getMetadata().get("source") + "]\n" + d.getText())
                .collect(Collectors.joining("\n\n"));

        // 3. Generate: answer strictly from the retrieved context
        String answer = chatClient.prompt()
                .system(s -> s.text("""
        You are BrightCart's support assistant. You have two knowledge sources:
        1. Support documents in the context below — cite the source filename like [return-policy.md].
        2. An order lookup tool — use it when the customer asks about a specific order (IDs look like BC-1001).
        If the customer asks about an order but does not provide an order ID, DO NOT assume/default the order ID.  Instead ask the customer for the order ID. Never guess, invent, or reuse an example order ID.
        Answer ONLY from these sources. If neither has the answer, say you don't know and offer to connect the customer with support.
        Context:
        {context}
        """).param("context", context))
                .user(question)
                .tools(orderTools)
                .call()
                .content();

        List<String> sources = docs.stream()
                .map(d -> (String) d.getMetadata().get("source"))
                .distinct()
                .toList();

        return Map.of("answer", answer, "sources", sources);
    }

}
