package Forcas_armadas;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;


public class Main extends Thread {

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        Conectar conn = null;

        
        Scanner scanner = new Scanner(System.in);
        System.out.println("Bem vindo. Escolha uma opção;");

        int userInput;
        Boolean isLogin = false;

        do {
            try {
            conn = new Conectar();
        } catch (ConnectException c) {
            System.out.println("Não foi possivel conectar ao servidor");
            System.exit(0);
        }
            
            System.out.println("1 - Registo");
            System.out.println("2 - Login");
            System.out.println("0 - Sair");

            userInput = Integer.parseInt(scanner.nextLine());
            String message;
            String[] parts;
            if (userInput == 0) {
                System.exit(0);

            }
            if (userInput == 1) {
                System.out.println("Para registar usar o seguinte comando:");
                System.out.println("/registo user pass rank");
                System.out.println("ranks: Capitao General Sargento");
                System.out.println("/exit para voltar");
                do {

                    message = scanner.nextLine();
                    parts = message.split(" ");
                    if (parts.length == 4 && parts[0].equalsIgnoreCase("/registo")) {
                        conn.envia(message);
                    } else {
                        System.out.println("Por favor introduza /registo user pass rank");
                    }
                } while (!(parts.length == 4 && parts[0].equalsIgnoreCase("/registo")) && !message.equalsIgnoreCase("/exit"));
            } else if (userInput == 2) {
                System.out.println("/login user pass");
                do {

                    message = scanner.nextLine();
                    parts = message.split(" ");
                    if (parts.length == 3 && parts[0].equalsIgnoreCase("/login")) {
                        conn.envia(message);

                        //da delay enquanto servidor responde
                        Thread.sleep(1000);
                        userInput = 0;
                        if (conn.getUser() != null) {
                            isLogin = true;
                        }
                        ConectarGrupo co;
                        for (Integer i : conn.grupos) {
                            co = new ConectarGrupo(i, conn.getUser());
                            conn.conns.add(co);
                        }

                    } else {
                        System.out.println("Por favor introduza /login user pass");
                    }
                } while (!(parts.length == 3 && parts[0].equalsIgnoreCase("/login")) && !message.equalsIgnoreCase("/exit"));
            }

            if (isLogin) {
                userInput = 9999;
                do {
                    System.out.println("1 - Mensagens");
                    System.out.println("2 - Canais");
                    System.out.println("3 - Pedidos");
                    System.out.println("0 - Sair");

                    userInput = Integer.parseInt(scanner.nextLine());

                    if (userInput == 1) {
                        System.out.println("/mensagens para ler mensagens");
                        System.out.println("/enviar 'user' 'mensagem' para enviar mensagens ");
                        System.out.println("/grave 'mensagem' para enviar uma mensagem GRAVE  para todos os utilizadores ");
                        System.out.println("/exit para voltar");
                        do {

                            message = scanner.nextLine();

                            if (message.equals("/mensagens")) {
                                conn.envia(message + " " + conn.getUser());
                            } else if (message.startsWith("/enviar")) {
                                conn.envia(message + " " + conn.getUser());
                            } else if (message.startsWith("/grave")) {
                                conn.envia(message + " " + conn.getUser());
                            } else if (message.equalsIgnoreCase("/exit")) {
                                break;
                            } else {
                                System.out.println("Por favor introduza 1 comando válido");
                            }
                        } while (true);

                    } else if (userInput == 2) {
                        System.out.println("/canais para ver os canais");
                        System.out.println("/canal 'porta' para selecionar");
                        System.out.println("/canalenviar 'numero' para enviar em 1 canal");
                        System.out.println("/canaladd 'porta' 'nome' para criar 1 canal");
                        System.out.println("/canaldel 'porta' para remover 1 canal");
                        System.out.println("/exit voltar");
                        do {
                            message = scanner.nextLine();
                            parts = null;
                            parts = message.split(" ");
                            if (message.equals("/canais")) {
                                conn.envia(message + " " + conn.getUser());

                            } else if (parts[0].equals("/canal") && parts.length == 2) {
                                conn.envia(message + " " + conn.getUser());
                            } else if (parts[0].contains("/canalenviar")) {
                                if(!conn.grupos.contains(Integer.parseInt(parts[1]))){
                                    System.out.println("Não pertence ao grupo");
                                }else{
                                    for (ConectarGrupo cg : conn.getConns()){
                                        if (cg.getPorta()==Integer.parseInt(parts[1])){
                                            ArrayList<String> msg = new ArrayList<>();
                                            for(String pts : parts){
                                                msg.add(pts);
                                            }
                                            msg.remove(0);
                                            msg.remove(0);
                                            String res = String.join(" ", msg);
                                            cg.envia(res);
                                        }
                                    }
                                }
                                
                                if (conn.nextConect != -1) {
                                    for (ConectarGrupo cg : conn.getConns()) {
                                            
                                        if (cg.getPorta() == conn.nextConect) {
                                            cg.envia("asd");
                                        }
                                    }
                                }
                                
                            } else if (parts[0].equals("/canaladd") && parts.length == 3) {
                                conn.envia(message + " " + conn.getUser());
                            } else if (parts[0].equals("/canaldel") && parts.length == 2) {
                                conn.envia(message + " " + conn.getUser());
                            } else if (message.equalsIgnoreCase("/exit") || message.equalsIgnoreCase("/sair")) {
                                break;
                            } else {
                                System.out.println("Introduza um comando válido");
                            }
                        } while (true);
                    } else if (userInput == 3) {
                        System.out.println("/pedidos para ver os pedidos");
                        System.out.println("/realizar 'pedido' para realizar um pedido");
                        System.out.println("/autorizar 'id' para autorizar um pedido");
                        System.out.println("/aceitar 'id' para selecionar");
                        System.out.println("/exit voltar");
                        do {
                            message = scanner.nextLine();
                            parts = message.split(" ");
                            if (message.equals("/pedidos")) {
                                conn.envia(message + " " + conn.getUser());
                            } else if (parts[0].contains("/realizar")) {
                                conn.envia(message + " " + conn.getUser());
                            } else if (parts[0].equals("/autorizar")) {
                                conn.envia(message + " " + conn.getUser());
                            } else if (parts[0].equals("/aceitar")) {
                                conn.envia(message + " " + conn.getUser());
                            } else if (message.equalsIgnoreCase("/exit")) {
                                break;
                            } else {
                                System.out.println("Introduza um comando válido");
                            }
                        } while (!message.equalsIgnoreCase("/realizar") && !message.equalsIgnoreCase("/exit"));

                 } else if (userInput == 0) {
                        isLogin = false;
                        
                        
                        conn.envia("/logout");
                        conn.conns.forEach(g -> {
                            g.fecha();
                        });
                        
                        Thread.sleep(500);
                        userInput = 999;
                    }
                } while (isLogin);
            }
            userInput = -1;
        } while (userInput != 0);
        conn.fecha();
        return;
    }
}
