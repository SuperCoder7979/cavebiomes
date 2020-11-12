package supercoder79.cavebiomes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import supercoder79.cavebiomes.api.CaveBiomesAPI;
import supercoder79.cavebiomes.command.MapCaveBiomesCommand;
import supercoder79.cavebiomes.command.MapCavernsCommand;
import supercoder79.cavebiomes.command.MapOreNodulesCommand;
import supercoder79.cavebiomes.command.NightVisionCommand;
import supercoder79.cavebiomes.config.ConfigData;
import supercoder79.cavebiomes.config.ConfigIO;
import supercoder79.cavebiomes.world.carver.CaveBiomeCarvers;
import supercoder79.cavebiomes.world.compat.VanillaCompat;
import supercoder79.cavebiomes.world.decorator.CaveDecorators;
import supercoder79.cavebiomes.world.feature.CaveBiomesFeatures;

public class CaveBiomes implements ModInitializer {
	public static final String VERSION = "0.6.0";

	public static ConfigData CONFIG;

	@Override
	public void onInitialize() {
		CONFIG = ConfigIO.init();

		VanillaCompat.addVanillaBiomes();

		// Base cave decorators
		CaveBiomesAPI.registerBaseCaveDecorator(CaveDecorators.WATER);
		CaveBiomesAPI.registerBaseCaveDecorator(CaveDecorators.LAVA);
		CaveBiomesAPI.registerBaseCaveDecorator(CaveDecorators.LUSH);
		CaveBiomesAPI.registerBaseCaveDecorator(CaveDecorators.OBSIDIAN);
		CaveBiomesAPI.registerBaseCaveDecorator(CaveDecorators.MAGMA);
		CaveBiomesAPI.registerBaseCaveDecorator(CaveDecorators.COBBLESTONE);
		CaveBiomesAPI.registerBaseCaveDecorator(CaveDecorators.GRAVEL);
		CaveBiomesAPI.registerBaseCaveDecorator(CaveDecorators.SANDSTONE);

		// Stone cave decorators
		CaveBiomesAPI.registerCaveDecorator(CaveDecorators.ANDESITE);
		CaveBiomesAPI.registerCaveDecorator(CaveDecorators.DIORITE);
		CaveBiomesAPI.registerCaveDecorator(CaveDecorators.GRANITE);

		// Rare cave decorators
		CaveBiomesAPI.registerCaveDecorator(CaveDecorators.COBWEB);
		CaveBiomesAPI.registerCaveDecorator(CaveDecorators.FULL_OBSIDIAN);
		CaveBiomesAPI.registerCaveDecorator(CaveDecorators.MUSHROOM);

		// Ore cave decorators
		CaveBiomesAPI.registerCaveDecorator(CaveDecorators.COAL);
		CaveBiomesAPI.registerCaveDecorator(CaveDecorators.IRON);
		CaveBiomesAPI.registerCaveDecorator(CaveDecorators.GOLD);
		CaveBiomesAPI.registerCaveDecorator(CaveDecorators.REDSTONE);
		CaveBiomesAPI.registerCaveDecorator(CaveDecorators.LAPIS);
		CaveBiomesAPI.registerCaveDecorator(CaveDecorators.DIAMOND);

		// Register cave layers

		// Carver stuff
		Registry.register(Registry.CARVER, new Identifier("cavebiomes", "room_carver"), CaveBiomeCarvers.ROOM);
		Registry.register(Registry.CARVER, new Identifier("cavebiomes", "vertical_carver"), CaveBiomeCarvers.VERTICAL);
		Registry.register(Registry.CARVER, new Identifier("cavebiomes", "horizontal_carver"), CaveBiomeCarvers.HORIZONTAL);
		Registry.register(Registry.CARVER, new Identifier("cavebiomes", "lava_room_carver"), CaveBiomeCarvers.LAVA_ROOM);
		Registry.register(Registry.CARVER, new Identifier("cavebiomes", "perlerp_carver"), CaveBiomeCarvers.PERLERP);

		// Add enabled chests and spawners
		CaveBiomesFeatures.addEnabledFeatures(CONFIG);

		// Development-only commands
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			MapCaveBiomesCommand.init();
			MapCavernsCommand.init();
			MapOreNodulesCommand.init();
			NightVisionCommand.init();
		}
	}
}
