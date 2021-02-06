package me.marty.openpixelmon.client.model.studiomdl.loader;

import dev.thecodewarrior.binarysmd.formats.SMDBinaryReader;
import dev.thecodewarrior.binarysmd.studiomdl.NodesBlock;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFileBlock;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
import me.marty.openpixelmon.OpenPixelmon;
import me.marty.openpixelmon.client.model.studiomdl.animation.AnimationData;
import me.marty.openpixelmon.compatibility.OtherModCompat;
import org.apache.commons.io.IOUtils;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SMDReader {
	public static LazySMDContext createLazyModel(String location) {
		return new LazySMDContext(location);
	}

	public static SmdModel readPokemonMdl(String location) {
		try {
			return new SmdModel("assets/generations/models/" + location, parseInfo(location));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static SMDFile safeReadFile(String location) {
		try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(OtherModCompat.INSTANCE.getPixelmonModel("models/" + location))) {
			return new SMDBinaryReader().read(unpacker);
		} catch (IOException e) {
			OpenPixelmon.LOGGER.fatal("Unable to read model!");
			e.printStackTrace();
		}
		return null;
	}

	private static SmdModel.Info parseInfo(String location) throws IOException {
		String[] slashSplit = location.split("/");
		String infoFileLocation = "models/" + location + "/" + slashSplit[slashSplit.length - 1] + ".pqc"; //Returns pokemon name for example "pokemon/abra" returns "abra"
		if (OtherModCompat.INSTANCE.getPixelmonInfo(infoFileLocation) == null) {
			OpenPixelmon.LOGGER.warn(location + " could not be loaded!");
			return null;
		}
		String[] infoFileProperties = IOUtils.toString(OtherModCompat.INSTANCE.getPixelmonInfo(infoFileLocation), StandardCharsets.UTF_8).replace("$", "").replace("\r", "").replace("\t", "").split("\n");

		return parseAnimationData(infoFileProperties, location);
	}

	private static SmdModel.Info parseAnimationData(String[] infoFileProperties, String location) {
		Map<String, AnimationData> animationDataMap = new HashMap<>();
		String bodyFileLocation = null;

		for (String infoFileProperty : infoFileProperties) {
			String[] split = infoFileProperty.split(" ");
			String property = split[0];
			String value = split[1];
			if (property.equals("body")) {
				bodyFileLocation = value;
			} else if (property.equals("anim")) {
				String animPath = split[2].replace(".smdx", "");
				SMDFile animation = safeReadFile(location + "/" + animPath);
				if (animation == null) {
					throw new RuntimeException("Couldn't read animation!");
				}
				animationDataMap.put(animPath, getAnimData(animation));
			}
		}

		return new SmdModel.Info(
				safeReadFile(location + "/" + bodyFileLocation),
				1,
				animationDataMap
		);
	}

	private static AnimationData getAnimData(SMDFile animation) {
		AnimationData data = new AnimationData();
		for (SMDFileBlock block : animation.blocks) {
			if (block instanceof NodesBlock) {
				NodesBlock nodeBlock = (NodesBlock) block;
				data.bones = nodeBlock.bones;
			}
			if (block instanceof SkeletonBlock) {
				SkeletonBlock nodeBlock = (SkeletonBlock) block;
				data.keyframes = nodeBlock.keyframes;
			}
		}
		return data;
	}
}
