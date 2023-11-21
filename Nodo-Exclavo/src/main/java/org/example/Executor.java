package org.example;

import java.io.*;
import java.net.Socket;

public class Executor implements Acciones{

    String[] contenido;
    OutputStream out;

    public Executor(String[] contenido, OutputStream out){
        this.contenido=contenido;
        this.out = out;
    }

    @Override
    public void ProcesoLectura(){
        // Formato Lectura:  modo-idSolicitud-idCuenta [L-02-1992]
        System.out.println(" [+] MODO LECTURA");
        String idCuenta = contenido[2];
        boolean CuentaEncontrada = false;
        String monto_encontrado = "";
        //Leer el archivo de cuentas
        try(BufferedReader br = new BufferedReader(new FileReader("cuentas.txt"))){

            String linea;
            while((linea=br.readLine())!=null){
                String[] partes = linea.split(" ");
                if(partes.length ==2){
                    String idCuenta_txt = partes[0].trim();
                    String dinero = partes[1].trim();

                    if(idCuenta.equals(idCuenta_txt)){
                        CuentaEncontrada = true;
                        monto_encontrado = dinero;
                        break;
                    }
                }
            }

            if(CuentaEncontrada){
                System.out.println(" -> "+idCuenta+" - "+monto_encontrado);
                EnvioMensaje(monto_encontrado);

            }else{
                System.out.println(" [-] Registro no encontrado");
            }


        }catch(IOException e){
            System.out.println(e);

        }
    }

    private void EnvioMensaje(String monto) {
        PrintWriter escritor = new PrintWriter(out, true);
        // Enviar el mensaje al socket
        String mensaje = contenido[0] + contenido[1] + contenido[2] + monto;
        escritor.println(mensaje);
    }

    @Override
    public void ProcesoTransaccion() {
        // Formato Transacción: modo-cuentaOrigen-monto1 - cuentaDestino-monto2 [A-1456-345.72-1345-345.78]
        System.out.println(" [+] MODO TRANSACCIÓN");


    }
    @Override
    public void ProcesoMinero(){

    }
    @Override
    public void ProcesoValidacion(){

    }
    @Override
    public void ProcesoBlockchain(){

    }
}
