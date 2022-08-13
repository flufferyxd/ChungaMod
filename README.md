# Chungamod for Minecraft 1.12

The other CHungamod repo is on a random website, i put it on github

Chungamod is an open source, funny Minecraft client mod providing a framework for in-game utilities.  The goal of Chungamod is to allow utilities from many projects to be connected under the same user interfaces, which are extensible and provide complex configuration management.

> Discord: https://discord.gg/DeEWQxFr2j

## Installation

1. Install Minecraft 1.12.2
2. Install [Forge](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.12.2.html) for Minecraft 1.12.2
3. Download a Chungamod jar from releases
4. From your Minecraft directory (usually `.minecraft`), put the jar in `mods`

### Plugins

1. Download the jar file of a Chungamod plugin (only from a trusted source)
2. From your Minecraft directory, put the jar in `chungamod/plugins`

### Plugin Development

1. Set up a [Forge mod development environment](https://docs.minecraftforge.net/en/1.12.x/gettingstarted/)
2. Add Chungamod as a dependency
    - easiest way to do this is to open `build.gradle` and add the line `implementation files("path")` in the dependencies block where `path` is the path to your Chungamod jar
3. (Highly recommended) Download the Chungamod source code from the same release as the Chungamod jar, so you can attach the source code in your IDE

## User Interfaces

- **ClickGUI** a highly extensible GUI used to interact with settings of all modules
- **Commands** a simple command system supporting most functionality of the click GUI as well as commands added by plugins

## Structure

- **Plugins** plugins containing modules can be built and easily added by anyone
- **Modules** a module can be turned on and off, can contain settings, and shows up in the click GUI
- **Configs** groups of settings can be saved to a config folder, and multiple configs can be loaded in a specified order at any time
- **Events** still new, an extensible observer system for events is used
