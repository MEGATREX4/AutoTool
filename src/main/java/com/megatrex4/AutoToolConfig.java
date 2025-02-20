package com.megatrex4;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.*;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.List;

@Config(name = "autotool")
public class AutoToolConfig implements ConfigData {

    @Comment("The order of weapons to prioritize when switching tools.")
    private List<String> weaponOrder = List.of("minecraft:sword", "minecraft:trident", "minecraft:axe");

    @Comment("Enable or disable debug mode.")
    private boolean debugMode = false;

    @Comment("Additional settings for tool behavior.")
    private boolean enableAutoSwitch = true;

    // Getters for configuration values
    public List<String> getWeaponOrder() {
        return weaponOrder;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isEnableAutoSwitch() {
        return enableAutoSwitch;
    }
}
