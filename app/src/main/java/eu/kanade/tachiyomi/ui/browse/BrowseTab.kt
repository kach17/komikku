package eu.kanade.tachiyomi.ui.browse

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import eu.kanade.presentation.browse.BrowseTabWrapper
import eu.kanade.presentation.browse.FeedScreen
import eu.kanade.presentation.browse.sources.SourcesScreen
import eu.kanade.presentation.browse.sources.browse.BrowseSourceScreen
import eu.kanade.presentation.browse.sources.latest.LatestScreen
import eu.kanade.presentation.browse.sources.migration.MigrationScreen
import eu.kanade.presentation.browse.sources.migration.SourceMigrationScreen
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.browse.extensions.ExtensionsScreen
import eu.kanade.tachiyomi.ui.browse.feed.FeedScreenModel
import eu.kanade.tachiyomi.ui.browse.sources.SourcesScreenModel
import eu.kanade.tachiyomi.ui.browse.sources.browse.BrowseSourceScreenModel
import eu.kanade.tachiyomi.ui.browse.sources.latest.LatestScreenModel
import eu.kanade.tachiyomi.ui.browse.sources.migration.MigrationScreenModel
import eu.kanade.tachiyomi.ui.browse.sources.migration.SourceMigrationScreenModel
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.util.system.loadResource
import kotlinx.coroutines.flow.collectLatest
import tachiyomi.i18n.MR
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.transitions.ScreenTransition

sealed class BrowseTab(
    override val options: TabOptions,
    val searchRes: Int?,
) : Tab {

    @Composable
    override fun Content() {
        val tabNavigator = LocalTabNavigator.current
        val search = { query: String ->
            (tabNavigator.current as BrowseTab).search(query)
        }
        Navigator(
            screen = BrowseTabWrapper(
                tab = this,
                search = search,
            ),
        ) { navigator ->
            ScreenTransition(navigator = navigator) { screen ->
                screen.Content()
            }
        }
    }

    abstract fun search(query: String?)

    // KMK -->
    data object Feed : BrowseTab(
        options = TabOptions(
            index = 0u,
            title = MR.strings.browse_tab_feed.let { stringResource(it) },
            icon = Icons.Outlined.Feed,
        ),
        searchRes = MR.strings.action_search,
    ) {
        override fun search(query: String?) {
            // TODO: Yet to be implemented
        }
    }

    data object Unified : BrowseTab(
        options = TabOptions(
            index = 1u,
            title = MR.strings.browse_tab_sources.let { stringResource(it) },
            icon = Icons.Outlined.TravelExplore,
        ),
        searchRes = MR.strings.action_search_sources,
    ) {
        override fun search(query: String?) {
            // TODO: Handled in UnifiedSourcesScreen
        }
    }
    // KMK <--
}
