package me.marty.openpixelmon.client;

import com.google.common.collect.ImmutableMap;
import me.marty.openpixelmon.api.battle.client.ClientBattleManager;
import me.marty.openpixelmon.client.model.entity.GeckolibModel;
import me.marty.openpixelmon.client.render.GameRendererAccessor;
import me.marty.openpixelmon.client.render.entity.GenerationsPixelmonRenderer;
import me.marty.openpixelmon.client.render.entity.NonLivingGeckolibModelRenderer;
import me.marty.openpixelmon.client.render.entity.PixelmonEntityRenderer;
import me.marty.openpixelmon.client.render.gui.Overlays;
import me.marty.openpixelmon.entity.Entities;
import me.marty.openpixelmon.network.Packets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.render.VertexFormats.*;

@Environment(EnvType.CLIENT)
public class OpenPixelmonClient implements ClientModInitializer {

    protected static final RenderPhase.Shader PIXELMON_SOLID_SHADER = new RenderPhase.Shader(OpenPixelmonClient::getPixelmonShader);
    public static final ClientBattleManager battleManager = new ClientBattleManager();
    public static final VertexFormatElement UINT_ELEMENT = new VertexFormatElement(0, VertexFormatElement.DataType.UINT, VertexFormatElement.Type.GENERIC, 1);
    public static final VertexFormatElement WEIGHT_ELEMENT = new VertexFormatElement(0, VertexFormatElement.DataType.FLOAT, VertexFormatElement.Type.GENERIC, 1);
    public static final VertexFormat PIXELMON_VERTEX_FORMAT = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", POSITION_ELEMENT)
            .put("UV0", TEXTURE_0_ELEMENT)
            .put("Color", COLOR_ELEMENT)
            .put("Normal", NORMAL_ELEMENT)
            .put("Padding", PADDING_ELEMENT)
            .put("WeirdPadding", UINT_ELEMENT)
            .put("BoneMap[0]", UINT_ELEMENT)
            .put("BoneMap[1]", UINT_ELEMENT)
            .put("BoneMap[2]", UINT_ELEMENT)
            .put("BoneMap[3]", UINT_ELEMENT)
            .put("WeightMap[0]", WEIGHT_ELEMENT)
            .put("WeightMap[1]", WEIGHT_ELEMENT)
            .put("WeightMap[2]", WEIGHT_ELEMENT)
            .put("WeightMap[3]", WEIGHT_ELEMENT)
            .build());
    public static Shader pixelmonSolidShader;

    private static Shader getPixelmonShader() {
        return pixelmonSolidShader;
    }

    public static void loadShaders(ResourceManager manager, GameRendererAccessor gameRenderer) throws IOException {
        pixelmonSolidShader = gameRenderer.loadPixelmonShader(manager, "pixelmon", OpenPixelmonClient.PIXELMON_VERTEX_FORMAT);
    }

    @Override
    public void onInitializeClient() {
        registerEntityRenderers();
        registerS2CPackets();
        registerKeybindings();
        registerHudRenderers();
    }

    private void registerHudRenderers() {
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> Overlays.renderPartyOverlay(matrices, MinecraftClient.getInstance(), MinecraftClient.getInstance().getWindow().getScaledHeight()));
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> Overlays.renderBattleOverlay(matrices, MinecraftClient.getInstance(), MinecraftClient.getInstance().getWindow().getScaledHeight()));
    }

    private void registerKeybindings() {
        KeyBinding keyBinding = new KeyBinding("keybind.pixelmon.throw_pixelmon", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.pixelmon.pixelmon");
        KeyBindingHelper.registerKeyBinding(keyBinding);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                ClientPlayNetworking.send(Packets.SEND_OUT, PacketByteBufs.create());
            }
        });
    }

    private void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(Packets.BATTLE_START, (client, handler, buf, responseSender) -> {
            int participantCount = buf.readVarInt();
            List<PlayerEntity> participants = new ArrayList<>();
            for (int i = 0; i < participantCount; i++) {
                participants.add((PlayerEntity) client.world.getEntityById(buf.readVarInt()));
            }

            OpenPixelmonClient.battleManager.startBattle(participants, client);
        });

        ClientPlayNetworking.registerGlobalReceiver(Packets.BATTLE_END, (client, handler, buf, responseSender) -> {
            boolean forced = buf.readBoolean();
            if (forced) {
                client.execute(OpenPixelmonClient.battleManager::forceStopBattle);
            }
        });
    }

    private void registerEntityRenderers() {
        if (useCompatModels()) {
            EntityRendererRegistry.INSTANCE.register(Entities.PIXELMON, GenerationsPixelmonRenderer::new);
        } else {
            EntityRendererRegistry.INSTANCE.register(Entities.PIXELMON, PixelmonEntityRenderer::new);
        }
        EntityRendererRegistry.INSTANCE.register(Entities.POKEBALL_ENTITY, ctx -> new NonLivingGeckolibModelRenderer<>(ctx, new GeckolibModel<>("pokeball", "pokeball/pokeball"), 0.2f));
    }

    private boolean useCompatModels() {
        return true; //TODO: currently forced due to us not having models for every pixelmon :pensive:
    }
}