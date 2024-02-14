package Servidor;

import Servidor.Entidade.Cargo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseHelper {

    private static final String URL = "jdbc:mysql://localhost:3306/sddb";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "Mysqlroot1?";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static List<Entidade> getAllUsers() {
        List<Entidade> userList = new ArrayList<>();

        try (Connection connection = connect()) {
            String query = "SELECT * FROM entidades WHERE id <> -1";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    // int userId = resultSet.getInt("id");
                    String username = resultSet.getString("username");
                    String password = resultSet.getString("password");
                    // cargo é em string mudar isto
                    String cargoString = resultSet.getString("cargo");
                                  
                    
                    Cargo cargo = Cargo.valueOf(cargoString); // Converter rank com a string
                    Entidade user = new Entidade(username, password, cargo);
                    userList.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); //estourou
        }

        return userList;
    }
    
    
    
    //get all user canais
      public static HashMap<String, Integer> getAllUserCanais() {
        HashMap<String, Integer> user_can = new HashMap<>();

        try (Connection connection = connect()) {
            String query = "SELECT * FROM user_canais";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                   
                    int user = resultSet.getInt("user_id");
                    int porta = resultSet.getInt("porta");
                    
                    
                    
                    
                    String key = String.valueOf(user)+porta;
                    user_can.put(key , porta);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }

        return user_can;
    }

      
      //get all user canais
      public static ArrayList<String> getUserCanais_Entidades() {
       ArrayList<String> user_can = new ArrayList<>();

        try (Connection connection = connect()) {
            String query = "SELECT * FROM user_canais";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                   
                    int user = resultSet.getInt("user_id");
                    int porta = resultSet.getInt("porta");

                    user_can.add(user + " " + porta);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }

        return user_can;
    }
      
      
      
      //add entidade
    public static void addEntidadeToDatabase(Entidade entidade) {
        try (Connection connection = connect()) {
            String query = "INSERT INTO entidades (username, password, cargo) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, entidade.getUsername());
                preparedStatement.setString(2, entidade.getPassword());

                // Check if getCargo returns a non-null value before calling toString
                Entidade.Cargo cargo = entidade.getCargo();

                // Ensure that cargo is not null before setting the value
                if (cargo != null) {
                    preparedStatement.setString(3, cargo.toString());
                } else {
                    // Handle the case where cargo is null (you might set a default value or throw an error)
                    // For now, let's assume a default value of "Unknown"
                    preparedStatement.setString(3, "Unknown");
                }
                
                preparedStatement.executeUpdate();
                
                 System.out.println("Utilizador registado com sucesso!");
            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("Username já existe");
                // Optionally, you can log the exception or take further action if needed
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
    }
    
 public static List<String> getMessagesForUser(Entidade user) {
    List<String> messages = new ArrayList<>();

    try (Connection connection = connect()) {
        String query = "SELECT entidades.username AS remetente_username, mensagens.message " +
                       "FROM mensagens " +
                       "JOIN entidades ON mensagens.remetente_id = entidades.id " +
                       "WHERE mensagens.user_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, user.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String remetenteUsername = resultSet.getString("remetente_username");
                String mensagem = resultSet.getString("message");
                messages.add(remetenteUsername + ": " + mensagem);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace(); // Handle the exception according to your needs
    }

    return messages;
}
 //add mensagem na bd
    public static void addMessageToDatabase(Entidade remetente, Entidade destinatario, String mensagem) {
        try (Connection connection = connect()) {
            String query = "INSERT INTO mensagens (remetente_id, user_id, message) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, remetente.getId());
                preparedStatement.setInt(2, destinatario.getId());
                preparedStatement.setString(3, mensagem);
                preparedStatement.executeUpdate();
                System.out.println("Mensagem enviada com sucesso!");
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
    }
    
    
    
    
    
     
     
    
   

    
}
