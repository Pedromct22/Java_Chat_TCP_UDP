package Servidor;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Mysqlroot1?
 *
 */
public class Servidor {

    private static final int PORT = 8080;
    private static Set<PrintWriter> writers = new HashSet<>();
    private static Hashtable<PrintWriter, Entidade.Cargo> users = new Hashtable<>();
    private static Central central = new Central();

    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) throws Exception {

        System.out.println("O servidor está ativo na porta " + PORT);

        ServerSocket serverSocket = new ServerSocket(PORT);

        ConectarGrupo grupo0000 = new ConectarGrupo(8000, "Sistema");
        ConectarGrupo grupo0001 = new ConectarGrupo(8001, "Sistema");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                MulticastPeriodico(grupo0000, grupo0001);
            } catch (IOException ex) {
                System.out.println("Erro ao enviar Notificação periódica");
            }
        }, 0, 60, TimeUnit.SECONDS);

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } finally {
            scheduler.shutdown();
            serverSocket.close();
        }

    }

    private static class ClientHandler extends Thread {

        private Socket clientSocket;
        private PrintWriter writer;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {

            try {
                Scanner in = new Scanner(clientSocket.getInputStream());
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                while (true) {
                    String message = in.nextLine();
                    if (message.equals("/exit")) {
                        break;
                    } else if (message.startsWith("/registo")) {
                        registo(message);
                    } else if (message.startsWith("/login")) {
                        if (login(message)) {
                            writers.add(writer);
                        }
                    } else if (message.startsWith("/logout")) {
                        users.remove(writer);
                    } else if (message.startsWith("/mensagens")) {
                        mostraMensagens(message);
                    } else if (message.startsWith("/enviar")) {
                        enviarMensagem(message);
                    } else if (message.startsWith("/canais")) {
                        mostraCanais(message);
                    } else if (message.startsWith("/canaladd")) {
                        CriarCanal(message);
                    } else if (message.startsWith("/canaldel")) {
                        RemoveCanal(message);
                    } else if (message.startsWith("/canal")) {
                        SelecionaCanal(message);
                    } else if (message.startsWith("/pedidos")) {
                        mostraPedidos(message);
                    } else if (message.startsWith("/realizar")) {
                        addPedido(message);
                    } else if (message.startsWith("/autorizar")) {
                        autorizarPedidos(message);
                    } else if (message.startsWith("/aceitar")) {
                        aceitarPedidos(message);
                    } else if (message.startsWith("/grave")) {
                        enviarMensagemGrave(message);
                    } else {
                        broadcast(message);
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (writer != null) {
                    writers.remove(writer);
                    users.remove(writer);
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized void registo(String message) {
            // Extract username and password from the registration command
            String[] parts = message.split(" ");
            if (parts.length == 4) {
                String username = parts[1];
                String password = parts[2];
                Entidade.Cargo cargo = null;
                if (parts[3].equals(Entidade.Cargo.Capitao.toString())) {
                    cargo = Entidade.Cargo.Capitao;
                } else if (parts[3].equals(Entidade.Cargo.Sargento.toString())) {
                    cargo = Entidade.Cargo.Sargento;
                } else if (parts[3].equals(Entidade.Cargo.General.toString())) {
                    cargo = Entidade.Cargo.General;
                }

                if ((username != null) && (password != null) && (cargo != null)) {
                    Entidade ent = new Entidade(username, password, cargo);
                    central.registar(ent);

                    // Send a registration success message to the client
                    writer.println("Registado com sucesso! Bem-vindo, " + username + "!");
                }
            } else {
                writer.println("Erro ao registar");
            }
        }

        // login
        public synchronized boolean login(String message) {
            String[] parts = message.split(" ");
            if (parts.length == 3) {
                String username = parts[1];
                String password = parts[2];

                if ((username != null) && (password != null)) {

                    Entidade entidade = central.getEntidadeByUsername(parts[1]);
                    String key = "";

                    
                    //atualiza user_canais
                    central.user_canais = DatabaseHelper.getAllUserCanais();
                    
                    
                    //portas dos grupos para enviar ao utilziador apos login
                    ArrayList<String> portas = new ArrayList<>();
                    for (Canal cn : central.getCanais()) {
                        key = String.valueOf(entidade.getId()) + cn.getPorta();
                        boolean isSelecionado = false;
                        if (central.getUser_canais().containsKey(key)) {
                            System.out.println("key" + key);
                            portas.add(String.valueOf(cn.getPorta()));

                        }

                    }
                    String enviaPortas = String.join(" ", portas);

                    if (entidade != null) {

                        writer.println("/login Bem-vindo " + entidade.getUsername() + " " + enviaPortas);

                        users.put(writer, entidade.getCargo());

                        return true;

                    } else {
                        writer.println("Não foi possivel fazer login");
                    }

                }
            } else {
                writer.println("Erro ao fazer login");
            }
            return false;
        }

        private synchronized void mostraMensagens(String message) {
            String parts[] = message.split(" ");
            List<String> mensagens = central.getMessages(central.getEntidadeByUsername(parts[1]));

            if (mensagens.isEmpty()) {
                writer.println("não tem menssagens");
            } else {
                for (String ms : mensagens) {
                    writer.println(ms);
                }
            }

        }

        private synchronized void enviarMensagem(String message) {
            //divie a mesnagem
            String parts[] = message.split(" ");
            //enviar destinatario mensagem remetente
            //arraylist para tirar o /enviar - user e destinatario
            ArrayList msg = new ArrayList();
            for (String part : parts) {
                msg.add(part);
            }
            String rem = msg.get(msg.size() - 1).toString();

            String dest = msg.get(1).toString();

            if(central.getEntidadeByUsername(dest)==null){
                writer.println("Utilizador não existe");
                return;
            }
            
            msg.remove(msg.get(0));
            msg.remove(msg.get(0));
            msg.remove(msg.size() - 1);

            Entidade entidade = central.getEntidadeByUsername(rem);

            String mensagem = String.join(" ", msg);

            central.enviarMensagem(entidade, dest, mensagem);

            writer.println("Mensagem enviada com sucesso");

        }

        private synchronized void enviarMensagemGrave(String message) {
            // Divide a mensagem
            String[] parts = message.split(" ");

            ArrayList msg = new ArrayList();
            for (String part : parts) {
                msg.add(part);
            }
            String rem = msg.get((msg.size() - 1)).toString();

            msg.remove(0);
            msg.remove(msg.size() - 1);
            // Monta a mensagem completa
            String mensagem = String.join(" ", msg);
            mensagem = "ALERTA GRAVE DE : " + rem + " ALERTA: " + mensagem;

            broadcast(mensagem);
            broadcast(mensagem);

            // Envia a mensagem para todos os users
            for (Entidade entidade : central.getUtilizadores()) {
                central.enviarMensagem(entidade, entidade.getUsername(), mensagem);
            }

            writer.println("Alerta enviado");
        }

        public synchronized void mostraCanais(String message) {
            
            
            if(central.getCanais().isEmpty()){
                writer.println("Não há Canais");
            }

            String parts[] = message.split(" ");
            Entidade entidade = central.getEntidadeByUsername(parts[1]);
            String key = "";

            for (Canal cn : central.getCanais()) {
                key = String.valueOf(entidade.getId()) + cn.getPorta();
                boolean isSelecionado = false;
                if (central.getUser_canais().containsKey(key)) {
                    isSelecionado = true;
                    System.out.println(key);
                   
                }
                String status = isSelecionado ? "- Selecionado" : "- Não Selecionado";
                writer.println("Porta: " + cn.getPorta() + " " + cn.getNome() + " " + status);
            }

        }

        private synchronized void SelecionaCanal(String message) {
            
            //divie a mesnagem
            String parts[] = message.split(" ");
            Entidade entidade = central.getEntidadeByUsername(parts[2]);
            Integer porta = Integer.parseInt(parts[1]);
            
            if (entidade.getCanais().contains(porta)) {
                entidade.removeCanal(porta);
                writer.println("Canal removido com sucesso!");
                
            } else {
                entidade.addCanal(porta);
                writer.println("Canal adicionado com sucesso!");
                 writer.println("Por favor volte a entrar na sua conta!");
                
            }
            central.addEntidadeCanais();
            central.refresCanais();
        }



        public synchronized void CriarCanal(String message) {

            //divie a mesnagem
            String parts[] = message.split(" ");

            Entidade entidade = central.getEntidadeByUsername(parts[3]);

            if (!(entidade.getCargo() == Entidade.Cargo.Capitao)) {
                writer.println("O seu cargo é " + entidade.getCargo() + ", comando Apenas para cargo capitão");
                return;
            }

            String nome = parts[2];

            String porta = parts[1];

            // Instanciar canal
            Canal newCanal = new Canal(Integer.parseInt(porta), nome);

            // Cria o canal
            central.addCanal(newCanal);

            writer.println("Canal criado com sucesso");

        }

        private synchronized void RemoveCanal(String message) {
            //divie a mesnagem
            String parts[] = message.split(" ");

            Entidade entidade = central.getEntidadeByUsername(parts[2]);
            if (!(entidade.getCargo() == Entidade.Cargo.Capitao)) {
                writer.println("O seu cargo é " + entidade.getCargo() + ", comando apenas para cargo capitão");
                return;
            }

            String nome = parts[2];
            String porta = parts[1];

            // Remove da db
            central.removeCanal(porta);

        }

        private synchronized void mostraPedidos(String message) {

            if (central.getPedidos().isEmpty()) {
                writer.println("Não há pedidos");
                return;
            }

            for (Pedido pedido : central.getPedidos()) {
                System.out.println("PED MESN " + pedido.getPedido_mensagem());

                if (pedido.getUser_autorizou_id() == -1) {
                    Entidade entidade = central.getEntidadeById(pedido.getUser_criou_id());
                    writer.println("Pedido: Id: " + pedido.getId() + " - criado por: " + entidade.getUsername() + " - estado: " + pedido.getEstado().toString());
                    writer.println("Informação:" + pedido.getPedido_mensagem());
                } else if (pedido.getUser_autorizou_id() != -1 && pedido.getUser_aceitou_id() == -1) {
                    Entidade criou = central.getEntidadeById(pedido.getUser_criou_id());
                    Entidade autorizou = central.getEntidadeById(pedido.getUser_autorizou_id());
                    writer.println("Pedido: Id: " + pedido.getId() + " - estado: " + pedido.getEstado().toString() + " - criado por: " + criou.getUsername() + " - autorizado por: " + autorizou.getUsername());
                    writer.println("Informação:" + pedido.getPedido_mensagem());
                } else if (pedido.getUser_aceitou_id() != -1) {
                    Entidade criou = central.getEntidadeById(pedido.getUser_criou_id());
                    Entidade autorizou = central.getEntidadeById(pedido.getUser_autorizou_id());
                    Entidade aceitou = central.getEntidadeById(pedido.getUser_aceitou_id());
                    writer.println("Pedido: Id: " + pedido.getId() + " - estado: "
                            + pedido.getEstado().toString() + " - criado por: "
                            + criou.getUsername() + " - autorizado por: " + autorizou.getUsername()
                            + " - aceite por: " + aceitou.getUsername());
                    writer.println("Informação:" + pedido.getPedido_mensagem());
                }

            }

        }

        private synchronized void addPedido(String message) {
            String[] parts = message.split(" ");

            ArrayList msg = new ArrayList();
            for (String part : parts) {
                msg.add(part);
            }
            String criador = msg.get(msg.size() - 1).toString();
            msg.remove(0);
            msg.remove(msg.size() - 1);

            String pedido = String.join(" ", msg);

            Entidade ent = central.getEntidadeByUsername(criador);
            // Criar um novo pedido
            Pedido newPedido = new Pedido(ent.getId(), pedido);

            // Chamar o método que adiciona o pedido a bd
            //Erro não sei porque
            central.addPedido(newPedido, ent);
            writer.println("Pedido criado com sucesso");

            for (Enumeration<PrintWriter> keys = users.keys(); keys.hasMoreElements();) {
                PrintWriter key = keys.nextElement();

                Entidade.Cargo carg = ent.getCargo();
                Entidade.Cargo value = users.get(key);

                if (carg == Entidade.Cargo.Sargento) {
                    if (value != Entidade.Cargo.Sargento) {
                        for (PrintWriter pw : writers) {
                            if (pw.equals(key)) {
                                pw.println("Há um novo pedido para ser autorizado");
                            }
                        }
                    }

                } else if (carg == Entidade.Cargo.General) {
                    if (value == Entidade.Cargo.Capitao) {
                        for (PrintWriter pw : writers) {
                            if (pw.equals(key)) {
                                pw.println("Há um novo pedido para ser autorizado");
                            }
                        }
                    }
                }

            }

        }

        private synchronized void autorizarPedidos(String message) {
            String[] parts = message.split(" ");

            ArrayList msg = new ArrayList();
            for (String part : parts) {
                msg.add(part);
            }
            //id utilziador que vai autorizar
            Entidade autorizador = central.getEntidadeByUsername(msg.get(msg.size() - 1).toString());

            Pedido pedido = central.getPedidoById(Integer.parseInt(msg.get(1).toString()));
            if (pedido.getUser_autorizou_id() != -1) {
                writer.println("Este pedido já foi autorizado");
                return;
            }

            //cridor do pedido para verificar se pode autorizar
            Entidade criador = central.getEntidadeById(pedido.getUser_criou_id());

            if (autorizador.getCargo().equals(Entidade.Cargo.Capitao)) {
                pedido.setUser_autorizou_id(autorizador.getId());
                central.editPedidoAutorizado(pedido);
                writer.println("Pedido Autorizado");
                //autoriza tudo
            } else if (autorizador.getCargo().equals(Entidade.Cargo.Sargento)) {
                writer.println("Sargentos não podem autorizar pedidos");
            } else if (criador.getCargo().equals(Entidade.Cargo.General) && autorizador.getCargo().equals(Entidade.Cargo.General)) {
                writer.println("Pedido criado por 1 General. So pode ser autorizado por capitães");
                central.editPedidoAutorizado(pedido);
            }

            broadcast("1 Pedido foi autorizado");

        }

        private synchronized void aceitarPedidos(String message) {
            String[] parts = message.split(" ");

            ArrayList msg = new ArrayList();
            for (String part : parts) {
                msg.add(part);
            }
            Entidade aceitador = central.getEntidadeByUsername(msg.get(msg.size() - 1).toString());

            Pedido pedido = central.getPedidoById(Integer.parseInt(msg.get(1).toString()));

            if (pedido.getUser_aceitou_id() != -1) {
                writer.println("Este pedido já foi aceite");
                return;
            }

            if (pedido.getEstado() != Pedido.Estado.autoriazada) {
                writer.println("Não é possivel aceitar este pedido");
                return;
            }
            pedido.setUser_aceitou_id(aceitador.getId());
            central.editPedidoAceite(pedido);
            writer.println("Pedido aceite com suecesso");

        }

    }

    private static void broadcast(String message) {
        for (PrintWriter writer : writers) {
            writer.println(message);
        }
    }

    private static void MulticastPeriodico(ConectarGrupo grupo1, ConectarGrupo grupo2) throws IOException {
        int nPedidos = 0;
        int auts = 0;
        int uts = 0;

        for (Enumeration<PrintWriter> keys = users.keys(); keys.hasMoreElements();) {
            PrintWriter key = keys.nextElement();

            for (PrintWriter pw : writers) {
                if (pw.equals(key)) {

                    nPedidos = central.getPedidos().size();
                    if (!writers.isEmpty()) {
                        uts = writers.size();
                    }

                    for (Pedido ped : central.getPedidos()) {
                        if (ped.getEstado() != Pedido.Estado.criado) {
                            auts++;
                        }
                    }
                    pw.println("--- Mensagem Automática do sistema ---");
                    pw.println("Numero de pedidos: " + nPedidos);
                    pw.println("Numero de autorizações: " + auts);

                }
            }
        }

        grupo2.envia(" Utilizadores Online: " + uts);

        grupo1.envia(" Nº de pedidos: " + nPedidos + " Nº de autorizações: " + auts);
    }

}
