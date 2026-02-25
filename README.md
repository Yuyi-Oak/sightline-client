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
- Locale bundles: `en_us`, `zh_cn`, `ru_ru`, `fr_fr`, `de_de`.
