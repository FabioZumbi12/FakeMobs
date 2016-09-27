package de.howaner.FakeMobs.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.base.Charsets;
import com.google.common.collect.Multimap;

import de.howaner.FakeMobs.FakeMobsPlugin;
import de.howaner.FakeMobs.interact.InteractAction;
import de.howaner.FakeMobs.merchant.ReflectionUtils;

public class FakeMob {
	private final int id;
	private final UUID uniqueId;
	private String name = null;
	private Location loc;
	private EntityType type;
	private WrappedDataWatcher dataWatcher = null;
	private final List<Player> loadedPlayers = new ArrayList<Player>();

	private boolean sitting = false;
	private boolean gliding = false;
	private boolean invisibility = false;
	private boolean playerLook = false;
	private boolean layering = true;
	private MobInventory inventory = new MobInventory();
	private MobShop shop = null;
	private Multimap<String, WrappedSignedProperty> playerSkin;  // Only used if this.getType() == EntityType.PLAYER
	private final List<InteractAction> interacts = new ArrayList<InteractAction>();
	private boolean vary = false;
	static Serializer serialString = Registry.get(String.class);
	static Serializer serialByte = Registry.get(Byte.class);
	//static Serializer serialFloat = Registry.get(Float.class);
	static Serializer serialBool = Registry.get(Boolean.class);
	
	public FakeMob(int id, Location loc, EntityType type) {
		this.id = id;
		this.loc = loc;
		this.type = type;
		this.uniqueId = UUID.nameUUIDFromBytes(("FakeMob-" + id).getBytes(Charsets.UTF_8));
		this.dataWatcher = DataWatchCreator.createDefaultWatcher(this);
	}

	public UUID getUniqueID() {
		return this.uniqueId;
	}
	
	public List<InteractAction> getInteractActions() {
		return this.interacts;
	}
	
	public void clearInteractAction() {
		this.interacts.clear();
	}
	
	public void addInteractAction(InteractAction action) {
		this.interacts.add(action);
	}
	
	public void removeInteractAction(InteractAction action) {
		this.interacts.remove(action);
	}
	
	public MobInventory getInventory() {
		return this.inventory;
	}
	
	public void setInventory(MobInventory inv) {
		this.inventory = inv;
		if (this.inventory == null)
			this.inventory = new MobInventory();
	}
	
	public boolean haveShop() {
		return (this.shop != null);
	}
	
	public MobShop getShop() {
		return this.shop;
	}
	
	public void setShop(MobShop shop) {
		this.shop = shop;
	}
	
	public int getEntityId() {
		return 2300 + this.id;
	}

	public Multimap<String, WrappedSignedProperty> getPlayerSkin() {
		return this.playerSkin;
	}

	public void setPlayerSkin(Multimap<String, WrappedSignedProperty> skin) {
		this.playerSkin = skin;
	}

	public void setPlayerSkin(Player player) {
		this.playerSkin = WrappedGameProfile.fromPlayer(player).getProperties();
	}
	
	public List<Player> getNearbyPlayers() {
		return this.getNearbyPlayers(3D);
	}
	
	public List<Player> getNearbyPlayers(double radius) {
		List<Player> players = new ArrayList<Player>();

		for (Player player : this.getWorld().getPlayers()) {
			if (this.loc.distance(player.getLocation()) <= radius) {
				players.add(player);
			}
		}
		
		return players;
	}
	
	public void updateInventory() {
		for (Player p : this.loadedPlayers)
			this.sendInventoryPacket(p);
	}

	public boolean isPlayerLoaded(Player player) {
		return this.loadedPlayers.contains(player);
	}

	public void loadPlayer(Player player) {
		if (this.isPlayerLoaded(player)) return;

		this.loadedPlayers.add(player);
		this.sendSpawnPacket(player);
	}

	public void unloadPlayer(Player player) {
		if (!this.isPlayerLoaded(player)) return;

		this.loadedPlayers.remove(player);
		this.sendDestroyPacket(player);
	}

