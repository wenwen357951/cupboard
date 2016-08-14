package tw.mics.spigot.plugin.cupboard.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;
import tw.mics.spigot.plugin.cupboard.utils.Util;

public class CupboardsData {
    private File dbfile;
    Connection db_conn;
    
	final private int PROTECT_DIST = Config.CUPBOARD_PROTECT_DIST.getInt();
	final private int CUPBOARD_DIST = Config.CUPBOARD_BETWEEN_DIST.getInt();
    
	final int maxEntries = 10000;
	
	private Cupboard plugin;
	public CupboardsData(File dataFolder, Cupboard p){
		this.plugin = p;
		dbfile = new File(dataFolder, "database.db");
        this.initDatabase();
    }
    
    private void initDatabase() {
        try {
          Class.forName("org.sqlite.JDBC");
          db_conn = DriverManager.getConnection("jdbc:sqlite:"+dbfile.getPath());
       
          //新增表格
          Statement stmt = db_conn.createStatement();
          String sql = "CREATE TABLE IF NOT EXISTS CUPBOARDS " +
                  "(CID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                  " WORLD   TEXT     NOT NULL," +
                  " X       INTEGER  NOT NULL," +
                  " Y       INTEGER  NOT NULL," +
                  " Z       INTEGER  NOT NULL," +
                  " LOC     TEXT     NOT NULL)";
          stmt.executeUpdate(sql);

          sql = "CREATE TABLE IF NOT EXISTS PLAYER_OWN_CUPBOARDS " +
                  "(UUID TEXT NOT NULL," +
                  " CID  INTEGER NOT NULL)";
          stmt.executeUpdate(sql);
          
          sql = "CREATE INDEX IF NOT EXISTS player_cid_index " +
                  "on PLAYER_OWN_CUPBOARDS (CID)";
          stmt.executeUpdate(sql);
          stmt.close();
          db_conn.setAutoCommit(false);
          
        } catch ( SQLException | ClassNotFoundException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        plugin.logDebug("Opened database successfully");
	}

	public boolean putCupboard(Block b, Player p){
	    if(!checkAccess(b, p, CUPBOARD_DIST)) return false;
        try {
            //insert cupboard
            String sql = "INSERT INTO CUPBOARDS (WORLD, X, Y, Z, LOC) " +
                    "VALUES (?,?,?,?,?);"; 
            PreparedStatement pstmt = db_conn.prepareStatement(sql);
            pstmt.setString(1, b.getWorld().getName());
            pstmt.setInt(2,b.getX());
            pstmt.setInt(3,b.getY());
            pstmt.setInt(4,b.getZ());
            pstmt.setString(5, Util.LocToString(b.getLocation()));
            pstmt.execute();
            
            int cid = pstmt.getGeneratedKeys().getInt(1);
            pstmt.close();

            sql = "INSERT INTO PLAYER_OWN_CUPBOARDS (UUID, CID) " +
                    "VALUES (?,?);";
            pstmt = db_conn.prepareStatement(sql);
            pstmt.setString(1, p.getUniqueId().toString());
            pstmt.setInt(2, cid);
            pstmt.execute();
            pstmt.close();
            
            db_conn.commit();
        } catch ( SQLException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    	return true;
    }
	
    public boolean removeCupboard(Block b){
        boolean flag = false;
        
        try {
            Statement stmt = db_conn.createStatement();
            String sql = String.format("SELECT CUPBOARDS.CID FROM CUPBOARDS WHERE LOC = \"%s\"", Util.LocToString(b.getLocation()));
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                int cid = rs.getInt(1);
                sql = String.format("DELETE FROM CUPBOARDS WHERE CID = %d;", cid);
                stmt.execute(sql);
                sql = String.format("DELETE FROM PLAYER_OWN_CUPBOARDS WHERE CID = %d;", cid);
                stmt.execute(sql);
                flag = true;
            } else {
                flag = false;
            }
            rs.close();
            db_conn.commit();
        } catch ( SQLException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } 
        
        return flag;
    }
    
	public Boolean toggleBoardAccess(Player p, Block b){
        Boolean access_flag = null;
        Boolean return_flag = null;
	    try {
	        
	        //確認是否有該金磚權限
            Statement stmt = db_conn.createStatement();
            String sql = String.format( 
                    "SELECT CUPBOARDS.CID, PLAYER_OWN_CUPBOARDS.UUID FROM CUPBOARDS "
                    + "LEFT JOIN PLAYER_OWN_CUPBOARDS "
                    + "ON CUPBOARDS.CID = PLAYER_OWN_CUPBOARDS.CID "
                    + "WHERE LOC=\"%s\""
                    , Util.LocToString(b.getLocation())
            );
            ResultSet rs = stmt.executeQuery(sql);
            Integer cid = null;
            if(rs.next()){
                access_flag = false;
                cid = rs.getInt(1);
                do{
                    String uuid = rs.getString(2);
                    if(uuid != null && uuid.equals(p.getUniqueId().toString())){
                        access_flag = true;
                        break;
                    }
                }while(rs.next());
            }
            

            //執行切換動作
            if(access_flag != null){
                if(access_flag){
                    sql = String.format("DELETE FROM PLAYER_OWN_CUPBOARDS WHERE UUID=\"%s\" AND CID=%d;", 
                            p.getUniqueId().toString(), cid);
                    stmt.execute(sql);
                    return_flag = false;
                } else {
                    sql = String.format("INSERT INTO PLAYER_OWN_CUPBOARDS (UUID, CID) " +
                            "VALUES (\"%s\", %d);", 
                            p.getUniqueId().toString(), cid);
                    stmt.execute(sql);
                    return_flag = true;
                }
            }

            db_conn.commit();
            stmt.close();
            rs.close();
        } catch ( SQLException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            if(Config.DEBUG.getBoolean())e.printStackTrace();
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } 
        return return_flag;
	}
	
	public boolean checkIsLimit(Block b){
		return !checkAccess(b);
	}
	
	public boolean checkIsLimit(Location l){
		return !checkAccess(l);
	}
	
	public boolean checkIsLimit(Block b, Player p){
		return !checkAccess(b, p);
	}
	
    public boolean checkIsLimit(Location l, Player p){
        return !checkAccess(l, p);
    }

    public boolean checkIsLimitByUUIDString(Block b, String uuid){
        return !checkAccess(b, uuid);
    }
    
    public boolean checkIsLimitByUUIDString(Location l, String uuid){
        return !checkAccess(l, uuid);
    }

    public int cleanNotExistCupboard() {
        //TODO
        int remove_count = 0;
        return remove_count;
    }
    
    public void close() {
        try {
            db_conn.close();
        } catch ( SQLException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    private boolean checkAccess(Block b){
        return checkAccess(b.getWorld().getName(), b.getX(), b.getY(), b.getZ(), null, PROTECT_DIST);
    }
    
    private boolean checkAccess(Location l){
        return checkAccess(l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), null, PROTECT_DIST);
    }

    private boolean checkAccess(Block b, Player p){
        return checkAccess(b.getWorld().getName(), b.getX(), b.getY(), b.getZ(), p.getUniqueId().toString(), PROTECT_DIST);
    }
    
    private boolean checkAccess(Location l, Player p){
        return checkAccess(l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), p.getUniqueId().toString(), PROTECT_DIST);
    }
    
    private boolean checkAccess(Block b, String uuid){
        return checkAccess(b.getWorld().getName(), b.getX(), b.getY(), b.getZ(), uuid, PROTECT_DIST);
    }
    
    private boolean checkAccess(Location l, String uuid){
        return checkAccess(l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), uuid, PROTECT_DIST);
    }
    
