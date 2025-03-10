package com.darksheep.sheepnote.config;

import com.darksheep.sheepnote.data.NoteData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.darksheep.sheepnote.config.SQLiteConnection.getConnection;

public class NoteDataRepository {
    public static Connection connection;

    static {
        connection = getConnection();
    }


    public static int insert(NoteData noteData) {
        int id = 0;
        try (Connection conn = getConnection()) {
            // 使用事务以确保同时插入新记录并获取新记录ID的一致性
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO notes(title, file_path, line_number, select_code, create_time, update_time) VALUES (?, ?, ?, ?, datetime('now'), datetime('now'))")) {
                stmt.setString(1, noteData.noteTitle);
                stmt.setString(2, noteData.noteFilePath);
                stmt.setInt(3, noteData.noteLineNumber);
                stmt.setString(4, noteData.selectCode);
                stmt.executeUpdate();
            }

            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid() AS id");
                if (rs.next()) {
                    id = rs.getInt("id");
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }


    /**
     * 从数据库中查询所有笔记数据
     *
     * @return 所有笔记数据对象的列表
     * @throws SQLException 查询失败抛出异常
     */
    public static List<NoteData> getAllNoteData()  {
        List<NoteData> noteDataList = new ArrayList<>();
        try{
            String sql = "SELECT * FROM notes";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (resultSet.next()) {
                NoteData noteData = new NoteData();
                noteData.id = resultSet.getInt("id");
                noteData.noteTitle = resultSet.getString("title");
                noteData.noteFilePath = resultSet.getString("file_path");
                noteData.noteLineNumber = resultSet.getInt("line_number");
                noteData.selectCode = resultSet.getString("select_code");
                String createTime = resultSet.getString("create_time");
                String updateTime = resultSet.getString("update_time");
                noteData.createTime =dateFormat.parse(createTime);
                noteData.updateTime = dateFormat.parse(updateTime);
                noteDataList.add(noteData);
            }
            resultSet.close();
            statement.close();
            return noteDataList;
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            return noteDataList;
        }

    }

    public static void deleteNoteData(NoteData noteData){
        //新添加 直接渲染到ui上的数据 目前存在ui上的内容 还没有id
        if(Objects.isNull(noteData.id)){
            //do noting
        }
        else{
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement("delete from notes where id = ?")) {
                stmt.setInt(1, noteData.id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
