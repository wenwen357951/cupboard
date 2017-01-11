package tw.mics.spigot.plugin.cupboard.utils;

import java.util.StringTokenizer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Wolf;
import org.bukkit.util.Vector;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;

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
	
	public static void setUpTNT(Location l){
	    class RunnableSelfCancelAble implements Runnable{
	        int schdule_id;
	        Location loc;
	        TNTPrimed tnt;
	        public void init(int id,Location loc,TNTPrimed t){
	            schdule_id = id;
	            this.loc = loc.clone();
	            tnt = t;
	        }
	        @Override
	        public void run() {
	            if(tnt == null || tnt.isDead()){
	                selfstop();
	            } else {
	                tnt.teleport(loc);
	                tnt.setVelocity(new Vector(0, 0, 0));
	            }
	        }
	        private void selfstop(){
	            Bukkit.getScheduler().cancelTask(schdule_id);
	        }
	    }
        TNTPrimed tnt = l.getWorld().spawn(l, TNTPrimed.class);
        tnt.setGravity(false);
        tnt.setGlowing(true);
        tnt.setVelocity(new Vector(0, 0, 0));
        tnt.setFuseTicks(Config.TNT_FUSETICK.getInt());
        RunnableSelfCancelAble runnable = new RunnableSelfCancelAble();
        int schudle_id = Bukkit.getScheduler().scheduleSyncRepeatingTask(Cupboard.getInstance(), runnable, 1, 5);
        runnable.init(schudle_id, l, tnt);
    }
}

