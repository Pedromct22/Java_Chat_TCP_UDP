package Forcas_armadas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Conectar {

    private static Socket socket;

    private BufferedReader reader;
    private PrintWriter writer;
    public Thread envi;
    public Thread recebe;
    public String user;
    public ArrayList<Integer> grupos;
    public int nextConect;
    public ArrayList<ConectarGrupo> conns ;

    public Conectar() throws IOException {
        this.grupos = new ArrayList<>();
        this.conns = new ArrayList<>();
        this.nextConect = 0;
        this.user = null;
        this.socket = new Socket("localhost", 8080);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.recebe = new Thread(() -> {
            try {
                while (true) {
                    String message = reader.readLine();
                    if (message == null) {
                        break;
                    } else if (message.startsWith("/login")) {
                        String[] parts = message.split(" ");
                        ArrayList<String> partsArr = new ArrayList<>();
                        for (String s : parts){
                         partsArr.add(s);
                           
                        }
                            partsArr.remove(0);
                            partsArr.remove(0);
                            partsArr.remove(0);
                            
                            for(Object o : partsArr){
                                this.grupos.add(Integer.parseInt(o.toString()));
                            }
                        message = parts[1] + " " + parts[2];
                        this.user = parts[2];
                        
                        // ver se chegou aqui
                        //System.out.println( message);

                    } 
                    
                    
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        this.recebe.start();

    }

    public synchronized void fecha() throws InterruptedException, IOException {
    try {
        if (envi != null) {
            envi.join();
        }
        recebe.join();
        socket.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public synchronized void registo() throws InterruptedException, IOException {
        // Aguarda até que ambas as threads terminem
        try {
            this.envi.join();
            this.recebe.join();
            this.socket.close();
        } catch (Exception e) {
    e.printStackTrace();
}
    }

    public synchronized boolean envia(String message) {

        this.envi = new Thread(() -> {
            writer.println(message);

        });
        this.envi.start();

        return true;
    }

    public String getUser() {
        return user;
    }

    public ArrayList<ConectarGrupo> getConns() {
        return conns;
    }

    public void setConns(ArrayList<ConectarGrupo> conns) {
        this.conns = conns;
    }

    
}
