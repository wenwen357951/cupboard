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
                //TODO Finder
                sender.sendMessage("此功能尚未完成.");
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
                            plugin.cupboards.giveAcceee(sender_uuid, uuid);
                            sender.sendMessage(ChatColor.GREEN + "已將所有已授權金磚賦予給" + Bukkit.getPlayer(UUID.fromString(uuid)).getDisplayName());
                            Player receiver = Bukkit.getPlayer(UUID.fromString(uuid));
                            receiver.sendMessage(ChatColor.GREEN + "您收到來自 " + ((Player)sender).getDisplayName() + " 的已授權金磚賦予");
                            
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
                sender.sendMessage(ChatColor.GREEN + "確定要將所有已授權金磚賦予給 " + player.getDisplayName() + " ?");
                sender.sendMessage(ChatColor.GREEN + "如果確定賦予請在 30 秒內輸入 /gold confirm");
                return true;
            }
        }
        
        sender.sendMessage(ChatColor.GOLD + "金磚管理指令:");
        sender.sendMessage(ChatColor.YELLOW + "/gold give <player>  - 把所有已授權金磚賦予該玩家");
        sender.sendMessage(ChatColor.YELLOW + "/gold nearest        - 找出  20 格內已授權金磚");
        return true;
    }

}
