
package eu.kanade.presentation.browse

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import eu.kanade.presentation.browse.feed.FeedScreen
import eu.kanade.presentation.browse.sources.SourcesScreen
import eu.kanade.tachiyomi.ui.browse.BrowseTab
import tachiyomi.presentation.core.components.material.Scaffold

data class BrowseTabWrapper(
    private val tab: BrowseTab,
    private val search: (String) -> Unit,
) : Screen {
    @Composable
    override fun Content() {
        Scaffold(
            topBar = {
                // TODO: Top bar with search
            },
        ) { paddingValues ->
            when (tab) {
                is BrowseTab.Feed -> FeedScreen(
                    modifier = Modifier.padding(paddingValues),
                )
                is BrowseTab.Unified -> SourcesScreen(
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}
