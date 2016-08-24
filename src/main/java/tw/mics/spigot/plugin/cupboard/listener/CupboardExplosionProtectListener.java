package tw.mics.spigot.plugin.cupboard.listener;

import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;
import tw.mics.spigot.plugin.cupboard.data.CupboardsData;

public class CupboardExplosionProtectListener extends MyListener {
	private CupboardsData data;

    private LinkedList<Integer> disable_explosion_id;

    public CupboardExplosionProtectListener(Cupboard instance)
    {
        super(instance);
        this.data = this.plugin.cupboards;
        disable_explosion_id = new LinkedList<Integer>();
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplosion(ExplosionPrimeEvent e){
        Entity entity = e.getEntity();
        if(entity instanceof TNTPrimed && !Config.ANTI_TNT_EXPLOSION.getBoolean()) return;
        if(!(entity instanceof TNTPrimed) && !Config.ANTI_OTHERS_EXPLOSION.getBoolean()) return;
        if(!data.checkExplosionAble(e.getEntity().getLocation(), e.getRadius())){
            disable_explosion_id.push(e.getEntity().getEntityId());
        }
        /*
        if(disable_explosion_id.size() > 5){
            disable_explosion_id.pollLast();
        }
        */
    }
	
    //TNT or Creeper爆炸
    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplode(EntityExplodeEvent event){
    	if(disable_explosion_id.contains(event.getEntity().getEntityId())){
    	    event.blockList().clear();
    	} else {
    	    Iterator<Block> itr = event.blockList().iterator();
            while(itr.hasNext()){
                if(itr.next().getType() == Material.GOLD_BLOCK){
                    itr.remove();
                }
            }
    	}
    }

    //防止Armor stand被炸毀
    @EventHandler
    public void onArmorStandExplosion(EntityDamageByEntityEvent e){
    	if(
    		e.getEntity().getType() == EntityType.ARMOR_STAND &&
    		e.getCause() == DamageCause.ENTITY_EXPLOSION
		){        
    	    if(disable_explosion_id.contains(e.getDamager().getEntityId())){
                e.setCancelled(true);
            }
    	}
    }
    
    //防止Hanging類物品 被Creeper炸掉 / 被TNT炸掉
    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent e) {
		if(e.getCause() == RemoveCause.ENTITY){ //by Entity
		    if(disable_explosion_id.contains(e.getRemover().getEntityId())){
                e.setCancelled(true);
            }
		}
	}
}
