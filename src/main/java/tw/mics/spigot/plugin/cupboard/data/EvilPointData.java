package tw.mics.spigot.plugin.cupboard.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import tw.mics.spigot.plugin.cupboard.Cupboard;

public class EvilPointData {
    Cupboard plugin;
    Connection db_conn;
    public EvilPointData(Cupboard p, Connection conn){
        this.plugin = p;
        db_conn = conn;
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

    public void plusEvil(Player player, int point){
        int old_point = getEvil(player);
        try {
            String sql = "UPDATE PLAYER_EVIL_POINT SET EVIL_POINT=? WHERE UUID=?;"; 
            PreparedStatement pstmt = db_conn.prepareStatement(sql);
            if(getEvil(player) - point > 100000){
                pstmt.setInt(1,100000);
            } else {
                pstmt.setInt(1, old_point + point);
            }
            pstmt.setString(2, player.getUniqueId().toString());
            pstmt.execute();
            pstmt.close();
            db_conn.commit();
        } catch ( SQLException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } 
    }

    public void minusEvil(Player player, int point){
        int old_point = getEvil(player);
        try {
            String sql = "UPDATE PLAYER_EVIL_POINT SET EVIL_POINT=? WHERE UUID=?;"; 
            PreparedStatement pstmt = db_conn.prepareStatement(sql);
            if(getEvil(player) - point < 0){
                pstmt.setInt(1,0);
            } else {
                pstmt.setInt(1, old_point - point);
            }
            pstmt.setString(2, player.getUniqueId().toString());
            pstmt.execute();
            pstmt.close();
            db_conn.commit();
        } catch ( SQLException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } 
    }
    
    public int getEvil(Player player){
        int point = 0;
        try {
            Statement stmt = db_conn.createStatement();
            String sql = String.format("SELECT EVIL_POINT FROM PLAYER_EVIL_POINT WHERE UUID = \"%s\"", player.getUniqueId());
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
        return point;
    }
}
