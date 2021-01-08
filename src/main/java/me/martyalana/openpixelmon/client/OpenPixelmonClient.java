package me.martyalana.openpixelmon.client;

import me.martyalana.openpixelmon.api.pixelmon.PokedexData;
import me.martyalana.openpixelmon.client.model.entity.GeckolibModel;
import me.martyalana.openpixelmon.client.render.entity.EmptyEntityRenderer;
import me.martyalana.openpixelmon.client.render.entity.NonLivingGeckolibModelRenderer;
import me.martyalana.openpixelmon.entity.Entities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class OpenPixelmonClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		registerEntityRenderers();
	}

	private void registerEntityRenderers() {
		EntityRendererRegistry.INSTANCE.register(Entities.POKEBALL_ENTITY, (manager, context) -> new NonLivingGeckolibModelRenderer<>(manager, new GeckolibModel<>("pokeball", "pokeball/pokeball")));

		// Pixelmon
		for (PokedexData pokedexData : Entities.GENERATION_3.getPokemon()) {
			EntityRendererRegistry.INSTANCE.register(pokedexData.type, EmptyEntityRenderer::new);
		}
	}
}
