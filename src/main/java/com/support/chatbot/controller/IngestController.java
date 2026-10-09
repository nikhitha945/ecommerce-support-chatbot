package com.support.chatbot.controller;


import com.support.chatbot.injest.DocIngestion;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class IngestController {
    public final DocIngestion docIngestion;

    public IngestController(DocIngestion docIngestion) {
        this.docIngestion = docIngestion;
    }

    @PostMapping("/ingest")
    public Map<String, Object> ingest() {
        int chunks = docIngestion.ingest();
        return Map.of("chunksStored", chunks);
    }

}
