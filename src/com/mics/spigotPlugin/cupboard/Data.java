package com.mics.spigotPlugin.cupboard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

public class Data {
	private HashMap<String, List<String>> cupboards;
	
	private Map<String, HashSet<String>> location_limit_check_temp;
	
	private File cupboardsFile;
	static private int PROTECT_DIST = 10;
	static private int CUPBOARD_DIST = 18;

	Data(File dataFolder){
        cupboardsFile = new File(dataFolder, "cupboards.json");
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
			//e.printStackTrace(); //if not exist
			cupboards = new HashMap<String, List<String>>();
		}  
    	int maxEntries = 10000;
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
    	if(this.checkCupboardLimit(b))return false;
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
			FileWriter file = new FileWriter(cupboardsFile);
			file.write(strObject);
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	private List<Block> findActiveCupboards(Location l,int dist){
		Set<String> cups = cupboards.keySet();
		List<Block> activeCups = new ArrayList<Block>();
		for( String cup_str : cups){
			StringTokenizer st = new StringTokenizer(cup_str, ",");
			World world = Bukkit.getWorld((st.nextToken()));
			double x = Double.parseDouble(st.nextToken());
			double y = Double.parseDouble(st.nextToken());
			double z = Double.parseDouble(st.nextToken());
			Location cup_loc = new Location(world,x,y,z);
			if(!cup_loc.getChunk().isLoaded())continue; //如果未載入則略過 (較快
			double diffx = Math.abs(l.getBlockX() - cup_loc.getBlockX());
			double diffy = Math.abs(l.getBlockY() - cup_loc.getBlockY());
			double diffz = Math.abs(l.getBlockZ() - cup_loc.getBlockZ());
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
	public boolean checkCupboardLimit(Block b){
		List<Block> cups = this.findActiveCupboards(b.getLocation(), CUPBOARD_DIST);
		for( Block cup : cups ){
			if(cup.getType() != Material.GOLD_BLOCK){
				cupboards.put(Util.LocToString(cup.getLocation()), null);
				continue; //此方塊不是黃金磚則刪除後換下一個
			}
			return true;
		}
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
		String str_l = Util.LocToString(l);
		if(!location_limit_check_temp.containsKey(str_l)){
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
					accessAbleUserUUIDList.addAll(cupboards.get(Util.LocToString(cup.getLocation())));
				}
				location_limit_check_temp.put(str_l, accessAbleUserUUIDList);
			}
		}
		if(location_limit_check_temp.get(str_l) == null) return false;
		if(p == null) return true;
		//p.sendMessage("Size: " + location_limit_check_temp.size());
		if(location_limit_check_temp.get(str_l).contains(p.getUniqueId().toString())) return false;
		return true;
	}
	
	public void cleanTempCheck(){
		this.location_limit_check_temp.clear();
	}

	public boolean checkIsLimit_old(Block b){
		List<Block> cups = this.findActiveCupboards(b.getLocation(), PROTECT_DIST);
		for( Block cup : cups ){
			if(cup.getType() != Material.GOLD_BLOCK){
				cupboards.put(Util.LocToString(cup.getLocation()), null);
				continue; //此方塊不是黃金磚則刪除後換下一個
			}
			return true;
		}
		return false;
	}

	public boolean checkIsLimit_old(Block b, Player p){
		return checkIsLimit_old(b.getLocation(),p);
	}
	
	public boolean checkIsLimit_old(Location l, Player p){
		boolean flagIsLimit = false;
		
		List<Block> cups = this.findActiveCupboards(l, PROTECT_DIST);
		for( Block cup : cups ){
			if(cup.getType() != Material.GOLD_BLOCK){
				cupboards.put(Util.LocToString(cup.getLocation()), null);
				continue;
				//此方塊不是黃金磚則刪除後換下一個
			}
			
			List<String> AccessAbleUserUUIDList = (List<String>) cupboards.get(Util.LocToString(cup.getLocation()));
			
			if(!AccessAbleUserUUIDList.contains(p.getUniqueId().toString())) {
				flagIsLimit = true;
				break; //假如有找不到的 直接中斷跳出
			}
		}
		return flagIsLimit;
	}
}
