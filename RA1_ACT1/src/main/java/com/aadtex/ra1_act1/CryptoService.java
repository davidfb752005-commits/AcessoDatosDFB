package com.aadtex.ra1_act1;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class CryptoService {

    private static final Path KEY_PATH =
            Paths.get("C:/workspace/store_data/keys/AadTexSecret.key");

    public byte[] decrypt(byte[] encryptedData) throws Exception {

        String key = Files.readString(
                KEY_PATH,
                StandardCharsets.UTF_8
        ).trim();

        SecretKeySpec secretKey = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8),
                "AES"
        );

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return cipher.doFinal(encryptedData);
    }
}

