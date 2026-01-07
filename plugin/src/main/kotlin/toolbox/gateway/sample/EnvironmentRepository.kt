package toolbox.gateway.sample

import com.jetbrains.toolbox.api.core.diagnostics.Logger
import com.jetbrains.toolbox.api.core.util.LoadableState
import com.jetbrains.toolbox.api.localization.LocalizableStringFactory
import com.jetbrains.toolbox.api.remoteDev.states.RemoteEnvironmentState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import toolbox.gateway.sample.datasource.DataSourceException
import toolbox.gateway.sample.datasource.EnvironmentDataSource
import toolbox.gateway.sample.environment.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Repository managing the lifecycle of remote environments.
 * 
 * Responsibilities:
 * - Fetches environment configs from data sources
 * - Creates/updates RemoteEnvironment instances
 * - Manages background polling
 * - Exposes reactive state for the provider
 *
 */
class EnvironmentRepository(
  private val dataSource: EnvironmentDataSource,
  private val logger: Logger,
  private val coroutineScope: CoroutineScope,
  private val contentsViewFactory: EnvironmentContentsViewFactory = ManualContentsViewFactory(),
  private val refreshInterval: Duration = 10.minutes,
  private val localizableStringFactory: LocalizableStringFactory
) {
  // Internal mutable state
  private val _environments = MutableStateFlow<LoadableState<List<RemoteEnvironment>>>(
    LoadableState.Loading
  )

  // Cache of created environments by ID - allows updating existing instances
  private val environmentCache = mutableMapOf<String, RemoteEnvironment>()

  // Observable environment list for the provider
  val environments: MutableStateFlow<LoadableState<List<RemoteEnvironment>>> = _environments

  fun startPolling() {
    coroutineScope.launch(CoroutineName("EnvironmentRepository-Polling")) {
      // Initial fetch
      refreshEnvironments()

      // Periodic refresh
      while (isActive) {
        delay(refreshInterval)
        refreshEnvironments()
      }
    }
  }

  suspend fun refreshEnvironments() {
    logger.debug("Refreshing environments from ${dataSource::class.simpleName}")

    try {
      val configs = dataSource.fetchEnvironments()
      val environments = configs.map { config ->
        getOrCreateEnvironment(config)
      }

      // Remove environments that no longer exist
      val currentIds = configs.map { it.id }.toSet()
      environmentCache.keys.removeAll { it !in currentIds }

      _environments.value = LoadableState.Value(environments)
      logger.info("PLUGIN: Setting environments to ${environments.size} items: ${environments.map { it.id }}")

    } catch (e: CancellationException) {
      throw e
    } catch (e: DataSourceException) {
      logger.error("Data source error: ${e.message}")
    } catch (e: Exception) {
      logger.error("Unexpected error: ${e.message}")
    }
  }

  /**
   * Gets an existing environment or creates a new one.
   * This preserves reactive subscriptions when refreshing.
   */
  private fun getOrCreateEnvironment(config: EnvironmentConfig): RemoteEnvironment {
    return environmentCache.getOrPut(config.id) {
      logger.debug("Creating new environment: ${config.id}")
      config.toRemoteEnvironment(contentsViewFactory, localizableStringFactory, logger)
    }.also { existingEnv ->
      // Update config if it changed
      if (existingEnv.getConfig() != config) {
        logger.debug("Updating environment config: ${config.id}")
        existingEnv.updateConfig(config)
      }
    }
  }

  /**
   * Manually update a specific environment's state.
   * Useful for health check results, error reporting, etc.
   */
  fun updateEnvironmentState(
    environmentId: String,
    state: RemoteEnvironmentState,
    errorMessage: String? = null
  ) {
    environmentCache[environmentId]?.updateState(state, errorMessage)
      ?: logger.warn("Cannot update unknown environment: $environmentId")
  }

  /**
   * Get a specific environment by ID.
   */
  fun getEnvironment(id: String): RemoteEnvironment? = environmentCache[id]
}