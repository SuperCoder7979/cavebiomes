package supercoder79.cavebiomes.util;

import com.google.common.collect.Sets;
import net.minecraft.util.Identifier;

import java.util.Set;

public class TreasureLootTables {
	private TreasureLootTables() {
	}

	private static final Set<Identifier> LOOT_TABLES = Sets.newHashSet();

	public static final Identifier CAVERN_CHEST = register("chests/cavern_chest");
	public static final Identifier SPELUNKERS_CHEST = register("chests/spelunkers_chest");
	public static final Identifier NETHER_CHEST = register("chests/nether_chest");

	private static Identifier register(String id) {
		Identifier identifier = new Identifier("cavebiomes", id);

		if (LOOT_TABLES.add(identifier)) {
			return identifier;
		} else {
			throw new IllegalArgumentException("Loot table " + id + " already exists!");
		}
	}
}
