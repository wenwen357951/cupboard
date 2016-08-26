package tw.mics.spigot.plugin.cupboard.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
	
	private Map<String, Boolean> check_access_cache;
	
	private Cupboard plugin;
	public CupboardsData(File dataFolder, Cupboard p){
		this.plugin = p;
		dbfile = new File(dataFolder, "database.db");
		check_access_cache = new LinkedHashMap<String, Boolean>(maxEntries*10/7, 0.7f, true) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                return size() > maxEntries;
            }
        };
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
                  " LOC     TEXT     UNIQUE NOT NULL)";
          stmt.executeUpdate(sql);

          sql = "CREATE TABLE IF NOT EXISTS PLAYER_OWN_CUPBOARDS " +
                  "(UUID TEXT NOT NULL," +
                  " CID  INTEGER NOT NULL)";
          stmt.executeUpdate(sql);
          
          sql = "CREATE INDEX IF NOT EXISTS player_cid_index " +
                  "on PLAYER_OWN_CUPBOARDS (UUID)";
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

	public boolean putCupboard(Block b, OfflinePlayer p){
	    if(p != null && !checkAccess(b, p, CUPBOARD_DIST)) return false;
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
            
            if(p != null){
                sql = "INSERT INTO PLAYER_OWN_CUPBOARDS (UUID, CID) " +
                        "VALUES (?,?);";
                pstmt = db_conn.prepareStatement(sql);
                pstmt.setString(1, p.getUniqueId().toString());
                pstmt.setInt(2, cid);
                pstmt.execute();
                pstmt.close();
            }
            
            db_conn.commit();
        } catch ( SQLException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        
        check_access_cache.clear();
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
            stmt.close();
            rs.close();
            db_conn.commit();
        } catch ( SQLException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } 
        check_access_cache.clear();
        return flag;
    }
    
	public Boolean toggleBoardAccess(OfflinePlayer p, Block b){
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
	    check_access_cache.clear();
        return return_flag;
	}
	public boolean checkExplosionAble(Location l, float radius){
        return checkAccess(l, PROTECT_DIST + ((int)Math.ceil(radius)) + 2); // 加2才不會炸到
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

    public void cleanNotExistCupboard() {
        //TODO clear not exist player data.
        //TODO clear empty gold block.
        
        List<Integer> remove_cid_list = new ArrayList<Integer>();
        List<String> remove_uuid_list = new ArrayList<String>();
        try {
            Statement stmt = db_conn.createStatement();
            //Clear non-exist player data
            String sql = "SELECT UUID FROM PLAYER_OWN_CUPBOARDS GROUP BY UUID;";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                File player_file = new File(plugin.getServer().getWorlds().get(0).getWorldFolder(), 
                         File.separatorChar + "playerdata" + File.separatorChar + rs.getString(1) + ".dat");
                if(!player_file.exists()){
                    remove_uuid_list.add(rs.getString(1));
                }
            }
            
            String sql_remove_player = "DELETE FROM PLAYER_OWN_CUPBOARDS WHERE UUID = ?";
            PreparedStatement pstmt_player = db_conn.prepareStatement(sql_remove_player);
            for(String uuid : remove_uuid_list){
                pstmt_player.setString(1, uuid);
                pstmt_player.addBatch();
            }
            pstmt_player.executeBatch();
            pstmt_player.close();
            
            //Clean no anyone can access gold block
            sql = "SELECT CID, LOC FROM CUPBOARDS WHERE CID NOT IN "
                    + "(SELECT CID FROM PLAYER_OWN_CUPBOARDS GROUP BY CID)";
            rs = stmt.executeQuery(sql);
            while(rs.next()){
                Util.StringToLoc(rs.getString(2)).getBlock().setType(Material.AIR);
                remove_cid_list.add(rs.getInt(1));
            }
            
            //Clear not gold block data
            sql = "SELECT CID, LOC FROM CUPBOARDS;";
            rs = stmt.executeQuery(sql);
            while(rs.next()){
                if(Util.StringToLoc(rs.getString(2)).getBlock().getType() != Material.GOLD_BLOCK){
                    remove_cid_list.add(rs.getInt(1));
                }
            }
            
            stmt.close();
            
            String sql_remove_cupboards = "DELETE FROM CUPBOARDS WHERE CID = ?";
            String sql_remove_player_owner = "DELETE FROM PLAYER_OWN_CUPBOARDS WHERE CID = ?";
            PreparedStatement pstmt_cupboards = db_conn.prepareStatement(sql_remove_cupboards);
            PreparedStatement pstmt_player_owner = db_conn.prepareStatement(sql_remove_player_owner);
            for(Integer cid : remove_cid_list){
                pstmt_cupboards.setInt(1, cid);
                pstmt_player_owner.setInt(1, cid);
                pstmt_cupboards.addBatch();
                pstmt_player_owner.addBatch();
            }
            pstmt_cupboards.executeBatch();
            pstmt_player_owner.executeBatch();
            pstmt_cupboards.close();
            pstmt_player_owner.close();
            
            db_conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.plugin.log("Cleaned %d not exist player!", remove_uuid_list.size());
        this.plugin.log("Cleaned %d not exist or non anyone can access cupboards!", remove_cid_list.size());
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
    
    private boolean checkAccess(Block b, OfflinePlayer p, int radius){
        return checkAccess(b.getWorld().getName(), b.getX(), b.getY(), b.getZ(), p.getUniqueId().toString(), radius);
    }
    
    private boolean checkAccess(Location b, int radius){
        return checkAccess(b.getWorld().getName(), b.getBlockX(), b.getBlockY(), b.getBlockZ(), null, radius);
    }
    
    private boolean checkAccess(String world, int x, int y, int z, String uuid,int radius){
        boolean flag_access = true;
        String cache_key = String.format("%s,%d,%d,%d,%s,%d",world,x,y,z,uuid,radius);
        if(check_access_cache.containsKey(cache_key)){
            flag_access = check_access_cache.get(cache_key);
        } else {
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
            check_access_cache.put(cache_key, flag_access);
        }
        if(flag_access) return true;
        return false;
    }
}
