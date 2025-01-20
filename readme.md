# CCustomores

## **Overview**
**CCustomores** is a Minecraft plugin that introduces a custom ore generation system with configurable effects, spawn rates, and unique properties. It also includes a randomized chest spawning system to enhance gameplay.

## **Features**
- **Custom Ore Spawning**  
  - Generate ores dynamically based on a configuration file.
  - Assign custom light levels and potion effects to ores.
  - Ensure ores only spawn underground, following configurable rules.

- **Randomized Chest Spawning**  
  - Chests spawn randomly when a player enters a chunk.
  - Configurable loot tables define what items can appear in chests.
  - Each chest spawn chance is customizable.

- **Full Configuration Support**  
  - Modify ores and their properties in `ores.yml`.
  - Customize messages in `messages.yml`.

## **Installation**
1. Download the `CCustomores.jar` file.
2. Place it in the `plugins` folder of your Minecraft server.
3. Restart or reload the server.

## **Configuration**
- Edit `ores.yml` to define custom ores.
- Modify `messages.yml` to change plugin messages.
- Update `plugin.yml` if necessary for version compatibility.

## **Commands**
| Command | Description |
|---------|-------------|
| `/ccustomores reload` | Reloads the configuration files. |
| `/ccustomores give <orename> <player> <amount>` | Gives a player a specific custom ore. |
| `/ccustomores list` | Displays all available custom ores. |

## **Permissions**
| Permission | Description |
|------------|-------------|
| `ccustomores.admin` | Allows access to all admin commands. |
| `ccustomores.use` | Grants permission to use ores with special effects. |

## **Support & Contributions**
- Report issues and suggest features via GitHub.
- Contributions via pull requests are welcome!
