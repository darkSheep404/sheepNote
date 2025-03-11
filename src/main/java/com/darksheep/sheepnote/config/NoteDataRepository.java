package com.darksheep.sheepnote.config;

import com.darksheep.sheepnote.data.FlowchartData;
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
        initFlowchartTable();
    }

    private static void initFlowchartTable() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS flowcharts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "data TEXT NOT NULL," +
                "create_time BIGINT NOT NULL" +
                ")"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    public static List<NoteData> getAllNoteData() {
        List<NoteData> noteDataList = new ArrayList<>();
        try {
            String sql = "SELECT id, title, file_path, line_number, select_code, tags, create_time, update_time FROM notes";
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
                noteData.tags = resultSet.getString("tags");
                String createTime = resultSet.getString("create_time");
                String updateTime = resultSet.getString("update_time");
                noteData.createTime = dateFormat.parse(createTime);
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

    // 流程图相关方法
    public static void saveFlowchart(FlowchartData flowchart) {
        String sql = "INSERT INTO flowcharts (name, data, create_time) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, flowchart.getName());
            pstmt.setString(2, flowchart.getData());
            pstmt.setLong(3, flowchart.getCreateTime());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<FlowchartData> getAllFlowcharts() {
        List<FlowchartData> flowcharts = new ArrayList<>();
        String sql = "SELECT * FROM flowcharts ORDER BY create_time DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                FlowchartData flowchart = new FlowchartData();
                flowchart.setId(rs.getInt("id"));
                flowchart.setName(rs.getString("name"));
                flowchart.setData(rs.getString("data"));
                flowchart.setCreateTime(rs.getLong("create_time"));
                flowcharts.add(flowchart);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return flowcharts;
    }

    public static FlowchartData getFlowchartById(int id) {
        String sql = "SELECT * FROM flowcharts WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            FlowchartData flowchart = new FlowchartData();
            if (rs.next()) {
                flowchart.setId(rs.getInt("id"));
                flowchart.setName(rs.getString("name"));
                flowchart.setData(rs.getString("data"));
                flowchart.setCreateTime(rs.getLong("create_time"));
                return flowchart;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 笔记搜索方法
    public static List<NoteData> searchNotes(String keyword) {
        List<NoteData> results = new ArrayList<>();
        String sql = "SELECT id, title, file_path, line_number, select_code, tags, create_time, update_time " +
                    "FROM notes " +
                    "WHERE title LIKE ? OR select_code LIKE ? " +
                    "ORDER BY update_time DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (rs.next()) {
                NoteData noteData = new NoteData();
                noteData.id = rs.getInt("id");
                noteData.noteTitle = rs.getString("title");
                noteData.noteFilePath = rs.getString("file_path");
                noteData.noteLineNumber = rs.getInt("line_number");
                noteData.selectCode = rs.getString("select_code");
                noteData.tags = rs.getString("tags");
                String createTime = rs.getString("create_time");
                String updateTime = rs.getString("update_time");
                noteData.createTime = dateFormat.parse(createTime);
                noteData.updateTime = dateFormat.parse(updateTime);
                results.add(noteData);
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        
        return results;
    }

    /**
     * 根据ID获取笔记
     * @param id 笔记ID
     * @return 笔记数据，如果不存在返回null
     */
    public static NoteData getNoteById(Integer id) {
        String sql = "SELECT id, title, file_path, line_number, select_code, tags, create_time, update_time " +
                    "FROM notes WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                NoteData note = new NoteData();
                note.id = rs.getInt("id");
                note.noteTitle = rs.getString("title");
                note.noteFilePath = rs.getString("file_path");
                note.noteLineNumber = rs.getInt("line_number");
                note.selectCode = rs.getString("select_code");
                note.tags = rs.getString("tags");
                note.createTime = rs.getTimestamp("create_time");
                note.updateTime = rs.getTimestamp("update_time");
                return note;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新笔记的标签
     * @param note 包含更新后标签的笔记数据
     */
    public static void updateNoteTags(NoteData note) {
        String sql = "UPDATE notes SET tags = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, note.getTags());
            pstmt.setInt(2, note.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
