package com.megatrex4;

import com.nimbusds.oauth2.sdk.id.Identifier;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;

@Environment(EnvType.CLIENT)
public class AutoTool implements ClientModInitializer {
	public static final String MOD_ID = "autotool";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	boolean enabled;

	private static AutoToolConfig config;
	private static final KeyBinding AutoToolBinding = new KeyBinding(
			"key.autotool.toggle",
			GLFW.GLFW_KEY_G,
			"category.autotool"
	);

	@Override
	public void onInitializeClient() {
		LOGGER.info("AutoTool initialized!");

		AutoConfig.register(AutoToolConfig.class, GsonConfigSerializer::new);
		ConfigHolder<AutoToolConfig> configHolder = AutoConfig.getConfigHolder(AutoToolConfig.class);
		config = configHolder.getConfig();

		enabled = config.isEnableAutoSwitch();

		ClientTickEvents.END_CLIENT_TICK.register(this::tick);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (AutoToolBinding.isPressed()) {
				config.setEnableAutoSwitch(!config.isEnableAutoSwitch());
				enabled = config.isEnableAutoSwitch();
				if (config.isDebugMode()) {
					LOGGER.info("Auto-tool switch {}!", config.isEnableAutoSwitch() ? "enabled" : "disabled");
				}
			}

			if (client.world != null && client.player != null) {
				if (client.crosshairTarget != null && client.options.attackKey.isPressed() && config.isEnableAutoSwitch()) {
					switchToBestTool();
				}
			}
		});
	}



	public void tick(MinecraftClient client) {
		if (client.player != null && client.world != null) {
			if (AutoToolBinding.wasPressed()) {
				enabled = !enabled;
				var msg = enabled
						? Text.translatable("autotool.message.enabled")
						: Text.translatable("autotool.message.disabled");
				client.player.sendMessage(msg, true);
				config.setEnableAutoSwitch(enabled);
			}
		}
	}

	private void switchToBestTool() {
		MinecraftClient client = MinecraftClient.getInstance();

		if (client.player == null || client.player.getInventory() == null || client.crosshairTarget == null) return;

		int bestSlot = -1;
		float bestEfficiency = 0.0F;
		boolean isWeaponRequired = false;

		// Determine if the target is an entity
		if (client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
			isWeaponRequired = true;
		}

		if (isWeaponRequired) {
			// Weapon selection logic
			for (String weaponType : config.getWeaponOrder()) {
				for (int i = 0; i < 9; i++) {
					ItemStack stack = client.player.getInventory().getStack(i);

					if (stack.getItem().toString().equals(weaponType) ||
							(stack.getItem() instanceof SwordItem && weaponType.equals("sword")) ||
							(stack.getItem() instanceof TridentItem && weaponType.equals("trident")) ||
							(stack.getItem() instanceof AxeItem && weaponType.equals("axe"))) {
						bestSlot = i;
						break;
					}
				}
				if (bestSlot != -1) break;
			}
		}

		// If no weapon is required, fall back to tool selection for blocks
		if (bestSlot == -1 && !isWeaponRequired) {
			if (client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
				BlockHitResult blockHit = (BlockHitResult) client.crosshairTarget;
				BlockState blockState = client.world.getBlockState(blockHit.getBlockPos());

				for (int i = 0; i < 9; i++) {
					ItemStack stack = client.player.getInventory().getStack(i);

					if (stack.isSuitableFor(blockState)) {
						float efficiency = stack.getMiningSpeedMultiplier(blockState);

						if (efficiency > bestEfficiency) {
							bestEfficiency = efficiency;
							bestSlot = i;
						}
					}
				}
			}
		}

		// Switch to the determined slot if it's different from the current slot
		if (bestSlot != -1 && bestSlot != client.player.getInventory().selectedSlot) {
			client.player.getInventory().selectedSlot = bestSlot;
			if (config.isDebugMode()) {
				LOGGER.debug("Switched to the best tool/weapon in slot {}", bestSlot);
			}
		}
	}

}