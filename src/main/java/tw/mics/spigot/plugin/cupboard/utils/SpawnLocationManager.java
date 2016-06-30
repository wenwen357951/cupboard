package tw.mics.spigot.plugin.cupboard.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import tw.mics.spigot.plugin.cupboard.config.Config;

public class SpawnLocationManager {
    public static Location last_spawnLocation;
    public static long last_spawnLocationTime = 0;

    static List<Biome> biomeBlockList;
    static List<Material> blockBlockList;
    
    boolean ENABLE = true;
    int RESET_SEC = 180;
    
    public static void init(){
        biomeBlockList = new ArrayList<Biome>();
        biomeBlockList.add(Biome.OCEAN);
        biomeBlockList.add(Biome.DEEP_OCEAN);
        biomeBlockList.add(Biome.RIVER);
        
        blockBlockList = new ArrayList<Material>();
        blockBlockList.add(Material.STATIONARY_LAVA);
        blockBlockList.add(Material.LAVA);
        blockBlockList.add(Material.STATIONARY_WATER);
        blockBlockList.add(Material.WATER);
        blockBlockList.add(Material.LEAVES);
        blockBlockList.add(Material.LEAVES_2);
        blockBlockList.add(Material.LOG);
        blockBlockList.add(Material.LOG_2);
    }
    
    public static Double getTimeLeft(){
        long time_diff = last_spawnLocationTime + Config.PP_PLAYER_RANDOM_SPAWN_NEW_LOCATION_TIME.getInt() * 1000 - System.currentTimeMillis();
        return Double.valueOf(time_diff / 1000.0D);
    }

    public static Location getSpawnLocation() {
        return last_spawnLocation;
    }

    public static boolean checkNewSpawnLocation() {
        long time_diff = last_spawnLocationTime + Config.PP_PLAYER_RANDOM_SPAWN_NEW_LOCATION_TIME.getInt() * 1000 - System.currentTimeMillis();
        if( time_diff < 0){
            last_spawnLocation = findNewSpawnLocation(Config.PP_PLAYER_RANDOM_SPAWN_WORLD.getString());
            last_spawnLocationTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }
    
    public static boolean checkPlayerSpawn(Location l, Player p){
        if(l == null || p.getBedSpawnLocation() == null) return false;
        Double dist = l.distance(p.getBedSpawnLocation());
        if(dist <= 2.24)
            return true;
        return false;
    }
    
    private static Location findNewSpawnLocation(String string) {
        return findNewSpawnLocation(Bukkit.getServer().getWorld(string));
    }

    private static Location findNewSpawnLocation(World world){
        WorldBorder wb = world.getWorldBorder();
        double max_distance = 10000;
        double center_x = wb.getCenter().getX();
        double center_z = wb.getCenter().getZ();

        if(max_distance > wb.getSize()) max_distance = wb.getSize();
        
        Location location;
        while(true){
            location = world.getHighestBlockAt( (int)(center_x + getRandom(max_distance)), (int)(center_z + getRandom(max_distance))).getLocation();
            if(biomeBlockList.contains(location.getBlock().getBiome())) continue;
            if(blockBlockList.contains(location.getBlock().getType())) continue;
            break;
        }
        return location;
    }
    
    private static int getRandom(int max){
        return new Random().nextInt(max + 1) - (max / 2);
    }
    
    private static int getRandom(double max) {
        return getRandom((int)max);
    }
}
