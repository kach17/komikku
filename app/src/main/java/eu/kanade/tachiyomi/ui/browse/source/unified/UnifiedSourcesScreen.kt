package eu.kanade.tachiyomi.ui.browse.source.unified

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.browse.components.BaseSourceItem
import eu.kanade.presentation.components.LoadingScreen
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.browse.extension.details.ExtensionDetailsScreen
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceScreen

class UnifiedSourcesScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { UnifiedSourcesScreenModel() }
        val state by screenModel.state.collectAsState()

        when (val currentState = state) {
            is UnifiedSourcesScreenModel.State.Loading -> LoadingScreen()
            is UnifiedSourcesScreenModel.State.Error -> Text(text = currentState.error.message ?: "Unknown error")
            is UnifiedSourcesScreenModel.State.Success -> {
                LazyColumn {
                    items(currentState.items) { (extension, sources) ->
                        BaseSourceItem(
                            source = extension,
                            onClickItem = { navigator.push(ExtensionDetailsScreen(extension.pkgName)) },
                            onLongClickItem = {},
                            onClickPin = {},
                        ) {
                            Column {
                                sources.forEach { (source, count) ->
                                    SourceItem(
                                        name = source.name,
                                        count = count,
                                        onClick = { navigator.push(BrowseSourceScreen(source.id)) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceItem(
    name: String,
    count: Long,
    onClick: () -> Unit,
) {
    Text(
        text = "$name ($count)",
        modifier = Modifier.clickable(onClick = onClick),
    )
}
