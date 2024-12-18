package com.meong9.backend.global.banword.wordloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class WordLoader {
    public List<String> readBanWords() {
        return read("static/banWords.json");
    }

    public List<String> readExceptWords() {
        return read("static/exceptWords.json");
    }

    private List<String> read(String path) {
        try {
            return new ObjectMapper().readValue(new ClassPathResource(path).getInputStream(),
                                                new TypeReference<>() {});
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}

