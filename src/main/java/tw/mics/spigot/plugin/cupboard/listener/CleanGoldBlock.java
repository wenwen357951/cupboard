package tw.mics.spigot.plugin.cupboard.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;

import tw.mics.spigot.plugin.cupboard.Cupboard;

public class CleanGoldBlock extends MyListener {
    public CleanGoldBlock(Cupboard instance) {
        super(instance);
    }
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event){
        if(event.isNewChunk()){
            Bukkit.getScheduler().runTaskAsynchronously(Cupboard.getInstance(), new Runnable(){
                @Override
                public void run() {
                    plugin.cupboards.cleanNotExistCupboard(event.getChunk().getChunkSnapshot());
                }
            });
        }
    }
}
