package com.dfenbel224.add.accesoDatos;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Slf4j
public class BinaryFileExample {

    public void processBinaryFile(String filename) {
        File file = new File(filename);

        if (!file.exists()) {
            log.error("No se encuentra el fichero binario: " + filename);
            return;
        }

        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            String hash = HexFormat.of().formatHex(digest.digest());

            log.info("Fichero: " + file.getName());
            log.info("Tamaño: " + (file.length() / 1024) + " KB");
            log.info("SHA-256: " + hash);

        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Error procesando fichero binario: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        BinaryFileExample bfe = new BinaryFileExample();
        bfe.processBinaryFile("students.csv");
    }
}