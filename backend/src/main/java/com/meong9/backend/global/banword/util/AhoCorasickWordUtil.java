package com.meong9.backend.global.banword.util;

import com.meong9.backend.global.banword.domain.Word;

import java.util.*;

public class AhoCorasickWordUtil implements WordUtil {

    private final TrieNode root;
    private static final String REMOVE_PATTERN = "[\\p{N}\\s\\u3164\\p{L}\\p{P}&&[^ㄱ-ㅎ가-힣ㅏ-ㅣa-zA-Z]]";

    public AhoCorasickWordUtil() {
        this.root = new TrieNode();
    }

    static private class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        TrieNode failureLink = null;
        Set<String> output = new HashSet<>();
    }

    @Override
    public void addWord(String word) {
        TrieNode node = root;
        for (int i = 0; i < word.length(); i++) {
            node = node.children.computeIfAbsent(word.charAt(i), k -> new TrieNode());
        }
        node.output.add(word);
    }

    @Override
    public void build() {
        Queue<TrieNode> queue = new LinkedList<>();
        root.failureLink = root;

        for (TrieNode node : root.children.values()) {
            node.failureLink = root;
            queue.add(node);
        }

        while (!queue.isEmpty()) {
            TrieNode current = queue.poll();

            for (Map.Entry<Character, TrieNode> entry : current.children.entrySet()) {
                char c = entry.getKey();
                TrieNode child = entry.getValue();

                TrieNode failure = current.failureLink;
                while (failure != root && !failure.children.containsKey(c)) {
                    failure = failure.failureLink;
                }

                if (failure.children.containsKey(c) && failure.children.get(c) != child) {
                    child.failureLink = failure.children.get(c);
                } else {
                    child.failureLink = root;
                }

                child.output.addAll(child.failureLink.output);
                queue.add(child);
            }
        }
    }

    @Override
    public List<Word> search(String word) {
        List<Word> result = new ArrayList<>();
        if (word == null || word.isEmpty()) return result;
        TrieNode node = root;

        int realIndex = 0; // 원본 문자열 인덱스
        int nonSpaceIndex = 0; // 변환된 문자열 인덱스

        // 변환된 문자열 인덱스를 원본 문자열 인덱스와 매핑
        Map<Integer, Integer> startIndices = new HashMap<>();

        // 변환된 문자열 만들기 (제거 규칙 적용)
        StringBuilder transformed = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);

            // REMOVE_PATTERN에 포함되지 않는 문자만 추가
            if (!String.valueOf(c).matches(REMOVE_PATTERN)) {
                startIndices.put(nonSpaceIndex, realIndex);
                transformed.append(c);
                nonSpaceIndex++;
            }
            realIndex++;
        }

        // 변환된 문자열에서 금칙어 탐지
        node = root;
        for (int i = 0; i < transformed.length(); i++) {
            char c = transformed.charAt(i);

            while (node != root && !node.children.containsKey(c)) {
                node = node.failureLink;
            }

            if (node.children.containsKey(c)) {
                node = node.children.get(c);
            }

            for (String pattern : node.output) {
                int transformedStart = i - (pattern.length() - 1);
                int start = startIndices.getOrDefault(transformedStart, -1);
                int end = startIndices.getOrDefault(i, -1) + 1;

                if (start != -1 && end != -1) {
                    // 원본 문자열에서 금칙어 텍스트 추출
                    String originalMatch = word.substring(start, end);
                    result.add(new Word(originalMatch, start, end));
                }
            }
        }

        return result;
    }
}