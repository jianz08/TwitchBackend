package com.laioffer.jupiter.db;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MySQLTableCreator {
    //数据库初始化，只要运行一次
    //Run this as a Java application to reset the database
    public static void main(String[] args) {
        try{
            //Step 1: connect to MySQL
            System.out.println("Connecting to " + MySQLDBUtil.getMySQLAddress());
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(MySQLDBUtil.getMySQLAddress());
            if (conn == null) {
                return;
            }
            System.out.println("connected!");
            //step 2: Drop tables in case they exist
            Statement statement = conn.createStatement();

            String sql = "DROP TABLE IF EXISTS favorite_records";//need to drop tables with foreign keys first
            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS items";
            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS users";
            statement.executeUpdate(sql);

            //step 3: create new tables
            sql = "CREATE TABLE items (" +
                    "id VARCHAR(255) NOT NULL," +
                    "title VARCHAR(255)," +
                    "url VARCHAR(255)," +
                    "thumbnail_url VARCHAR(255)," +
                    "broadcaster_name VARCHAR(255)," +
                    "game_id VARCHAR(255)," +
                    "type VARCHAR(255) NOT NULL," +
                    "PRIMARY KEY (id)" +
                    ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE users (" +
                    "id VARCHAR(255) NOT NULL," +
                    "password VARCHAR(255) NOT NULL," +
                    "first_name VARCHAR(255)," +
                    "last_name VARCHAR(255)," +
                    "PRIMARY KEY (id)" +
                    ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE favorite_records (" +
                    "user_id VARCHAR(255) NOT NULL," +
                    "item_id VARCHAR(255) NOT NULL," +
                    "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (user_id,item_id)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)," +
                    "FOREIGN KEY (item_id) REFERENCES items(id)" +
                    ")";
            statement.executeUpdate(sql);

            //step 4: insert fake user 1111/3229c1097c00d497a0fd282d586be050.
            sql = "INSERT INTO users VALUES ('1111', '3229c1097c00d497a0fd282d586be050','John', 'Smith')";
            statement.executeUpdate(sql);

            conn.close();
            System.out.println(("Import done successfully"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
