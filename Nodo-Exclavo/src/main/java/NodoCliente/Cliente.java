package NodoCliente;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {


    public static void main(String[] args) throws IOException {
        final String servidorIP = "localhost";
        final int puerto = 50000;

        Socket socket  = new Socket(servidorIP,puerto );

        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();

        PrintWriter escribir = new PrintWriter(out,true);

        // Consulta : [L-02-1992]
        // Transaccion : [A-1456-12.3-1456-2.78]
        // Minero :  [M-5-0123546045-0154065046]
        // Validacion : [V-1482611-5-0123546045-0154065046-00000b98585c99d37fbaad938591c3239b879938937e27ac1a4e841eda3be0ab]
        // Blockchain : B-1992-4567-45678.78-0001354654;B-1993-4567-45678.78-0001354654;
        // Rellenado inicial: [C-1-120.4;2-0.0;3-455.5]
        String mensaje  = "V-1482611-5-0123546045-0154065046-000";
        escribir.println(mensaje);

        Scanner sc =  new Scanner(in);
        System.out.println(sc.hasNextLine());

        if(sc.hasNextLine()){
            System.out.println("Se encontró mensaje :");
            System.out.println(sc.nextLine());
        }




    }

}
