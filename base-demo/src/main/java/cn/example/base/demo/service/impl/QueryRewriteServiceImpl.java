package cn.example.base.demo.service.impl;

import cn.example.base.demo.service.QueryRewriteService;
import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 11030
 */
@Slf4j
@Service
public class QueryRewriteServiceImpl implements QueryRewriteService {

    @Value("${rag.query-rewrite.enabled:true}")
    private boolean enabled;

    @Value("${rag.query-rewrite.max-alternatives:3}")
    private int maxAlternatives;

    // 同义词映射
    private final Map<String, List<String>> synonymMap = Map.of(
            "get", Arrays.asList("obtain", "acquire", "retrieve"),
            "show", Arrays.asList("display", "demonstrate", "present"),
            "explain", Arrays.asList("describe", "clarify", "illustrate"),
            "how", Arrays.asList("method", "way", "process"),
            "what", Arrays.asList("which", "describe"),
            "why", Arrays.asList("reason", "cause", "purpose"),
            "problem", Arrays.asList("issue", "error", "bug"),
            "solution", Arrays.asList("answer", "fix", "resolution"),
            "best", Arrays.asList("optimal", "top", "excellent"),
            "fast", Arrays.asList("quick", "rapid", "speedy")
    );

    // 停用词
    private final Set<String> stopWords = Set.of("the", "a", "an", "and", "or", "but",
            "in", "on", "at", "to", "for", "of", "with", "by", "as", "is", "are", "was", "were");

    /**
     * 查询改写：生成多个相关查询
     */
    @Override
    public List<String> rewriteQuery(String originalQuery) {
        if (!enabled) {
            return Collections.singletonList(originalQuery);
        }

        List<String> rewrittenQueries = new ArrayList<>();
        rewrittenQueries.add(originalQuery);

        // 1. 同义词扩展
        rewrittenQueries.addAll(expandWithSynonyms(originalQuery));

        // 2. 问题形式转换
        rewrittenQueries.addAll(transformQuestionForms(originalQuery));

        // 3. 查询简化
        rewrittenQueries.add(simplifyQuery(originalQuery));

        // 4. 查询扩展
        rewrittenQueries.addAll(expandQuery(originalQuery));

        // 去重并限制数量
        return rewrittenQueries.stream()
                .distinct()
                .limit(maxAlternatives)
                .collect(Collectors.toList());
    }

    /**
     * 同义词扩展
     */
    private List<String> expandWithSynonyms(String query) {
        List<String> expanded = new ArrayList<>();
        String[] words = query.toLowerCase().split("\\s+");

        for (int i = 0; i < words.length; i++) {
            if (synonymMap.containsKey(words[i])) {
                for (String synonym : synonymMap.get(words[i])) {
                    String[] newWords = words.clone();
                    newWords[i] = synonym;
                    expanded.add(String.join(" ", newWords));
                }
            }
        }

        return expanded;
    }

    /**
     * 问题形式转换
     */
    private List<String> transformQuestionForms(String query) {
        List<String> transformed = new ArrayList<>();

        if (query.endsWith("?")) {
            // 移除问号
            transformed.add(query.substring(0, query.length() - 1).trim());

            // 转换为什么问题
            if (query.toLowerCase().startsWith("how")) {
                transformed.add("What is the process of " + query.substring(4));
            } else if (query.toLowerCase().startsWith("what")) {
                transformed.add("Explain " + query.substring(5));
            } else if (query.toLowerCase().startsWith("why")) {
                transformed.add("What is the reason for " + query.substring(4));
            }
        } else {
            // 添加问号形式
            transformed.add(query + "?");

            // 转换为陈述句
            transformed.add("I need information about " + query);
        }

        return transformed;
    }

    /**
     * 查询简化
     */
    private String simplifyQuery(String query) {
        return Arrays.stream(query.toLowerCase().split("\\s+"))
                .filter(word -> !stopWords.contains(word) && word.length() > 2)
                .reduce((a, b) -> a + " " + b)
                .orElse(query);
    }

    /**
     * 查询扩展
     */
    private List<String> expandQuery(String query) {
        List<String> expanded = new ArrayList<>();

        // 添加常见扩展
        expanded.add(query + " definition");
        expanded.add(query + " explanation");
        expanded.add(query + " tutorial");
        expanded.add(query + " guide");
        expanded.add(query + " example");

        return expanded;
    }

    /**
     * 查询增强：添加上下文
     */
    public String enhanceQueryWithContext(String query, List<String> contextKeywords) {
        if (CollUtil.isNotEmpty(contextKeywords)) {
            String context = String.join(" ", contextKeywords);
            return query + " " + context;
        }
        return query;
    }

    /**
     * 获取查询意图
     */
    public String detectQueryIntent(String query) {
        query = query.toLowerCase();

        if (query.contains("how to") || query.contains("how do i")) {
            return "tutorial";
        } else if (query.contains("what is") || query.contains("what are")) {
            return "definition";
        } else if (query.contains("why") || query.contains("reason")) {
            return "explanation";
        } else if (query.contains("compare") || query.contains("vs")) {
            return "comparison";
        } else if (query.contains("best") || query.contains("top")) {
            return "recommendation";
        } else if (query.contains("error") || query.contains("problem")) {
            return "troubleshooting";
        } else {
            return "general";
        }
    }

}
