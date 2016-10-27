package tw.mics.spigot.plugin.cupboard.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import tw.mics.spigot.plugin.cupboard.Cupboard;

public class EvilPointData {
    Cupboard plugin;
    Connection db_conn;
    Scoreboard board;
    Objective objective;
    private LinkedHashMap<String, Integer> cache;
    public EvilPointData(Cupboard p, Connection conn){
        this.plugin = p;
        db_conn = conn;
        cache = new LinkedHashMap<String, Integer>();
        scoreboardInit();
    }
    private void scoreboardInit(){
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = board.registerNewObjective("evilpoint", "dummy");
        objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        for(Player online : Bukkit.getOnlinePlayers()){
            online.setScoreboard(board);
            String UUID = online.getUniqueId().toString();
            objective.getScore(UUID).setScore(getEvil(online));
        }
    }
    @SuppressWarnings("deprecation")
    public void scoreboardUpdate(Player player){
        player.setScoreboard(board);
        objective.getScore(player).setScore(getEvil(player));
    }
    @SuppressWarnings("deprecation")
    public void scoreboardUpdate(){
        for(Player online : Bukkit.getOnlinePlayers()){
            online.setScoreboard(board);
            objective.getScore(online).setScore(getEvil(online));
        }
    }
    private void initEvil(Player player){
        try {
            String sql = "INSERT INTO PLAYER_EVIL_POINT (UUID, EVIL_POINT) " +
                    "VALUES (?,?);"; 
            PreparedStatement pstmt = db_conn.prepareStatement(sql);
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setInt(2,0);
            pstmt.execute();
            pstmt.close();
            db_conn.commit();
        } catch ( SQLException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    public void plusEvil(Player player, int modify_point){
        int old_point = getEvil(player);
        int point = old_point + modify_point;
        String UUID = player.getUniqueId().toString();
        if(point > 100000) point = 100000;
        try {
            String sql = "UPDATE PLAYER_EVIL_POINT SET EVIL_POINT=? WHERE UUID=?;"; 
            PreparedStatement pstmt = db_conn.prepareStatement(sql);
            pstmt.setInt(1, point);
            pstmt.setString(2, UUID);
            pstmt.execute();
            pstmt.close();
            db_conn.commit();
        } catch ( SQLException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } 
        cache.put(UUID, point);
        scoreboardUpdate(player);
    }

    public void minusEvil(Player player, int modify_point){
        int old_point = getEvil(player);
        int point = old_point - modify_point;
        String UUID = player.getUniqueId().toString();
        if(point < 0) point = 0;
        try {
            String sql = "UPDATE PLAYER_EVIL_POINT SET EVIL_POINT=? WHERE UUID=?;"; 
            PreparedStatement pstmt = db_conn.prepareStatement(sql);
            pstmt.setInt(1, point);
            pstmt.setString(2, UUID);
            pstmt.execute();
            pstmt.close();
            db_conn.commit();
        } catch ( SQLException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } 
        cache.put(UUID, point);
        scoreboardUpdate(player);
    }
    
    public int getEvil(Player player){
        int point = 0;
        String UUID = player.getUniqueId().toString();
        if(cache.containsKey(UUID)){
            return cache.get(UUID);
        }
        try {
            Statement stmt = db_conn.createStatement();
            String sql = String.format("SELECT EVIL_POINT FROM PLAYER_EVIL_POINT WHERE UUID = \"%s\"", UUID);
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                point = rs.getInt(1);
            } else {
                initEvil(player);
            }
            stmt.close();
            rs.close();
            db_conn.commit();
        } catch ( SQLException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } 
        cache.put(UUID, point);
        return point;
    }
}
