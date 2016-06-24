package com.mics.spigotPlugin.cupboard.entity;

import com.mics.spigotPlugin.cupboard.Cupboard;

public class PackageEntity {
	
	protected int counter = 0;
	
	protected void summon(){
		
	}
	
	public void remove(){
		
	}
	
	protected void tick(){
		
	}
	
	protected void retick(){
		
		Cupboard.getInstance().getServer().getScheduler().runTaskLater(Cupboard.getInstance(), new Runnable() {
			
			public void run() {
				tick();
			}
			
		}, 5L);
	}
}
