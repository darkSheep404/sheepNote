package com.darksheep.sheepnote.config;

import com.darksheep.sheepnote.data.NoteData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.darksheep.sheepnote.config.SQLiteConnection.getConnection;

public class NoteDataRepository {
    public static Connection connection;

    static {
        connection = getConnection();
    }


    public static void insert(NoteData noteData) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement("INSERT INTO notes(title, file_path, line_number, select_code, create_time, update_time) VALUES (?, ?, ?, ?, datetime('now'), datetime('now'))")) {
            stmt.setString(1, noteData.noteTitle);
            stmt.setString(2, noteData.noteFilePath);
            stmt.setInt(3, noteData.noteLineNumber);
            stmt.setString(4, noteData.selectCode);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 从数据库中查询所有笔记数据
     *
     * @param connection 数据库连接对象
     * @return 所有笔记数据对象的列表
     * @throws SQLException 查询失败抛出异常
     */
    public static List<NoteData> getAllNoteData() throws SQLException, ParseException {
        String sql = "SELECT * FROM notes";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();
        List<NoteData> noteDataList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (resultSet.next()) {
            NoteData noteData = new NoteData();
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
    }
}
