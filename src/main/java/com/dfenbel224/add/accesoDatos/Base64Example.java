package com.dfenbel224.add.accesoDatos;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Slf4j
public class Base64Example {

    public void processBase64(Path imagePath, Path restoredPath) {

        if (!Files.exists(imagePath)) {
            log.error("El fichero no existe: " + imagePath);
            return;
        }

        try {
            // Leer el fichero original como bytes
            byte[] binaryData = Files.readAllBytes(imagePath);
            log.info("Tamaño original: " + binaryData.length + " bytes");

            // Codificar bytes a Base64
            String base64String = Base64.getEncoder().encodeToString(binaryData);
            log.info("Tamaño en Base64: " + base64String.length() + " caracteres");

            // Decodificar Base64 de vuelta a bytes
            byte[] restoredData = Base64.getDecoder().decode(base64String);

            // Crear el fichero restaurado
            Files.write(restoredPath, restoredData);

            log.info("Fichero restaurado con éxito en: " + restoredPath);

        } catch (IOException e) {
            log.error("Error procesando Base64: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        Base64Example be = new Base64Example();

        Path original = Path.of("imagen.jpg");
        Path restaurado = Path.of("imagen_restaurada.jpg");

        be.processBase64(original, restaurado);
    }
}