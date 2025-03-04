package toolbox.gateway.sample

import com.jetbrains.toolbox.api.core.util.LoadableState
import com.jetbrains.toolbox.api.remoteDev.environments.CachedIdeStub
import com.jetbrains.toolbox.api.remoteDev.environments.CachedProjectStub
import com.jetbrains.toolbox.api.remoteDev.environments.ManualEnvironmentContentsView
import kotlinx.coroutines.flow.MutableStateFlow


class SampleEnvironmentContentsView : ManualEnvironmentContentsView {
    override val ideListState: MutableStateFlow<LoadableState<List<CachedIdeStub>>> = MutableStateFlow(
        LoadableState.Value(
            listOf(
                object : CachedIdeStub {
                    override val productCode: String = "IU-231.8770.65"
                    override fun isRunning(): Boolean? = null

                }
            )
        )
    )
    override val projectListState: MutableStateFlow<LoadableState<List<CachedProjectStub>>> = MutableStateFlow(
        LoadableState.Value(
            listOf(
                object : CachedProjectStub {
                    override val path: String = "/root/IdeaProjects/amazing-project"
                }
            )
        )
    )
}