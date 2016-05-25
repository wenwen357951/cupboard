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
import com.mics.spigotPlugin.cupboard.listener.TNTCraftListener;
import com.mics.spigotPlugin.cupboard.listener.TNTExplosionListener;
import com.mics.spigotPlugin.cupboard.listener.WorldProtectListener;
import com.mics.spigotPlugin.cupboard.utils.Config;
import com.mics.spigotPlugin.cupboard.utils.Locales;

import net.milkbowl.vault.permission.Permission;


public class Cupboard extends JavaPlugin implements Listener {
    private static Cupboard INSTANCE;
	
	public Data data;
	@Override
	public void onEnable() {
		INSTANCE = this;
        
        //load config
        Config.load();
        this.logDebug("Loaded Config!");
        Locales.load();
        this.logDebug("Loaded Locales!");
        
        //load cupboards
        data = new Data(getDataFolder(),this);
        this.logDebug("Loaded Cupboards data!");
        
	    setUpProtectEntity();
        setupPermissions();
        
        //register command
        this.getCommand("esc").setExecutor(new EscCommand(this));
        
        //register listener
        new CupboardEntityProtectListener(this);
        new CupboardExplosionProtectListener(this);
        new CupboardBlockProtectListener(this);
        new CupboardUseProtectListener(this);
        new GoldBlockListener(this);
        new TNTExplosionListener(this);
        new WorldProtectListener(this);
        
        //rewrite TNT Receipts Listener
        if(Config.TNT_SP_ENABLE.getBoolean()){
            new TNTCraftListener(this);
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

    public Permission perms;
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    
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
