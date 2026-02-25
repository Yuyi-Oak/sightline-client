# Sightline Client

Optional NeoForge client module for CSMC that provides advanced HUD rendering, camera features, and input integrity hooks.

## Development
- Java 21
- Minecraft 1.21.11 / NeoForge 21.11.x

## Notes
This client is optional. The server plugin runs without it, but enabling it improves HUD fidelity and anti-cheat signals.

## Current Features
- Lightweight in-game HUD panel (HP/Armor/FPS/Ping).
- HUD visibility hotkey: `H` (rebindable in controls under Sightline category).
- Camera view cycle hotkey: `V` (first-person -> third-back -> third-front).
- HUD density hotkey: `J` (full/compact).
- HUD position hotkey: `K` (top-left -> top-right -> bottom-right -> bottom-left).
- HUD opacity hotkeys: `U`/`I` (decrease/increase opacity).
- Full HUD mode now includes a layout line (mode, anchor, opacity).
- HUD now shows current spectating target while in spectator mode (free camera/player/entity).
- HUD panel width now auto-expands for long weapon/spectator labels (with sane max width cap).
- HUD layout reset hotkey: `O` (resets mode, position, opacity).
- Reticle toggle hotkey: `M`.
- Reticle rendering is suppressed in spectator mode.
- Reticle gap dynamically expands while moving or airborne.
- Reticle now shifts to warning colors on low/empty magazine when ammo data is available.
- Full HUD now reads weapon ammo from CSMC item names (e.g., `[30/90]`) and renders `Ammo: mag/reserve`.
- Full HUD now also shows parsed weapon name from main-hand CSMC item display.
- Compact HUD now renders both weapon and ammo lines (same parsed source).
- Locale bundles: `en_us`, `zh_cn`, `ru_ru`, `fr_fr`, `de_de`.
