package org.example;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileManager {
    public Map<String, String> cuentas;

    public FileManager() {
        this.cuentas = new HashMap<>();
        LeerArchivoCuentas();
    }

    public void LeerArchivoCuentas() {
        try (BufferedReader br = new BufferedReader(new FileReader("cuentas.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(" ");
                if (partes.length == 2) {
                    cuentas.put(partes[0].trim(), partes[1].trim());
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    public void actualizarTxt() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("cuentas.txt"))) {
            for (Map.Entry<String, String> entry : cuentas.entrySet()) {
                String idCuenta = entry.getKey();
                String monto = entry.getValue();
                String linea = idCuenta + " " + monto;
                bw.write(linea);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println(e);
        }

    }

    public void mostrarCuentas() {
        System.out.println("Cuentas:");
        for (Map.Entry<String, String> entry : cuentas.entrySet()) {
            String idCuenta = entry.getKey();
            String monto = entry.getValue();
            System.out.println("\tID: " + idCuenta + ", Monto: " + monto);
        }
    }

    public boolean verificarExistenciaArchivo(String archivo) {
        File file = new File(archivo);
        return file.exists();
    }

    public void agregarRegistroBlockchain(String registro) {
        String archivoBlockchain = "blockchain.txt";
        try (FileWriter fw = new FileWriter(archivoBlockchain, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            // No es necesario agregar la etiqueta cada vez que se agrega un registro
            String r ="";
            String[] partes = registro.split("-");
            r = partes[1]+" "+partes[2]+" "+partes[3]+" "+partes[4];
            out.println(r);
            System.out.println("[+] Registro añadido al Blockchain.");
        } catch (IOException e) {
            System.out.println("[-] Error al escribir en el archivo Blockchain: " + e.getMessage());
        }
    }

    public void crearArchivo(String file) {
        File nuevoArchivo = new File(file);

        try {
            if (nuevoArchivo.createNewFile()) {
                System.out.println("[+] Archivo creado exitosamente: " + file);
            } else {
                System.out.println("[-] El archivo ya existe: " + file);
            }
        } catch (IOException e) {
            System.out.println("[-] Error al crear el archivo: " + e.getMessage());
        }
    }
}