package toolbox.gateway.sample

import com.jetbrains.toolbox.api.remoteDev.EnvironmentVisibilityState
import com.jetbrains.toolbox.api.remoteDev.RemoteProviderEnvironment
import com.jetbrains.toolbox.api.remoteDev.environments.EnvironmentContentsView
import com.jetbrains.toolbox.api.remoteDev.states.EnvironmentDescription
import com.jetbrains.toolbox.api.remoteDev.states.RemoteEnvironmentState
import com.jetbrains.toolbox.api.remoteDev.states.StandardRemoteEnvironmentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class SampleRemoteEnvironment(
    private val environment: EnvironmentDTO,
    override var name: String = environment.name,
) : RemoteProviderEnvironment(environment.id) {
    override val state: StateFlow<RemoteEnvironmentState> = MutableStateFlow(StandardRemoteEnvironmentState.Active)
    override val description: StateFlow<EnvironmentDescription> = MutableStateFlow(EnvironmentDescription.General(null))

    override suspend fun getContentsView(): EnvironmentContentsView {
        return SampleEnvironmentContentsView()
    }

    override fun setVisible(visibilityState: EnvironmentVisibilityState) {
    }

    override fun onDelete() {
    }
}