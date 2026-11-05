
package com.aadtex.ra1_act1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Component
public class StoreIngestionScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(StoreIngestionScheduler.class);

    private static final Path BASE =
            Paths.get("C:/workspace/store_data");

    private static final Path INPUT = BASE.resolve("input");
    private static final Path OUTPUT = BASE.resolve("output");
    private static final Path PROCESSED = OUTPUT.resolve("processed");
    private static final Path SIGNATURES =
            PROCESSED.resolve("signatures");
    private static final Path ERROR = OUTPUT.resolve("error");

    private static final Path BUCKET =
            OUTPUT.resolve("bucket/aadtex-daily-reports/2026/09");

    private final CryptoService cryptoService;
    private final CsvProcessor csvProcessor;
    private final SignatureProcessor signatureProcessor;
    private final ZipProcessor zipProcessor;
    private final CloudBucketService cloudBucketService;

    private volatile boolean applicationReady = false;

    public StoreIngestionScheduler(
            CryptoService cryptoService,
            CsvProcessor csvProcessor,
            SignatureProcessor signatureProcessor,
            ZipProcessor zipProcessor,
            CloudBucketService cloudBucketService) {

        this.cryptoService = cryptoService;
        this.csvProcessor = csvProcessor;
        this.signatureProcessor = signatureProcessor;
        this.zipProcessor = zipProcessor;
        this.cloudBucketService = cloudBucketService;
    }

    // Se activa cuando Spring termina de ejecutar los CommandLineRunner.
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        applicationReady = true;
        log.info("Aplicación preparada. Scheduler de ingestión activado.");
    }

    @Scheduled(fixedRate = 5000)
    public void processStoreBatches() {

        // No procesar nada hasta que termine la inicialización.
        if (!applicationReady) {
            return;
        }

        try {
            // Comprobar la existencia de los directorios principales.
            if (!Files.exists(INPUT)) {
                Files.createDirectories(INPUT);
            }

            if (!Files.exists(OUTPUT)) {
                Files.createDirectories(OUTPUT);
            }

            // Crear los directorios necesarios para el procesamiento.
            Files.createDirectories(PROCESSED);
            Files.createDirectories(SIGNATURES);
            Files.createDirectories(ERROR);
            Files.createDirectories(BUCKET);

            try (Stream<Path> files = Files.list(INPUT)) {

                files.filter(Files::isRegularFile)
                        .forEach(this::processFile);
            }

        } catch (Exception e) {
            log.error("Error en el proceso de ingestion", e);
        }
    }

    private void processFile(Path file) {

        String fileName = file.getFileName().toString();

        int extensionPosition = fileName.lastIndexOf('.');

        if (extensionPosition == -1) {
            return;
        }

        String extension = fileName.substring(
                extensionPosition + 1
        ).toLowerCase();

        try {
            // Comprobar si el archivo está vacío.
            if (Files.size(file) == 0) {

                log.error(
                        "[ALERT EMAIL SENT -> soporte@aadtex.com] CRITICAL: Empty/Corrupt batch file: {}",
                        fileName
                );

                moveFile(file, ERROR.resolve(fileName));
                return;
            }

            double totalSales = 0;
            boolean generateSummary = false;

            switch (extension) {

                case "csv":
                    log.info("Procesando CSV: {}", fileName);

                    try (ByteArrayInputStream input =
                                 new ByteArrayInputStream(
                                         Files.readAllBytes(file))) {

                        totalSales = csvProcessor.processCsv(input);
                    }

                    generateSummary = true;
                    break;

                case "enc":
                    log.info("Descifrando archivo financiero: {}", fileName);

                    byte[] encrypted = Files.readAllBytes(file);
                    byte[] decrypted = cryptoService.decrypt(encrypted);

                    try (ByteArrayInputStream input =
                                 new ByteArrayInputStream(decrypted)) {

                        totalSales = csvProcessor.processCsv(input);
                    }

                    generateSummary = true;
                    break;

                case "jpg":
                    log.info("Copiando firma: {}", fileName);

                    signatureProcessor.copySignature(
                            file,
                            SIGNATURES.resolve(fileName)
                    );
                    break;

                case "zip":
                    log.info("Procesando ZIP: {}", fileName);

                    try (ByteArrayInputStream input =
                                 new ByteArrayInputStream(
                                         Files.readAllBytes(file))) {

                        totalSales = zipProcessor.processZip(input);
                    }

                    generateSummary = true;
                    break;

                default:
                    log.info("Formato no reconocido, se ignora: {}", fileName);
                    return;
            }

            // Generar resumen únicamente para archivos con ventas.
            if (generateSummary) {

                int nextId = getNextSummaryId();

                String summaryName =
                        "summary_batch_" + nextId + ".json";

                String json = createSummary(fileName, totalSales);

                cloudBucketService.saveSummary(summaryName, json);
            }

            // Mover el archivo para que no vuelva a procesarse.
            moveFile(file, PROCESSED.resolve(fileName));

            log.info("Archivo procesado correctamente: {}", fileName);

        } catch (Exception e) {

            log.error("Error procesando el archivo: {}", fileName, e);

            try {
                moveFile(file, ERROR.resolve(fileName));
            } catch (Exception moveError) {
                log.error(
                        "No se pudo mover el archivo a errores: {}",
                        fileName,
                        moveError
                );
            }
        }
    }

    // Busca el siguiente identificador de resumen que no exista.
    private int getNextSummaryId() throws IOException {

        int id = 1;

        while (Files.exists(
                BUCKET.resolve("summary_batch_" + id + ".json"))) {

            id++;
        }

        return id;
    }

    private String createSummary(String fileName, double totalSales) {

        String safeFileName = fileName.replace("\\", "\\\\")
                .replace("\"", "\\\"");

        return "{\n" +
                "  \"file\": \"" + safeFileName + "\",\n" +
                "  \"totalSales\": " + totalSales + "\n" +
                "}\n";
    }

    // Intenta mover el archivo atómicamente.
    private void moveFile(Path source, Path destination)
            throws IOException {

        Files.createDirectories(destination.getParent());

        try {
            Files.move(
                    source,
                    destination,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (AtomicMoveNotSupportedException e) {

            // Si no se admite el movimiento atómico, utiliza el normal.
            Files.move(
                    source,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}
