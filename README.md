# Toolbox Gateway plugin sample

## Quick Installation
To load the plugin into Toolbox App, run: `./gradlew build copyPlugin`

## Manual Installation
Place plugin files in the appropriate directory for your operating system:

- **Windows**: `%LocalAppData%/JetBrains/Toolbox/cache/plugins/plugin-id`
- **macOS**: `~/Library/Caches/JetBrains/Toolbox/plugins/plugin-id`
- **Linux**: `~/.local/share/JetBrains/Toolbox/plugins/plugin-id`

### Required Files
Copy the following files:
- Plugin JAR files
- localization files (should be in `localization` subfolder)
- `extensions.json`
- `icon.svg`


> **Important**: Do not include dependencies that are already present in the Toolbox App to avoid resolution conflicts.