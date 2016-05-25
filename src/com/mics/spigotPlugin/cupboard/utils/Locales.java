package com.mics.spigotPlugin.cupboard.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.mics.spigotPlugin.cupboard.Cupboard;

public enum Locales {

	OP_BYPASS("Message.op-bypass", "&cWARNING: YOU ARE BYPASS PROTECT AREA."),
	GOLD_PLACE("Message.place-gold-block", "Block of Gold is placed and got access. (Shift + Right Click can get help)"),
	GOLD_REMOVE("Message.remove-gold-block", "Block of Gold is removed."),
	GOLD_TOO_CLOSE("Message.gold-block-too-close-to-place", "Block of Gold is too close to place."),
	GOLD_ACCESS_BLOCKED("Message.access-blocked-gold-block", "It is blocked, can't access it."),
	GOLD_DATA_NOT_FOUND("Message.gold-data-not-found", "This Block of Gold is not place by player or data missing."),
	GOLD_GRANT_ACCESS("Message.grant-access", "Grant access."),
	GOLD_REVOKE_ACCESS("Message.revoke-access", "Revoke access."),
	NO_ACCESS("Message.no-access", "&4No access &7(blocked? ,try /esc to escape)."),
	DO_NOT_BLOCK_NETHER_DOOR("Message.do-not-block-nether-door", "&7Please do not block nether door."),
	HELP("Help", new String[] {
		"&aBlock of Gold can protect 19x19x19 area, Block of Gold is at center¡C",
		"&aIt Protect Following: ",
		"&71. No access player can't place/remove block",
		"&72. No access player can't use stone plate/button",
		"&73. Mobs can't trigger stone plate.",
		"&74. Creeper can't destory block in protect area.",
		"&cWARNING: Following CAN be do by any Player:",
		"&71. Using wooden door/button/plate, chest, lever, piston, Fences door"
	});
	
	private final Object value;
	private final String path;
	private static YamlConfiguration cfg;
	private static final File localeFolder = new File(Cupboard.getInstance().getDataFolder().getAbsolutePath() + File.separator + "locales");
	private static final File f = new File(localeFolder, Config.LOCALE.getString() + ".yml");
	
	private Locales(String path, Object val) {
	    this.path = path;
	    this.value = val;
	}
	
	public String getPath() {
	    return path;
	}
	
	public Object getDefaultValue() {
	    return value;
	}

	public String getString() {
	    return Util.replaceColors(cfg.getString(path));
	}
	
	public List<String> getStringList() {
		List<String> strlist = new ArrayList<String>();
		for( String str : cfg.getStringList(path)){
			strlist.add(Util.replaceColors(str));
		}
	    return strlist;
	}
	
	public void send(CommandSender s) {
	    s.sendMessage(getString());
	}
	
	public void send(CommandSender s, Map<String, String> map) {
	    String msg = getString();
	    for (String string : map.keySet()) {
	        msg = msg.replaceAll(string, map.get(string));
	    }
	    s.sendMessage(msg);
	}
	
	public static void load() {
		boolean save_flag = false;
	    localeFolder.mkdirs();
	    if (!f.exists()) {
	    	try{
	    		Cupboard.getInstance().saveResource("locales" + File.separator + Config.LOCALE.getString() + ".yml", true);
	    	} catch (IllegalArgumentException ex) {
	    		Cupboard.getInstance().log("Can't find this locales file, create new file for it");
    	    }
	    } 

		cfg = YamlConfiguration.loadConfiguration(f);

        for (Locales c : values()) {
            if (!cfg.contains(c.getPath())) {
            	save_flag = true;
                c.set(c.getDefaultValue(), false);
            }
        }
        
        if(save_flag){
        	save();
    		cfg = YamlConfiguration.loadConfiguration(f);
        }
	}

	public void set(Object value, boolean save){
	    cfg.set(path, value);
	    if (save) {
            Locales.save();
	    }
	}
	
	public static void save(){
	    localeFolder.mkdirs();
		try {
			cfg.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void set(Object value) throws IOException {
	    this.set(value, true);
	}
	
	public static Locales fromPath(String path) {
	    for (Locales loc : values()) {
	        if (loc.getPath().equalsIgnoreCase(path)) return loc;
	    }
	    return null;
	}
}
