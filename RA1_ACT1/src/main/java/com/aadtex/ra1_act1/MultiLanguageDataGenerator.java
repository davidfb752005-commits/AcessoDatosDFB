
package com.aadtex.ra1_act1;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Order(1)
public class MultiLanguageDataGenerator implements CommandLineRunner {

    private static final Path BASE =
            Paths.get("C:/workspace/store_data");

    private static final String KEY = "AadTexSecret2026";

    private static final Path GENERATOR_MARKER =
            BASE.resolve(".generator-completed");

    @Override
    public void run(String... args) throws Exception {

        // Si ya se generaron los datos anteriormente, no repetirlos.
        if (Files.exists(GENERATOR_MARKER)) {
            System.out.println(
                    "Los datos de prueba ya fueron generados anteriormente.");
            return;
        }

        Path input = BASE.resolve("input");
        Path keys = BASE.resolve("keys");

        Files.createDirectories(input);
        Files.createDirectories(keys);

        Files.createDirectories(
                BASE.resolve("output/processed/signatures"));
        Files.createDirectories(BASE.resolve("output/error"));
        Files.createDirectories(
                BASE.resolve("output/bucket/aadtex-daily-reports/2026/09"));

        // Clave AES
        Files.writeString(
                keys.resolve("AadTexSecret.key"),
                KEY,
                StandardCharsets.UTF_8);

        // CSV de España
        String spain =
                "StoreID,StoreName,SalesAmount,Category,TransactionDate\n" +
                        "ZARA_ES_001,Zara Puerta del Sol Madrid,14500.50,WOMEN,2026-09-13\n" +
                        "ZARA_ES_001,Zara Puerta del Sol Madrid,3600.30,MEN,2026-09-13\n" +
                        "ZARA_ES_001,Zara Gran Vía Madrid,2000.00,KIDS,2026-09-13\n";

        Files.writeString(
                input.resolve("daily_sales_spain.csv"),
                spain,
                StandardCharsets.UTF_8);

        // CSV de China
        String china =
                "StoreID,StoreName,SalesAmount,Category,TransactionDate\n" +
                        "ZARA_CH_001,Zara 上海南京东路店,28900.00,WOMEN,2026-09-13\n" +
                        "ZARA_CH_001,Zara 上海南京东路店,12400.80,MEN,2026-09-13\n";

        Files.writeString(
                input.resolve("daily_sales_china.csv"),
                china,
                StandardCharsets.UTF_8);

        // Crear una imagen JPEG válida para las firmas de prueba.
        BufferedImage image =
                new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);

        Path tempImage = Files.createTempFile("signature", ".jpg");

        byte[] jpeg;

        try {
            ImageIO.write(image, "jpg", tempImage.toFile());
            jpeg = Files.readAllBytes(tempImage);
        } finally {
            Files.deleteIfExists(tempImage);
        }

        Files.write(input.resolve("signature_spain.jpg"), jpeg);
        Files.write(input.resolve("signature_china.jpg"), jpeg);
        Files.write(input.resolve("signature_financial.jpg"), jpeg);

        // CSV financiero cifrado con AES-128.
        String financial =
                "StoreID,StoreName,SalesAmount,Category,TransactionDate\n" +
                        "ZARA_CENTRAL_FIN,Central Financial Office,95000.00,FINANCIAL_REPORT,2026-09-13\n";

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        SecretKeySpec secretKey = new SecretKeySpec(
                KEY.getBytes(StandardCharsets.UTF_8),
                "AES");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encrypted = cipher.doFinal(
                financial.getBytes(StandardCharsets.UTF_8));

        Files.write(
                input.resolve("daily_sales_financial.enc"),
                encrypted);

        // Archivo vacío para probar la detección de errores.
        Files.write(
                input.resolve("daily_sales_broken.csv"),
                new byte[0]);

        // Crear el ZIP de Estados Unidos directamente en memoria.
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(bytes)) {

            zip.putNextEntry(new ZipEntry("daily_sales_usa.csv"));

            String usa =
                    "StoreID,StoreName,SalesAmount,Category,TransactionDate\n" +
                            "ZARA_US_001,Zara New York,18000.00,WOMEN,2026-09-13\n";

            zip.write(usa.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("signature_usa.jpg"));
            zip.write(jpeg);
            zip.closeEntry();

            zip.finish();

            Files.write(
                    input.resolve("batch_usa.zip"),
                    bytes.toByteArray());
        }

        // El marcador se crea solamente cuando tod0 ha terminado bien.
        Files.writeString(
                GENERATOR_MARKER,
                "Datos de prueba generados correctamente.",
                StandardCharsets.UTF_8);

        System.out.println(
                "Archivos de prueba generados en: " + input.toAbsolutePath());
    }
}
