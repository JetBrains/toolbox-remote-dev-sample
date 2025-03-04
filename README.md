# Toolbox Gateway plugin sample

## Quick Installation
To load the plugin into Toolbox App, run: `./gradlew build copyPlugin`

> **Important**: Do not include dependencies that are already present in the Toolbox App to avoid resolution conflicts.

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

### Localization
To generate localization files:

1. Run the gettext gradle task:
   `./gradlew gettext`
2. The generated `messages.pot` file will be placed in the `resources/` directory.
3. Create translation files (*.po) for each target language based on the POT template
4. Place the localization files in the `resources/localization` subfolder before building the plugin, named like `<languageTag>.po`