	public boolean isInRange(Player player) {
		return this.loc.getWorld() == player.getLocation().getWorld() && (this.loc.distance(player.getLocation()) <= 48D);
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getCustomName() {
		return this.name;
	}
	
	public Location getLocation() {
		return this.loc;
	}
	
	public World getWorld() {
		return this.loc.getWorld();
	}
	
	public EntityType getType() {
		return this.type;
	}
	
	public boolean isLayering() {		
		return this.layering;
	}
	
	public void showLayer(boolean layer) {		
		this.layering = layer;
	}
	
	public boolean isGliding() {
		return this.gliding;
	}
	
	public boolean isSitting() {
		return this.sitting;
	}

	public boolean isInvisibility() {
		return this.invisibility;
	}
	
	public boolean isPlayerLook() {
		return this.playerLook;
	}
	
	public void setLocation(Location loc) {
		this.loc = loc;
	}
	
	public void setCustomName(String name) {
		if (name != null && name.length() > 32) name = name.substring(0, 32);
		this.name = name;

		if (this.name != null && this.name.isEmpty()) {
			this.name = null;
		}

		if (this.type == EntityType.PLAYER) {
			// No need to change the watcher
			return;
		}

		if (this.name == null) {
			this.dataWatcher.setObject(new WrappedDataWatcherObject(3, serialBool), true);
			this.dataWatcher.setObject(new WrappedDataWatcherObject(2, serialString), "");
		} else {
			this.dataWatcher.setObject(new WrappedDataWatcherObject(3, serialBool), true);
			this.dataWatcher.setObject(new WrappedDataWatcherObject(2, serialString), this.name);
		}
	}
	
	public void setSitting(boolean sitting) {
		if (this.type != EntityType.OCELOT && this.type != EntityType.WOLF && this.type != EntityType.PLAYER) return;
		if (this.sitting == sitting) return;
		this.sitting = sitting;

		int dif = 0;
		if (FakeMobsPlugin.getPlugin().version >= 1100){
			dif++;
		}
		if (this.getType() == EntityType.PLAYER) {
			byte current = this.dataWatcher.getByte(0);
			if (sitting){
				this.dataWatcher.setObject(new WrappedDataWatcherObject(0, serialByte), (byte) (current | 1 << 1));
			} else {
				this.dataWatcher.setObject(new WrappedDataWatcherObject(0, serialByte), (byte) (current & (1 << 1 ^ 0xFFFFFFFF)));
			}
		} else if (sitting) {
			this.dataWatcher.setObject(new WrappedDataWatcherObject(12+dif, serialByte), (byte) 0x1);
		} else {
			this.dataWatcher.setObject(new WrappedDataWatcherObject(12+dif, serialByte), (byte) 0x0);
		}
	}
	
	public void setGliding(boolean gliding) {
		if (this.type != EntityType.PLAYER) return;
		if (this.gliding == gliding) return;
		this.gliding = gliding;		
		
		if (gliding){
			this.dataWatcher.setObject(new WrappedDataWatcherObject(0, serialByte), (byte) 0x80);
		} else {
			this.dataWatcher.setObject(new WrappedDataWatcherObject(0, serialByte), (byte) 0);
		}
	}

	public void setInvisibility(boolean invisibility) {
		if (this.invisibility == invisibility) return;
		this.invisibility = invisibility;

		byte current = this.dataWatcher.getByte(0);
		if (invisibility) {
			this.dataWatcher.setObject(new WrappedDataWatcherObject(0, serialByte), (byte) (current | 1 << 5));
		} else {
			this.dataWatcher.setObject(new WrappedDataWatcherObject(0, serialByte), (byte) (current & (1 << 5 ^ 0xFFFFFFFF)));
		}
	}
	
	public void setPlayerLook(boolean look) {
		if (this.playerLook == look) return;

		if (!look) {
			for (Player player : this.loadedPlayers)
				this.sendLookPacket(player, this.loc.getYaw(), this.loc.getPitch());
		}

		this.playerLook = look;
	}
	
	public void teleport(Location loc) {
		this.loc = loc;

		for (Player player : this.loadedPlayers)
			this.sendPositionPacket(player);
	}
	
	public void setType(EntityType type) {
		if (type == null || this.type == type || !type.isAlive()) return;
		
		for (Player p : this.loadedPlayers)
			this.sendDestroyPacket(p);

		this.type = type;
		this.dataWatcher = DataWatchCreator.createDefaultWatcher(this);

		for (Player p : this.loadedPlayers)
			this.sendSpawnPacket(p);
	}

	public void updateMetadata() {
		for (Player player : this.loadedPlayers) {
			this.sendMetaPacket(player);
		}
	}
	
	public void updateCustomName() {
		for (Player player : this.loadedPlayers) {
			if (this.getType() == EntityType.PLAYER) {
				this.sendDestroyPacket(player);
			} else
				this.sendMetaPacket(player);
		}

		// Need a 5 tick delay because mojang did mistakes in 1.8 ...
		if (this.getType() == EntityType.PLAYER) {
			Bukkit.getScheduler().runTaskLater(FakeMobsPlugin.getPlugin(), new Runnable() {
				@Override
				public void run() {
					for (Player player : FakeMob.this.loadedPlayers) {
						FakeMob.this.sendSpawnPacket(player);
					}
				}
			}, 5L);
		}
	}


	//////////////// -- PACKETS -- ////////////////

	public void sendSpawnPacket(Player player) {
		if (this.getType() == EntityType.PLAYER)
			this.sendPlayerSpawnPacket(player);
		else
			this.sendEntitySpawnPacket(player);
	}

	@SuppressWarnings("unchecked")
	public void sendPlayerSpawnPacket(final Player player) {
		PacketContainer packet = FakeMobsPlugin.getPlugin().getProtocolManager().createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
		
		packet.getIntegers().write(0, this.getEntityId());
		packet.getDoubles().write(0, this.loc.getX()); //X
		packet.getDoubles().write(1, this.loc.getY()); //Y
		packet.getDoubles().write(2, this.loc.getZ()); //Z
		
		packet.getBytes().write(0, (byte)(int)(this.loc.getYaw() * 256.0F / 360.0F)); //Yaw
		packet.getBytes().write(1, (byte)(int)(this.loc.getPitch() * 256.0F / 360.0F)); //Pitch
		
		final WrappedGameProfile profile = new WrappedGameProfile(this.uniqueId, (this.getCustomName() == null) ? "No Name" : this.getCustomName());
		if (this.playerSkin != null) {
			profile.getProperties().putAll(this.playerSkin);
		}
		
		packet.getSpecificModifier(UUID.class).write(0, profile.getUUID());
		packet.getDataWatcherModifier().write(0, this.dataWatcher);

		int protocolVersion = FakeMobsPlugin.getPlugin().getProtocolManager().getProtocolVersion(player);
		if (protocolVersion >= 47 || protocolVersion == Integer.MIN_VALUE) {
			PacketContainer infoPacket = FakeMobsPlugin.getPlugin().getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);

			Object playerInfo = ReflectionUtils.createPlayerInfoData(profile.getHandle(), GameMode.SURVIVAL, 0, " ");
			infoPacket.getSpecificModifier(ReflectionUtils.PlayerInfoAction.getNMSClass()).write(0, ReflectionUtils.PlayerInfoAction.ADD_PLAYER);
			infoPacket.getSpecificModifier(List.class).write(0, Arrays.asList(new Object[] { playerInfo }));

			try {
				FakeMobsPlugin.getPlugin().getProtocolManager().sendServerPacket(player, infoPacket);				
			} catch (Exception e) {
				FakeMobsPlugin.log.log(Level.SEVERE, "Can''t send player info packet to {0}", player.getName());
				e.printStackTrace();
			}

			Bukkit.getScheduler().runTaskLater(FakeMobsPlugin.getPlugin(), new Runnable() {
				@Override
				public void run() {
					if (!FakeMob.this.isPlayerLoaded(player)) {
						return;
					}
					PacketContainer infoPacket = FakeMobsPlugin.getPlugin().getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);

					Object playerInfo = ReflectionUtils.createPlayerInfoData(profile.getHandle(), GameMode.SURVIVAL, 0, "");
					infoPacket.getSpecificModifier(ReflectionUtils.PlayerInfoAction.getNMSClass()).write(0, ReflectionUtils.PlayerInfoAction.REMOVE_PLAYER);
					infoPacket.getSpecificModifier(List.class).write(0, Arrays.asList(new Object[] { playerInfo }));

					try {
						FakeMobsPlugin.getPlugin().getProtocolManager().sendServerPacket(player, infoPacket);
					} catch (Exception e) {
						FakeMobsPlugin.log.log(Level.WARNING, "Can''t send player info packet to {0}", player.getName());
						e.printStackTrace();
					}
				}
			}, (this.playerSkin == null ? 5L : 40L));
		}

