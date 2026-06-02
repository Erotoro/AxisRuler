# AxisRuler

AxisRuler is a client-side Fabric Minecraft mod for precise visual block measurement.

It shows exact X, Y, and Z size in-world, renders the measured area around your selection, and keeps the result readable while you build.

## What It Does

AxisRuler gives you:

- two-point measurement with exact axis readouts
- live preview after Point A before you place Point B
- in-world selection box, guides, labels, and line rendering
- pinned measurements you can keep on screen while working
- HUD readout for the current measurement
- color customization and style presets

## Use

AxisRuler works with `Alt` + keybinds.

Basic workflow:

1. Look at a block and press `Alt + Z` to set Point A
2. Look at another block and press `Alt + X` to set Point B
3. AxisRuler will show the X / Y / Z size in-world and in the HUD

After Point A is set, the mod shows a live preview on the block under your crosshair before you confirm Point B.

Default controls:

- `Alt + Z` set Point A
- `Alt + X` set Point B
- `Alt + C` clear points
- `Alt + V` swap points
- `Alt + R` cycle measurement mode
- `Alt + H` toggle HUD
- `Alt + G` toggle guides
- `Alt + L` toggle labels
- `Alt + J` toggle connection line
- `Alt + M` copy current measurement
- `Alt + P` pin current measurement
- `Alt + U` clear all pinned measurements

Pinned measurements let you keep finished boxes visible while working on something else.

You can change the keybinds in:

`Options -> Controls -> AxisRuler`

## Configuration

AxisRuler includes visual configuration for supported builds with Mod Menu integration.

You can adjust:

- overlay colors
- HUD position
- HUD scale
- visible overlay elements
- presets
- custom preset values

## Supported Versions

AxisRuler currently supports:

- Minecraft `1.21.4`
- Minecraft `1.21.8`
- Minecraft `1.21.11`
- Minecraft `26.1`, `26.1.1`, `26.1.2`

There are separate release files for each Minecraft branch.

Release files:

- `axisruler-mc1214-1.1.0.jar`
- `axisruler-mc1218-1.1.0.jar`
- `axisruler-mc12111-1.1.0.jar`
- `axisruler-mc261x-1.1.0.jar`

## Install

You should have:

- Fabric Loader
- Fabric API
- Mod Menu for config screen support

Then place the correct AxisRuler jar into your `mods` folder.

## Notes

- Fabric only
- Client-side only
- Safe on multiplayer clients
- No server installation required
