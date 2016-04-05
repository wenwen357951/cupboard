package com.mics.spigotPlugin.cupboard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	private File cupboardsFile;
	static private int CUPBOARD_DIST = 10;

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
	}

	public boolean putCupboard(Block b, Player p){
    	Location pubBlockLocation = b.getLocation();
    	String pubBlockLocation_str = Util.LocToString(pubBlockLocation);
    	if(this.checkIsLimit(b))return false;
    	List<String> AccessAbleUserUUIDList = new ArrayList<String>();
    	AccessAbleUserUUIDList.add(p.getUniqueId().toString());
    	cupboards.put(pubBlockLocation_str, AccessAbleUserUUIDList);
    	this.save();
    	return true;
    }
    public void removeCupboard(Block b){
    	Location pubBlockLocation = b.getLocation();
    	String pubBlockLocation_str = Util.LocToString(pubBlockLocation);
    	cupboards.remove(pubBlockLocation_str);
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
	private List<Block> findActiveCupboards(Location l){
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
			double dist = cup_loc.distance(l);
			if(dist < Data.CUPBOARD_DIST){
				activeCups.add(cup_loc.getBlock());
			}
		}
		return activeCups;
	}
	
	public boolean checkIsLimit(Block b){
		List<Block> cups = this.findActiveCupboards(b.getLocation());
		for( Block cup : cups ){
			if(cup.getType() != Material.GOLD_BLOCK){
				cupboards.put(Util.LocToString(cup.getLocation()), null);
				continue; //此方塊不是黃金磚則刪除後換下一個
			}
			return true;
		}
		return false;
	}
	
	public boolean checkIsLimit(Block b, Player p){
		boolean flagIsLimit = false;
		
		List<Block> cups = this.findActiveCupboards(b.getLocation());
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

	/*
	@SuppressWarnings("unchecked")
	public boolean checkIsLimitOld(Block b, Player p) {
		boolean flag = false;
		
		List<String> cups_str = (List<String>)limitblocks.getList(Util.LocToString(b.getLocation()));
		if(cups_str == null) return false;
		if(cups_str.isEmpty()) return false;
		
		for( String str : cups_str ){
			StringTokenizer st = new StringTokenizer(str,",");
			World world = Bukkit.getWorld((st.nextToken()));
			double x = Double.parseDouble(st.nextToken());
			double y = Double.parseDouble(st.nextToken());
			double z = Double.parseDouble(st.nextToken());
			Location cup_l = new Location(world,x,y,z);
			if(cup_l.getBlock().getType() != Material.GOLD_BLOCK){
				cupboards.set(str, null);
				break;
			}
			List<String> accessAbleUsers = (List<String>) cupboards.getList(Util.LocToString(cup_l));
			
			if(!accessAbleUsers.contains(p.getUniqueId().toString())) {
				flag = true;
				break; //假如有找不到的 直接中斷跳出
			}
		}
		
		return flag;
	}
	*/
}