		try {
			this.sendMetaPacket(player);
			FakeMobsPlugin.getPlugin().getProtocolManager().sendServerPacket(player, packet);			
		} catch (Exception e) {
			FakeMobsPlugin.log.log(Level.SEVERE, "Can''t send spawn packet to {0} from mob #{1}", new Object[]{ player.getName(), this.getId() });
			e.printStackTrace();
			return;
		}

		this.sendLookPacket(player, this.loc.getYaw(), this.loc.getPitch());
		this.sendInventoryPacket(player);
	}
	
	@SuppressWarnings("deprecation")
	public void sendEntitySpawnPacket(Player player) {
		PacketContainer packet = FakeMobsPlugin.getPlugin().getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

		packet.getIntegers().write(0, this.getEntityId());
		packet.getIntegers().write(1, (int) this.type.getTypeId()); //Id
		packet.getDoubles().write(0, this.loc.getX()); //X
		packet.getDoubles().write(1, this.loc.getY()); //Y
		packet.getDoubles().write(2, this.loc.getZ()); //Z

		packet.getBytes().write(0, (byte)(int)(this.loc.getYaw() * 256.0F / 360.0F)); //Yaw
		packet.getBytes().write(1, (byte)(int)(this.loc.getPitch() * 256.0F / 360.0F)); //Pitch
		packet.getBytes().write(2, (byte)(int)(this.loc.getYaw() * 256.0F / 360.0F)); //Head

		packet.getDataWatcherModifier().write(0, this.dataWatcher);

		try {
			FakeMobsPlugin.getPlugin().getProtocolManager().sendServerPacket(player, packet);			
		} catch (Exception e) {
			FakeMobsPlugin.log.log(Level.SEVERE, "Can''t send spawn packet to {0} from mob #{1}", new Object[]{player.getName(), this.getId()});
			e.printStackTrace();
			return;
		}

		this.sendInventoryPacket(player);
	}
	
	public void sendMetaPacket(Player player) {
		PacketContainer packet = FakeMobsPlugin.getPlugin().getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);

		packet.getIntegers().write(0, this.getEntityId());
		int dif = 0;
		if (FakeMobsPlugin.getPlugin().version >= 1100){
			dif++;
		}
		if (this.type.equals(EntityType.PLAYER)){
			if (this.isLayering())
				this.dataWatcher.setObject(new WrappedDataWatcherObject(12+dif, serialByte), (byte) 0x7F);
			else 
				this.dataWatcher.setObject(new WrappedDataWatcherObject(12+dif, serialByte), (byte) 0);
		}		
		packet.getWatchableCollectionModifier().write(0, this.dataWatcher.getWatchableObjects());

		try {
			FakeMobsPlugin.getPlugin().getProtocolManager().sendServerPacket(player, packet);			
		} catch (Exception e) {
			FakeMobsPlugin.log.log(Level.SEVERE, "Can''t send metadata oacket to {0} from mob #{1}", new Object[]{player.getName(), this.getId()});
			e.printStackTrace();
		}
	}
	
	public void sendInventoryPacket(Player player) {
		List<PacketContainer> packets = this.inventory.createPackets(this.getEntityId());
		if (packets.isEmpty()) return;
		
		try {
			for (PacketContainer packet : packets)
				FakeMobsPlugin.getPlugin().getProtocolManager().sendServerPacket(player, packet);
		} catch (Exception e) {
			FakeMobsPlugin.log.log(Level.SEVERE, "Can''t send inventory packets to {0} from mob #{1}", new Object[]{player.getName(), this.getId()});
			e.printStackTrace();
		}
	}
	
	public void sendLookPacket(Player player) {				
		this.sendLookPacket(player, getLook(player.getLocation())[0], getLook(player.getLocation())[1]);
	}
	
	public float[] getLook(Location point){
		Vector direction = this.loc.toVector().subtract(point.toVector()).normalize();
	    double x = direction.getX();
	    double y = direction.getY();
	    double z = direction.getZ();
		return new float[]{180 - toDegree(Math.atan2(x, z)),90 - toDegree(Math.acos(y))};
	}
	
	private float toDegree(double angle) {
	    return (float) Math.toDegrees(angle);
	}
	
	public void sendLookPacket(Player player, float yaw, float pitch) {
		sendBodyLookPacket(player, yaw, pitch);//Rotate body
		
		PacketContainer packet = FakeMobsPlugin.getPlugin().getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
		
		packet.getIntegers().write(0, this.getEntityId());
		packet.getBytes().write(0, (byte)(int)(yaw * 256.0F / 360.0F));
		
		try {
			FakeMobsPlugin.getPlugin().getProtocolManager().sendServerPacket(player, packet);
		} catch (Exception e) {
			FakeMobsPlugin.log.log(Level.SEVERE, "Can''t send look packet to {0} from mob #{1}", new Object[]{player.getName(), this.getId()});
			e.printStackTrace();
		}
	}
	
	private void sendBodyLookPacket(Player player, float yaw, float pitch) {
		PacketContainer packet = FakeMobsPlugin.getPlugin().getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_LOOK);

		packet.getIntegers().write(0, this.getEntityId());
		packet.getBytes().write(0, (byte)(int)(yaw * 256.0F / 360.0F));
		packet.getBytes().write(1, (byte)(int)(pitch * 256.0F / 360.0F));
		
		try {
			FakeMobsPlugin.getPlugin().getProtocolManager().sendServerPacket(player, packet);
		} catch (Exception e) {
			FakeMobsPlugin.log.log(Level.SEVERE, "Can''t send look packet to {0} from mob #{1}", new Object[]{player.getName(), this.getId()});
			e.printStackTrace();
		}
	}
	
	public void sendPositionPacket(Player player) {
		PacketContainer packet = FakeMobsPlugin.getPlugin().getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_TELEPORT);

		packet.getIntegers().write(0, this.getEntityId()); //Id
		packet.getDoubles().write(0, this.loc.getX()); //X
		packet.getDoubles().write(1, this.loc.getY()); //Y
		packet.getDoubles().write(2, this.loc.getZ()); //Z
		
		packet.getBytes().write(0, (byte)(int)(this.loc.getYaw() * 256.0F / 360.0F)); //Yaw
		packet.getBytes().write(1, (byte)(int)(this.loc.getPitch() * 256.0F / 360.0F)); //Pitch
		
		try {
			FakeMobsPlugin.getPlugin().getProtocolManager().sendServerPacket(player, packet);
		} catch (Exception e) {
			FakeMobsPlugin.log.log(Level.SEVERE, "Can''t send position packet to {0} from mob #{1}", new Object[]{player.getName(), this.getId()});
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void sendDestroyPacket(Player player) {
		PacketContainer packet = FakeMobsPlugin.getPlugin().getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		packet.getIntegerArrays().write(0, new int[] { this.getEntityId() });
		
		try {
			FakeMobsPlugin.getPlugin().getProtocolManager().sendServerPacket(player, packet);
		} catch (Exception e) {
			FakeMobsPlugin.log.log(Level.SEVERE, "Can''t send destroy packet to {0} from mob #{1}", new Object[]{player.getName(), this.getId()});
			e.printStackTrace();
			return;
		}

		if (FakeMobsPlugin.getPlugin().getProtocolManager().getProtocolVersion(player) >= 47 && this.getType() == EntityType.PLAYER) {
			WrappedGameProfile profile = new WrappedGameProfile(this.uniqueId, (this.getCustomName() == null) ? "No Name" : this.getCustomName());
			PacketContainer infoPacket = FakeMobsPlugin.getPlugin().getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);

			Object playerInfo = ReflectionUtils.createPlayerInfoData(profile.getHandle(), GameMode.SURVIVAL, 0, "");
			infoPacket.getSpecificModifier(ReflectionUtils.PlayerInfoAction.getNMSClass()).write(0, ReflectionUtils.PlayerInfoAction.REMOVE_PLAYER);
			infoPacket.getSpecificModifier(List.class).write(0, Arrays.asList(new Object[] { playerInfo }));

			try {
				FakeMobsPlugin.getPlugin().getProtocolManager().sendServerPacket(player, infoPacket);
			} catch (Exception e) {
				FakeMobsPlugin.log.log(Level.SEVERE, "Can''t send player info destroy packet to {0}", player.getName());
				e.printStackTrace();
			}
		}
	}
	
	public boolean getVary(){
		return this.vary;
	}
	
	public int changeVary(boolean vary) {
		int dif = 0;
		if (FakeMobsPlugin.getPlugin().version >= 1100){
			dif++;
		}
		this.vary = vary;
		switch (this.type) {
		case CREEPER:
			this.dataWatcher.setObject(new WrappedDataWatcherObject(12+dif, serialBool), vary);
			return 12+dif;
		case PIG:
			this.dataWatcher.setObject(new WrappedDataWatcherObject(12+dif, serialBool), vary);
			return 12+dif;	
		case POLAR_BEAR:
			this.dataWatcher.setObject(new WrappedDataWatcherObject(12+dif, serialBool), vary);
			return 12+dif;	
		case SKELETON:
			this.dataWatcher.setObject(new WrappedDataWatcherObject(12+dif, serialBool), vary);
			return 12+dif;	
		case ZOMBIE:
			this.dataWatcher.setObject(new WrappedDataWatcherObject(14+dif, serialBool), vary);
			return 14+dif;	
		case ENDERMAN:
			this.dataWatcher.setObject(new WrappedDataWatcherObject(12+dif, serialBool), vary);
			return 12+dif;	
		case WOLF:			
			this.dataWatcher.setObject(new WrappedDataWatcherObject(15+dif, serialBool), vary);
			return 15+dif;	
		default:
			return 0;	
		}
	}			
}
