package tw.mics.spigot.plugin.cupboard.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
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
                  "(UUID TEXT PRIMARY KEY NOT NULL," +
                  " CID  INTEGER NOT NULL)";
          stmt.executeUpdate(sql);
          
          sql = "CREATE INDEX IF NOT EXISTS player_cid_index " +
                  "on PLAYER_OWN_CUPBOARDS (CID)";
          stmt.executeUpdate(sql);
          stmt.close();
          db_conn.setAutoCommit(false);
          
        } catch ( Exception e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        plugin.logDebug("Opened database successfully");
	}

	public boolean putCupboard(Block b, Player p){
        try {
            
            //insert cupboard
            String sql = "INSERT INTO CUPBOARDS (WORLD, X, Y, Z, LOC) " +
                    "VALUES (?,?,?,?,?);"; 
            PreparedStatement stmt = db_conn.prepareStatement(sql);
            stmt.setString(1, b.getWorld().getName());
            stmt.setInt(2,b.getX());
            stmt.setInt(3,b.getY());
            stmt.setInt(4,b.getZ());
            stmt.setString(5, Util.LocToString(b.getLocation()));
            stmt.execute();
            
            int cid = stmt.getGeneratedKeys().getInt(1);
            stmt.close();

            sql = "INSERT INTO PLAYER_OWN_CUPBOARDS (UUID, CID) " +
                    "VALUES (?,?);";
            stmt = db_conn.prepareStatement(sql);
            stmt.setString(1, p.getUniqueId().toString());
            stmt.setInt(2, cid);
            stmt.execute();
            
            db_conn.commit();
        } catch ( Exception e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    	return true;
    }
	
    public boolean removeCupboard(Block b){
        //TODO check exist if yes remove and return true
        // if no, return false
        return false;
    }
    public boolean checkCupboardExist(Block b){
        //TODO check exist if yes return true
        // if no, return false
        return false;
    }
    
	public boolean toggleBoardAccess(Player p, Block b){
        //TODO toggle cupboard access, if grant access, return true
	    // else return false
	    return false;
	}
	
	public boolean checkIsLimit(Block b){
		return checkIsLimit(b.getLocation(), null);
	}
	
	public boolean checkIsLimit(Location l){
		return checkIsLimit(l, null);
	}
	
	public boolean checkIsLimit(Block b, Player p){
		return checkIsLimit(b.getLocation(), p);
		
	}
	
    public boolean checkIsLimit(Location l, Player p){
        return false;
    }

    public boolean checkIsLimitByUUIDString(Block b, String uuid){
        return checkIsLimitByUUIDString(b.getLocation(), uuid);
    }
    
    public boolean checkIsLimitByUUIDString(Location l, String uuid){
        return false;
    }

    public int cleanNotExistCupboard() {
        int remove_count = 0;
        return remove_count;
    }
}
