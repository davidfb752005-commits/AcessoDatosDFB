package com.aadtex.ra1_act1;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ZipProcessor {

    private final CsvProcessor csvProcessor;
    private final SignatureProcessor signatureProcessor;

    private static final Path SIGNATURES_PATH =
            Paths.get("C:/workspace/store_data/output/processed/signatures");

    public ZipProcessor(CsvProcessor csvProcessor,
                        SignatureProcessor signatureProcessor) {
        this.csvProcessor = csvProcessor;
        this.signatureProcessor = signatureProcessor;
    }

    public double processZip(InputStream input) throws Exception {

        double totalSales = 0;

        try (ZipInputStream zip = new ZipInputStream(input)) {

            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {

                String fileName = Path.of(entry.getName())
                        .getFileName()
                        .toString();

                if (entry.isDirectory()) {
                    zip.closeEntry();
                    continue;
                }

                ByteArrayOutputStream content =
                        new ByteArrayOutputStream();

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = zip.read(buffer)) != -1) {
                    content.write(buffer, 0, bytesRead);
                }

                byte[] data = content.toByteArray();

                if (fileName.toLowerCase().endsWith(".csv")) {

                    System.out.println("CSV encontrado en ZIP: " + fileName);

                    totalSales += csvProcessor.processCsv(
                            new ByteArrayInputStream(data)
                    );

                } else if (fileName.toLowerCase().endsWith(".jpg")) {

                    System.out.println("Firma encontrada en ZIP: " + fileName);

                    Path destination = SIGNATURES_PATH.resolve(fileName);

                    try (InputStream imageInput =
                                 new ByteArrayInputStream(data)) {

                        java.nio.file.Files.createDirectories(
                                destination.getParent()
                        );

                        try (java.io.BufferedInputStream bufferedInput =
                                     new java.io.BufferedInputStream(imageInput);
                             java.io.BufferedOutputStream bufferedOutput =
                                     new java.io.BufferedOutputStream(
                                             java.nio.file.Files.newOutputStream(destination))) {

                            int count;
                            byte[] imageBuffer = new byte[4096];

                            while ((count = bufferedInput.read(imageBuffer)) != -1) {
                                bufferedOutput.write(imageBuffer, 0, count);
                            }
                        }
                    }
                }

                zip.closeEntry();
            }
        }

        System.out.println("Total de ventas del ZIP: " + totalSales);

        return totalSales;
    }
}

