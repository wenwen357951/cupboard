package tw.mics.spigot.plugin.cupboard.config;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.utils.Util;

public enum Config {

	DEBUG("debug", false, "is plugin show debug message?"),
	CUPBOARD_PROTECT_DIST("cupboard.protect_dist", 9, "this is cupboard protect area size (ex 9 is 9+9+1 -> 19*19*19)"),
    CUPBOARD_BETWEEN_DIST("cupboard.between_dist", 18, "this is how many block between cupboard can put another cupboard"),
	ANTI_TNT_EXPLOSION("cupboard.anti-tnt-explosion", false, "is cupboard protect explosion from TNT?"),
	ANTI_OTHERS_EXPLOSION("cupboard.anti-creeper-explosion", true, "is cupboard protect explosion from CREEPER?"),
    OP_BYPASS("cupboard.is-op-creative-bypass", true, "is OP user can bypass block protect when in creative mode?"),
    ENABLE_WORLD("cupboard.enable-world", new String[]{
            "world"
    }, "witch world is enable cupboard?"),
	

	PP_PLAYER_RANDOM_SPAWN_ENABLE("player-protect.random-spawn.enable", true, ""),
	PP_PLAYER_RANDOM_SPAWN_THE_END_ENABLE("player-protect.random-spawn.the-end-enable", true, "enable random spawn when teleport from the end"),
    PP_PLAYER_RANDOM_SPAWN_FIRSTJOIN("player-protect.random-spawn.first-join", false, ""),
    PP_PLAYER_RANDOM_SPAWN_WORLD("player-protect.random-spawn.world", "world", ""),
    PP_PLAYER_RANDOM_SPAWN_NEW_LOCATION_TIME("player-protect.random-spawn.NewRandomPointTimeSec", 180, ""),
    PP_PLAYER_REMOVE_SPAWN_WITH_LAVA("player-protect.remove-bed-spawn-when-have-lava", true, ""),
    PP_PLAYER_SPAWN_PROTECT("player-protect.player-spawn-protect", true, ""),

	TNT_SP_ENABLE("tnt.enable", true, "let TNT can desotry obsidian, water, lava, TNT Can put in protect area and TNT hard to craft."),
    TNT_EXPLOSION_RADIUS("tnt.radius", 6, "TNT power radius"),
    TNT_EXPLOSION_DISTANCE_LIMIT("tnt.distance-limit", 4, "TNT explosion max break block distance (set -1 to disable)"),
	TNT_BREAK_RADIUS("tnt.break-radius", 3, "destory obsidian/water/lava check radius"),
	TNT_OBSIDIAN_BREAK_PROBABILITY("tnt.obsidian.break-probability", 0.5, "turn obsidian block probability (after explosion)"),
	TNT_OBSIDIAN_BREAK_TO("tnt.obsidian.break-to", "COBBLESTONE", "turn obsidian to which block"),
	TNT_OTHERS_HIGH_BLAST_RESISTANCE_BREAK_PROBABILITY("tnt.others.break-probability", 0.25, "destory high blast resistance block probability (after explosion)"),
	TNT_WATER_BREAK_PROBABILITY("tnt.water.break-probability", 1, "turn water block to air probability (before explosion)"),
    TNT_LAVA_BREAK_PROBABILITY("tnt.lava.break-probability", 1, "turn lava to air probability (before explosion)"),
    
	LOCALE("locale", "en-EN", "language file name");
	
	private final Object value;
	private final String path;
	private final String description;
	private static YamlConfiguration cfg;
	private static final File f = new File(Cupboard.getInstance().getDataFolder(), "config.yml");
	
	private Config(String path, Object val, String description) {
	    this.path = path;
	    this.value = val;
	    this.description = description;
	}
	
	public String getPath() {
	    return path;
	}
	
	public String getDescription() {
	    return description;
	}
	
	public Object getDefaultValue() {
	    return value;
	}

	public boolean getBoolean() {
	    return cfg.getBoolean(path);
	}
	
	public int getInt() {
	    return cfg.getInt(path);
	}
	
	public double getDouble() {
	    return cfg.getDouble(path);
	}
	
	public String getString() {
	    return Util.replaceColors(cfg.getString(path));
	}
	
	public List<String> getStringList() {
	    return cfg.getStringList(path);
	}
	
	public static void load() {
		boolean save_flag = false;
		
        Cupboard.getInstance().getDataFolder().mkdirs();
        String header = "";
		cfg = YamlConfiguration.loadConfiguration(f);

        for (Config c : values()) {
            if(c.getDescription().toLowerCase().equals("removed")){
                if(cfg.contains(c.getPath())){
                    save_flag = true;
                    cfg.set(c.getPath(), null);
                }
                continue;
            }
        	header += c.getPath() + ": " + c.getDescription() + System.lineSeparator();
            if (!cfg.contains(c.getPath())) {
            	save_flag = true;
                c.set(c.getDefaultValue(), false);
            }
        }
        cfg.options().header(header);
        
        if(save_flag){
        	save();
    		cfg = YamlConfiguration.loadConfiguration(f);
        }
	}
	
	public static void save(){
		try {
			cfg.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void set(Object value, boolean save) {
	    cfg.set(path, value);
	    if (save) {
            save();
	    }
	}
}
