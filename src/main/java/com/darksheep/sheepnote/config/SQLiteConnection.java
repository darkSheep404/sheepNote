package com.darksheep.sheepnote.config;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteConnection {
    private static Connection connection;
    public static final String db_path = "D:/sheepnote/data.sqlite";

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // 加载 SQLite JDBC 驱动
                Class.forName("org.sqlite.JDBC");
                // 连接 SQLite 数据库
                String url = getDbUrl();
                connection = DriverManager.getConnection(url);

                // 检查并创建表
                try (Statement stmt = connection.createStatement()) {
                    // 创建 notes 表
                    stmt.execute(
                            "CREATE TABLE IF NOT EXISTS notes (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                    "title TEXT NOT NULL," +
                                    "file_path TEXT NOT NULL," +
                                    "line_number INTEGER NOT NULL," +
                                    "select_code TEXT," +
                                    "tags TEXT," +
                                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                                    ")"
                    );

                    // 检查是否存在 tags 列
                    ResultSet rs = stmt.executeQuery("PRAGMA table_info(notes)");
                    boolean hasTagsColumn = false;
                    while (rs.next()) {
                        if ("tags".equals(rs.getString("name"))) {
                            hasTagsColumn = true;
                            break;
                        }
                    }

                    // 如果不存在 tags 列，添加它
                    if (!hasTagsColumn) {
                        stmt.execute("ALTER TABLE notes ADD COLUMN tags TEXT");
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
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

