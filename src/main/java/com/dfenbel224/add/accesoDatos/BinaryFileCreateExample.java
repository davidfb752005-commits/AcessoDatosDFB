package com.dfenbel224.add.accesoDatos;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BinaryFileCreateExample {

    public void createBinaryFile() {

        try (DataOutputStream output =
                     new DataOutputStream(new FileOutputStream("students.bin"))) {

            // Número de estudiantes
            output.writeInt(2);

            // Estudiante 1
            output.writeInt(1);
            output.writeUTF("Sophia");
            output.writeUTF("Developer");

            // Estudiante 2
            output.writeInt(2);
            output.writeUTF("Marcus");
            output.writeUTF("Project Manager");

            System.out.println("Fichero binario creado correctamente.");

        } catch (IOException e) {
            System.out.println("Error al crear el fichero: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        BinaryFileCreateExample bfce = new BinaryFileCreateExample();
        bfce.createBinaryFile();
    }
}