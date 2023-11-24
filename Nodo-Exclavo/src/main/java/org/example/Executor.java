package org.example;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

public class Executor implements Acciones {

    String[] contenido;
    OutputStream out;
    FileManager fileManager;

    static boolean detenerMinero ;


    public Executor(String[] contenido, OutputStream out) {
        this.contenido = contenido;
        this.out = out;
        this.fileManager = new FileManager();
        detenerMinero = false;
    }
    @Override
    public void ProcesoActualizacionInicial(){
        System.out.println(" [+] MODO ACTUALIZACIÓN INICIAL");
        fileManager.cuentas.clear();
        fileManager.actualizarTxt();
        for (String registro: contenido) {
            String[] p = registro.split("-");
            String iD = p[0] , saldo = p[1];
            fileManager.cuentas.put(iD,saldo);
        }
        fileManager.actualizarTxt();
        fileManager.mostrarCuentas();
    }

    @Override
    public void ProcesoLectura() {
        // Formato Lectura:  modo-idSolicitud-idCuenta [L-02-1992]
        System.out.println(" [+] MODO CONSULTA");
        String idCuenta = contenido[2];

        Map<String, String> ListaCuentas = fileManager.cuentas;

        String monto = ListaCuentas.get(idCuenta);
        if (monto != null) {
            System.out.println(" [+]  Se encontró Cuenta : \n\tID: " + idCuenta + " - Money: " + monto);
            String mensaje = contenido[0] + "-" +
                    contenido[1] + "-" +
                    contenido[2] + "-" + monto;
            EnvioMensaje(mensaje);
        } else {
            String mensaje = "F-mensaje de error";
            EnvioMensaje(mensaje);
            System.out.println("[-] No se encontró Cuenta");
        }
    }

    private void EnvioMensaje(String mensaje) {
        PrintWriter escritor = new PrintWriter(out, true);
        // Enviar el mensaje al socket
        System.out.println("[DEBUG] Enviando mensaje al servidor: " + mensaje);
        escritor.println(mensaje);
    }

    @Override
    public void ProcesoTransaccion() {
        // Formato Transacción: modo-cuentaOrigen- monto1 - cuentaDestino-monto2 [A-1456-345.72-1345-345.78]
        System.out.println(" [+] MODO TRANSACCIÓN");
        String cuentaOrigen = contenido[1], cuentaDestino = contenido[3];
        String monto1 = contenido[2], monto2 = contenido[4];

        Map<String, String> ListaCuentas = fileManager.cuentas;
        ListaCuentas.put(cuentaOrigen, monto1);
        ListaCuentas.put(cuentaDestino, monto2);

        // Actualizar el txt
        fileManager.actualizarTxt();
        fileManager.mostrarCuentas();
    }

    @Override
    public void ProcesoMinero() {
        // Formato: modo-nroCeros-HashBloqueAnterior-HashRaiz [M-3-0123546045-0154065046]
        System.out.println(" [+] MODO MINERO");
        String n_Ceros = contenido[1];
        String HashAnt = contenido[2];
        String HasRaiz = contenido[3];
        // Formato Envio :modo-nonce-tiempo-nroCeros-HashTotal
        String[] envio = new String[5];
        envio[0] = contenido[0];
        int nonce = 0;
        String HashActual = "";

        double tiempoInicio = System.nanoTime(); // Obtener el tiempo de inicio

        while (true) {
            String datosConcatenados = HashAnt + HasRaiz + nonce;
            // Calcular el hash de los datos concatenados
            HashActual = calcularHash(datosConcatenados);

            System.out.println(" -> " + nonce + " - " + HashActual);

            if (CriterioParada(HashActual, n_Ceros)) {
                double tiempoFin = System.nanoTime(); // Obtener el tiempo de finalización
                double tiempoTotal = (tiempoFin - tiempoInicio) / 1e9; // Convertir a segundos
                System.out.println("[+] COMPLETED!!");
                System.out.println("[*] Hash : " + HashActual);
                System.out.println("[*] Nonce : " + nonce);
                System.out.println("[*] Tiempo : " + tiempoTotal);

                envio[1] = String.valueOf(nonce);
                envio[2] = String.valueOf(tiempoTotal);
                envio[3] = n_Ceros;

                envio[4] = HashActual;
                break;
            }

            if (hayMensajeDeValidacion()) {
                System.out.println("[-] Se recibió un mensaje de validación. Saliendo del bucle de minería.");
                Executor.detenerMinero = false;
                return;
            }

            nonce++;
        }


        // Formato Envio :modo-nonce-tiempo-nroCeros-hashAnt-hashRaiz-HashTotal
        String mensaje = envio[0] + "-" + envio[1] + "-" + envio[2] + "-" + envio[3] + "-" + HashAnt + "-" + HasRaiz +
                "-" + envio[4];
        EnvioMensaje(mensaje);

    }

    private boolean hayMensajeDeValidacion() {
        return detenerMinero;
    }
    private String calcularHash(String datosConcatenados) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(datosConcatenados.getBytes());

            // Convertir el hash a formato hexadecimal
            StringBuilder hashHex = new StringBuilder();
            for (byte b : hashBytes) {
                hashHex.append(String.format("%02x", b));
            }

            return hashHex.toString();


        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al calcular el hash", e);
        }
    }

    boolean CriterioParada(String HashActual, String n_Ceros) {
        int n_ceros = Integer.parseInt(n_Ceros);
        if (HashActual.length() >= n_ceros) {
            for (int i = 0; i < n_ceros; i++) {
                if (HashActual.charAt(i) != '0') {
                    return false;
                }
            }
            return true;

        } else {
            return false;
        }
    }

    @Override
    public void ProcesoValidacion() {
        System.out.println(" [+] MODO VALIDACIÓN");
        // Formato: modo-nonce-nroCeros-HashBloqueAnterior-HashRaiz-HashTotal [V-32-8-0123546045-0154065046-1231412]
        String nonce = contenido[1], n_ceros = contenido[2];
        String HashAnterior = contenido[3], Hashraiz = contenido[4];
        String HashTotal = contenido[5];

        String datosConcatenados = HashAnterior + Hashraiz + nonce;
        String hashCalculado = calcularHash(datosConcatenados);
        String mensaje = "";
        if (CriterioParada(hashCalculado, n_ceros)) {
            System.out.println("[+] VALIDACIÓN EXITOSA");
            System.out.println("[*] Hash Total: " + HashTotal);
            mensaje = contenido[0] + "-" + "true";

        } else {
            System.out.println("[-] VALIDACIÓN FALLIDA");
            mensaje = contenido[0] + "-" + "False";
        }
        EnvioMensaje(mensaje);
    }
    @Override
    public void ProcesoBlockchain() {
        System.out.println(" [+] MODO BLOCKCHAIN");

        // Verificar si el archivo blockchain existe, si no, crearlo
        if (!fileManager.verificarExistenciaArchivo("blockchain.txt")) {
            fileManager.crearArchivo("blockchain.txt");
            System.out.println("[+] Archivo Blockchain creado exitosamente.");
        }

        // Agregar cada registro al archivo blockchain
        for (String registro : contenido) {
            if (!registro.trim().isEmpty()) {
                fileManager.agregarRegistroBlockchain(registro.trim());
            }
        }
    }
}
