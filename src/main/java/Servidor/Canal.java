package Servidor;

import static Servidor.DatabaseHelper.connect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Canal {
    private int porta;
    //ip do servidor
    private String address = "239.0.0.0";
    private String nome;
    
    public Canal(int porta, String nome) {
        this.porta = porta;
        this.nome = nome;
    }

 

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    private List<Pedido> pedidos = new ArrayList<>();

    public void adicionarPedido(Pedido pedido) {
        this.pedidos.add(pedido);
    }

    
    
    
    //add canal base dados
    public static void addCanais(Canal canal) {
        try (Connection connection = connect()) {
            String query = "INSERT INTO canais (porta, address, nome) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, canal.getPorta());
                preparedStatement.setString(2, "239.0.0.0");
                preparedStatement.setString(3, canal.getNome());
                preparedStatement.executeUpdate();
                System.out.println("Canal criado com sucesso!");
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
    }
    
     // get canal da bd por id
    public static Canal getCanalFromDatabase(int canalId) {
        Canal canal = null;
        try (Connection connection = DatabaseHelper.connect()) {
            String query = "SELECT * FROM canais WHERE porta = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, canalId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int porta = resultSet.getInt("porta");
                        String address = resultSet.getString("address");
                        String nome = resultSet.getString("nome");
                        canal = new Canal(porta, nome);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        return canal;
    }
    
    // get todos os canais
    public static List<Canal> getAllCanaisFromDatabase() {
        List<Canal> canais = new ArrayList<>();
        try (Connection connection = DatabaseHelper.connect()) {
            String query = "SELECT * FROM canais";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int porta = resultSet.getInt("porta");
                    String address = resultSet.getString("address");
                    String nome = resultSet.getString("nome");
                    Canal canal = new Canal(porta, nome);
                    canais.add(canal);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        return canais;
    }
    
    
    //remove canal da bd
     public static void removeCanal(String porta) {
    try (Connection connection = connect()) {
        
        String query1 = "DELETE FROM user_canais WHERE porta = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query1)) {
            preparedStatement.setString(1, porta);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Canal removido, Utilizadores removidos");
            } else {
                System.out.println("Canal não encontrado.");
            }
        }
        
        
        
        String query = "DELETE FROM canais WHERE porta = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, porta);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Canal removido com sucesso!");
            } else {
                System.out.println("Canal não encontrado.");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
     
     
    
}
