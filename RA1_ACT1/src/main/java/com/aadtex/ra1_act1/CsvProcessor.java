package com.aadtex.ra1_act1;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class CsvProcessor {

    public double processCsv(InputStream input) throws Exception {

        double total = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        input,
                        StandardCharsets.UTF_8))) {

            String line;
            boolean header = true;

            while ((line = reader.readLine()) != null) {

                if (header) {
                    header = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split(",", -1);

                if (data.length < 5) {
                    throw new Exception("Fila CSV incorrecta: " + line);
                }

                double salesAmount = Double.parseDouble(data[2].trim());

                total += salesAmount;
            }
        }

        System.out.println("Total de ventas: " + total);

        return total;
    }

    public double processCsv(byte[] data) throws Exception {
        return processCsv(new ByteArrayInputStream(data));
    }
}

