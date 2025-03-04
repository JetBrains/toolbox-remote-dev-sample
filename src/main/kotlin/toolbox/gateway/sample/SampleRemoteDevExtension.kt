package toolbox.gateway.sample

import com.jetbrains.toolbox.api.core.ServiceLocator
import com.jetbrains.toolbox.api.remoteDev.RemoteDevExtension
import com.jetbrains.toolbox.api.remoteDev.RemoteProvider

class SampleRemoteDevExtension : RemoteDevExtension {
    override fun createRemoteProviderPluginInstance(serviceLocator: ServiceLocator): RemoteProvider {
        return SampleRemoteProvider(serviceLocator)
    }
}
