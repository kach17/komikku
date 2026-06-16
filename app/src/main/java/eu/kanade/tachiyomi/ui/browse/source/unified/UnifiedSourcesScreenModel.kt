package eu.kanade.tachiyomi.ui.browse.source.unified

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.domain.extension.interactor.GetExtensionsByType
import eu.kanade.domain.extension.model.Extension
import eu.kanade.tachiyomi.extension.ExtensionManager
import eu.kanade.tachiyomi.extension.model.InstallStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.domain.source.interactor.GetSourcesWithFavoriteCount
import tachiyomi.domain.source.model.Source
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class UnifiedSourcesScreenModel(
    private val getSourcesWithFavoriteCount: GetSourcesWithFavoriteCount = Injekt.get(),
    private val getExtensions: GetExtensionsByType = Injekt.get(),
    private val extensionManager: ExtensionManager = Injekt.get(),
) : StateScreenModel<UnifiedSourcesScreenModel.State>(State()) {

    private val currentDownloads = MutableStateFlow<Map<String, InstallStep>>(hashMapOf())

    init {
        screenModelScope.launchIO {
            combine(
                getExtensions.subscribe(),
                getSourcesWithFavoriteCount.subscribe(),
                currentDownloads,
            ) { (updates, installed, untrusted, available), sourcesWithCount, downloads ->
                val items = installed
                    .map { extension ->
                        val sources = extension.sources.mapNotNull { source ->
                            sourcesWithCount.find { it.source.id == source.id }?.let { (source, count) ->
                                UnifiedSourceItem(source, count)
                            }
                        }
                        val installStep = downloads[extension.pkgName]
                        UnifiedExtensionItem(extension, sources, installStep)
                    }
                state.update { it.copy(items = items, isLoading = false) }
            }
            .distinctUntilChanged()
            .launchIn(screenModelScope)
        }
    }

    fun search(query: String?) {
        state.update { it.copy(searchQuery = query) }
        // TODO: Implement search logic
    }

    fun updateExtension(extension: Extension.Installed) {
        screenModelScope.launchIO {
            extensionManager.updateExtension(extension).collectToInstallUpdate(extension)
        }
    }

    fun cancelInstallUpdateExtension(extension: Extension) {
        extensionManager.cancelInstallUpdateExtension(extension)
    }

    private suspend fun Flow<InstallStep>.collectToInstallUpdate(extension: Extension) =
        this
            .onEach { addDownloadState(extension, it) }
            .takeWhile { it != InstallStep.Installed }
            .onCompletion { removeDownloadState(extension) }
            .collect()

    private fun addDownloadState(extension: Extension, installStep: InstallStep) {
        currentDownloads.update { it + (extension.pkgName to installStep) }
    }

    private fun removeDownloadState(extension: Extension) {
        currentDownloads.update { it - extension.pkgName }
    }

    data class State(
        val searchQuery: String? = null,
        val items: List<UnifiedExtensionItem> = emptyList(),
        val isLoading: Boolean = true,
    )

    data class UnifiedExtensionItem(
        val extension: Extension.Installed,
        val sources: List<UnifiedSourceItem>,
        val installStep: InstallStep?,
    )

    data class UnifiedSourceItem(
        val source: Source,
        val count: Long,
    )
}
