package tw.mics.spigot.plugin.cupboard;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import tw.mics.spigot.plugin.cupboard.command.AirdropCommand;
import tw.mics.spigot.plugin.cupboard.command.KillCommand;
import tw.mics.spigot.plugin.cupboard.command.ReloadCommand;
import tw.mics.spigot.plugin.cupboard.command.RspCommand;
import tw.mics.spigot.plugin.cupboard.config.Config;
import tw.mics.spigot.plugin.cupboard.config.Drops;
import tw.mics.spigot.plugin.cupboard.config.Locales;
import tw.mics.spigot.plugin.cupboard.data.CupboardsData;
import tw.mics.spigot.plugin.cupboard.entity.PackageEntity;
import tw.mics.spigot.plugin.cupboard.listener.AirdropInteractListener;
import tw.mics.spigot.plugin.cupboard.listener.CupboardBlockProtectListener;
import tw.mics.spigot.plugin.cupboard.listener.CupboardEntityProtectListener;
import tw.mics.spigot.plugin.cupboard.listener.CupboardExplosionProtectListener;
import tw.mics.spigot.plugin.cupboard.listener.GoldBlockListener;
import tw.mics.spigot.plugin.cupboard.listener.MyListener;
import tw.mics.spigot.plugin.cupboard.listener.PlayerRespawnListener;
import tw.mics.spigot.plugin.cupboard.listener.PlayerRespawnProtectListener;
import tw.mics.spigot.plugin.cupboard.listener.SuicideListener;
import tw.mics.spigot.plugin.cupboard.listener.TNTCraftListener;
import tw.mics.spigot.plugin.cupboard.listener.TNTExplosionListener;
import tw.mics.spigot.plugin.cupboard.listener.WorldProtectListener;
import tw.mics.spigot.plugin.cupboard.schedule.AirDrop;
import tw.mics.spigot.plugin.cupboard.schedule.Compass;
import tw.mics.spigot.plugin.cupboard.utils.SpawnLocationManager;


public class Cupboard extends JavaPlugin implements Listener {
	public CupboardsData cupboards;
    public Drops drops;
    private static Cupboard INSTANCE;
    private ArrayList<Object> registedObject;
	
	@Override
	public void onEnable() {
		
		INSTANCE = this;
		registedObject = new ArrayList<Object>();
        //load config
        Config.load();
        this.logDebug("Loaded Config!");
        Locales.load();
        this.logDebug("Loaded Locales!");
        
        //load Airdrops
        drops = new Drops();
        
        //load cupboards
        cupboards = new CupboardsData(getDataFolder(),this);
        this.logDebug("Loaded Cupboards data!");

        this.getCommand("kill").setExecutor(new KillCommand(this));
        this.getCommand("cupboardreload").setExecutor(new ReloadCommand(this));
        this.getCommand("airdrop").setExecutor(new AirdropCommand(this));
        this.getCommand("rsp").setExecutor(new RspCommand(this));
        
        registerObject();
        SpawnLocationManager.init();
    }
	private void registerObject(){
		//register listener
        registedObject.add(new CupboardEntityProtectListener(this));
        registedObject.add(new CupboardExplosionProtectListener(this));
        registedObject.add(new CupboardBlockProtectListener(this));
        registedObject.add(new GoldBlockListener(this));
        registedObject.add(new WorldProtectListener(this));
        registedObject.add(new PlayerRespawnListener(this));
        registedObject.add(new SuicideListener(this));
        registedObject.add(new AirdropInteractListener(this));
        registedObject.add(new PlayerRespawnProtectListener(this));
        
        //rewrite TNT Receipts Listener
        if(Config.TNT_SP_ENABLE.getBoolean()){
            registedObject.add(new TNTExplosionListener(this));
        	registedObject.add(new TNTCraftListener(this));
        }
        
        if(Config.AIR_DROP_ENABLE.getBoolean()){
            registedObject.add(new AirDrop(this));
        }
        
        if(Config.COMPASS_ENABLE.getBoolean()){
            registedObject.add(new Compass(this));
        }
	}
	
	public void reload(){
		this.logDebug("");
		this.logDebug("============================================");
		this.logDebug("Unregister Object");
		this.logDebug("============================================");
		for(Object l : registedObject){
			if(l instanceof MyListener){
				((MyListener)l).unregisterListener();
			} else {
				this.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				this.log("[ERROR] Object " + l.getClass().getName() + " Can't unreigster");
				this.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
		}

		this.logDebug("");
		this.logDebug("============================================");
		this.logDebug("Unregister ALL Listener and Schedule tasks");
		this.logDebug("============================================");
		//force unregister again
        this.logDebug("Unregister Listener!");
		HandlerList.unregisterAll();
        this.logDebug("Unregister Schedule tasks!");
		this.getServer().getScheduler().cancelAllTasks();
		
		//new registerdListeners
		registedObject = new ArrayList<Object>();
        //load config
		this.logDebug("");
		this.logDebug("============================================");
		this.logDebug("Reloading Config / Locales");
		this.logDebug("============================================");
        Config.load();
        this.logDebug("Loaded Config!");
        Locales.load();
        this.logDebug("Loaded Locales!");

		this.logDebug("");
		this.logDebug("============================================");
		this.logDebug("Register Listener");
		this.logDebug("============================================");
		registerObject();
		this.logDebug("");
	}
    @Override
    public void onDisable() {
    	PackageEntity.removeAll(); //remove all airdrop
    }
    
    public boolean isOP(Player p){
    	return isOP(p, true);
    }
    
    public boolean isOP(Player p, boolean notice){
        if(!Config.OP_BYPASS.getBoolean())return false;
        if(p.isOp() && p.getGameMode() == GameMode.CREATIVE){
            if(notice)p.sendMessage(Locales.OP_BYPASS.getString());
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
		getLogger().info(message);
	}

	public static Cupboard getInstance() {
		return INSTANCE;
	}
    
    
}
