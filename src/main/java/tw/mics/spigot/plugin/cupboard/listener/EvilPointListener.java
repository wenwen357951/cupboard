package tw.mics.spigot.plugin.cupboard.listener;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.data.EvilPointData;

public class EvilPointListener extends MyListener {
    EvilPointData evilpoint;
    public EvilPointListener(Cupboard instance) {
        super(instance);
        evilpoint = Cupboard.getInstance().evilpoint;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        evilpoint.scoreboardUpdate(event.getPlayer());
    }
    
    //傷害玩家
    @EventHandler
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event){
        if(!(event.getEntity() instanceof Player)) return;
        Player damager = null;
        if(event.getDamager() instanceof Player){
            damager = (Player) event.getDamager();
        } else if(event.getDamager() instanceof Arrow){
            Arrow arrow = (Arrow)event.getDamager();
            if(arrow.getShooter() instanceof Player){
                damager = (Player) arrow.getShooter();
            }
        } else if(event.getDamager() instanceof ThrownPotion){
            ThrownPotion potion = (ThrownPotion)event.getDamager();
            if(potion.getShooter() instanceof Player){
                damager = (Player) potion.getShooter();
            }
        }
        if(damager != null){
            evilpoint.plusEvil((Player) damager, (int)Math.ceil(event.getFinalDamage()));
            evilpoint.scoreboardUpdate((Player) damager);
        }
    }
    //弓箭傷害玩家
    //藥水傷害玩家
    //或許岩漿傷害玩家?
    
    //殺死玩家
    
    //放置TNT
    //製作TNT
}
