package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Listener implements Runnable{

    String mensaje;
    int puerto;
    ServerSocket serverSocket;

    InputStream in ;
    OutputStream out;

    Executor ex ;

    // Crear un mapa de acciones
    Map<String, Runnable> acciones = new HashMap<>();


    public Listener(int puerto) throws IOException {
        this.puerto=puerto;
        serverSocket = new ServerSocket(puerto);
    }

    @Override
    public void run(){
        while(true){
            try {
                System.out.println("Escuchando en el puerto ("+puerto+") . . . ");
                Socket socketRedirection = serverSocket.accept();
                // Leemos el input
                in = socketRedirection.getInputStream();
                out = socketRedirection.getOutputStream();

                String mensaje = ReadInput(in);
                this.mensaje  = mensaje;
                if(mensaje!=null){
                    DefinoProcesos();
                    EvaluarMensaje(mensaje);
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void DefinoProcesos() {
        acciones.put("L", ()-> {ex.ProcesoLectura();});
        acciones.put("A", ()-> {ex.ProcesoTransaccion();});
        acciones.put("M", ()-> {ex.ProcesoMinero();});

        acciones.put("V", ()-> {
            Executor.detenerMinero=true;
            ex.ProcesoValidacion();});
        acciones.put("B", ()-> {
            ex.contenido = mensaje.split(";");
            ex.ProcesoBlockchain();});
    }

    private void EvaluarMensaje(String mensaje) {

        String[] contenido = mensaje.split("-");
        ex = new Executor(contenido,out);

        Runnable ac = acciones.getOrDefault(contenido[0],()->{
           //Acccion por defecto
            System.out.println("Modo no reconocido:" + contenido[0] );
        });

        // Crear e iniciar un nuevo hilo para ejecutar la lógica
        new Thread(ac).start();

    }

    private String ReadInput(InputStream in) {
        String r =null;
        Scanner mensaje_Nodo = new Scanner(in);
        if(mensaje_Nodo.hasNextLine()){
            r = mensaje_Nodo.nextLine();
            System.out.println("[+] Mensaje Leido : "+r );
        }
        return r;
    }

}
