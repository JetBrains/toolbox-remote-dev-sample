# JetBrains Toolbox Plugin Development Template

A starter template for building JetBrains Toolbox App plugins with Gradle. Use this project as a foundation for creating your own Toolbox plugins.

### Prerequisites

- **JDK 21** or later
- **JetBrains Toolbox App**

### Implementation Notes

- The first major decision is whether you want to connect via `AgentConnectionBasedEnvironmentContentsView` or `SshEnvironmentContentsView`
   - `SshEnvironmentContentsView` is a much easier implementation.
- There are no examples of UI in this template, you can make one that includes additional authentication as needed.
   - More details on that, see the [Toolbox UI](https://www.jetbrains.com/help/toolbox-app/ui-api.html#toolboxUI)
- This is separated as logically as I could with the following details:
   - `/datasource` should contain all logic pertaining to retreiving and managing the environment data.
   - `/environment` should contain all logic pertaining to making that data accessable to Toolbox.
      - I use `ManualEnvironmentContentsView` because the data in this sample all hardcoded.
      - You can use `AgentConnectionBasedEnvironmentContentsView` or `SshEnvironmentContentsView` instead and rely on `ManualEnvironmentContentsView` for fallback data.
      - For more complex scenarios, you can use `PortForwardingCapableEnvironmentContentsView`.

### Commands

| Command                      | Description                           |
|------------------------------|---------------------------------------|
| `./gradlew :plugin:assemble` | Build the plugin ZIP                  |
| `./gradlew :plugin:build`    | Build and run tests (none here)       |
| `./gradlew clean`            | Clean all build outputs               |
| `./gradlew installPlugin`    | Build and install directly to Toolbox |


### Installing Your Plugin

Once built and assembled, you can install the plugin directly into Toolbox using `./gradlew installPlugin` task or adding the plugin files to the folowing default directory:

- Windows: `%LocalAppData%/JetBrains/Toolbox/cache/plugins/plugin-id`
- macOS: `~/Library/Caches/JetBrains/Toolbox/plugins/plugin-id`
- Linux: `~/.local/share/JetBrains/Toolbox/plugins/plugin-id`

### Resources

See the [Toolbox API documentation](https://www.jetbrains.com/help/toolbox-app/) for detailed usage.

See [Coder's Open-Sourced Plugin](https://github.com/coder/coder-jetbrains-toolbox/tree/main) for a live example of a Toolbox Plugin.
