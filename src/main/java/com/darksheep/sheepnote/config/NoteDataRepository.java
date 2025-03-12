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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.darksheep.sheepnote.config.SQLiteConnection.getConnection;

public class NoteDataRepository {
    public static Connection connection;

    static {
        connection = getConnection();
        initFlowchartTable();
    }

    private static void initFlowchartTable() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS flowcharts (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "name TEXT NOT NULL," +
                            "data TEXT NOT NULL," +
                            "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                            "update_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
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

            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO notes(title, file_path, line_number, select_code, create_time, update_time,tags) VALUES (?, ?, ?, ?, datetime('now'), datetime('now'),?)")) {
                stmt.setString(1, noteData.noteTitle);
                stmt.setString(2, noteData.noteFilePath);
                stmt.setInt(3, noteData.noteLineNumber);
                stmt.setString(4, noteData.selectCode);
                stmt.setString(5, noteData.tags);
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
            String sql = "SELECT id, title, file_path, line_number, select_code, tags, create_time, update_time FROM notes order by create_time desc";
            PreparedStatement statement = getConnection().prepareStatement(sql);
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

    public static List<String> getAllTags() {
        List<String> allTags = new ArrayList<>();
        Set<String> uniqueTags = new HashSet<>();

        List<NoteData> allNotes = getAllNoteData();
        for (NoteData note : allNotes) {
            if (note.getTags() != null && !note.getTags().isEmpty()) {
                String[] tags = note.getTags().split(",");
                for (String tag : tags) {
                    String trimmedTag = tag.trim();
                    if (!trimmedTag.isEmpty()) {
                        uniqueTags.add(trimmedTag);
                    }
                }
            }
        }

        allTags.addAll(uniqueTags);
        return allTags;
    }

    public static void deleteNoteData(Integer noteId){
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement("delete from notes where id = ?")) {
            stmt.setInt(1, noteId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // 流程图相关方法
    public static Integer saveFlowchart(FlowchartData flowchart) {
        String sql = "INSERT OR REPLACE INTO flowcharts (name, data, update_time,id) VALUES (?, ?,datetime('now'),?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, flowchart.getName());
            pstmt.setString(2, flowchart.getData());
            if (flowchart.getId() != null) {
                pstmt.setInt(3, flowchart.getId());
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            pstmt.executeUpdate();
            if(flowchart.getId() != null){
                return flowchart.getId();
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating flowchart failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save flowchart: " + e.getMessage());
        }
    }

    public static List<FlowchartData> getAllFlowcharts() {
        List<FlowchartData> flowcharts = new ArrayList<>();
        String sql = "SELECT * FROM flowcharts ORDER BY create_time DESC";
        
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                FlowchartData flowchart = new FlowchartData();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                flowchart.setId(rs.getInt("id"));
                flowchart.setName(rs.getString("name"));
                flowchart.setData(rs.getString("data"));
                String createTime = rs.getString("create_time");
                String updateTime = rs.getString("update_time");
                flowchart.createTime = dateFormat.parse(createTime);
                flowchart.updateTime = dateFormat.parse(updateTime);
                flowcharts.add(flowchart);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return flowcharts;
    }

    public static FlowchartData getFlowchartById(int id) {
        String sql = "SELECT * FROM flowcharts WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            FlowchartData flowchart = new FlowchartData();
            if (rs.next()) {
                flowchart.setId(rs.getInt("id"));
                flowchart.setName(rs.getString("name"));
                flowchart.setData(rs.getString("data"));
                String createTime = rs.getString("create_time");
                String updateTime = rs.getString("update_time");
                flowchart.createTime = dateFormat.parse(createTime);
                flowchart.updateTime = dateFormat.parse(updateTime);
                return flowchart;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
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
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (rs.next()) {
                NoteData note = new NoteData();
                note.id = rs.getInt("id");
                note.noteTitle = rs.getString("title");
                note.noteFilePath = rs.getString("file_path");
                note.noteLineNumber = rs.getInt("line_number");
                note.selectCode = rs.getString("select_code");
                note.tags = rs.getString("tags");
                String createTime = rs.getString("create_time");
                String updateTime = rs.getString("update_time");
                note.createTime = dateFormat.parse(createTime);
                note.updateTime = dateFormat.parse(updateTime);
                return note;
            }
        } catch (Exception e) {
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

    public static void updateNote(NoteData noteData) {
        String sql = "UPDATE notes SET title = ?, file_path = ?, line_number = ?, select_code = ?, tags = ?, update_time = datetime('now') WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, noteData.noteTitle);
            pstmt.setString(2, noteData.noteFilePath);
            pstmt.setInt(3, noteData.noteLineNumber);
            pstmt.setString(4, noteData.selectCode);
            pstmt.setString(5, noteData.tags);
            pstmt.setInt(6, noteData.id);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update note: " + e.getMessage());
        }
    }

    public static void deleteFlowchart(int id) {
        String sql = "DELETE FROM flowcharts WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete flowchart: " + e.getMessage());
        }
    }
}
