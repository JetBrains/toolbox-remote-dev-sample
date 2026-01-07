package toolbox.gateway.sample.datasource

import toolbox.gateway.sample.environment.EnvironmentConfig

/**
 * Note: Returns [EnvironmentConfig], not [RemoteEnvironment].
 * The repository is responsible for creating RemoteEnvironment
 * instances from configs. This separation allows:
 * - Data sources to be pure data fetchers
 * - Caching/lifecycle management in the repository
 * - Easy testing of data sources without UI dependencies
 */
fun interface EnvironmentDataSource {
  /**
   * Fetches current environment configurations.
   * @throws DataSourceException on failure
   */
  suspend fun fetchEnvironments(): List<EnvironmentConfig>
}

/**
 * Exception wrapper for data source errors.
 */
class DataSourceException(
  message: String,
  cause: Throwable? = null
) : Exception(message, cause)

