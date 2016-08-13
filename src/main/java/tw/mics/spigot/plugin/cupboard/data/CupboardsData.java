package tw.mics.spigot.plugin.cupboard.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.google.gson.Gson;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;
import tw.mics.spigot.plugin.cupboard.utils.Util;

public class CupboardsData {
	private HashMap<String, List<String>> cupboards;
	
	private Map<String, HashSet<String>> location_limit_check_temp;
	
	private File cupboardsFile;
	private int PROTECT_DIST;
	private int CUPBOARD_DIST;
	final int maxEntries = 10000;
	
	@SuppressWarnings("unused")
	private Cupboard plugin;
	public CupboardsData(File dataFolder, Cupboard p){
		this.plugin = p;
        cupboardsFile = new File(dataFolder, "cupboards.json");
        PROTECT_DIST = Config.CUPBOARD_PROTECT_DIST.getInt()+1;
        CUPBOARD_DIST = Config.CUPBOARD_BETWEEN_DIST.getInt()+1;
        this.loadCuppboards();
    }
    
    @SuppressWarnings("unchecked")
	private void loadCuppboards() {
    	cupboards = new HashMap<String, List<String>>();
    	Gson gson = new Gson();
    	try {
			FileReader file = new FileReader(cupboardsFile);
			cupboards = gson.fromJson(file, HashMap.class);
		} catch (FileNotFoundException e) {
			cupboards = new HashMap<String, List<String>>();
		}
    	
    	//temp owner
    	location_limit_check_temp = new LinkedHashMap<String, HashSet<String>>(maxEntries*10/7, 0.7f, true) {
			private static final long serialVersionUID = 1L;
			@Override
	        protected boolean removeEldestEntry(Map.Entry<String, HashSet<String>> eldest) {
	            return size() > maxEntries;
	        }
	    };
    	
	}

	public boolean putCupboard(Block b, Player p){
    	Location pubBlockLocation = b.getLocation();
    	String pubBlockLocation_str = Util.LocToString(pubBlockLocation);
    	if(this.checkCupboardLimit(b, p))return false;
    	List<String> AccessAbleUserUUIDList = new ArrayList<String>();
    	AccessAbleUserUUIDList.add(p.getUniqueId().toString());
    	cupboards.put(pubBlockLocation_str, AccessAbleUserUUIDList);
    	this.cleanTempCheck();
    	this.save();
    	return true;
    }
	
    public void removeCupboard(Block b){
    	Location pubBlockLocation = b.getLocation();
    	String pubBlockLocation_str = Util.LocToString(pubBlockLocation);
    	cupboards.remove(pubBlockLocation_str);
    	this.cleanTempCheck();
    	this.save();
    }
    
    public boolean checkCupboardExist(Block b){
    	if(cupboards.get(Util.LocToString(b.getLocation())) == null)return false;
		return true;
    }
	public boolean toggleBoardAccess(Player p, Block b){
    	String pubBlockLocation_str = Util.LocToString(b.getLocation());
    	String toogleUserUUID = p.getUniqueId().toString();
    	List<String> AccessAbleUserUUIDList;
    	boolean rtn;
    	AccessAbleUserUUIDList = (List<String>) cupboards.get(pubBlockLocation_str);
    	if(AccessAbleUserUUIDList == null)AccessAbleUserUUIDList = new ArrayList<String>();
    	
    	if(AccessAbleUserUUIDList.contains(toogleUserUUID)){
    		AccessAbleUserUUIDList.remove(toogleUserUUID);
    		rtn = false;
    	} else {
    		AccessAbleUserUUIDList.add(toogleUserUUID);
    		rtn = true;
    	}
    	cupboards.put(pubBlockLocation_str, AccessAbleUserUUIDList);
    	this.cleanTempCheck();
    	this.save();
    	return rtn;
	}
    
