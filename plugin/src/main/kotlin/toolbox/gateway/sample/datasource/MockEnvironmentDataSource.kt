package toolbox.gateway.sample.datasource

import com.jetbrains.toolbox.api.core.diagnostics.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import toolbox.gateway.sample.environment.EnvironmentConfig

/**
 * Mock data source returning static configurations.
 */
class MockEnvironmentDataSource(
  private val configs: List<EnvironmentConfig> = DEFAULT_CONFIGS,
  logger: Logger
) : EnvironmentDataSource {

  override suspend fun fetchEnvironments(): List<EnvironmentConfig> = configs

  companion object {
    val DEFAULT_CONFIGS = listOf(
      EnvironmentConfig(
        id = "dev-01",
        name = MutableStateFlow("Backend Dev Space"),
        description = "[Mocked] Environment with all backend dependencies.",
        host = "example.example.com",
        availableIdeProductCodes = listOf("IU", "WS"),
        projectPaths = listOf("/home/dev/webapp", "/home/dev/api"),
      ),
      EnvironmentConfig(
        id = "dev-02",
        name = MutableStateFlow("Frontend Dev Space"),
        description = "[Mocked] Environment with all front-end dependencies.",
        host = "example.example.com",
        port = 2222,
        availableIdeProductCodes = listOf("IU"),
        projectPaths = listOf("/opt/app"),
      )
    )
  }
}
