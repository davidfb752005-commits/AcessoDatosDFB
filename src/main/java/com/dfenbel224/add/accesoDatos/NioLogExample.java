package com.dfenbel224.add.accesoDatos;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j


public class NioLogExample {

    public void writeLogEntry(String message) {
        Path logFile = Path.of("logs", "application.log");

        try {
            // Crear directorios padres si no existen
            if (logFile.getParent() != null) {
                Files.createDirectories(logFile.getParent());
            }

            // Escribir añadiendo al final (APPEND)
            Files.writeString(
                    logFile,
                    message + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            log.info("Log registrado en: " + logFile.toAbsolutePath());
            log.info("Tamaño actual: " + Files.size(logFile) + " bytes");

        } catch (IOException e) {
            log.error("Error al escribir el archivo de log: " + e.getMessage());
        }
    }

    public static void main(String[] args){
        NioLogExample nle = new NioLogExample();
        nle.writeLogEntry("Hola buenas");

    }
}
