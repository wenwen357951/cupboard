package tw.mics.spigot.plugin.cupboard.listener;

import java.util.LinkedList;

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
    
    
    @EventHandler
    public void onExplosion(ExplosionPrimeEvent e){
        Entity entity = e.getEntity();
        if(entity instanceof TNTPrimed && !Config.ANTI_TNT_EXPLOSION.getBoolean()) return;
        if(!(entity instanceof TNTPrimed) && !Config.ANTI_OTHERS_EXPLOSION.getBoolean()) return;
        if(!data.checkExplosionAble(e.getEntity().getLocation(), e.getRadius())){
            disable_explosion_id.push(e.getEntity().getEntityId());
        }
        
        //保存50個爆炸id 應該不會有50個爆炸同時發生吧....
        while(disable_explosion_id.size() > 50){
            disable_explosion_id.pollLast();
        }
    }
	
    //TNT or Creeper爆炸
    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplode(EntityExplodeEvent event){
        if((
                Config.ANTI_OTHERS_EXPLOSION.getBoolean() && 
                event.getEntityType() == EntityType.MINECART_TNT
            ) || (
                disable_explosion_id.contains(event.getEntity().getEntityId())
            )){
            event.blockList().clear();
        }
    }

    //防止Armor stand被炸毀
    @EventHandler(priority = EventPriority.LOWEST)
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
        //TODO fix TNT can destory (低重要度)
		if(e.getCause() == RemoveCause.ENTITY){ //by Entity
		    if(disable_explosion_id.contains(e.getRemover().getEntityId())){
                e.setCancelled(true);
            }
		}
	}
}
