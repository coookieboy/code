package org.coookieboy;

public class Hello {
    public static void main(String[] args) {
        String original = "Hello, World!";
        int key = 42;

        // 加密
        String encrypted = xorEncryptDecrypt(original, key);
        System.out.println("加密后的字符串: " + encrypted);

        // 解密
        String decrypted = xorEncryptDecrypt(encrypted, key);
        System.out.println("解密后的字符串: " + decrypted);
    }

    public static String xorEncryptDecrypt(String input, int key) {
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] ^ key);
        }
        return new String(chars);
    }
}
