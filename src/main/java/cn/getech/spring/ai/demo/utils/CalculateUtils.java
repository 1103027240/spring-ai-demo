package cn.getech.spring.ai.demo.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * @author 11030
 */
public class CalculateUtils {

    /**
     * 计算文档内容的哈希值
     */
    public static String calculateHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating hash", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

}
