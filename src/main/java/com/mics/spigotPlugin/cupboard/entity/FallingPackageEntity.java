package com.mics.spigotPlugin.cupboard.entity;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import com.mics.spigotPlugin.cupboard.Cupboard;


public class FallingPackageEntity extends PackageEntity {
	
	World world;
	Location startLoc;
	Material material;
	FallingBlock blocky = null;
	
	public FallingPackageEntity(Location loc, Material m){
		
		startLoc = applyOffset(loc);
		world = loc.getWorld();
		material = m;
		
		summon();
	}
	
	@SuppressWarnings("deprecation")
	public void summon() {
		
		blocky = world.spawnFallingBlock(startLoc, material, (byte) 0);
		summonSpawnFireworks();
		
		tick();
	}

	@SuppressWarnings("deprecation")
	public void tick(){
		
		if(world.getBlockAt(blocky.getLocation().clone().add(0, -1, 0)).getType() == Material.AIR){
			
			counter++;
			world.spawnParticle(Particle.SMOKE_NORMAL, blocky.getLocation(), 50, 0.1, 0.1, 0.1, 0.1);
			
			if (blocky.isDead()){
				Location oldLoc = blocky.getLocation();
				Vector oldVelocity = blocky.getVelocity();
				
				blocky = world.spawnFallingBlock(oldLoc, material, (byte) 0);
				blocky.setVelocity(oldVelocity);
			}
			
			if (counter % 5 == 0){
				summonUpdateFireworks();
			}
			
			
			retick();
		} else {
			remove();
		}
	}
	
	@Override
	public void remove() {
		blocky.remove();
		new LandedPackageEntity(blocky.getLocation(), blocky.getMaterial());
	}
	
	private void summonUpdateFireworks(){
		if ( true ){ //fireworks_on_fall
			final Firework fw = (Firework) world.spawnEntity(blocky.getLocation(), EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();
			
            fwm.addEffect(FireworkEffect.builder().with(Type.BALL).withColor(Color.RED).withColor(Color.WHITE).build());
            fw.setFireworkMeta(fwm);
            
            Cupboard.getInstance().getServer().getScheduler().runTaskLater( Cupboard.getInstance(), new Runnable() {

				public void run() {
					fw.detonate();
				}
            	
            }, 1L);
            
		}
	}
	
	private void summonSpawnFireworks(){
		if ( true ){ //firework
            final Firework fw = (Firework) world.spawnEntity(blocky.getLocation(), EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();
			
            fwm.addEffect(FireworkEffect.builder().with(Type.BALL_LARGE).withColor(Color.RED).withColor(Color.WHITE).build());
            fw.setFireworkMeta(fwm);
            
            Cupboard.getInstance().getServer().getScheduler().runTaskLater(Cupboard.getInstance(), new Runnable() {

				public void run() {
					fw.detonate();
				}
            	
            }, 1L);
            
		}
	}
	
	private Location applyOffset(Location loc){
		
		int bounds = 5; //drop_location_offset
		
		if ( bounds < 1 ){
			return loc;
		}
		
		int xOff;
		int zOff;
		Random r = new Random();
		
		zOff = (r.nextInt(bounds * 2) + 1) - bounds;
		xOff = (r.nextInt(bounds * 2) + 1) - bounds;
		
		return loc.add(xOff, 0, zOff);
		
	}
	

}
