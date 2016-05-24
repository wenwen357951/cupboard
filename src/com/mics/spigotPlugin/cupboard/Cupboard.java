package com.mics.spigotPlugin.cupboard;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.mics.spigotPlugin.cupboard.command.EscCommand;
import com.mics.spigotPlugin.cupboard.listener.CupboardEntityProtectListener;
import com.mics.spigotPlugin.cupboard.listener.CupboardExplosionProtectListener;
import com.mics.spigotPlugin.cupboard.listener.CupboardBlockProtectListener;
import com.mics.spigotPlugin.cupboard.listener.CupboardUseProtectListener;
import com.mics.spigotPlugin.cupboard.listener.GoldBlockListener;
import com.mics.spigotPlugin.cupboard.listener.WorldProtectListener;

import net.milkbowl.vault.permission.Permission;


public class Cupboard extends JavaPlugin implements Listener {
	// CONFIG
	public boolean CFG_DEBUG = true;
	public boolean CFG_ANTI_NETHER_DOOR_BLOCK = true;
	public boolean CFG_ANTI_NETHER_DOOR_ENTITY_TELEPORT = true;
	public boolean CFG_PROTECT_EVERYTHING_WEHN_OFFLINE = true; //NOT FINISH
	public boolean CFG_OP_BYPASS = true;
	public boolean CFG_ANTI_TNT_EXPLOSION = true;
	public boolean CFG_ANTI_CREEPER_EXPLOSION = true;
	// END OF CONFIG
	
	public Data data;
	@Override
	public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        
        //TODO check cupboard.json file
        //處理資料庫
        data = new Data(getDataFolder(),this);
        

	    setUpProtectEntity();
	    setUpVipProtect();
        
        setupPermissions();
        
        //register command
        this.getCommand("esc").setExecutor(new EscCommand(this));
        
        //register listener
        new CupboardEntityProtectListener(this);
        new CupboardExplosionProtectListener(this);
        new CupboardBlockProtectListener(this);
        new CupboardUseProtectListener(this);
        new GoldBlockListener(this);
        new WorldProtectListener(this);
    }
	
		public ArrayList<Material> protect_vehicle;
	    private void setUpProtectEntity(){
	    	protect_vehicle = new ArrayList<Material>();
	    	protect_vehicle.add(Material.ARMOR_STAND);
	    	protect_vehicle.add(Material.BOAT);
	    	protect_vehicle.add(Material.MINECART );
	    	protect_vehicle.add(Material.COMMAND_MINECART );
	    	protect_vehicle.add(Material.EXPLOSIVE_MINECART );
	    	protect_vehicle.add(Material.HOPPER_MINECART );
	    	protect_vehicle.add(Material.POWERED_MINECART );
	    	protect_vehicle.add(Material.STORAGE_MINECART );
	    }
	    
	    public ArrayList<Material> vip_protect_block;
	    private void setUpVipProtect(){
	    	vip_protect_block = new ArrayList<Material>();
	    	vip_protect_block.add(Material.CHEST);
	    	vip_protect_block.add(Material.TRAPPED_CHEST);
	    	vip_protect_block.add(Material.FURNACE);
	    	vip_protect_block.add(Material.BURNING_FURNACE);
	    	vip_protect_block.add(Material.JUKEBOX);
	    	vip_protect_block.add(Material.BREWING_STAND);
	    	vip_protect_block.add(Material.ANVIL);
	    	vip_protect_block.add(Material.DROPPER);
	    	vip_protect_block.add(Material.DISPENSER);
	    	vip_protect_block.add(Material.HOPPER);
	    }
	
    @Override
    public void onDisable() {

    }

    public Permission perms;
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    
    public boolean isOP(Player p){
    	if(p.isOp() && p.getGameMode() == GameMode.CREATIVE){
    		p.sendMessage("§c警告: 管理員權限已忽視保護區");
    		return true;
    	}
		return false;
    }

	public void logDebug(String str, Object... args)
	{
		String message = String.format(str, args);
		if(this.CFG_DEBUG) {
			getLogger().info("(DEBUG) " + message);
		}
	}
    
    
}
