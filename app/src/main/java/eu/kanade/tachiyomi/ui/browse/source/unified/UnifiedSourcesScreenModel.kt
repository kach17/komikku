package eu.kanade.tachiyomi.ui.browse.source.unified

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.domain.source.interactor.GetExtensionsByType
import eu.kanade.domain.source.interactor.GetSourcesWithFavoriteCount
import eu.kanade.domain.source.model.Extension
import eu.kanade.domain.source.model.Source
import exh.log.xLogE
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import tachiyomi.core.common.util.lang.launchIO
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class UnifiedSourcesScreenModel(
    private val getExtensions: GetExtensionsByType = Injekt.get(),
    private val getSourcesWithFavoriteCount: GetSourcesWithFavoriteCount = Injekt.get(),
) : StateScreenModel<UnifiedSourcesScreenModel.State>(State.Loading) {

    init {
        screenModelScope.launchIO {
            combine(
                getExtensions.subscribe(GetExtensionsByType.Installed),
                getSourcesWithFavoriteCount.subscribe(),
            ) { extensions, sourcesWithCount ->
                val sourcesByPkg = sourcesWithCount.groupBy { it.first.pkgName }

                val items = extensions
                    .map { extension ->
                        val sources = sourcesByPkg[extension.pkgName]
                            ?.map { (source, count) ->
                                SourceWithCount(source, count)
                            }
                            ?: emptyList()
                        ExtensionItem(extension, sources)
                    }
                    .sortedWith(
                        compareBy(String.CASE_INSENSITIVE_ORDER) { it.extension.name },
                    )

                mutableState.value = State.Success(items)
            }
                .catch {
                    xLogE("Error in UnifiedSourcesScreenModel", it)
                    mutableState.value = State.Error(it)
                }
                .launchIn(screenModelScope)
        }
    }

    /**
     * The state of the screen.
     */
    sealed class State {
        data object Loading : State()
        data class Success(val items: List<ExtensionItem>) : State()
        data class Error(val error: Throwable) : State()
    }

    /**
     * A wrapper class for an extension and its sources.
     */
    data class ExtensionItem(
        val extension: Extension.Installed,
        val sources: List<SourceWithCount>,
    )

    /**
     * A wrapper class for a source and its favorite count.
     */
    data class SourceWithCount(
        val source: Source,
        val count: Long,
    )
}
