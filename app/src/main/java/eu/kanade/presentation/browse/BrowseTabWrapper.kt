package eu.kanade.presentation.browse

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import eu.kanade.presentation.browse.FeedScreen
import eu.kanade.tachiyomi.ui.browse.BrowseTab
import eu.kanade.tachiyomi.ui.browse.source.unified.UnifiedSourcesScreen

// KMK -->
@Composable
fun BrowseTabWrapper(tab: BrowseTab) {
    when (tab) {
        is BrowseTab.Feed -> {
            FeedScreen(
                onFabClick = { /*TODO*/ },
                onLatestClicked = { /*TODO*/ },
                onBrowseClicked = { /*TODO*/ },
            )
        }
        is BrowseTab.Unified -> {
            // Correctly instantiate the screen
            UnifiedSourcesScreen().Content()
        }
    }
}
// KMK <--
