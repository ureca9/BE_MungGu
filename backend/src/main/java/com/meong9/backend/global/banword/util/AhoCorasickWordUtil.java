package com.meong9.backend.global.banword.util;

import com.meong9.backend.global.banword.domain.Word;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class AhoCorasickWordUtil implements WordUtil {

    private final TrieNode root;

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

        int realIndex = 0;
        int nonSpaceIndex = 0;

        Map<Integer, Integer> startIndices = new HashMap<>();

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);

            if (Character.isWhitespace(c)) {
                realIndex++;
                continue;
            }

            startIndices.put(nonSpaceIndex, realIndex);

            while (node != root && !node.children.containsKey(c)) {
                node = node.failureLink;
            }

            if (node.children.containsKey(c)) {
                node = node.children.get(c);
            }

            for (String pattern : node.output) {
                int start = startIndices.getOrDefault(nonSpaceIndex - (pattern.length() - 1), -1);
                int end = realIndex + 1;

                if (start != -1) {
                    result.add(new Word(pattern, start, end));
                }
            }

            nonSpaceIndex++;
            realIndex++;
        }

        for (Word r:result) {
            log.info("검색 결과: {}",r.word());
        }
        return result;
    }

}