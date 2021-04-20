package com.laioffer.jupiter.db;


import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.entity.ItemType;
import com.laioffer.jupiter.entity.User;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;


public class MySQLConnection {
    //create a connection to the MySQL database
    private final Connection conn;

    public MySQLConnection() throws MySQLException {
        try {
//            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
//            conn = DriverManager.getConnection(MySQLDBUtil.getMySQLAddress());
            Context context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc/jupiterDB");
            conn = dataSource.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            throw new MySQLException("Failed to connect to database");
        }
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //Verify if the given user Id and password are correct. Returns the user name when it passes
    public String verifyLogin(String userId, String password) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to database");
        }
        String name = "";
        String sql = "SELECT first_name, last_name FROM users WHERE id = ? AND password = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                name = rs.getString("first_name") + " " + rs.getString("last_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to verify user id and password from database");
        }
        return name;
    }

    //Add a new user to the database
    public boolean addUser(User user) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to database");
        }
        String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, user.getUserId());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getFirstName());
            statement.setString(4, user.getLastName());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to add user information to database");
        }
    }

    //Insert a favorite record to the database
    public void setFavoriteItem(String userId, Item item) throws MySQLException{
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to database");
        }
        //user应该已经在database了
        //item如果不在database，需要先保存
        //Need to make sure item is added to the items table first because the foreign key restriction on item_id(favorite_records)->id(items)
        saveItem(item);
        //Using ? and preparedStatement to prevent SQL injection
        String sql = "INSERT IGNORE INTO favorite_records (user_id, item_id) VALUES (?,?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, item.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to save favorite item to database");
        }
    }

    //Remove a favorite record from favorite_records table
    public void unsetFavoriteItem(String userId, String itemId) throws MySQLException{
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to database");
        }
        //check and maybe delete
        //需要考虑从item table里删掉没有任何人 favorite 的item
        //可以一段时间offline做garbage collection
        String sql = "DELETE FROM favorite_records WHERE user_id = ? AND item_id = ?";
        try{
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1,userId);
            statement.setString(2, itemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to delete favorite item from database");
        }
    }
    //Insert an item to the items table
    public void saveItem(Item item) throws MySQLException{
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to database");
        }
        String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
        try{
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, item.getId());
            statement.setString(2, item.getTitle());
            statement.setString(3, item.getUrl());
            statement.setString(4, item.getThumbnailUrl());
            statement.setString(5, item.getBroadcasterName());
            statement.setString(6, item.getGameId());
            statement.setString(7, item.getType().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to add item to items table");
        }
    }
    //Get favorite item ids for the given user
    public Set<String> getFavoriteItemIds(String userId) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to database");
        }
        Set<String> favoriteItemIds = new HashSet<>();
        String sql = "SELECT item_id FROM favorite_records WHERE user_id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String itemId = rs.getString("item_id");
                favoriteItemIds.add(itemId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to get favorite item ids from database");
        }
        return favoriteItemIds;
    }
    //Get favorite items for the given user. The returned map includes three entries
    // like {"Video":[item1, item2, item3], "Stream":[item4, item5, item6], "Clip":[item7, item8, item9]}
    public Map<String, List<Item>> getFavoriteItems(String userId) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to database");
        }
        Map<String, List<Item>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), new ArrayList<>());
        }
        Set<String> favoriteItemIds = getFavoriteItemIds(userId);
        String sql = "SELECT * FROM items WHERE id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (String itemId : favoriteItemIds) {
                statement.setString(1, itemId);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {//最多返回一个，所以可以用if
                    ItemType itemType = ItemType.valueOf(rs.getString("type"));
                    Item item = new Item.Builder()
                            .setId(rs.getString("id"))
                            .setTitle(rs.getString("title"))
                            .setUrl(rs.getString("url"))
                            .setThumbnailUrl(rs.getString("thumbnail_url"))
                            .setBroadcasterName(rs.getString("broadcaster_name"))
                            .setGameId(rs.getString("game_id"))
                            .setType(itemType).build();
                    itemMap.get(itemType.toString()).add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to get favorite items from database");
        }
        return itemMap;
    }
    //Get favorite game ids for the given user. The returned map includes three entries like
    //{"Video":["1234", "5678", ...], "Streams":["abcd","efgh",...], "Clips":["4321", "5675"...]
    public Map<String, List<String>> getFavoriteGameIds(Set<String> favoriteItemIds) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to database");
        }
        Map<String, List<String>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), new ArrayList<>());
        }
        String sql = "SELECT game_id, type FROM items WHERE id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (String itemId : favoriteItemIds) {
                statement.setString(1, itemId);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    itemMap.get(rs.getString("type")).add(rs.getString("game_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw  new MySQLException("Failed to get favorite game ids from database");
        }
        return itemMap;
    }

}