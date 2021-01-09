package me.martyalana.openpixelmon.mixin;

import com.mojang.authlib.GameProfile;
import me.martyalana.openpixelmon.api.pc.PcBox;
import me.martyalana.openpixelmon.api.player.PixelmonPlayer;
import me.martyalana.openpixelmon.entity.pixelmon.PixelmonEntity;
import me.martyalana.openpixelmon.network.Packets;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements PixelmonPlayer {

	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
		super(world, pos, yaw, profile);
	}

	@Shadow public abstract void sendMessage(Text message, boolean actionBar);

	private int pp;
	private final List<PixelmonEntity> party = new ArrayList<>();
	private final List<PcBox> pcBoxes = new ArrayList<>();

	@Override
	public int getPP() {
		return pp;
	}

	@Override
	public List<PixelmonEntity> getParty() {
		return party;
	}

	@Override
	public List<PcBox> getPcBoxes() {
		return pcBoxes;
	}

	@Override
	public void setPP(int pp) {
		this.pp = pp;
	}

	@Override
	public void clearParty() {
		this.party.clear();
	}

	@Override
	public void givePixelmon(PixelmonEntity entity) {
		ServerPlayNetworking.send((ServerPlayerEntity) (Object)this, Packets.SYNC_PIXELMON, createPixelmonBuf(entity));
		if(party.size() > 5) {
			//TODO: pc logic
			sendMessage(new LiteralText(entity.getNickname() + " has been sent to your pc!").formatted(Formatting.GRAY), false);
		} else {
			party.add(entity);
			sendMessage(new LiteralText(entity.getNickname() + " is now in your party!").formatted(Formatting.GRAY), false);
		}
	}

	private PacketByteBuf createPixelmonBuf(PixelmonEntity entity) {
		PacketByteBuf packetByteBuf = PacketByteBufs.create();
		packetByteBuf.writeIdentifier(entity.getPokedexData().name);
		packetByteBuf.writeInt(entity.getLevel());
		return packetByteBuf;
	}
}
