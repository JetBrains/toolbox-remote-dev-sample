package toolbox.gateway.sample

import com.jetbrains.toolbox.api.core.ServiceLocator
import com.jetbrains.toolbox.api.core.diagnostics.Logger
import com.jetbrains.toolbox.api.core.ui.icons.SvgIcon
import com.jetbrains.toolbox.api.core.util.LoadableState
import com.jetbrains.toolbox.api.localization.LocalizableStringFactory
import com.jetbrains.toolbox.api.remoteDev.ProviderVisibilityState
import com.jetbrains.toolbox.api.remoteDev.RemoteProvider
import com.jetbrains.toolbox.api.remoteDev.RemoteProviderEnvironment
import com.jetbrains.toolbox.api.remoteDev.ui.EnvironmentUiPageManager
import com.jetbrains.toolbox.api.ui.ToolboxUi
import com.jetbrains.toolbox.api.ui.actions.RunnableActionDescription
import com.jetbrains.toolbox.api.ui.components.CheckboxField
import com.jetbrains.toolbox.api.ui.components.LabelField
import com.jetbrains.toolbox.api.ui.components.TextField
import com.jetbrains.toolbox.api.ui.components.TextType
import com.jetbrains.toolbox.api.ui.components.UiField
import com.jetbrains.toolbox.api.ui.components.UiPage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language
import java.net.URI
import kotlin.time.Duration.Companion.seconds

class SampleRemoteProvider(
    private val serviceLocator: ServiceLocator,
) : RemoteProvider("Sample Provider") {
    private val scope = serviceLocator.getService(CoroutineScope::class.java)
    private val logger = serviceLocator.getService(Logger::class.java)
    private val i18n = serviceLocator.getService(LocalizableStringFactory::class.java)

    override val environments: MutableStateFlow<LoadableState<List<RemoteProviderEnvironment>>> = MutableStateFlow(
        LoadableState.Value(
            listOf(
                SampleRemoteEnvironment(EnvironmentDTO("first", "Vlad"))
            )
        )
    )

    init {
        scope.launch {
            while (true) {
                try {
                    logger.debug("Updating remote environments for Sample Plugin")
//                    val response = httpClient.newCall(request).await()
//                    val body = response.body ?: continue
                    @Language("json")
                    val body = """
                        { 
                            "environments": [
                                { 
                                    "id": "perfectly.normal.identifier",
                                    "name": "My shiny new environment"
                                }
                            ]
                        }
                    """.trimIndent()
                    val dto = Json.decodeFromString(EnvironmentsDTO.serializer(), body)
                    try {
                        environments.value = LoadableState.Value(dto.environments.map { SampleRemoteEnvironment(it) })
                    } catch (_: CancellationException) {
                        logger.debug("Environments update cancelled")
                        break
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to retrieve environments: ${e.message}")
                }
                // only for demo purposes!
                delay(3.seconds)
            }
        }
    }

    override fun close() {}

    override val svgIcon: SvgIcon = SvgIcon(
        this::class.java.getResourceAsStream("/icon.svg")?.readAllBytes() ?: byteArrayOf(),
        SvgIcon.IconType.Default
    )

    override val canCreateNewEnvironments: Boolean = true
    override val isSingleEnvironment: Boolean = false

    override fun setVisible(visibilityState: ProviderVisibilityState) {}

    override fun getNewEnvironmentUiPage(): UiPage? {
        return object : UiPage(i18n.pnotr("Sample")) {
            override val fields: StateFlow<List<UiField>> = MutableStateFlow(
                listOf(
                    LabelField(i18n.ptrl("This is UI example")),
                    CheckboxField(false, i18n.ptrl("Warmup")),
                    TextField(i18n.ptrl("Name"), "", TextType.General),
                )
            )
        }
    }

    override fun getOverrideUiPage(): UiPage? {
        if (confirmedDummyPage) return null

        return object : UiPage(i18n.ptrl("Sample plugin login")) {
            val page = this

            override val fields: StateFlow<List<UiField>> = MutableStateFlow(
                listOf(
                    LabelField(i18n.ptrl("Just click that button to pretend you logged in")),
                )
            )

            override val actionButtons: StateFlow<List<RunnableActionDescription>> =
                MutableStateFlow(
                    listOf(object : RunnableActionDescription {
                        override fun run() {
                            confirmedDummyPage = true
                            val uiService = serviceLocator.getService(ToolboxUi::class.java)
                            uiService.hideUiPage(page)
                            val sshUiService = serviceLocator.getService(EnvironmentUiPageManager::class.java)
                            sshUiService.showPluginEnvironmentsPage(false)
                        }

                        override val label = i18n.ptrl("Login")
                        override val shouldClosePage = false
                    })
                )

            override val svgIcon: SvgIcon? = null
        }
    }

    override suspend fun handleUri(uri: URI) {
        logger.debug { "External request: $uri" }
    }

    private var confirmedDummyPage = false
}
