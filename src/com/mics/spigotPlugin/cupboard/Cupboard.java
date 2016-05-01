package com.mics.spigotPlugin.cupboard;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.mics.spigotPlugin.cupboard.command.EscCommand;

import net.milkbowl.vault.permission.Permission;


public class Cupboard extends JavaPlugin implements Listener {
	public Data data;
	@Override
	public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        
        //處理資料庫
        data = new Data(getDataFolder(),this);

        //設定允許觸發授權的阻擋方塊
        setUpAllowFaceBlock();
        //設定保護的entity
        setUpProtectEntity();
        
        setUpVipProtect();
        
        setupPermissions();
        
        //register command
        this.getCommand("esc").setExecutor(new EscCommand(this));
    }
	
    @Override
    public void onDisable() {

    }

    //移除金磚
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGoldBlockBreak(BlockBreakEvent event){
    	if(event.isCancelled())return;
    	if(event.getBlock().getType() == Material.GOLD_BLOCK){
    		Player p = event.getPlayer();
    		data.removeCupboard(event.getBlock());
    		Util.msgToPlayer(p, "金磚已拆除");
    	}
    }
    
    //放置金磚
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGoldBlockPlace(BlockPlaceEvent event){
    	if(event.isCancelled())return;
    	if(event.getBlockPlaced().getType() == Material.GOLD_BLOCK){
    		Player p = event.getPlayer();
    		if(!data.putCupboard(event.getBlockPlaced(), p)){
    			Util.msgToPlayer(p, "距離其他金磚太近。");
    			event.setCancelled(true);
    			return;
    		}
    		Util.msgToPlayer(p, "金磚已放置並取得授權。(蹲下右鍵取得說明)");
    	}
    }
    ArrayList<Material> allow_face_block;
    private void setUpAllowFaceBlock(){
    	allow_face_block = new ArrayList<Material>();
    	allow_face_block.add(Material.AIR);
    	allow_face_block.add(Material.TORCH);
    	allow_face_block.add(Material.REDSTONE_TORCH_OFF);
    	allow_face_block.add(Material.REDSTONE_TORCH_ON);
    	allow_face_block.add(Material.LONG_GRASS);
    	allow_face_block.add(Material.LEVER);
    	allow_face_block.add(Material.STONE_BUTTON);
    	allow_face_block.add(Material.WOOD_BUTTON);
    	allow_face_block.add(Material.REDSTONE_WIRE);
    }

    ArrayList<Material> protect_vehicle;
    private void setUpProtectEntity(){
    	protect_vehicle = new ArrayList<Material>();
    	protect_vehicle.add(Material.ARMOR_STAND);
    	protect_vehicle.add(Material.BOAT);
    	protect_vehicle.add(Material.MINECART );
    	protect_vehicle.add(Material.COMMAND_MINECART );
    	protect_vehicle.add(Material.EXPLOSIVE_MINECART );
    	protect_vehicle.add(Material.HOPPER_MINECART );
    	protect_vehicle.add(Material.POWERED_MINECART );
    	protect_vehicle.add(Material.STORAGE_MINECART );
    }
    
    ArrayList<Material> vip_protect_block;
    private void setUpVipProtect(){
    	vip_protect_block = new ArrayList<Material>();
    	vip_protect_block.add(Material.CHEST);
    	vip_protect_block.add(Material.TRAPPED_CHEST);
    	vip_protect_block.add(Material.FURNACE);
    	vip_protect_block.add(Material.BURNING_FURNACE);
    	vip_protect_block.add(Material.JUKEBOX);
    	vip_protect_block.add(Material.BREWING_STAND);
    	vip_protect_block.add(Material.ANVIL);
    	vip_protect_block.add(Material.DROPPER);
    	vip_protect_block.add(Material.DISPENSER);
    	vip_protect_block.add(Material.HOPPER);
    }

    public Permission perms;
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    
    //授權/取消授權
    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event){
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;                    		// off hand packet, ignore.
    	if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.GOLD_BLOCK) return;       	// 非黃金磚則無視
    	if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return; 						// 非右鍵方塊則無視
        //if (event.getItem() != null && event.getItem().getType().isBlock()) return;   	// 非空手則無視
    	Location front_block_loc = event.getClickedBlock().getLocation().clone();
		Player p = event.getPlayer();
    	switch(event.getBlockFace()){
    	case EAST:
    		front_block_loc.add(1,0,0);
    		break;
    	case WEST:
    		front_block_loc.add(-1,0,0);
    		break;
    	case SOUTH:
    		front_block_loc.add(0,0,1);
    		break;
    	case NORTH:
    		front_block_loc.add(0,0,-1);
    		break;
    	case UP:
    		front_block_loc.add(0,1,0);
    		break;
    	case DOWN:
    		front_block_loc.add(0,-1,0);
    		break;
		default:
			break;
    	}
    	if(!allow_face_block.contains(front_block_loc.getBlock().getType())){
    		p.sendMessage("被擋住了，無法授權/取消授權。");
    		return;
    	}
    	
		if (p.isSneaking()){
			//說明
			p.sendMessage("§a金磚放置後產生19x19x19的方形保護區，金磚在正中央。");
			p.sendMessage("§a保護區只防止下列事項: ");
			p.sendMessage("§71. 未授權者無法放置/移除方塊。");
			p.sendMessage("§72. 未授權者無法使用石製壓力版/石製按鈕。");
			p.sendMessage("§71. 怪物/動物無法觸發石製壓力版");
			p.sendMessage("§73. 防止任何種類爆炸炸掉保護區。");
			p.sendMessage("§73. 防止黃金磚被活塞推動。");
			p.sendMessage("§c特別警告以下為可執行事件: ");
			p.sendMessage("§72. 使用木製門/按鈕/壓力版/箱子/控制桿/活塞/柵欄門");
			p.sendMessage("§73. 打掉方塊使上方會掉落之方塊掉落");
			return;
		}
		String str;
		if(!data.checkCupboardExist(event.getClickedBlock())){
			str="此金磚非由玩家放置或資料遺失，請拆除後重新放置";
		} else if(data.toggleBoardAccess(p, event.getClickedBlock())){
			str="已授權";
		} else {
			str="已取消授權";
		}
		event.setCancelled(true);
		p.updateInventory();
		Util.msgToPlayer(p, str);
    }
    
    //==========以下為保護措施=========
    
    
    //防止 VIP 箱子/熔爐/漏斗/製藥水裝置被開啟
    @EventHandler
    public void onVipBlockUsed(PlayerInteractEvent e){
    	if(
			e.getAction() == Action.RIGHT_CLICK_BLOCK &&
			vip_protect_block.contains(e.getClickedBlock().getType())
		){
        	if (data.checkIsLimitOffline(e.getClickedBlock(), e.getPlayer())){
        		if(this.isOP(e.getPlayer()))return;
        		e.getPlayer().sendMessage("§4沒有權限 §7(VIP離線保護)");
        		e.setCancelled(true);
        	}
    	}
    }
    
    //防止 VIP 裝備架被使用
    @EventHandler
    public void onVipArmorStandUsed(PlayerInteractAtEntityEvent e){
    	if(
			e.getRightClicked().getType() == EntityType.ARMOR_STAND
		){
        	if (data.checkIsLimitOffline(e.getRightClicked().getLocation().getBlock(), e.getPlayer())){
        		if(this.isOP(e.getPlayer()))return;
        		e.getPlayer().sendMessage("§4沒有權限 §7(VIP離線保護)");
        		e.setCancelled(true);
        	}
    	}
    }
    
    //防止 VIP 物品展示框被放置物品
    @EventHandler
    public void onVipFrameUsed(PlayerInteractEntityEvent e){
    	if(
    			e.getRightClicked().getType() == EntityType.ITEM_FRAME
		){
        	if (data.checkIsLimitOffline(e.getRightClicked().getLocation().getBlock(), e.getPlayer())){
        		if(this.isOP(e.getPlayer()))return;
        		e.getPlayer().sendMessage("§4沒有權限 §7(VIP離線保護)");
        		e.setCancelled(true);
        	}
    	}
    }
    
    //防止 VIP 物品展示框被移除物品
    @EventHandler
    public void onVipFrameRemove(EntityDamageByEntityEvent e){
    	if(
			e.getEntity().getType() == EntityType.ITEM_FRAME &&
			e.getDamager() instanceof Player
		){
    		Player p = (Player) e.getDamager();
        	if (data.checkIsLimitOffline(e.getEntity().getLocation().getBlock(), p)){
        		if(this.isOP(p))return;
        		p.sendMessage("§4沒有權限 §7(VIP離線保護)");
        		e.setCancelled(true);
        	}
    	}
    }

    //防止船隻/礦車/盔甲架被放置
    @EventHandler
    public void onBoatPlace(PlayerInteractEvent e){
    	if (
			e.getAction() == Action.RIGHT_CLICK_BLOCK &&
			e.getItem() != null &&
			protect_vehicle.contains(e.getItem().getType())
		){
    	Player p = e.getPlayer();
	    	if (data.checkIsLimit(e.getClickedBlock(), p)){
	    		if(this.isOP(p))return;
	    		e.setCancelled(true);
	    		e.getPlayer().updateInventory();
	    	}
    	}
    }
    
    //防止盔甲架被移除
    @EventHandler
    public void onArmorStandDamage(EntityDamageByEntityEvent e){
    	if (e.getEntity().getType() != EntityType.ARMOR_STAND) return;
    	if (e.getDamager() instanceof Player){
    		Player p = (Player) e.getDamager();
        	if (data.checkIsLimit(e.getEntity().getLocation().getBlock(), p)){
        		if(this.isOP(p))return;
        		e.setCancelled(true);
        	}
    	}
    }
    
    //防止Armor stand被炸毀
    @EventHandler
    public void onArmorStandExplosion(EntityDamageEvent e){
    	if(
    		e.getEntity().getType() == EntityType.ARMOR_STAND &&
    		( 
				e.getCause() == DamageCause.BLOCK_EXPLOSION ||
				e.getCause() == DamageCause.ENTITY_EXPLOSION
    		) &&
    		data.checkIsLimit(e.getEntity().getLocation().getBlock())
		){
    		e.setCancelled(true);
    	}
    }
    
    //防止Hanging類物品被未授權玩家移除 / 被Creeper炸掉 / 被TNT炸掉
    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent e) {
    	//NEEDFIX -- TNT LIGHT BY ALLOW USER WILL DESTORY HANGING ITEM
		Location bl = e.getEntity().getLocation().getBlock().getLocation();
    	if (e.getRemover() instanceof Player){
    		Player p = (Player) e.getRemover();
    		if(data.checkIsLimit(bl, p)){
    			if(this.isOP(p)) return;
    			e.setCancelled(true);
    		}
    	} else {
    		if(e.getCause() == RemoveCause.ENTITY){ //by creeper
    			if(data.checkIsLimit(bl)) e.setCancelled(true);
    		}
    	}
	}
    
    @EventHandler
    public void onHangingBreak(HangingBreakEvent e) {
    	Location bl = e.getEntity().getLocation().getBlock().getLocation();
		if(e.getCause() == RemoveCause.EXPLOSION){ //by TNT
			if(data.checkIsLimit(bl)) e.setCancelled(true);
		}
    }
    

    //防止Hanging類物品被未授權玩家放置
    @EventHandler
    public void onHangingPlace(HangingPlaceEvent e) {
		Location bl = e.getEntity().getLocation().getBlock().getLocation();
		Player p = e.getPlayer();
		if(data.checkIsLimit(bl, p)){
			if(this.isOP(p)) return;
			e.setCancelled(true);
			e.getPlayer().updateInventory();
		}
	}
    
    //禁止使用石製開關 以及 石製踏板
      @EventHandler
      public void onUseStoneButton(PlayerInteractEvent event){
      	Block b = event.getClickedBlock();
      	Player p = event.getPlayer();
      	if (
      			event.getAction() == Action.RIGHT_CLICK_BLOCK && 
  				b.getType() == Material.STONE_BUTTON
		) 
      		if(data.checkIsLimit(b, p)){
    			if(this.isOP(p)) return;
      			event.setCancelled(true);
      		}
      	
      }
      
      //禁止玩家使用石製踏板
        @EventHandler
        public void onUseStonePlate(PlayerInteractEvent event){
        	Block b = event.getClickedBlock();
        	Player p = event.getPlayer();
        	if (
        			event.getAction() == Action.PHYSICAL &&
        			b.getType() == Material.STONE_PLATE
			){
        		if(data.checkIsLimit(b, p)){
        			if(this.isOP(p)) return;
        			event.setCancelled(true);
        		}
        	}
        }

    //禁止動物/怪物使用石製踏板 (玩家騎在動物上則增加玩家權限判斷
    @EventHandler
    public void onEntryUseStonePlate(EntityInteractEvent event){
    	Block b = event.getBlock();
    	if( b.getType() == Material.STONE_PLATE ){
        	Entity e = event.getEntity();
        	if (e.getPassenger() instanceof Player){
        		Player p = (Player) e.getPassenger();
        		if(data.checkIsLimit(b, p)){
        			if(this.isOP(p)) return;
        			event.setCancelled(true);
        		}
        	} else {
        		if(data.checkIsLimit(b)) event.setCancelled(true);
        	}
    	}
    }
    //防止其他玩家破壞方塊
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e){
    	Player p = e.getPlayer();
    	Block b = e.getBlock();
        if( p != null){
        	if(data.checkIsLimit(b, p)){
        		if(this.isOP(p))return;
        		e.setCancelled(true);
    			p.sendMessage("§4沒有權限 §7(被關住了? 試試 /esc)");
        	}
        } else {
        	if(data.checkIsLimit(b)){
        		e.setCancelled(true);
        	}
        }
    }
    //防止其他玩家放置方塊
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e){
    	Player p = e.getPlayer();
    	Block b = e.getBlock();
        if( p != null){
        	if(data.checkIsLimit(b, p)){
            	if(this.isOP(p))return;
        		e.setCancelled(true);
    			p.sendMessage("§4沒有權限 §7(被關住了? 試試 /esc)");
        	}
        } else {
        	if(data.checkIsLimit(b))
        		e.setCancelled(true);
        }
    }
    
    //防止除玩家之外之物件透過地獄門傳送
    @EventHandler
    public void onEntityPortal(EntityPortalEvent e){
		e.setCancelled(true);
    }
    
    
    //防止玩家擋住地獄門
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockNetherDoor(BlockPlaceEvent e){
    	Player p = e.getPlayer();
    	Block b = e.getBlock();
    	if(
    			b.getLocation().add(1,0,0).getBlock().getType() == Material.PORTAL ||
    			b.getLocation().add(-1,0,0).getBlock().getType() == Material.PORTAL ||
    			b.getLocation().add(0,0,1).getBlock().getType() == Material.PORTAL ||
    			b.getLocation().add(0,0,-1).getBlock().getType() == Material.PORTAL
    			){
	        if( p != null && b.getType().isSolid()){
	        	if(b.getType() == Material.WOOD_PLATE) return;
	        	if(b.getType() == Material.STONE_PLATE) return;
	        	if(b.getType() == Material.IRON_PLATE) return;
	        	if(b.getType() == Material.GOLD_PLATE) return;
        		p.sendMessage("§7請勿將地獄門堵死");
        		e.setCancelled(true);
	    	}
    	}
        
    }
    
    //防止玩家使用水桶
    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent e){
    	Player p = e.getPlayer();
    	Block b = e.getBlockClicked().getLocation()
    			.add(e.getBlockFace().getModX(),e.getBlockFace().getModY(),e.getBlockFace().getModZ())
    			.getBlock();
        if( p != null){
        	if(data.checkIsLimit(b, p)){
        		e.setCancelled(true);
        		p.updateInventory();
        	}
        }
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketFill(PlayerBucketFillEvent e){
    	Player p = e.getPlayer();
    	Block b = e.getBlockClicked();
        if( p != null){
        	if(data.checkIsLimit(b, p)){
        		e.setCancelled(true);
        		p.updateInventory();
        	}
        }
    }
    
    //TNT or Creeper爆炸
    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplode(EntityExplodeEvent event){
    	for (Block block : new ArrayList<Block>(event.blockList())){
    		if(data.checkIsLimit(block)){
				event.blockList().remove(block);
    		}
    	}
    }
    
    //模組爆炸
    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplode(BlockExplodeEvent event){
    	for (Block block : new ArrayList<Block>(event.blockList())){
    		if(data.checkIsLimit(block)){
    			event.blockList().remove(block);
    		}
    	}
    }
    
    // 防止方塊被燒壞
    // TODO 火焰不會消失
    @EventHandler
    void onBlockBurnDamage(BlockBurnEvent e){
    	Block b = e.getBlock();
    	if(data.checkIsLimit(b)){
    		e.setCancelled(true);
    		Location l = e.getBlock().getLocation();
    		if(Math.random() < 0.1){ //當物品要損壞時10%機率火焰消失
    			if(l.add(1,0,0).getBlock().getType().equals(Material.FIRE))
    				l.getBlock().setType(Material.AIR);
    			if(l.add(-2,0,0).getBlock().getType().equals(Material.FIRE))
    				l.getBlock().setType(Material.AIR);
    			if(l.add(1,1,0).getBlock().getType().equals(Material.FIRE))
    				l.getBlock().setType(Material.AIR);
    			if(l.add(0,-2,0).getBlock().getType().equals(Material.FIRE))
    				l.getBlock().setType(Material.AIR);
    			if(l.add(0,1,1).getBlock().getType().equals(Material.FIRE))
    				l.getBlock().setType(Material.AIR);
    			if(l.add(0,0,-2).getBlock().getType().equals(Material.FIRE))
    				l.getBlock().setType(Material.AIR);
    		}
    	}
    }

    @EventHandler
    void onFireSpread(BlockSpreadEvent e){
    	if(e.getSource().getType() != Material.FIRE) return;
    	if(data.checkIsLimit(e.getBlock())){
    		e.getSource().setType(Material.AIR);
    	}
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    void onCupboardPiston(BlockPistonExtendEvent e){
    	for(Block block : e.getBlocks()){
    		if(block.getType().equals(Material.GOLD_BLOCK)){
    			e.setCancelled(true);
    		}
    	}
    }
    
    boolean isOP(Player p){
    	if(p.isOp() && p.getGameMode() == GameMode.CREATIVE){
    		p.sendMessage("§c警告: 管理員權限已忽視保護區");
    		return true;
    	}
		return false;
    }
    
    
}
