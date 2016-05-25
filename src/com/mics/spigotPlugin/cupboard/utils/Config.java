package com.mics.spigotPlugin.cupboard.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;
import com.mics.spigotPlugin.cupboard.Cupboard;

public enum Config {

	DEBUG("debug", false, "is plugin show debug message?"),
	CUPBOARD_PROTECT_DIST("cupboard.protect_dist", 9, "this is cupboard protect area size (ex 9 is 9+9+1 -> 19*19*19)"),
	CUPBOARD_BETWEEN_DIST("cupboard.between_dist", 18, "this is how many block between cupboard can put another cupboard"),
	ANTI_TNT_EXPLOSION("cupboard.anti-tnt-explosion", false, "is cupboard protect explosion from TNT?"),
	ANTI_OTHERS_EXPLOSION("cupboard.anti-creeper-explosion", true, "is cupboard protect explosion from CREEPER?"),
	OP_BYPASS("cupboard.is-op-creative-bypass", true, "is OP user can bypass block protect when in creative mode?"),
	ANTI_NETHER_DOOR_BLOCK("world-protect.anti-nether-door-block", true, "let nether door can't block"),
	ANTI_NETHER_DOOR_ENTITY_TELEPORT("world-protect.anti-nether-door-entity-teleport", true, "let nether door can't teleport ant entity"),
	TNT_SP_ENABLE("tnt.enable", true, "let TNT can desotry obsidian, water, lava, and TNT hard to craft."),
	TNT_BREAK_RADIUS("tnt.break-radius", 2, "destory check radius"),
	TNT_OBSIDIAN_BREAK_PROBABILITY("tnt.obsidian.break-probability", 0.3, "turn obsidian block probability"),
	TNT_OBSIDIAN_BREAK_TO("tnt.obsidian.break-to", "COBBLESTONE", "turn obsidian to which block"),
	TNT_WATER_BREAK_PROBABILITY("tnt.water.break-probability", 1, "turn water block to aire probability"),
	TNT_LAVA_BREAK_PROBABILITY("tnt.lava.break-probability", 1, "turn lava to obsidian/cobblestone probability"),
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
