package Servidor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Central {

    private List<Entidade> utilizadores;
    private List<Canal> canais;
    private List<Pedido> pedidos;
    private Map<Entidade, List<String>> mensagensPendentes;
    public int usersOn;
    public HashMap<Integer, Integer> membrosOn;
    public HashMap<String, Integer> user_canais;

    public Central() {
        this.membrosOn = new HashMap<>();
        this.utilizadores = new ArrayList<>();
        this.canais = new ArrayList<>();
        this.pedidos = new ArrayList<>();
        this.mensagensPendentes = new HashMap<>();
        // Load users from the database when the Central is created
        this.utilizadores.addAll(DatabaseHelper.getAllUsers());

        this.canais.addAll(Canal.getAllCanaisFromDatabase());
        this.pedidos.addAll(Pedido.getAllPedidosFromDatabase());
        this.usersOn = 0;
        Canal.getAllCanaisFromDatabase().forEach(cn -> membrosOn.put(cn.getPorta(), 0));
        this.user_canais = DatabaseHelper.getAllUserCanais();
        
        
        ArrayList<String> arr= DatabaseHelper.getUserCanais_Entidades();
        
        for (String s : arr){
           String parts[] = s.split(" ");
            System.out.println("PARTS 0 " + parts[0]);
            System.out.println("PARTS 1 " + parts[1]);
                getEntidadeById(Integer.parseInt(parts[0])).addCanal_(Integer.parseInt(parts[1]));
        }
           
       

    }

    
    
    //vai buscar os canais à bd e mete na lista para atualizar
    public void refresCanais(){
        this.user_canais = DatabaseHelper.getAllUserCanais();
    }
    public void addEntidadeCanais() {

        for (Entidade enti : this.utilizadores) {
            for (Integer porta : enti.getCanais()) {
                if (!this.user_canais.containsValue(porta)) {
                    enti.getCanais().add(porta);
                    System.out.println("user"+ enti.getUsername() + " " + porta);
                }
            }
        }
        this.user_canais = DatabaseHelper.getAllUserCanais();
    }

    //registar utilziador
    public void registar(Entidade entidade) {
        this.utilizadores.add(entidade);
        this.mensagensPendentes.put(entidade, new ArrayList<>());

        // add user na bd
        DatabaseHelper.addEntidadeToDatabase(entidade);
    }

    //get dos user_canais
    public HashMap<String, Integer> getUser_canais() {
        return user_canais;
    }

    
    //add canal
    public void addCanal(Canal canal) {
        this.canais.add(canal);

        // add na bd
        Canal.addCanais(canal);
    }

    //get das mensagens
    public static List<String> getMessages(Entidade userSession) {
        return DatabaseHelper.getMessagesForUser(userSession);
    }

    //add pedido
    public void addPedido(Pedido pedido, Entidade user) {
        this.pedidos.add(pedido);

        //add na bd
        Pedido.addPedido(user, pedido);
    }

    
    //remover canal
    public void removeCanal(String canal) {
        for (Canal cn : this.canais) {
            if (String.valueOf(cn.getPorta()).equals(canal)) {
                this.canais.remove(cn);
                break;
            }
        }

        //add na bd
        Canal.removeCanal(String.valueOf(canal));
    }

    //get entidade por nome
    public Entidade getEntidadeByUsername(String username) {
        for (Entidade entidade : utilizadores) {
            if (entidade.getUsername().equals(username)) {
                return entidade; // encontrou
            }
        }
        System.out.println("Utilziador não existe: " + username);
        return null; // Não encontrou
    }

    
    //get entidade por id
    public synchronized Entidade getEntidadeById(Integer id) {
        for (Entidade entidade : utilizadores) {
            if (entidade.getId() == id) {
                return entidade; // encontrou 
            }
        }
       // System.out.println("Id não existe: " + id);
        return null; // não existe
    }

    //get pedido
    public synchronized Pedido getPedidoById(Integer id) {
        for (Pedido pedido : this.pedidos) {
            if (pedido.getId() == id) {
                return pedido; // encontrou 
            }
        }
        System.out.println("Id não existe: " + id);
        return null; //nao existe
    }

    
    public synchronized void editPedidoAutorizado(Pedido ped) {
        Pedido.editPedidoAutorizado(ped);

    }

    public synchronized void editPedidoAceite(Pedido ped) {
        Pedido.editPedidoAceite(ped);

    }

    public synchronized void enviarMensagem(Entidade remetente, String destinatarioUsername, String mensagem) {
        // ve se exist
        Entidade destinatario = getEntidadeByUsername(destinatarioUsername);

        if (destinatario != null) {

            // encontrou = envia
            DatabaseHelper.addMessageToDatabase(remetente, destinatario, formatarMensagem(mensagem));
        } else {
            // Não encontrou fica pendente 
            System.out.println("Utilizador " + destinatarioUsername + " não encontrado");
        }
    }

    //formatar menssagem com a data
    public synchronized static String formatarMensagem(String conteudo) {
        Date dataEnvio = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "[" + sdf.format(dataEnvio) + "] " + ": " + conteudo;
    }

    public List<Canal> getCanais() {
        return Canal.getAllCanaisFromDatabase();
    }

    public List<Pedido> getPedidos() {

        return Pedido.getAllPedidosFromDatabase();
    }

    public int getUsersOn() {
        return usersOn;
    }

    public void setUsersOn(int usersOn) {
        this.usersOn = usersOn;
    }

    public List<Entidade> getUtilizadores() {
        return utilizadores;
    }

    public HashMap<Integer, Integer> getMembrosOn() {
        return membrosOn;
    }

    public void setMembrosOn(HashMap<Integer, Integer> membrosOn) {
        this.membrosOn = membrosOn;
    }

}
