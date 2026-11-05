package com.aadtex.ra1_act1;

import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class SignatureProcessor {

    private static final int BUFFER_SIZE = 4096;

    public void copySignature(Path source, Path destination)
            throws IOException {

        Files.createDirectories(destination.getParent());

        try (BufferedInputStream input = new BufferedInputStream(
                Files.newInputStream(source));
             BufferedOutputStream output = new BufferedOutputStream(
                     Files.newOutputStream(destination))) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }

        System.out.println("Firma copiada: " + destination);
    }
}
