
package eu.kanade.presentation.browse

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import eu.kanade.presentation.components.TabbedScreen
import eu.kanade.tachiyomi.ui.browse.BrowseTab
import tachiyomi.i18n.MR

@Composable
fun UnifiedSourcesScreen() {
    TabbedScreen(
        titleRes = MR.strings.browse_tab_sources,
        tabs = listOf(
            // TODO: Add "All" and "Pinned" tabs
        ),
    )
}
