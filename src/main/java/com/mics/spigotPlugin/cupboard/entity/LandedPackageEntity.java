package com.mics.spigotPlugin.cupboard.entity;

import java.util.ArrayList;
import java.util.List;
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
	static List<LandedPackageEntity> landedPackageEntities;
	
	public LandedPackageEntity(Location loc, Material m){
		this.loc = loc;
		world = loc.getWorld();
		material = m;
		
		if(landedPackageEntities == null){
			landedPackageEntities = new ArrayList<LandedPackageEntity>();
		}
		landedPackageEntities.add(this);
		
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
		}
	}
	
	public void remove() {
		loc.getBlock().setType(Material.AIR);
	}

	public static void removeAll() {
		if(landedPackageEntities == null) return;
		for(LandedPackageEntity e : landedPackageEntities){
			e.remove();
		}
	}
	
	

}

