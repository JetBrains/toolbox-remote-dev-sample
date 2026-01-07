package toolbox.gateway.sample.environment

import com.jetbrains.toolbox.api.core.util.LoadableState
import com.jetbrains.toolbox.api.remoteDev.environments.CachedIdeStub
import com.jetbrains.toolbox.api.remoteDev.environments.CachedProject
import com.jetbrains.toolbox.api.remoteDev.environments.EnvironmentContentsView
import com.jetbrains.toolbox.api.remoteDev.environments.ManualEnvironmentContentsView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Creates a ManualEnvironmentContentsView from static configuration.
 * 
 * Use this when:
 * - You know the available IDEs and projects upfront
 * - Data comes from your own API/config
 * - You want full control over what's displayed
 */
class ManualContentsViewFactory : EnvironmentContentsViewFactory {
    
    override suspend fun create(config: EnvironmentConfig): EnvironmentContentsView {
        val ides = config.availableIdeProductCodes.map { productCode ->
            SimpleIdeStub(productCode)
        }
        
        val projects = config.projectPaths.map { path ->
            CachedProject(path)
        }
        
        return SimpleManualContentsView(ides, projects)
    }
}

/**
 * Simple, immutable implementation of ManualEnvironmentContentsView.
 */
class SimpleManualContentsView(
    ides: List<CachedIdeStub>,
    projects: List<CachedProject>
) : ManualEnvironmentContentsView {
    
    // Expose as immutable flows - data is set once at construction
    override val ideListState: Flow<LoadableState<List<CachedIdeStub>>> =
        MutableStateFlow(LoadableState.Value(ides))

    override val projectListState: Flow<LoadableState<List<CachedProject>>> =
        MutableStateFlow(LoadableState.Value(projects))
}

/**
 * IDE stub with known state.
 */
data class SimpleIdeStub(
    override val productCode: String,
    private val running: Boolean? = null
) : CachedIdeStub {
    override fun isRunning(): Boolean? = running
}