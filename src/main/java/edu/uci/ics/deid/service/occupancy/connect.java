package edu.uci.ics.deid.service.occupancy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class connect  {
    Connection connection;

    public connect(String type) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            String user = null, pwd = null;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("credential.txt")))) {
                user = br.readLine();
                pwd = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(type.equals("local")){
                connection = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/Occupancy?useSSL=false&serverTimezone=PST", user,
                        pwd);
            }
            if(type.equals("server")){
                connection = DriverManager.getConnection(
                        "jdbc:mysql://sensoria-mysql.ics.uci.edu:3306/tippersdb_restored?useSSL=false&serverTimezone=PST",
                        "tippersUser", "tippers2018");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection get() {
        return connection;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
