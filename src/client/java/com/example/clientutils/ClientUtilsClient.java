package com.example.clientutils;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ClientUtilsClient implements ClientModInitializer {

	// ===== SETTINGS (yahan change karo) =====
	/** Held item ka size. 1.0 = normal, 1.5 = bada, 2.0 = bahut bada */
	public static final float BIG_ITEM_SCALE = 1.5f;

	/** Command keys: har key dabane pe ye command chalega (bina "/" ke) */
	private static final String[] COMMANDS = {"home", "spawn", "tpa"};
	private static final int[] KEYS = {GLFW.GLFW_KEY_H, GLFW.GLFW_KEY_J, GLFW.GLFW_KEY_K};
	// ========================================

	private static final KeyBinding.Category CATEGORY =
			KeyBinding.Category.create(Identifier.of("clientutils", "main"));
	private static final KeyBinding[] BINDINGS = new KeyBinding[COMMANDS.length];

	@Override
	public void onInitializeClient() {
		// --- Command keys ---
		for (int i = 0; i < COMMANDS.length; i++) {
			BINDINGS[i] = KeyBindingHelper.registerKeyBinding(new KeyBinding(
					"key.clientutils.cmd" + i, InputUtil.Type.KEYSYM, KEYS[i], CATEGORY));
		}
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;
			for (int i = 0; i < BINDINGS.length; i++) {
				while (BINDINGS[i].wasPressed()) {
					client.player.networkHandler.sendChatCommand(COMMANDS[i]);
				}
			}
		});

		// --- Kit saving: /kitsave <name>, /kitload <name>, /kitlist ---
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("kitsave")
					.then(ClientCommandManager.argument("name", StringArgumentType.word())
							.executes(ctx -> saveKit(ctx.getSource(),
									StringArgumentType.getString(ctx, "name")))));
			dispatcher.register(ClientCommandManager.literal("kitload")
					.then(ClientCommandManager.argument("name", StringArgumentType.word())
							.executes(ctx -> loadKit(ctx.getSource(),
									StringArgumentType.getString(ctx, "name")))));
			dispatcher.register(ClientCommandManager.literal("kitlist")
					.executes(ctx -> listKits(ctx.getSource())));
		});
	}

	// ---------- Kit helpers ----------

	private static Path kitDir() {
		return FabricLoader.getInstance().getConfigDir().resolve("clientutils-kits");
	}

	private static int saveKit(FabricClientCommandSource src, String name) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null) return 0;
		List<String> lines = new ArrayList<>();
		for (int i = 0; i < 41; i++) { // 0-35 inventory, 36-39 armor, 40 offhand
			ItemStack s = mc.player.getInventory().getStack(i);
			if (!s.isEmpty()) {
				lines.add(i + "," + Registries.ITEM.getId(s.getItem()) + "," + s.getCount());
			}
		}
		try {
			Files.createDirectories(kitDir());
			Files.write(kitDir().resolve(name + ".txt"), lines);
			src.sendFeedback(Text.literal("Kit '" + name + "' save ho gaya (" + lines.size() + " items)"));
		} catch (Exception e) {
			src.sendFeedback(Text.literal("Kit save nahi hua: " + e.getMessage()));
		}
		return 1;
	}

	private static int loadKit(FabricClientCommandSource src, String name) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.interactionManager == null) return 0;
		if (!mc.interactionManager.getCurrentGameMode().isCreative()) {
			src.sendFeedback(Text.literal("Kit load sirf creative mode mein chalta hai"));
			return 0;
		}
		Path file = kitDir().resolve(name + ".txt");
		if (!Files.exists(file)) {
			src.sendFeedback(Text.literal("Kit '" + name + "' nahi mila"));
			return 0;
		}
		try {
			Map<Integer, ItemStack> kit = new HashMap<>();
			for (String line : Files.readAllLines(file)) {
				String[] p = line.split(",");
				Identifier id = Identifier.tryParse(p[1]);
				if (id == null) continue;
				Item item = Registries.ITEM.get(id);
				kit.put(Integer.parseInt(p[0]), new ItemStack(item, Integer.parseInt(p[2])));
			}
			for (int i = 0; i < 41; i++) {
				ItemStack stack = kit.getOrDefault(i, ItemStack.EMPTY);
				mc.player.getInventory().setStack(i, stack);
				mc.player.networkHandler.sendPacket(
						new CreativeInventoryActionC2SPacket(containerSlot(i), stack));
			}
			src.sendFeedback(Text.literal("Kit '" + name + "' load ho gaya"));
		} catch (Exception e) {
			src.sendFeedback(Text.literal("Kit load nahi hua: " + e.getMessage()));
		}
		return 1;
	}

	private static int listKits(FabricClientCommandSource src) {
		try {
			if (!Files.exists(kitDir())) {
				src.sendFeedback(Text.literal("Abhi koi kit save nahi hai"));
				return 1;
			}
			try (Stream<Path> files = Files.list(kitDir())) {
				String names = String.join(", ", files
						.map(p -> p.getFileName().toString().replace(".txt", "")).toList());
				src.sendFeedback(Text.literal("Kits: " + (names.isEmpty() ? "(none)" : names)));
			}
		} catch (Exception e) {
			src.sendFeedback(Text.literal("Error: " + e.getMessage()));
		}
		return 1;
	}

	/** Player inventory index -> container slot id */
	private static int containerSlot(int inv) {
		if (inv < 9) return 36 + inv;   // hotbar
		if (inv < 36) return inv;       // main inventory
		if (inv < 40) return 44 - inv;  // armor (boots=36 -> 8, helmet=39 -> 5)
		return 45;                      // offhand
	}
}
