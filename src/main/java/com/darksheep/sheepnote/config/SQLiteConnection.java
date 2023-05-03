package com.darksheep.sheepnote.config;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteConnection {
    private static Connection connection;
    public static final String db_path = "D:/sheepnote/data.sqlite";

    public static Connection getConnection()  {
        try{
            // 加载 SQLite JDBC 驱动
            Class.forName("org.sqlite.JDBC");
            // 连接 SQLite 数据库
            String url = getDbUrl();
            connection = DriverManager.getConnection(url);
            // 创建表格
            Statement stmt = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS notes (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, file_path TEXT,line_number INTEGER, select_code TEXT,create_time DATETIME NOT NULL,update_time DATETIME NOT NULL)";
            stmt.execute(sql);
            stmt.close();
            return connection;
        }
        catch (Exception e){
           e.printStackTrace();
            return null;
        }
    }

    @NotNull
    private static String getDbUrl() {
        String url = "jdbc:sqlite:"+db_path;
        File dbFile = new File(db_path);
        /**
         * 如果不存在 创建文件夹和数据库文件
         */
        if (!dbFile.exists()) {
            File parentFile = dbFile.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            try {
                dbFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    public void disconnect(Connection connection) throws SQLException {
        // 断开连接
        if (connection != null) {
            connection.close();
        }
    }
}

