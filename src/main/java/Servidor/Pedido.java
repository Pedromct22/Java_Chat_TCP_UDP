package Servidor;

import Servidor.Entidade;
import Servidor.DatabaseHelper;
import static Servidor.DatabaseHelper.connect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Pedido {

    private int id;
    private int user_criou_id;
    private int user_aceitou_id;
    private int user_autorizou_id;
    private String pedido_mensagem;
    private Estado estado;

    enum Estado {
        criado, autoriazada, aceite
    }

    public Pedido(int user_criou_id, String pedido_mensagem) {
        this.user_criou_id = user_criou_id;
        this.pedido_mensagem = pedido_mensagem;
        this.user_autorizou_id = -1;
        this.user_aceitou_id = -1;
        this.estado = Estado.criado;
    }

    public Pedido(int id, int user_criou_id, int user_aceitou_id, int user_autorizou_id, String pedido_mensagem, String estado) {
        this.id = id;
        this.user_criou_id = user_criou_id;
        this.user_aceitou_id = user_aceitou_id;
        this.user_autorizou_id = user_autorizou_id;
        this.pedido_mensagem = pedido_mensagem;
        if (estado.equals("criado")) {
            this.estado = Estado.criado;
        }
        if (estado.equals("autorizado")) {
            this.estado = Estado.autoriazada;
        }
        if (estado.equals("aceite")) {
            this.estado = Estado.aceite;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    

    public int getUser_criou_id() {
        return user_criou_id;
    }

    public void setUser_criou_id(int user_criou_id) {
        this.user_criou_id = user_criou_id;
    }

    

    public int getUser_aceitou_id() {
        return user_aceitou_id;
    }

    

    public int getUser_autorizou_id() {
        return user_autorizou_id;
    }

    

    public String getPedido_mensagem() {
        return pedido_mensagem;
    }

    public void setUser_aceitou_id(int user_aceitou_id) {
        this.user_aceitou_id = user_aceitou_id;
    }

    public void setUser_autorizou_id(int user_autorizou_id) {
        this.user_autorizou_id = user_autorizou_id;
    }

    public void setPedido_mensagem(String pedido_mensagem) {
        this.pedido_mensagem = pedido_mensagem;
    }

    

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    // get canal por id
    public static Pedido getPedidoFromDatabase(int idd) {
        Pedido pedido = null;
        try (Connection connection = DatabaseHelper.connect()) {
            String query = "SELECT * FROM pedidos WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, idd);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int id = resultSet.getInt("idd");
                        int user_criou_id = resultSet.getInt("user_criou_id");
                        int user_autorizou_id = resultSet.getInt("user_autorizou_id");
                        int user_aceitou_id = resultSet.getInt("user_aceitou_id");
                        String ped = resultSet.getString("pedido");
                        String estado = resultSet.getString("estado");
                        pedido = new Pedido(id, user_criou_id, user_aceitou_id, user_autorizou_id, ped, estado);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        return pedido;
    }

    public static void editPedidoAutorizado(Pedido pedido) {
        try (Connection connection = DatabaseHelper.connect()) {

            String query = "UPDATE pedidos SET user_criou_id = ?, user_autorizou_id = ?, user_aceitou_id = ?, pedido = ?, estado = ? WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, pedido.getUser_criou_id());
                preparedStatement.setInt(2, pedido.getUser_autorizou_id());
                preparedStatement.setInt(3, -1);
                preparedStatement.setString(4, pedido.getPedido_mensagem());
                String estad = "autorizado";
                preparedStatement.setString(5, estad);
                preparedStatement.setInt(6, pedido.getId());

                int rowsUpdated = preparedStatement.executeUpdate();
                System.out.println("Rows updated: " + rowsUpdated);
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
    }

    public static void editPedidoAceite(Pedido pedido) {
        try (Connection connection = DatabaseHelper.connect()) {

            String query = "UPDATE pedidos SET user_criou_id = ?, user_autorizou_id = ?, user_aceitou_id = ?, pedido = ?, estado = ? WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, pedido.getUser_criou_id());
                preparedStatement.setInt(2, pedido.getUser_autorizou_id());
                preparedStatement.setInt(3, pedido.getUser_aceitou_id());
                preparedStatement.setString(4, pedido.getPedido_mensagem());
                String estad = "aceite";
                preparedStatement.setString(5, estad);
                preparedStatement.setInt(6, pedido.getId());

                int rowsUpdated = preparedStatement.executeUpdate();
                System.out.println("Rows updated: " + rowsUpdated);
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
    }

    public static List<Pedido> getAllPedidosFromDatabase() {
        ArrayList<Pedido> res = new ArrayList<Pedido>(900);
        try (Connection connection = DatabaseHelper.connect()) {
            String query = "SELECT * FROM pedidos";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query); ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    int criou = resultSet.getInt("user_criou_id");
                    int aceitou = resultSet.getInt("user_aceitou_id");
                    int autorizou = resultSet.getInt("user_autorizou_id");
                    String ped = resultSet.getString("pedido");
                    String estado = resultSet.getString("estado");
                    Pedido pedido = new Pedido(id, criou, aceitou, autorizou, ped, estado);
                    
                    res.add(pedido);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        
        //esta a guardar sempre a mesma na lsita!!
       
        return res;

    }

    public static void addPedido(Entidade user, Pedido pedido) {
        try (Connection connection = connect()) {
            String query = "INSERT INTO pedidos (user_criou_id,user_autorizou_id,user_aceitou_id, pedido, estado) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, user.getId());
                preparedStatement.setInt(2, -1);
                preparedStatement.setInt(3, -1);
                preparedStatement.setString(4, pedido.getPedido_mensagem());
                preparedStatement.setString(5, pedido.getEstado().toString());
                preparedStatement.executeUpdate();
                System.out.println("Pedido criado com sucesso!");
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
    }

}
