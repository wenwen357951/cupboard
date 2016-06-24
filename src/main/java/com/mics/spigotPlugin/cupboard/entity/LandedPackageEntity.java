package com.mics.spigotPlugin.cupboard.entity;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.metadata.FixedMetadataValue;

import com.mics.spigotPlugin.cupboard.Cupboard;

public class LandedPackageEntity extends PackageEntity {

	Location loc;
	World world;
	Material material;
	
	public LandedPackageEntity(Location loc, Material m){
		this.loc = loc;
		world = loc.getWorld();
		material = m;
		
		summon();
	}
	
	@Override
	public void summon() {
		loc.getWorld().getBlockAt(loc).setType(material);
		world.getBlockAt(loc).setMetadata("isPackage", new FixedMetadataValue(Cupboard.getInstance(), true));
		
		tick();
	}
	
	@Override
	public void tick(){
		if (loc.getWorld().getBlockAt(loc).getType() == material){
			
			counter++;
			
			loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc, 1, 0.1, 0.1, 0.1, 0.1);
			
			retick();
		} else {
			
		}
	}

	@Override
	public void remove() {
		
	}

}

