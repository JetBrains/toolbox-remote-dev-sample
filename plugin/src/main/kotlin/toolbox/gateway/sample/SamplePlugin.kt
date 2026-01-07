package toolbox.gateway.sample

import com.jetbrains.toolbox.api.core.ServiceLocator
import com.jetbrains.toolbox.api.core.diagnostics.Logger
import com.jetbrains.toolbox.api.localization.LocalizableStringFactory
import com.jetbrains.toolbox.api.remoteDev.RemoteDevExtension
import com.jetbrains.toolbox.api.remoteDev.RemoteProvider
import kotlinx.coroutines.CoroutineScope
import toolbox.gateway.sample.datasource.EnvironmentDataSource
import toolbox.gateway.sample.datasource.MockEnvironmentDataSource


/**
 * Sample Plugin meant to help developers bootstrap a Toolbox Plugin for custom management of remote environments.
 *
 * Architecture:
 * ```
 * DataSource ──▶ EnvironmentConfig ──▶ Repository ──▶ RemoteEnvironment ──▶ Provider ──▶ Toolbox UI
 *                                          │
 *                                          └── Manages lifecycle, caching, updates
 * ```
 **/
class SampleRemoteDevExtension : RemoteDevExtension {
  override fun createRemoteProviderPluginInstance(serviceLocator: ServiceLocator): RemoteProvider {
    val logger = serviceLocator.getService(Logger::class.java)
    val coroutineScope = serviceLocator.getService(CoroutineScope::class.java)
    val localizableStringFactory = serviceLocator.getService(LocalizableStringFactory::class.java)

    // Single data source, swap implementation as needed
    val dataSource = createDataSource(logger)

    // Initialized and manages your environments
    val repository = EnvironmentRepository(
      dataSource = dataSource,
      coroutineScope = coroutineScope,
      logger = logger,
      localizableStringFactory = localizableStringFactory
    )

    // Periodically refresh environments from the data source
    repository.startPolling()

    logger.info("Sample Remote Provider initialized with ${dataSource::class.simpleName}")
    return SampleRemoteProvider(repository)
  }

  private fun createDataSource(logger: Logger): EnvironmentDataSource {
    // It's recommended you implement your custom data source logic here.

    // Default to mock data
    return MockEnvironmentDataSource(logger = logger)
  }
}
