package eu.kanade.tachiyomi.ui.browse.source.unified

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.browse.components.BrowseSourceList
import eu.kanade.presentation.components.LoadingScreen
import eu.kanade.presentation.components.TextButton
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.browse.source.SourcesScreen
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceScreen
import tachiyomi.i18n.MR

class UnifiedSourcesScreen(
    private val smartSearchConfig: SourcesScreen.SmartSearchConfig? = null,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { UnifiedSourcesScreenModel(smartSearchConfig) }
        val state by screenModel.state.collectAsState()

        when (val currentState = state) {
            is UnifiedSourcesScreenModel.State.Loading -> LoadingScreen()
            is UnifiedSourcesScreenModel.State.Error -> {
                // TODO: Error screen
            }
            is UnifiedSourcesScreenModel.State.Success -> {
                BrowseSourceList(
                    items = currentState.items,
                    // TODO: search
                    // searchText = currentState.searchQuery,
                    // onSearchTextChange = screenModel::onSearch,
                    onClick = { navigator.push(BrowseSourceScreen(it.id)) },
                    onLongClick = {},
                    onPinClick = screenModel::togglePin,
                    onMigrateClick = null, // TODO
                    getMangaCountForSource = { screenModel.getMangaCountForSource(it) },
                    getLatestChapterForSource = { screenModel.getLatestChapterForSource(it) },
                    // TODO: Open extension details
                    onBrowseExtension = {},
                )
            }
        }
    }
}
