package de.howaner.FakeMobs.util;

import java.util.UUID;

import org.bukkit.entity.EntityType;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;

public class DataWatchCreator {

	static Serializer serialString = Registry.get(String.class);
	static Serializer serialByte = Registry.get(Byte.class);
	static Serializer serialFloat = Registry.get(Float.class);
	static Serializer serialShort = Registry.get(Short.class);
	static Serializer serialInt = Registry.get(Integer.class);
	static Serializer serialBool = Registry.get(Boolean.class);
	static Serializer serialUUID = Registry.get(UUID.class);
	
	public static WrappedDataWatcher createDefaultWatcher(FakeMob mob) {		
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		watcher = addEntityDefaults(watcher, mob.getType());

		//Custom Name:
		if (mob.getCustomName() != null && !mob.getCustomName().isEmpty()) {
			watcher.setObject(new WrappedDataWatcherObject(3, serialBool), true);
			watcher.setObject(new WrappedDataWatcherObject(2, serialString), mob.getCustomName());
		} 

		//Sitting:
		if (mob.isSitting()) {
			if (mob.getType() == EntityType.PLAYER) {
				watcher.setObject(new WrappedDataWatcherObject(0, serialByte), (byte) 0x02);
			} else {
				watcher.setObject(new WrappedDataWatcherObject(12, serialByte), (byte) 0x01);
			}
		}
				
		if (mob.getType() == EntityType.PLAYER){
			//isLayering
			if (mob.isLayering()){
				watcher.setObject(new WrappedDataWatcherObject(12, serialByte), (byte) 0x7F); //Skin flag
			} 
			
			//isGliding:
			if (mob.isGliding()) {
				watcher.setObject(new WrappedDataWatcherObject(0, serialByte), (byte) 0x80);
			}
		}
			

		return watcher;
	}

	public static WrappedDataWatcher addEntityDefaults(WrappedDataWatcher watcher, EntityType type) {
		// Add EntityLiving defaults:
		watcher.setObject(new WrappedDataWatcherObject(0, serialByte), (byte) 0); //Entity options (like invisibility)
		watcher.setObject(new WrappedDataWatcherObject(7, serialInt), 0); //Potion effect color
		watcher.setObject(new WrappedDataWatcherObject(8, serialBool), false); //Is potion effect active?
		watcher.setObject(new WrappedDataWatcherObject(9, serialInt), 0); //Number of Arrows
		watcher.setObject(new WrappedDataWatcherObject(6, serialFloat), (float) 1.0f); //Health

		// Add EntityInsentient defaults:
		if (type != EntityType.PLAYER) {
			watcher.setObject(new WrappedDataWatcherObject(3, serialBool), false); //Custom Name Visible (Minecraft 1.9)
			watcher.setObject(new WrappedDataWatcherObject(2, serialString), ""); //Custom Name (Minecraft 1.9)
		}

		switch (type) {
			case BAT:
				watcher.setObject(new WrappedDataWatcherObject(11, serialByte), (byte) 0); //Is Hanging?
				break;
			case BLAZE:
				watcher.setObject(new WrappedDataWatcherObject(11, serialByte), (byte) 0); //On fire
				break;
			case SPIDER:
			case CAVE_SPIDER:
				watcher.setObject(new WrappedDataWatcherObject(11, serialByte), (byte) 0); //In climbing?
				break;
			case CHICKEN:
				break;
			case CREEPER:
				watcher.setObject(new WrappedDataWatcherObject(11, serialInt), -1); //1 = Fuse, -1 = idle
				watcher.setObject(new WrappedDataWatcherObject(12, serialBool), false); //Is Powered
				break;
			case MUSHROOM_COW:
			case COW:
				break;
			case ENDERMAN:
				watcher.setObject(new WrappedDataWatcherObject(11, serialInt), 0); //Carried Block Data
				watcher.setObject(new WrappedDataWatcherObject(12, serialBool), false); //Is screaming?
				break;
			case ENDER_DRAGON:
				break;
			case GHAST:
				watcher.setObject(new WrappedDataWatcherObject(11, serialBool), false); //Is attacking?
				break;
			case GIANT:
				break;
			case HORSE:
				watcher.setObject(new WrappedDataWatcherObject(12, serialByte), (byte) 0);
				watcher.setObject(new WrappedDataWatcherObject(13, serialInt), 0); //Type: Horse
				watcher.setObject(new WrappedDataWatcherObject(14, serialInt), 0); //Color: White
				watcher.setObject(new WrappedDataWatcherObject(15, serialUUID), null); //Owner UUID
				watcher.setObject(new WrappedDataWatcherObject(19, serialInt), 0); //No Armor
				break;
			case IRON_GOLEM:
				watcher.setObject(new WrappedDataWatcherObject(11, serialByte), (byte) 0); //Is the iron golem from a player created?
				break;
			case SLIME:
			case MAGMA_CUBE:
				watcher.setObject(new WrappedDataWatcherObject(11, serialInt),  1); //Slime size 1
				break;
			case OCELOT:
				watcher.setObject(new WrappedDataWatcherObject(14, serialInt), 0); //Ocelot Type
				break;
			case PIG:
				watcher.setObject(new WrappedDataWatcherObject(12, serialBool), false); //Has saddle ?
				break;
			case PIG_ZOMBIE:
			case ZOMBIE:
				watcher.setObject(new WrappedDataWatcherObject(11, serialBool), false); //Is baby?
				watcher.setObject(new WrappedDataWatcherObject(12, serialInt), 0); //Is villager?
				watcher.setObject(new WrappedDataWatcherObject(13, serialBool), false); //Is converting?
				watcher.setObject(new WrappedDataWatcherObject(14, serialBool), false); //Is hands up?
				break;
			case PLAYER:				
				watcher.setObject(new WrappedDataWatcherObject(10, serialFloat), 0.0f); //Absorption hearts
				watcher.setObject(new WrappedDataWatcherObject(11, serialInt), 0); //Score
				break;
			case SHEEP:
				watcher.setObject(new WrappedDataWatcherObject(12, serialByte), (byte) 0); //Color
				break;
			case SILVERFISH:
				break;
			case SKELETON:
				watcher.setObject(new WrappedDataWatcherObject(11, serialInt), 0); //Type. 0 = Normal, 1 = Wither
				break;
			case SNOWMAN:
				break;
			case VILLAGER:
				watcher.setObject(new WrappedDataWatcherObject(12, serialInt), 1); //Type
				break;
			case WITCH:
				watcher.setObject(new WrappedDataWatcherObject(11, serialBool), false); //Is agressive?
				break;
			case WITHER:
				watcher.setObject(new WrappedDataWatcherObject(11, serialInt), 0);
				watcher.setObject(new WrappedDataWatcherObject(12, serialInt), 0);
				watcher.setObject(new WrappedDataWatcherObject(13, serialInt), 0);
				watcher.setObject(new WrappedDataWatcherObject(14, serialInt), 0);
				break;
			case WOLF:
				//watcher.setObject(new WrappedDataWatcherObject(14, serialFloat), 20.0f); //Damage taken
				watcher.setObject(new WrappedDataWatcherObject(15, serialBool), false); //Begging
				watcher.setObject(new WrappedDataWatcherObject(26, serialInt), 14); //Collar color
				break;
		default:
			break;
		}
		return watcher;
	}

}
