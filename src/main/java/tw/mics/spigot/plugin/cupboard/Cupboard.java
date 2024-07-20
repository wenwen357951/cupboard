package tw.mics.spigot.plugin.cupboard;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import tw.mics.spigot.plugin.cupboard.command.GoldCommand;
import tw.mics.spigot.plugin.cupboard.config.Config;
import tw.mics.spigot.plugin.cupboard.config.Locales;
import tw.mics.spigot.plugin.cupboard.data.CupboardsData;
import tw.mics.spigot.plugin.cupboard.data.Database;
import tw.mics.spigot.plugin.cupboard.listener.CleanGoldBlock;
import tw.mics.spigot.plugin.cupboard.listener.CupboardBlockProtectListener;
import tw.mics.spigot.plugin.cupboard.listener.CupboardEntityProtectListener;
import tw.mics.spigot.plugin.cupboard.listener.CupboardExplosionProtectListener;
import tw.mics.spigot.plugin.cupboard.listener.EvilEssenceListener;
import tw.mics.spigot.plugin.cupboard.listener.GoldBlockListener;
import tw.mics.spigot.plugin.cupboard.listener.MyListener;
import tw.mics.spigot.plugin.cupboard.listener.TNTCraftListener;
import tw.mics.spigot.plugin.cupboard.listener.TNTExplosionListener;


public class Cupboard extends JavaPlugin implements Listener {
    public CupboardsData cupboards;
    public Database database;
    private static Cupboard INSTANCE;
    private ArrayList<Object> registeredObject;

    //我是沒用的註解
    @Override
    public void onEnable() {
        INSTANCE = this;
        registeredObject = new ArrayList<Object>();
        //load config
        Config.load();
        this.logDebug("Loaded Config!");
        Locales.load();
        this.logDebug("Loaded Locales!");

        //load cupboards
        database = new Database(this, getDataFolder());
        cupboards = new CupboardsData(this, database.getConnection());
        cupboards.cleanNotExistUser();

        this.getCommand("gold").setExecutor(new GoldCommand(this));

        registerObject();
    }

    @Override
    public void onDisable() {
        unregisterObject();
        database.close();
    }

    private void registerObject() {
        //register listener
        registeredObject.add(new CleanGoldBlock(this));
        registeredObject.add(new CupboardEntityProtectListener(this));
        registeredObject.add(new CupboardExplosionProtectListener(this));
        registeredObject.add(new CupboardBlockProtectListener(this));
        registeredObject.add(new GoldBlockListener(this));
        registeredObject.add(new TNTCraftListener(this));

        //rewrite TNT Receipts Listener
        if (Config.TNT_SP_ENABLE.getBoolean()) {
            registeredObject.add(new TNTExplosionListener(this));
            //registeredObject.add(new TNTCraftListener(this));
        }

        if (Config.EVILESSENCE_ENABLE.getBoolean()) {
            registeredObject.add(new EvilEssenceListener(this));
        }
        
        /*if(Config.COMPASS_ENABLE.getBoolean()){
            registeredObject.add(new CompassListener(this));
        }*/
    }

    private void unregisterObject() {
        for (Object l : registeredObject) {
            if (l instanceof MyListener) {
                ((MyListener) l).unregisterListener();
            } else {
                this.log("[ERROR] Object " + l.getClass().getName() + " Can't unreigster");
            }
        }

        // Force unregister again
        HandlerList.unregisterAll();
        this.getServer().getScheduler().cancelTasks(this);
    }

    public boolean isOP(Player p) {
        return isOP(p, true);
    }

    public boolean isOP(Player p, boolean notice) {
        if (!Config.OP_BYPASS.getBoolean()) return false;
        if (
                p.hasPermission("cupboard.bypass") &&
                        p.getGameMode() != GameMode.SURVIVAL
        ) {
            if (notice) p.sendMessage(Locales.OP_BYPASS.getString());
            return true;
        }
        return false;
    }

    public void logDebug(String str, Object... args) {
        if (Config.DEBUG.getBoolean()) {
            String message = String.format(str, args);
            getLogger().info("(DEBUG) " + message);
        }
    }

    public void log(String str, Object... args) {
        String message = String.format(str, args);
        getLogger().info(message);
    }

    public static Cupboard getInstance() {
        return INSTANCE;
    }


}
