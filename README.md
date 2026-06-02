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

Set Point A, then Point B.

The mod shows:

- width on X
- height on Y
- depth on Z
- the measured box in-world
- the current result in the HUD

After Point A is set, AxisRuler previews the result on the block under your crosshair before you commit Point B.

You can also pin the current finished measurement and keep it visible while working on something else.

Default controls:

- `Alt + P` pin current measurement
- `Alt + U` clear all pinned measurements

Pinned measurements are session-only and clear when you leave the world.

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

Release files in `download/`:

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
