package tw.mics.spigot.plugin.cupboard.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;
import tw.mics.spigot.plugin.cupboard.utils.Util;

public class CupboardsData {
    Connection db_conn;
    
	final private int PROTECT_DIST = Config.CUPBOARD_PROTECT_DIST.getInt();
	final private int CUPBOARD_DIST = Config.CUPBOARD_BETWEEN_DIST.getInt();
    
	final int maxEntries = 10000;
	
	private Map<String, Boolean> check_access_cache;
	
	private Cupboard plugin;
	public CupboardsData(Cupboard p, Connection conn){
		this.plugin = p;
		db_conn = conn;
		check_access_cache = new LinkedHashMap<String, Boolean>(maxEntries*10/7, 0.7f, true) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                return size() > maxEntries;
            }
        };
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
        
        this.changelog(String.format("%s put Cupboard at %s.", p.getName(), Util.LocToString(b.getLocation())));
        check_access_cache.clear();
    	return true;
    }
	
    public boolean removeCupboard(Block b, OfflinePlayer p){
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
        
        this.changelog(String.format("%s remove Cupboard at %s.", p.getName(), Util.LocToString(b.getLocation())));
        check_access_cache.clear();
        return flag;
    }
    
    public void giveAcceee(String giver_uuid, String receiver_uuid){
        Statement stmt;
        try {
            stmt = db_conn.createStatement();
            String sql = String.format("INSERT INTO PLAYER_OWN_CUPBOARDS (UUID, CID) "
                    + "SELECT \"" + receiver_uuid + "\",CID  AS CT FROM PLAYER_OWN_CUPBOARDS  T1 "
                    + "WHERE (UUID=\"" + giver_uuid + "\" OR UUID=\"" + receiver_uuid + "\") "
                    + "GROUP BY CID having COUNT(CID)=1");
                    stmt.execute(sql);
                    stmt.close();
                    db_conn.commit();
        } catch (SQLException e) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            if(Config.DEBUG.getBoolean())e.printStackTrace();
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
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
                    this.changelog(String.format("%s revoke access at %s", p.getName(), Util.LocToString(b.getLocation())));
                } else {
                    sql = String.format("INSERT INTO PLAYER_OWN_CUPBOARDS (UUID, CID) " +
                            "VALUES (\"%s\", %d);", 
                            p.getUniqueId().toString(), cid);
                    stmt.execute(sql);
                    return_flag = true;
                    this.changelog(String.format("%s grant access at %s.", p.getName(), Util.LocToString(b.getLocation())));
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

    @SuppressWarnings("deprecation")
    public void cleanNotExistCupboard() {
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
            @Override
            public void run() {
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
                        changelog(String.format("Player data %s removed", uuid));
                    }
                    pstmt_player.executeBatch();
                    pstmt_player.close();
                    
                    //Clean no anyone can access gold block
                    sql = "SELECT CID, LOC FROM CUPBOARDS WHERE CID NOT IN "
                            + "(SELECT CID FROM PLAYER_OWN_CUPBOARDS GROUP BY CID)";
                    rs = stmt.executeQuery(sql);
                    while(rs.next()){
                        String loc = rs.getString(2);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
                            @Override
                            public void run() {
                                Util.StringToLoc(loc).getBlock().setType(Material.AIR);
                            }
                        });
                        remove_cid_list.add(rs.getInt(1));
                        changelog(String.format("Cupboard at %s removed (reason: no anyone can access)", rs.getString(2)));
                    }
                    
                    //Clear not gold block data
                    sql = "SELECT CID, LOC FROM CUPBOARDS;";
                    rs = stmt.executeQuery(sql);
                    while(rs.next()){
                        int cid = rs.getInt(1);
                        String loc = rs.getString(2);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
                            @Override
                            public void run() {
                                if(Util.StringToLoc(loc).getBlock().getType() != Material.GOLD_BLOCK){
                                    remove_cid_list.add(cid);
                                    changelog(String.format("Cupboard at %s removed (reason: not gold block)", loc));
                                }
                            }
                        });
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

                plugin.log("Cleaned %d not exist player!", remove_uuid_list.size());
                plugin.log("Cleaned %d not exist or non anyone can access cupboards!", remove_cid_list.size());
            }
        });
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
        return flag_access;
    }

    public boolean checkDirectAccess(Block block, Player player){
        return checkDirectAccess(block.getLocation(), player.getUniqueId().toString());
    }
    
    public boolean checkDirectAccess(Location location, String uuid){
        boolean flag_access = true;
        try {
            Statement stmt = db_conn.createStatement();
            String sql = "SELECT CUPBOARDS.CID FROM CUPBOARDS "
                    + String.format(
                            "WHERE X = %d "
                          + "AND Y = %d "
                          + "AND Z = %d "
                          + "AND WORLD = \"%s\" "
                          , location.getBlockX()
                          , location.getBlockY()
                          , location.getBlockZ()
                          , location.getWorld().getName());
            sql += String.format("AND CID NOT IN (SELECT CID FROM PLAYER_OWN_CUPBOARDS WHERE UUID=\"%s\")", uuid);
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
        return flag_access;
    }
    
    @SuppressWarnings("deprecation")
    public void findNearest(Player p){
        Location l = p.getLocation().clone();
        String uuid = p.getUniqueId().toString();
        int x = l.getBlockX();
        int y = l.getBlockY();
        int z = l.getBlockZ();
        int radius = 20;
        String world = l.getWorld().getName();
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
            @Override
            public void run() {
                double distance = radius*2;
                String nearest_loc = null;
                try {
                    Statement stmt = db_conn.createStatement();
                    String sql = "SELECT CUPBOARDS.LOC FROM CUPBOARDS "
                            + String.format(
                                    "WHERE X <= %d AND X >= %d "
                                  + "AND Y <= %d AND Y >= %d "
                                  + "AND Z <= %d AND Z >= %d "
                                  + "AND WORLD = \"%s\" "
                                  , x + radius, x - radius
                                  , y + radius, y - radius
                                  , z + radius, z - radius
                                  , world);
                    sql += String.format("AND CID IN (SELECT CID FROM PLAYER_OWN_CUPBOARDS WHERE UUID=\"%s\")", uuid);
                    ResultSet rs = stmt.executeQuery(sql);
                    while(rs.next()){
                        String this_loc_str = rs.getString(1);
                        double this_distance = l.distance(Util.StringToLoc(this_loc_str));
                        if(this_distance < distance){
                            nearest_loc = this_loc_str;
                            distance = this_distance;
                        }
                    }
                    rs.close();
                    stmt.close();
                } catch ( SQLException e ) {
                    plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
                    plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    if(uuid != null) plugin.getServer().getPlayer(UUID.fromString(uuid)).sendMessage(ChatColor.DARK_RED + "系統嚴重錯誤, 請聯繫管理員");
                }
                if(nearest_loc == null){
                    p.sendMessage(ChatColor.RED + "附近沒有已授權金磚");
                } else {
                    p.sendMessage(ChatColor.GOLD + "最近的已授權金磚在 " + nearest_loc);
                }
            }
        });
    }
    
    private void changelog(String msg){
        new Thread(() -> {
            try
            {
                File dataFolder = plugin.getDataFolder();
                if(!dataFolder.exists()){
                    dataFolder.mkdir();
                }
                File saveTo = new File(plugin.getDataFolder(), "cupboard.log");
                if (!saveTo.exists()){
                    saveTo.createNewFile();
                }
                FileWriter fw = new FileWriter(saveTo, true);
                PrintWriter pw = new PrintWriter(fw);
                DateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss] ");
                Calendar cal = Calendar.getInstance();
                pw.println(dateFormat.format(cal.getTime()) + msg);
                pw.flush();
                pw.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }).start();
    }
}
