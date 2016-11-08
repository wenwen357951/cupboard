package tw.mics.spigot.plugin.cupboard.command;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import tw.mics.spigot.plugin.cupboard.Cupboard;

public class GoldCommand implements CommandExecutor {
	Cupboard plugin;
	HashMap<String, String> confirm_list;
	public GoldCommand(Cupboard i){
		this.plugin = i;
		confirm_list = new HashMap<String, String>();
	}

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§4this command must run on player");
            return true;
        }
        String sender_uuid = ((Player)sender).getUniqueId().toString();
        if(args.length > 0){
            //-----------nearest-----------
            if(args[0].equals("nearest") && args.length == 1){
                sender.sendMessage(ChatColor.GREEN + "開始尋找...");
                plugin.cupboards.findNearest((Player) sender);
                return true;
            }

            Boolean have_confirm = confirm_list.containsKey(sender_uuid);
            
            //-----------confirm-----------
            if(args[0].equals("confirm") && args.length == 1){
                if(!have_confirm){
                    sender.sendMessage(ChatColor.RED + "沒有任何確認");
                    return true;
                }
                String[] action_string = confirm_list.get(sender_uuid).split(",");
                confirm_list.remove(sender_uuid);
                if(action_string[0].equals("give")){
                    sender.sendMessage(ChatColor.GREEN + "開始執行授權賦予動作...");
                    int sid = Integer.parseInt(action_string[1]);
                    String uuid = action_string[2];
                    Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
                        @Override
                        public void run() {
                            Bukkit.getScheduler().cancelTask(sid);
                            plugin.cupboards.giveAcceee(sender_uuid, uuid, ((Player)sender).getLocation().clone());
                            sender.sendMessage(ChatColor.GREEN + "已經把附近 ±200 格內已授權金磚賦予給 " + Bukkit.getPlayer(UUID.fromString(uuid)).getDisplayName());
                            Player receiver = Bukkit.getPlayer(UUID.fromString(uuid));
                            receiver.sendMessage(ChatColor.GREEN + ((Player)sender).getDisplayName() + " 已經把附近 ±200 格內已授權金磚賦予給您");
                            
                        }
                    });
                    return true;
                }
            }
            
            
    
            //-----------give-----------
            if(args[0].equals("give") && args.length == 2){
                if(have_confirm){
                    sender.sendMessage(ChatColor.RED + "您還有確認尚未完成, 無法使用需要確認的指令");
                    sender.sendMessage(ChatColor.RED + "請等待確認失效或執行確認.");
                    return true;
                }
                Player player = Bukkit.getPlayer(args[1]);
                if(player == null){
                    sender.sendMessage(ChatColor.RED + "該玩家目前不在線上");
                    return true;
                }
                if(player == sender){
                    sender.sendMessage(ChatColor.RED + "無法賦予給自己");
                    return true;
                }
                Location giver_location = ((Player)sender).getLocation();
                Location receiver_location = player.getLocation();
                if(
                        giver_location.getWorld() != receiver_location.getWorld() || 
                        giver_location.distance(receiver_location) > 20
                ){
                    sender.sendMessage(ChatColor.RED + "賦予需要雙方在 20 格內才能進行");
                    return true;
                }
                int sid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
                    @Override
                    public void run() {
                        confirm_list.remove(sender_uuid);
                        sender.sendMessage(ChatColor.RED + "確認已失效");
                    }
                }, 600);
                confirm_list.put(sender_uuid, args[0] + "," + String.valueOf(sid) + "," + player.getUniqueId().toString());
                sender.sendMessage(ChatColor.GREEN + "即將把附近 ±200 格內已授權金磚賦予給 " + player.getDisplayName() + ", 確定要執行?");
                sender.sendMessage(ChatColor.GREEN + "如果確定執行請在 30 秒內輸入 /gold confirm");
                sender.sendMessage(ChatColor.RED + "警告: 這並不會讓自己的授權消失, 對方將擁有完整授權, 此動作無法取消.");
                return true;
            }
        }
        
        sender.sendMessage(ChatColor.GOLD + "金磚管理指令:");
        sender.sendMessage(ChatColor.YELLOW + "/gold give <player>  - 把附近 ±200 格內已授權金磚賦予該玩家");
        sender.sendMessage(ChatColor.YELLOW + "/gold nearest        - 找出附近 ±20 格內最近的已授權金磚");
        return true;
    }

}
