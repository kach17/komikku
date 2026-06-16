package eu.kanade.tachiyomi.ui.browse.source.unified

import androidx.compose.runtime.Composable
import eu.kanade.presentation.util.Tab
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun unifiedSourcesTab(): Tab {
    return Tab(
        title = stringResource(MR.strings.label_sources),
        content = { UnifiedSourcesScreen.Content() },
    )
}
