package tw.mics.spigot.plugin.cupboard.utils;

import java.util.Arrays;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Config;
import tw.mics.spigot.plugin.cupboard.config.Locales;

public class SpawnLocationManager {
    private static Location last_spawnLocation;
    private static long last_spawnLocationTime = 0;
    
    public static void applyPlayerProtect(Player p){
        if(Config.PP_PLAYER_SPAWN_PROTECT.getBoolean()){
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 3));
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 3));
            p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 600, 3));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 3));
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
        }
    }
    
    public static boolean useNewSpawn(){
        if(getTimeLeft() < 0){
            return true;
        }
        return false;
    }
    
    public static Location getSpawnLocation() {
        return last_spawnLocation;
    }
    
    public static void teleportPlayerToNewSpawn(Player p){
        //傳送到新世界重生點
        new SpawnFinder(p);
    }

    public static Double getTimeLeft(){
        long time_diff = last_spawnLocationTime + Config.PP_PLAYER_RANDOM_SPAWN_NEW_LOCATION_TIME.getInt() * 1000 - System.currentTimeMillis();
        return Double.valueOf(time_diff / 1000.0D);
    }
    
    public static boolean checkPlayerSpawn(Location l, Player p){
        if(l == null || p.getBedSpawnLocation() == null) return false;
        if(l.getWorld() != p.getBedSpawnLocation().getWorld()) return false;
        Double dist = l.distance(p.getBedSpawnLocation());
        if(dist <= 2.24)
            return true;
        return false;
    }
    
    private static Material[] blockBlockList = {
            Material.STATIONARY_LAVA,
            Material.LAVA,
            Material.STATIONARY_WATER,
            Material.WATER,
    };
    
    private static Biome[] blockBiomeList = {
            Biome.OCEAN,
            Biome.DEEP_OCEAN,
            Biome.FROZEN_OCEAN,
            Biome.RIVER,
    };
    
    private static class SpawnFinder implements Runnable{
        World world;
        Player player;
        double max_distance;
        double center_x;
        double center_z;
        Location location;
        int id;

        @Override
        public void run() {
            if(findNext()){
                Cupboard.getInstance().getServer().getScheduler().cancelTask(id);
                player.teleport(location);
                player.sendMessage(Locales.BED_WORLD_SPAWN_UPDATED.getString());
                world.setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                last_spawnLocationTime = System.currentTimeMillis();
                
                location.add(0.5, 0, 0.5);
                last_spawnLocation = location;
            }
        }
        
        SpawnFinder(Player p){
            player = p;
            world = Cupboard.getInstance().getServer().getWorld(Config.PP_PLAYER_RANDOM_SPAWN_WORLD.getString());
            WorldBorder wb = world.getWorldBorder();
            max_distance = wb.getSize();
            center_x = wb.getCenter().getX();
            center_z = wb.getCenter().getZ();
            id = Cupboard.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Cupboard.getInstance(),
                    this, 0, 0);
        }
        
        boolean findNext(){
            location = world.getHighestBlockAt( (int)(center_x + getRandom(max_distance)), (int)(center_z + getRandom(max_distance))).getLocation();
            if(Arrays.asList(blockBiomeList).contains(location.getBlock().getBiome())) return false;
            if(Arrays.asList(blockBlockList).contains(location.getBlock().getType())) return false;
            return true;
        }
        private int getRandom(double max) {
            return getRandom((int)max);
        }
        private int getRandom(int max){
            return new Random().nextInt(max + 1) - (max / 2);
        }
        
    }
    
}