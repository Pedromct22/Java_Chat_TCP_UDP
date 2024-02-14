package Servidor;

import Servidor.Central;
import java.io.*;
import java.net.Socket;

public class EntidadeHandler implements Runnable {
    private Socket socket;
    private Central nodoCentral;

    public EntidadeHandler(Socket socket, Central nodoCentral) {
        this.socket = socket;
        this.nodoCentral = nodoCentral;
    }

    @Override
    public void run() {
        try (InputStream inputStream = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String linha;
            while ((linha = reader.readLine()) != null) {
                processarMensagem(linha);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   private void processarMensagem(String mensagem) {
    String[] partes = mensagem.split(":");
    if (partes.length == 2) {
        String destinatarioNome = partes[0];
        String conteudo = partes[1];
        Entidade remetente = obterRemetente();
        Entidade destinatario = obterDestinatario(destinatarioNome);

        if (destinatario != null) {
            nodoCentral.enviarMensagem(remetente, destinatario.getUsername(), conteudo);
        } else {
            System.out.println("Destinatário não encontrado: " + destinatarioNome);
        }
    }
}

    private Entidade obterRemetente() {
        // Implemente conforme necessário
        return null;
    }

    private Entidade obterDestinatario(String nome) {
        // Implemente conforme necessário
        return null;
    }
}
