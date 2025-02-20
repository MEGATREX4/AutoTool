package com.megatrex4;

import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;

@Environment(EnvType.CLIENT)
public class AutoTool implements ClientModInitializer {
	public static final String MOD_ID = "autotool";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static AutoToolConfig config;

	@Override
	public void onInitializeClient() {
		LOGGER.info("AutoTool initialized!");

		// Register the config class here
		AutoConfig.register(AutoToolConfig.class, GsonConfigSerializer::new);
		ConfigHolder<AutoToolConfig> configHolder = AutoConfig.getConfigHolder(AutoToolConfig.class);
		config = configHolder.getConfig();

		// Register the client tick event
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.world != null && client.player != null) {
				MinecraftClient mc = MinecraftClient.getInstance();
				if (mc.crosshairTarget != null && mc.options.attackKey.isPressed() && config.isEnableAutoSwitch()) {
					switchToBestTool();
				}
			}
		});
	}

	private void switchToBestTool() {
		MinecraftClient client = MinecraftClient.getInstance();

		if (client.player == null || client.player.getInventory() == null || client.crosshairTarget == null) return;

		if (client.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

		BlockHitResult blockHit = (BlockHitResult) client.crosshairTarget;
		BlockState blockState = client.world.getBlockState(blockHit.getBlockPos());

		int bestSlot = -1;
		float bestEfficiency = 0.0F;

		// Check weapon order from the config
		for (String weaponType : config.getWeaponOrder()) {
			for (int i = 0; i < 9; i++) {
				ItemStack stack = client.player.getInventory().getStack(i);

				// Check if this tool matches the weapon type and is suitable for the block
				if (stack.getItem().toString().contains(weaponType) && stack.isSuitableFor(blockState)) {
					float efficiency = stack.getMiningSpeedMultiplier(blockState);

					if (efficiency > bestEfficiency) {
						bestEfficiency = efficiency;
						bestSlot = i;
					}
				}
			}
		}

		if (bestSlot != -1 && bestSlot != client.player.getInventory().selectedSlot) {
			client.player.getInventory().selectedSlot = bestSlot;
			if (config.isDebugMode()) {
				LOGGER.debug("Switched to the best tool for the block in slot {}", bestSlot);
			}
		}
	}
}
