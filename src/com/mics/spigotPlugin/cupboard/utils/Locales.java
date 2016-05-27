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

	OP_BYPASS("gold-block.op-bypass", "&cWARNING: YOU ARE BYPASS PROTECT AREA."),
	GOLD_PLACE("gold-block.place-gold-block", "Block of Gold is placed and got access. (Shift + Right Click can get help)"),
	GOLD_REMOVE("gold-block.remove-gold-block", "Block of Gold is removed."),
	GOLD_TOO_CLOSE("gold-block.gold-block-too-close-to-place", "Block of Gold is too close to place."),
	GOLD_ACCESS_BLOCKED("gold-block.access-blocked-gold-block", "It is blocked, can't access it."),
	GOLD_DATA_NOT_FOUND("gold-block.gold-data-not-found", "This Block of Gold is not place by player or data missing."),
	GOLD_GRANT_ACCESS("gold-block.grant-access", "Grant access."),
	GOLD_REVOKE_ACCESS("gold-block.revoke-access", "Revoke access."),
	NO_ACCESS("gold-block.no-access", "&4No access &7."),
	
	//WORLD PROTECT
	DO_NOT_BLOCK_NETHER_DOOR("world-protect.do-not-block-nether-door", "&7Please do not block nether door."),
	
	//TELEPORT
	TELEPORT_FAIL("teleport.teleport-fail", "&4Teleport fail."),
	TELEPORT_NOW("teleport.teleport-now", "Will teleport after 10 sec, please do not move."),
	TELEPORT_NOT_FOUND("teleport.teleport-not-found", "Can't find good place to teleport, please try to close non-protect area."),
	NOT_IN_NO_ACCESS_AREA("teleport.not-in-no-access-area", "You are not in no access area."),
	
	//TNT
	TNT_EXPLOTION_NAME("tnt.explotion-name", "&4Explosion"),
	TNT_EXPLOTION_LORE("tnt.explotion-lore", new String[] {
			"&r&aPlease follow this to craft TNT",
	    	"&r&6E E E   &bE is &4Explosion",
	    	"&r&6E G E   &bG is Block of Gold",
	    	"&r&6E E E"
	}),
	
	//HELP
	HELP("Help", new String[] {
		"&aBlock of Gold can protect 19x19x19 area, Block of Gold is at center。",
		"&aIt Protect Following: ",
		"&71. No access player can't place/remove block",
		"&72. No access player can't use stone plate/button",
		"&73. Mobs can't trigger stone plate.",
		"&74. Creeper can't destory block in protect area."
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
