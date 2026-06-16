package eu.kanade.presentation.browse

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import eu.kanade.presentation.components.TabbedScreen
import eu.kanade.presentation.browse.FeedScreen
import eu.kanade.tachiyomi.ui.browse.BrowseTab
import eu.kanade.tachiyomi.ui.browse.source.unified.UnifiedSourcesScreen

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
            UnifiedSourcesScreen.Content()
        }
    }
}
