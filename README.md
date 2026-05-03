# AxisRuler

Precise visual measurement for Minecraft builders who want clean results, reliable numbers, and a better building workflow.

AxisRuler gives you exact spatial feedback directly in the world, so you can build with confidence instead of counting blocks by hand or placing temporary markers.

## Overview

AxisRuler is a client-side Fabric mod focused on one job: making measurement and alignment feel immediate, readable, and dependable.

It is built for players who care about structure, symmetry, spacing, and visual control. Whether you are planning an interior, sizing a hallway, checking roof height, or refining a large-scale build, AxisRuler keeps the information you need visible without cluttering the screen.

The goal is simple:

- reduce guesswork
- speed up building decisions
- keep the interface clean
- make precision feel natural

## Why AxisRuler

Good building tools should be quiet, fast, and trustworthy.

AxisRuler helps you:

- understand dimensions instantly
- see axis-based size at a glance
- check alignment before placing blocks
- keep measurement data visible while building
- adjust the visual style to fit your UI and shader setup

It is designed to feel like part of a polished building workflow, not an overlay fighting for attention.

## Key Features

- Two-point measurement with clear X, Y, and Z readouts
- Clean axis visualization for fast spatial understanding
- Distance and alignment help directly in the world
- Compact HUD for always-available measurement data
- Overlay labels, guides, selection box, and connection line
- Full color control for overlay and HUD elements
- Built-in style presets plus custom preset saving
- Live preview while editing colors and layout
- Lightweight rendering designed for normal play sessions
- Client-side only, with no server-side setup required

## How to Use

### Start measuring

Choose Point A and Point B in the world to define a measured area.

AxisRuler immediately shows:

- width on the X axis
- height on the Y axis
- depth on the Z axis
- visual selection guides
- current measurement values in the HUD

### Open the config

Use Mod Menu to open the AxisRuler configuration screen when running a supported release that includes the integration.

From there, you can:

- tune colors
- move and scale the HUD
- enable or disable overlay elements
- apply presets
- save your own look

### Adjust the look

If you prefer subtle overlays, bright high-contrast colors, or a more technical UI style, AxisRuler can be shaped around your setup without leaving the game.

## Customization

AxisRuler is not limited to a single look. Its visual system is built to be practical and easy to tune.

### Palette system

Separate color controls let you style both the world overlay and the HUD with precision.

You can customize:

- point colors
- measurement box color
- axis guide colors
- line color
- label colors
- HUD text colors
- HUD accent, background, border, and warning colors

### HUD control

The HUD is designed to stay useful without becoming noisy.

You can adjust:

- anchor position
- offsets
- scale
- compact mode
- text shadow
- background and border intensity

### Presets

Built-in presets make it easy to switch between different visual styles. If you already know what works for your UI, you can save a custom preset and keep it ready.

### Live preview

Changes apply immediately, so visual tuning feels direct instead of trial-and-error.

## Performance

AxisRuler is designed for regular gameplay, long building sessions, and large projects.

- Optimized overlay rendering
- No heavy background systems
- Minimal visual overhead
- Works well with performance-focused clients, including Sodium

The mod aims to stay responsive and readable even when it becomes part of your everyday toolset.

## Compatibility

- Fabric
- Legacy `1.21.11` release line preserved in branch `legacy/1.21.11`
- `26.1.2` migration work isolated in branch `port/26.1.2` until the new release is complete
- Client-side only
- Safe to use on multiplayer servers
- No server installation required

AxisRuler does not depend on server support, which makes it easy to keep in your standard client mod pack.

## Philosophy

AxisRuler is built around precision, restraint, and usability.

It gives serious builders the numbers and visual guidance they need, in a form that stays clear, calm, and dependable while they work.
