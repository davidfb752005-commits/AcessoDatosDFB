package com.dfenbel224.add.accesoDatos;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j

public class TextFileExample {

    public void processTextFile() {
        Path path = Paths.get("students.csv");

        try {
            String csvData = """
                ID,Name,Role
                1,Sophia,Developer
                2,Marcus,Project Manager
                """;

            //csvData = null;

            Files.writeString(path, csvData, StandardCharsets.UTF_8);
            log.info("Fichero escrito correctamente: " + path.toAbsolutePath());

            String retrievedContent = Files.readString(path, StandardCharsets.UTF_8);
            log.info("--- Contenido recuperado ---");
            log.info(retrievedContent);

        } catch (IOException e) {
            log.error("Error al procesar el fichero de texto: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        TextFileExample tfe = new TextFileExample();
        tfe.processTextFile();
    }
}