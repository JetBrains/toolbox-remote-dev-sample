package toolbox.gateway.sample

import com.jetbrains.toolbox.api.core.util.LoadableState
import com.jetbrains.toolbox.api.remoteDev.ProviderVisibilityState
import com.jetbrains.toolbox.api.remoteDev.RemoteProvider
import kotlinx.coroutines.flow.MutableStateFlow
import toolbox.gateway.sample.environment.RemoteEnvironment
import java.net.URI

/**
 * Remote Provider implementation that delegates environment management
 * to [EnvironmentRepository].
 */
class SampleRemoteProvider(
  repository: EnvironmentRepository
) : RemoteProvider("Sample Provider") {

  override val environments: MutableStateFlow<LoadableState<List<RemoteEnvironment>>> =
    repository.environments

  override val canCreateNewEnvironments: Boolean = false
  override val isSingleEnvironment: Boolean = false

  override fun setVisible(visibilityState: ProviderVisibilityState) {}

  // It's recommended you implement this handleURI method to support opening environments from a link
  override suspend fun handleUri(uri: URI) {}

  override fun close() {}
}
