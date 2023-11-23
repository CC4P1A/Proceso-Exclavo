package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NodoExclavo {

    public static void main(String[] args) throws IOException {

        // Crear el archivo cuentas.txt si no existe
        crearArchivoCuentas();

        Listener e = new Listener(50000);
        Thread listener = new Thread(e);
        listener.start();

        System.out.println("Hilo de Escucha [ INICIALIZADO ... ] ");

    }

    private static void crearArchivoCuentas() throws IOException {
        Path archivoCuentas = Path.of("cuentas.txt");

        // Verificar si el archivo no existe
        if (Files.notExists(archivoCuentas)) {
            try {
                // Crear el archivo
                Files.createFile(archivoCuentas);
            } catch (IOException e) {
                e.printStackTrace();
                throw e; // Relanzar la excepción para manejarla en el método principal
            }
        }
    }

}
