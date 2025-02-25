package toolbox.gateway.sample

import com.jetbrains.toolbox.api.remoteDev.AbstractRemoteProviderEnvironment
import com.jetbrains.toolbox.api.remoteDev.EnvironmentVisibilityState
import com.jetbrains.toolbox.api.remoteDev.environments.EnvironmentContentsView
import java.util.concurrent.CompletableFuture

class SampleRemoteEnvironment(
    private val environment: EnvironmentDTO,
    override var name: String = environment.name
) : AbstractRemoteProviderEnvironment(environment.id) {
    override suspend fun getContentsView(): EnvironmentContentsView {
        return SampleEnvironmentContentsView()
    }

    override fun setVisible(visibilityState: EnvironmentVisibilityState) {
    }

    override fun onDelete() {
    }
}