    private void save(){
    	Gson objGson= new Gson();
    	String strObject = objGson.toJson(cupboards);
    	try {
    		cupboardsFile.getParentFile().mkdirs();
			FileWriter file = new FileWriter(cupboardsFile);
			file.write(strObject);
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	private List<Block> findActiveCupboards(Location l,int dist){
		Set<String> cups = cupboards.keySet();
		List<Block> activeCups = new ArrayList<Block>();
		for( String cup_str : cups){
			StringTokenizer st = new StringTokenizer(cup_str, ",");
			World world = Bukkit.getWorld((st.nextToken()));
			int x = Integer.parseInt(st.nextToken());
			int y = Integer.parseInt(st.nextToken());
			int z = Integer.parseInt(st.nextToken());
			Location cup_loc = new Location(world,x,y,z);
			if(!cup_loc.getWorld().equals(l.getWorld())) continue; //如果是在地獄的 略過
			if(cup_loc.distance(l) > dist * 1.6) continue; //如果距離超過dist的1.6 倍 略過 (根號 1.414+1 = 1.554)
			int diffx = Math.abs(l.getBlockX() - cup_loc.getBlockX());
			int diffy = Math.abs(l.getBlockY() - cup_loc.getBlockY());
			int diffz = Math.abs(l.getBlockZ() - cup_loc.getBlockZ());
			if(
					diffx < dist &&
					diffy < dist &&
					diffz < dist
			){
				activeCups.add(cup_loc.getBlock());
			}
		}
		return activeCups;
	}
	public boolean checkCupboardLimit(Block b, Player p){
		List<Block> cups = this.findActiveCupboards(b.getLocation(), CUPBOARD_DIST);
        HashSet <String> accessAbleUserUUIDList = new HashSet<String>();
        boolean first = true;
		for( Block cup : cups ){
			if(cup.getType() != Material.GOLD_BLOCK){
				cupboards.put(Util.LocToString(cup.getLocation()), null);
				continue; //此方塊不是黃金磚則刪除後換下一個
			}
            if(first){
                accessAbleUserUUIDList.addAll(cupboards.get(Util.LocToString(cup.getLocation())));
                first = false;
			} else {
			    accessAbleUserUUIDList.retainAll(cupboards.get(Util.LocToString(cup.getLocation())));
			}
		}
		if(cups.isEmpty()) return false;
		if(accessAbleUserUUIDList.contains(p.getUniqueId().toString())){
		    return false;
		}
        return true;
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
        if(!location_limit_check_temp.containsKey(l)){
            this.calcLocationLimit(l);
        }
        String str_l = Util.LocToString(l);
        if(location_limit_check_temp.get(str_l) == null) return false;
        if(p == null) return true;
        //p.sendMessage("Size: " + location_limit_check_temp.size());
        if(location_limit_check_temp.get(str_l).contains(p.getUniqueId().toString())) return false;
        return true;
    }

    public boolean checkIsLimitByUUIDString(Block b, String uuid){
        return checkIsLimitByUUIDString(b.getLocation(), uuid);
    }
    public boolean checkIsLimitByUUIDString(Location l, String uuid){
        if(!location_limit_check_temp.containsKey(l)){
            this.calcLocationLimit(l);
        }
        String str_l = Util.LocToString(l);
        if(location_limit_check_temp.get(str_l) == null) return false;
        if(location_limit_check_temp.get(str_l).contains(uuid)) return false;
        return true;
    }
	
	//算出這格有誰保護~~~
	private void calcLocationLimit(Location l){
		String str_l = Util.LocToString(l);
		Boolean first = true;
		List<Block> cups = this.findActiveCupboards(l, PROTECT_DIST);
		HashSet <String> accessAbleUserUUIDList = new HashSet<String>();
		if(cups.isEmpty()){
			location_limit_check_temp.put(str_l, null);
		} else {
			for( Block cup : cups ){
				if(cup.getType() != Material.GOLD_BLOCK){
					cupboards.remove(Util.LocToString(cup.getLocation())); //此方塊不是黃金磚則刪除後換下一個
					continue;
				}
	            if(first){
	                accessAbleUserUUIDList.addAll(cupboards.get(Util.LocToString(cup.getLocation())));
	                first = false;
	            } else {
	                accessAbleUserUUIDList.retainAll(cupboards.get(Util.LocToString(cup.getLocation())));
	            }
			}
			location_limit_check_temp.put(str_l, accessAbleUserUUIDList);
		}
	}
	
	public void cleanTempCheck(){
		this.location_limit_check_temp.clear();
	}

    public int cleanNotExistCupboard() {
        int remove_count = 0;
        Iterator<String> itr = cupboards.keySet().iterator();
        while(itr.hasNext()){
            if(Util.StringToLoc(itr.next()).getBlock().getType() != Material.GOLD_BLOCK){
                itr.remove();
                remove_count++;
            }
        }
        if(remove_count > 0) this.save();
        return remove_count;
    }
}
