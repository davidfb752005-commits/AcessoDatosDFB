package com.aadtex.ra1_act1;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class CloudBucketService {

    private static final Path BUCKET_PATH = Paths.get(
            "C:/workspace/store_data/output/bucket/" +
                    "aadtex-daily-reports/2026/09"
    );

    public void saveSummary(String fileName, String json)
            throws IOException {

        Files.createDirectories(BUCKET_PATH);

        Path destination = BUCKET_PATH.resolve(fileName);

        Files.writeString(
                destination,
                json,
                StandardCharsets.UTF_8
        );

        System.out.println("Resumen guardado: " + destination);
    }
}

