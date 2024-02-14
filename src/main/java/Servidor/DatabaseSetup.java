package Servidor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {

    public static void main(String[] args) {
        createDatabaseAndTables();
    }

    public static void createDatabaseAndTables() {
        //connect com a bd "jdbc:mysql://localhost:3306/", "admin", "Mysqlroot1?"
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "admin", "Mysqlroot1?")) {
            Statement statement = connection.createStatement();

            // Create the database
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS sddb");

            // Switch to the created database
            statement.executeUpdate("USE sddb");

            // Create the entidades table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS entidades (\n"
                    + "    id INT AUTO_INCREMENT PRIMARY KEY,\n"
                    + "    username VARCHAR(255) NOT NULL UNIQUE,\n"
                    + "    password VARCHAR(255) NOT NULL,\n"
                    + "    `cargo` VARCHAR(20) NOT NULL\n"
                    + ");");

            // Create the messages table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS mensagens (\n"
                    + "    id INT AUTO_INCREMENT PRIMARY KEY,\n"
                    + "    user_id INT,\n"
                    + "    remetente_id INT,\n"
                    + "    message TEXT,\n"
                    + "    FOREIGN KEY (user_id) REFERENCES entidades(id),\n"
                    + "    FOREIGN KEY (remetente_id) REFERENCES entidades(id)\n"
                    + ");");

            
            // Create the canais table with an index on the porta column
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS canais (\n"
                    + "    porta INT,\n"
                    + "    address TEXT,\n"
                    + "    nome TEXT,\n"
                    + "    PRIMARY KEY (porta)\n"
                    + ");");
            
            // Create the user_canais table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS user_canais (\n"
                    + "    id VARCHAR(255),\n"
                    + "    user_id INT,\n"
                    + "    porta INT,\n"
                    + "    FOREIGN KEY (user_id) REFERENCES entidades(id),\n"
                    + "    FOREIGN KEY (porta) REFERENCES canais(porta),\n"
                    + "    PRIMARY KEY (id)\n"
                    + ");");
            
            
            // Create the Pedidos table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS pedidos (\n"
                    + "    id INT AUTO_INCREMENT PRIMARY KEY,\n"
                    + "    user_criou_id INT,\n"
                    + "    user_aceitou_id INT,\n"
                    + "    user_autorizou_id INT,\n"
                    + "    pedido TEXT,\n"
                    + "    estado TEXT,\n"
                    + "    FOREIGN KEY (user_criou_id) REFERENCES entidades(id),\n"
                    + "    FOREIGN KEY (user_aceitou_id) REFERENCES entidades(id),\n"
                    + "    FOREIGN KEY (user_autorizou_id) REFERENCES entidades(id)\n"
                    + ");");

            System.out.println("Database and tables created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
