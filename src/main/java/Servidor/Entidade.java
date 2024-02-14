package Servidor;

import Servidor.DatabaseHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Entidade {

    enum Cargo {General, Capitao, Sargento}

    private static final AtomicInteger count = new AtomicInteger(0);
    private int id;
    private String username;
    private String password;
    private Cargo cargo;
    private List<String> messages;
    private List<Integer> canais;

    public Entidade(String username, String password, Cargo cargo) {
        this.username = username;
        this.password = password;
        this.cargo = cargo;
        this.id = count.incrementAndGet();
        this.messages = new ArrayList<>();
        this.canais = new ArrayList<>();
    }

    public List<Integer> getCanais() {
        return canais;
    }

    public void setCanais(List<Integer> canais) {
        this.canais = canais;
    }

    public void addCanal(int porta) {
        if (!canais.contains(porta)) {
            canais.add(porta);
            
            System.out.println("porta: " + porta);
            String idrm = String.valueOf(this.id)+porta;
            saveCanaisToDatabase(idrm);
        }
    }
    
    public void addCanal_(int porta) {
        if (!canais.contains(porta)) {
            canais.add(porta);           
        }
    }

    public void removeCanal(int porta) {
        canais.remove(Integer.valueOf(porta));
        String idrm = String.valueOf(this.id)+porta;
        RemoveUser_canais(idrm);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
    
    
    
    // Save the channels to the database
    private void RemoveUser_canais(String idrm) {
        try (Connection connection = DatabaseHelper.connect()) {
            
            
                
              //  String id=this.id+String.valueOf(porta);
            String deleteQuery = "DELETE FROM user_canais WHERE id = ?";
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                System.out.println("key = " + idrm);
                deleteStatement.setString(1, idrm);
                deleteStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
    }
    

    // Save the channels to the database
    private void saveCanaisToDatabase(String idrm) {
        try (Connection connection = DatabaseHelper.connect()) {
            
            
                
              //  String id=this.id+String.valueOf(porta);
            String deleteQuery = "DELETE FROM user_canais WHERE id = ?";
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                System.out.println("key = " + idrm);
                deleteStatement.setString(1, idrm);
                deleteStatement.executeUpdate();
            }
            
            
            
              String porta = idrm.substring(idrm.length() - 4);
            // Insert new records for the channels
            String insertQuery = "INSERT INTO user_canais (id, user_id, porta) VALUES (?, ?, ?)";
            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                    insertStatement.setString(1, idrm);
                    insertStatement.setInt(2, this.id);
                    insertStatement.setInt(3, Integer.parseInt(porta));
                    insertStatement.executeUpdate();
                
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
    }

    // Other methods remain unchanged...
}
