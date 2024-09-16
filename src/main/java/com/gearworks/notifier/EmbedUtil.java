package com.gearworks.notifier;

import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;

import static com.gearworks.notifier.DiscordNotifier.EMBED_FIELDS_ARE_INLINE;

public class EmbedUtil {
	public static void addLocation(EmbedBuilder builder, ResourceKey<Level> dimension, BlockPos pos) {
		String dimensionName;

		if (LevelStem.OVERWORLD.location().equals(dimension.location())) {
			dimensionName = "the Overworld";
		} else if (LevelStem.NETHER.location().equals(dimension.location())) {
			dimensionName = "The Nether";
		} else if (LevelStem.END.location().equals(dimension.location())) {
			dimensionName = "The End";
		} else {
			dimensionName = dimension.location().toString();
		}

		builder.addField("Location", "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ") in " + dimensionName, EMBED_FIELDS_ARE_INLINE);
	}
}
