package toolbox.gateway.sample.environment

import com.jetbrains.toolbox.api.remoteDev.environments.EnvironmentContentsView

/**
 * Interface for creating environment content views.
 *
 * Different implementations support different integration types:
 * - ManualEnvironmentContentsView: Static lists that you control
 * - SSHEnvironmentContentsView: Toolbox handles SSH connection
 * - AgentConnectionBasedEnvironmentContentsView: Custom agent protocol
 */
fun interface EnvironmentContentsViewFactory {
  /**
   * Creates the appropriate contents view for the given environment.
   *
   * @param config The environment's configuration data
   * @return An EnvironmentContentsView implementation
   */
  suspend fun create(config: EnvironmentConfig): EnvironmentContentsView
}
