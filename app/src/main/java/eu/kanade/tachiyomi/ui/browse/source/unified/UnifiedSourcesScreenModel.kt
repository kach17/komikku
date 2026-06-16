package eu.kanade.tachiyomi.ui.browse.source.unified

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.domain.source.interactor.GetExtensionsByType
import eu.kanade.domain.source.interactor.GetSourcesWithFavoriteCount
import eu.kanade.domain.source.interactor.ToggleSourcePin
import eu.kanade.domain.source.model.Pin
import eu.kanade.domain.source.model.Source
import eu.kanade.tachiyomi.ui.browse.source.SourcesScreen
import exh.log.xLogE
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import tachiyomi.core.common.util.lang.launchIO
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class UnifiedSourcesScreenModel(
    private val smartSearchConfig: SourcesScreen.SmartSearchConfig? = null,
    private val getExtensions: GetExtensionsByType = Injekt.get(),
    private val getSourcesWithFavoriteCount: GetSourcesWithFavoriteCount = Injekt.get(),
    private val toggleSourcePin: ToggleSourcePin = Injekt.get(),
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
                                if (smartSearchConfig != null) {
                                    if (source.name.contains(smartSearchConfig.origTitle, ignoreCase = true)) {
                                        SourceWithCount(source, count)
                                    } else {
                                        null
                                    }
                                } else {
                                    SourceWithCount(source, count)
                                }
                            }
                            ?.filterNotNull()
                            ?: emptyList()
                        ExtensionItem(extension, sources)
                    }
                    .filter { it.sources.isNotEmpty() }
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

    fun togglePin(source: Source) {
        screenModelScope.launchIO {
            toggleSourcePin.await(Pin(source.id, source.lang))
        }
    }

    fun getMangaCountForSource(source: Source): Long {
        // TODO: Implement
        return 0
    }

    fun getLatestChapterForSource(source: Source): String? {
        // TODO: Implement
        return null
    }

    sealed class State {
        @Immutable
        data object Loading : State()
        @Immutable
        data class Success(
            val items: List<ExtensionItem>,
        ) : State()
        @Immutable
        data class Error(val error: Throwable) : State()
    }

    data class ExtensionItem(
        val extension: eu.kanade.domain.source.model.Extension.Installed,
        val sources: List<SourceWithCount>,
    )

    data class SourceWithCount(
        val source: Source,
        val count: Long,
    )
}
