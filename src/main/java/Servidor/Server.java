package Servidor;

import Servidor.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

class Server implements Runnable { 

  private int port; 
  private InetAddress group; 
  private MulticastSocket socket; 
  private static final int MAX_LEN = 1000; 

  public Server(MulticastSocket socket,InetAddress group,int port){ 
      this.socket = socket; 
      this.group = group; 
      this.port = port; 
  } 
      
  @Override
  public void run(){ 
    while(!ConectarGrupo.finished){ 
      byte[] buffer = new byte[Server.MAX_LEN]; 
      DatagramPacket datagram = new
      DatagramPacket(buffer,buffer.length,group,port); 
      String message; 
      try { 
        socket.receive(datagram); 
        message = new String(buffer,0,datagram.getLength(),"UTF-8"); 
        if(!message.startsWith(ConectarGrupo.username)){
          System.out.println(message); 
        }
      } catch(IOException e){ 
         System.out.println(""+ConectarGrupo.username+"  desconectou. "); 
      } 
    } 
  } 
}
