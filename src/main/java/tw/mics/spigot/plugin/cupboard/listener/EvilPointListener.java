package tw.mics.spigot.plugin.cupboard.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.data.EvilPointData;
import tw.mics.spigot.plugin.cupboard.utils.Util;

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
    public void onPlayerDamagePlayer(EntityDamageEvent event){
        if(!(event.getEntity() instanceof Player)) return;
        Player damager = null;
        if(event instanceof EntityDamageByEntityEvent)
            damager = Util.getDamager(((EntityDamageByEntityEvent)event).getDamager());
        //傷害計算
        double modifer = 1;
        if(damager != null && damager != event.getEntity()){
            //傷害增幅
            int ep = Cupboard.getInstance().evilpoint.getEvil((Player) event.getEntity());
            if(ep > 9999){
                modifer = 3;
            } else if(ep > 5000) {
                modifer = 2;
            } else if(ep > 3000) {
                modifer = 1.6;
            } else if(ep > 1000) {
                modifer = 1.4;
            } else if(ep > 500) {
                modifer = 1.2;
            } else if(ep < 300) {
                modifer = 0.9;
            } else if(ep < 100) {
                modifer = 0.7;
            } else if(ep == 0) {
                modifer = 0.5;
            }
        } else {
            int ep = Cupboard.getInstance().evilpoint.getEvil((Player) event.getEntity());
            if(ep > 9999){
                modifer = 3;
            } else if(ep > 5000) {
                modifer = 2;
            }
        }
        if(modifer != 1){
            double damage = event.getFinalDamage() * modifer;
            event.setDamage(DamageModifier.ABSORPTION, 0);
            event.setDamage(DamageModifier.ARMOR, 0);
            event.setDamage(DamageModifier.BLOCKING, 0);
            event.setDamage(DamageModifier.MAGIC, 0);
            event.setDamage(DamageModifier.RESISTANCE, 0);
            event.setDamage(DamageModifier.BASE, damage);
        }
        
        //點數計算
        if(damager != null && event.getFinalDamage() > 0.1){
            evilpoint.plusEvil(damager, (int)Math.ceil(event.getFinalDamage()));
            evilpoint.scoreboardUpdate(damager);
        }
    }
    
    //殺死玩家
    @EventHandler
    public void onPlayerKilledByPlayer(EntityDeathEvent event){
        if(!(event.getEntity() instanceof Player)) return;
        EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
        if(!(damageEvent instanceof EntityDamageByEntityEvent))
            return;
        Player killer = Util.getDamager(((EntityDamageByEntityEvent)damageEvent).getDamager());
        if(killer != null && killer != event.getEntity()){
            if(evilpoint.getEvil(killer) < evilpoint.getEvil((Player) event.getEntity())){
                evilpoint.plusEvil(killer, 10);
            } else {
                evilpoint.plusEvil(killer, 50);
            }
        }
    }
    
    //放置TNT
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTNTPlaced(BlockPlaceEvent event){
        if(event.isCancelled())return;
        if(event.getBlockPlaced().getType() == Material.TNT){
            Player player = event.getPlayer();
            evilpoint.plusEvil(player, 30);
        }
    }
    
    //製作TNT
    @EventHandler
    public void onTNTMaked(InventoryClickEvent event){
        if(event.getClickedInventory() == null)return;
        if(event.getClickedInventory().getType() != InventoryType.WORKBENCH)return;
        if(event.getCurrentItem().getType() != Material.TNT)return;
        //Cupboard.getInstance().log("%s %d",event.getAction().toString(), event.getRawSlot());
        
        if(event.getRawSlot() != 0) return;
        Inventory inv = event.getClickedInventory();
        switch(event.getAction()){
        case PICKUP_ONE:
        case PICKUP_HALF:
        case PICKUP_ALL:
            evilpoint.plusEvil((Player) event.getWhoClicked(), 20);
            break;
        case MOVE_TO_OTHER_INVENTORY:
            int min_craft = 64;
            for(int i=1 ;i<=9; i++){
                if(inv.getItem(i).getAmount() < min_craft)min_craft = inv.getItem(i).getAmount();
            }
            evilpoint.plusEvil((Player) event.getWhoClicked(), 20*min_craft);
            break;
        default:
            break;
        }
    }
}
