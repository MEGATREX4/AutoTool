**AutoTool** is a lightweight and customizable Minecraft client-side mod that automatically switches to the best tool or weapon in your inventory based on the target you're interacting with. Whether you're mining blocks or fighting mobs, the mod ensures you always use the most efficient tool or weapon for the job.

## Features:
- **Automatic Tool Selection**: Switches to the most suitable tool for breaking blocks.
- **Automatic Weapon Selection**: Automatically equips the best weapon when attacking mobs or entities.
- **Customizable Keybinding**: Toggle the auto-switch feature on or off with a keybinding.

## Configuration:
The mod includes a user-friendly configuration file (`autotool.json`) where you can:
- **Customize Weapon Prioritization**: Define the order of weapon selection using the `weaponOrder` list (e.g., `"sword"`, `"trident"`, `"axe"`).
- **Enable Debug Mode**: Toggle detailed logging for better debugging by setting `debugMode` to `true` or `false`.
- **Toggle Auto Switch**: Control whether the auto-switch feature is active with `enableAutoSwitch`.

### Example Configuration:
```json
{
  "weaponOrder": [
    "sword",
    "trident",
    "axe"
  ],
  "debugMode": true,
  "enableAutoSwitch": true
}
```
