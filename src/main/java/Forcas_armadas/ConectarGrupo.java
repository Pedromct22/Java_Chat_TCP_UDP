package Forcas_armadas;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Scanner;


public class ConectarGrupo extends Thread{

    private static final String EXIT = "/exit";
    public static String username;
    static volatile boolean finished = false;
    private static Scanner in = new Scanner(System.in);
    public MulticastSocket socket;
    public InetAddress group;
    private int porta;

    public ConectarGrupo(int porta, String username) {
        this.porta = porta;
        this.username = username;
        try {
            group = InetAddress.getByName("239.0.0.0");
            int port = porta;

            System.out.println("Conectado ao grupo numero: "+ porta);

            socket = new MulticastSocket(port);
            socket.setTimeToLive(0);
            socket.joinGroup(group);
            Thread thread = new Thread(new Server(socket, group, port));
            thread.start();

        } catch (SocketException e) {
            System.out.println("Erro a conectar");
        } catch (IOException e) {
            System.out.println("Erro no sistema.");
        }
    }

    public synchronized void fecha() {
    try {
        ConectarGrupo.finished = true; // fglag para desligar connect
        socket.leaveGroup(this.group);
        socket.close();
    } catch (IOException e) {
        e.printStackTrace(); // mensagem
    }
}

    
    //metodo para enviar mensagem para o grupo
    public synchronized void envia(String message) throws IOException {
        try {
            if (!message.isEmpty()) {
                message = this.getPorta()+": " + username + ": " + message;
            }
            byte[] buffer = message.getBytes();
            DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, this.porta);
            socket.send(datagram);

        } catch (SocketException e) {
            e.getMessage();
        } catch (IOException e) {
            System.out.println("Erro a enviar mensagem.");
        }

    }

    public int getPorta() {
        return porta;
    }
    
    

}
