package toolbox.gateway.sample.environment

import com.jetbrains.toolbox.api.remoteDev.states.EnvironmentDescription
import com.jetbrains.toolbox.api.remoteDev.states.RemoteEnvironmentState
import com.jetbrains.toolbox.api.remoteDev.states.StandardRemoteEnvironmentState
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Immutable snapshot of environment configuration data.
 *
 * Data can come from:
 * - Mock data for testing
 */
data class EnvironmentConfig(
    val id: String,
    val name: MutableStateFlow<String>,
    val description: String? = null,
    val host: String? = null,
    val port: Int = 22,
    val username: String? = null,
    val availableIdeProductCodes: List<String> = emptyList(),
    val projectPaths: List<String> = emptyList(),
    val tags: Map<String, String> = emptyMap() // For metadata like "region", "type", etc.
)

/**
 * Represents the current runtime state of an environment.
 * This changes over time as the environment is monitored.
 */
data class EnvironmentRuntimeState(
    val state: RemoteEnvironmentState = StandardRemoteEnvironmentState.Active,
    val description: EnvironmentDescription = EnvironmentDescription.General(null),
    val lastChecked: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)
