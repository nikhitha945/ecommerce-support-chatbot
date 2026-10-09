package com.support.chatbot.injest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class DocIngestion {

    private final VectorStore vectorStore;

    @Value("classpath:docs/*.md")
    private Resource[] docResources;

    public DocIngestion(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public int ingest() {
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(500)
                .withMinChunkSizeChars(100)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();
        int total = 0;
        for (Resource r : docResources) {
            String text;
            try {
                text = r.getContentAsString(StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read doc: " + r.getFilename(), e);
            }
            List<Document> chunks = splitter.split(
                    List.of(new Document(text, Map.of("source", r.getFilename()))));
            vectorStore.add(chunks);
            total += chunks.size();
        }
        return total;
    }
}
