package org.bn.sensation.security;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.crypto.password.PasswordEncoder;

//todo: удалить на проде, т.к. может расшифровать пароль обратно
public class AesPasswordEncoder implements PasswordEncoder {

    private static final String ALGORITHM = "AES";
    private final SecretKey secretKey;

    public AesPasswordEncoder(String key) {
        // ключ должен быть 16, 24 или 32 байта
        this.secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return PasswordEncoder.super.upgradeEncoding(encodedPassword);
    }

    @Override
    public String encode(CharSequence rawPassword) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(rawPassword.toString().getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при шифровании", e);
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            String decrypted = new String(cipher.doFinal(Base64.getDecoder().decode(encodedPassword)));
            return rawPassword.toString().equals(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при расшифровке", e);
        }
    }

    // Дополнительно — метод для ручной расшифровки
    public String decrypt(String encodedPassword) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encodedPassword)));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при расшифровке", e);
        }
    }

}
