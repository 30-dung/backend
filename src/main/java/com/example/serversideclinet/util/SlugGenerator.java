package com.example.serversideclinet.util;

import java.security.SecureRandom;
import java.util.Base64;

public class SlugGenerator {
    private static final String PREFIX = "APPT-";
    private static final int RANDOM_LENGTH = 5;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generateSlug() {
        StringBuilder slug = new StringBuilder(PREFIX);
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            slug.append(CHARACTERS.charAt(index));
        }
        return slug.toString(); // Ví dụ: APPT-X7Y8Z9
    }

    // Phương thức dùng Base64 để tạo slug ngắn (tùy chọn)
    public static String generateBase64Slug() {
        byte[] randomBytes = new byte[4]; // 4 bytes tạo ~6 ký tự Base64
        random.nextBytes(randomBytes);
        String base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return PREFIX + base64.substring(0, 6); // Ví dụ: APPT-abcdef
    }
}