package toolbox.gateway.sample.environment

import com.jetbrains.toolbox.api.core.diagnostics.Logger
import com.jetbrains.toolbox.api.localization.LocalizableStringFactory
import com.jetbrains.toolbox.api.remoteDev.EnvironmentVisibilityState
import com.jetbrains.toolbox.api.remoteDev.RemoteProviderEnvironment
import com.jetbrains.toolbox.api.remoteDev.environments.EnvironmentContentsView
import com.jetbrains.toolbox.api.remoteDev.states.EnvironmentDescription
import com.jetbrains.toolbox.api.remoteDev.states.RemoteEnvironmentState
import com.jetbrains.toolbox.api.remoteDev.states.StandardRemoteEnvironmentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


/**
 * Reactive wrapper around [EnvironmentConfig] that satisfies the Toolbox API.
 */
class RemoteEnvironment(
  private val initialConfig: EnvironmentConfig,
  private val contentsViewFactory: EnvironmentContentsViewFactory = ManualContentsViewFactory(),
  private val localizableStringFactory: LocalizableStringFactory,
  private val logger: Logger
) : RemoteProviderEnvironment(initialConfig.id) {

  // Mutable internal state
  private var _currentConfig: EnvironmentConfig = initialConfig
  private val _state = MutableStateFlow<RemoteEnvironmentState>(StandardRemoteEnvironmentState.Active)
  private val _description = MutableStateFlow<EnvironmentDescription>(
    EnvironmentDescription.General(initialConfig.description?.let { localizableStringFactory.ptrl(it) })
  )
  private var _visible: Boolean = false

  //  Public reactive properties (observed by Toolbox UI)
  override var nameFlow: MutableStateFlow<String> = _currentConfig.name

  override val state: MutableStateFlow<RemoteEnvironmentState> = _state

  override val description: MutableStateFlow<EnvironmentDescription> = _description

  override suspend fun getContentsView(): EnvironmentContentsView {
    logger.debug("PLUGIN: getContentsView called for id='${initialConfig.id}', name='${_currentConfig.name}'")
    val view = contentsViewFactory.create(_currentConfig)
    logger.debug("PLUGIN: Created view with ${_currentConfig.availableIdeProductCodes.size} IDEs, ${_currentConfig.projectPaths.size} projects")
    return view
  }

  override fun setVisible(visibilityState: EnvironmentVisibilityState) {
    _visible = visibilityState.statusVisible
  }

  override fun onDelete() {
    // It's recommended that you add any cleanup logic here.
  }

  // Update methods (called by Repository)
  fun updateState(newState: RemoteEnvironmentState, errorMessage: String? = null) {
    _state.update { newState }
    if (errorMessage != null) {
      _description.update { EnvironmentDescription.General(localizableStringFactory.ptrl(errorMessage)) }
    }
  }

  fun updateConfig(newConfig: EnvironmentConfig) {
    require(newConfig.id == initialConfig.id) { logger.info("Cannot change environment ID for ${initialConfig.id}") }
    _currentConfig = newConfig
    _description.update {
      EnvironmentDescription.General(newConfig.description?.let { text ->
        localizableStringFactory.ptrl(text)
      })
    }
  }

  fun getConfig(): EnvironmentConfig = _currentConfig
}

/**
 * Extension function to create RemoteEnvironment from config.
 */
fun EnvironmentConfig.toRemoteEnvironment(
  factory: EnvironmentContentsViewFactory = ManualContentsViewFactory(),
  localizableStringFactory: LocalizableStringFactory,
  logger: Logger
): RemoteEnvironment {
  return RemoteEnvironment(this, factory, localizableStringFactory, logger)
}