package tw.mics.spigot.plugin.cupboard.utils;

import java.util.Random;
import java.util.StringTokenizer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Wolf;

public class Util {
    public static Player getDamager(Entity e){
        Player damager = null;
        if(e instanceof Player){
            damager = (Player) e;
        } else if(e instanceof Arrow){
            Arrow arrow = (Arrow)e;
            if(arrow.getShooter() instanceof Player){
                damager = (Player) arrow.getShooter();
            }
        } else if(e instanceof ThrownPotion){
            ThrownPotion potion = (ThrownPotion)e;
            if(potion.getShooter() instanceof Player){
                damager = (Player) potion.getShooter();
            }
        } else if(e instanceof Wolf){
            Wolf wolf = (Wolf)e;
            if(wolf.getOwner() instanceof Player){
                damager = (Player) wolf.getOwner();
            }
        }
        return damager; //return null if not player
    }
    
	public static void msgToPlayer(Player p, String str){
		p.sendMessage(str);
	}
	
	public static String LocToString(Location l){
		String str = String.format("%s,%d,%d,%d",
				l.getWorld().getName(),
				l.getBlockX(),
				l.getBlockY(),
				l.getBlockZ());
		return str;
	}
	
	public static Location StringToLoc(String str){
	    StringTokenizer st = new StringTokenizer(str, ",");
        World world = Bukkit.getWorld((st.nextToken()));
        int x = Integer.parseInt(st.nextToken());
        int y = Integer.parseInt(st.nextToken());
        int z = Integer.parseInt(st.nextToken());
        Location loc = new Location(world,x,y,z);
        return loc;
	}
	
	public static String replaceColors(String message) {
        return message.replaceAll("&((?i)[0-9a-fk-or])", "§$1");
    }
	
	public static Location getRandomLocation(World world){ //but in border
		Location l = world.getWorldBorder().getCenter();
		int max = (int) world.getWorldBorder().getSize();
		int add_x = new Random().nextInt(max + 1) - (max / 2);
		int set_y = new Random().nextInt(world.getMaxHeight());
		int add_z = new Random().nextInt(max + 1) - (max / 2);
		l.setY(set_y);
		return l.add(add_x, 0, add_z);
	}
	
	public static Location changeLocationInBorder(Location l){
	    Location loc = l.clone();
	    double max = (int) l.getWorld().getWorldBorder().getSize()/2;
	    loc.setX(check_border(loc.getX(), max));
	    loc.setZ(check_border(loc.getZ(), max));
	    return loc;
	}
	
    private static double check_border(double i, double max) {
        if (i > max) {
            return max;
        } else if (i < -max) {
            return -max;
        }
        return i;
    }
}

