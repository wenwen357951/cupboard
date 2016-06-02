package com.mics.spigotPlugin.cupboard;

import java.util.ArrayList;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.mics.spigotPlugin.cupboard.command.KillCommand;
import com.mics.spigotPlugin.cupboard.listener.CupboardEntityProtectListener;
import com.mics.spigotPlugin.cupboard.listener.CupboardExplosionProtectListener;
import com.mics.spigotPlugin.cupboard.listener.CupboardBlockProtectListener;
import com.mics.spigotPlugin.cupboard.listener.CupboardUseProtectListener;
import com.mics.spigotPlugin.cupboard.listener.GoldBlockListener;
import com.mics.spigotPlugin.cupboard.listener.MyListener;
import com.mics.spigotPlugin.cupboard.listener.RespawnListener;
import com.mics.spigotPlugin.cupboard.listener.TNTCraftListener;
import com.mics.spigotPlugin.cupboard.listener.TNTExplosionListener;
import com.mics.spigotPlugin.cupboard.listener.WorldProtectListener;
import com.mics.spigotPlugin.cupboard.utils.Config;
import com.mics.spigotPlugin.cupboard.utils.Locales;


public class Cupboard extends JavaPlugin implements Listener {
	public Data data;
    private static Cupboard INSTANCE;
    private ArrayList<MyListener> registedListeners;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		registedListeners = new ArrayList<MyListener>();
        //load config
        Config.load();
        this.logDebug("Loaded Config!");
        Locales.load();
        this.logDebug("Loaded Locales!");
        
        //load cupboards
        data = new Data(getDataFolder(),this);
        this.logDebug("Loaded Cupboards data!");
        
	    setUpProtectEntity();
        //setupPermissions();
        
        //register command
        //this.getCommand("esc").setExecutor(new EscCommand(this));
        this.getCommand("kill").setExecutor(new KillCommand(this));
        
        
        //register listener
        registedListeners.add(new CupboardEntityProtectListener(this));
        registedListeners.add(new CupboardExplosionProtectListener(this));
        registedListeners.add(new CupboardBlockProtectListener(this));
        registedListeners.add(new CupboardUseProtectListener(this));
        registedListeners.add(new GoldBlockListener(this));
        registedListeners.add(new TNTExplosionListener(this));
        registedListeners.add(new WorldProtectListener(this));
        registedListeners.add(new RespawnListener(this));
        
        //setup worldborder
        if(Config.WB_ENABLE.getBoolean()){
        	//TODO let this unload able.
        	new WorldBorder(this);
        }
        
        //rewrite TNT Receipts Listener
        if(Config.TNT_SP_ENABLE.getBoolean()){
        	registedListeners.add(new TNTCraftListener(this));
        }
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
	
    @Override
    public void onDisable() {

    }

    /*
    public Permission perms;
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    */
    
    public boolean isOP(Player p){
    	if(p.isOp() && p.getGameMode() == GameMode.CREATIVE){
    		p.sendMessage(Locales.OP_BYPASS.getString());
    		return true;
    	}
		return false;
    }

	public void logDebug(String str, Object... args)
	{
		if(Config.DEBUG.getBoolean()) {
		String message = String.format(str, args);
			getLogger().info("(DEBUG) " + message);
		}
	}
	public void log(String str, Object... args)
	{
		String message = String.format(str, args);
		if(Config.DEBUG.getBoolean()) {
			getLogger().info(message);
		}
	}

	public static Cupboard getInstance() {
		return INSTANCE;
	}
    
    
}