    private boolean checkAccess(Block b, Player p, int radius){
        return checkAccess(b.getWorld().getName(), b.getX(), b.getY(), b.getZ(), p.getUniqueId().toString(), radius);
    }
    
    private boolean checkAccess(String world, int x, int y, int z, String uuid,int radius){
        boolean flag_access = true;
        try {
            Statement stmt = db_conn.createStatement();
            String sql = "SELECT CUPBOARDS.CID FROM CUPBOARDS "
                    + String.format(
                            "WHERE X <= %d AND X >= %d "
                          + "AND Y <= %d AND Y >= %d "
                          + "AND Z <= %d AND Z >= %d "
                          + "AND WORLD = \"%s\" "
                          , x + radius, x - radius
                          , y + radius, y - radius
                          , z + radius, z - radius
                          , world);
            if(uuid != null) sql += String.format("AND CID NOT IN (SELECT CID FROM PLAYER_OWN_CUPBOARDS WHERE UUID=\"%s\")", uuid);
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                flag_access = false;
            }
            rs.close();
            stmt.close();
        } catch ( SQLException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if(uuid != null) plugin.getServer().getPlayer(UUID.fromString(uuid)).sendMessage("系統嚴重錯誤, 請聯繫管理員");
            flag_access = false;
        }
        if(flag_access) return true;
        return false;
    }
}
