package com.campus.campus_server.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TextChunker {

    private static final int CHUNK_SIZE = 500;
    private static final int OVERLAP = 100;

    public List<String> split(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        int[] codePoints = text.codePoints().toArray();  //切成码点主要是防止emoji等字符文本被拆

        for (int start = 0; start < codePoints.length; ) {
            int end = Math.min(start + CHUNK_SIZE, codePoints.length);
            String chunk = new String(codePoints, start, end - start).trim();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            if (end == codePoints.length) {
                break;
            }
            start = end - OVERLAP;
        }

        return chunks;
    }
}