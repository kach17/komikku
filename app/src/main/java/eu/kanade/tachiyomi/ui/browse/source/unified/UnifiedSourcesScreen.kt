package eu.kanade.tachiyomi.ui.browse.source.unified

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.browse.components.BaseSourceItem
import eu.kanade.tachiyomi.extension.model.Extension
import eu.kanade.tachiyomi.extension.model.InstallStep
import eu.kanade.tachiyomi.ui.browse.extension.details.ExtensionDetailsScreen
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceScreen
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Badge
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.theme.header

object UnifiedSourcesScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { UnifiedSourcesScreenModel() }
        val state by screenModel.state.collectAsState()

        if (state.isLoading) {
            LoadingScreen()
            return
        }

        LazyColumn {
            items(state.items) { unifiedExtension ->
                UnifiedExtensionItem(
                    unifiedExtension = unifiedExtension,
                    onClickSource = { navigator.push(BrowseSourceScreen(it.id, null)) },
                    onClickSettings = { navigator.push(ExtensionDetailsScreen(unifiedExtension.extension.pkgName)) },
                    onClickUpdate = { screenModel.updateExtension(unifiedExtension.extension) },
                    onClickCancel = { screenModel.cancelInstallUpdateExtension(unifiedExtension.extension) },
                )
            }
        }
    }

    @Composable
    private fun UnifiedExtensionItem(
        unifiedExtension: UnifiedSourcesScreenModel.UnifiedExtensionItem,
        onClickSource: (tachiyomi.domain.source.model.Source) -> Unit,
        onClickSettings: () -> Unit,
        onClickUpdate: (Extension.Installed) -> Unit,
        onClickCancel: (Extension.Installed) -> Unit,
    ) {
        Column {
            Row(
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.padding.medium,
                    vertical = MaterialTheme.padding.small,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = unifiedExtension.extension.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.header,
                )

                ActionButton(
                    installStep = unifiedExtension.installStep,
                    extension = unifiedExtension.extension,
                    onClickUpdate = onClickUpdate,
                    onClickCancel = onClickCancel,
                )

                IconButton(onClick = onClickSettings) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(MR.strings.action_settings),
                    )
                }
            }

            unifiedExtension.sources.forEach { sourceItem ->
                BaseSourceItem(
                    source = sourceItem.source,
                    modifier = Modifier.clickable { onClickSource(sourceItem.source) },
                    action = {
                        if (sourceItem.count > 0) {
                            Badge(text = "${sourceItem.count}")
                        }
                    },
                )
            }
        }
    }

    @Composable
    private fun ActionButton(
        installStep: InstallStep?,
        extension: Extension.Installed,
        onClickUpdate: (Extension.Installed) -> Unit,
        onClickCancel: (Extension.Installed) -> Unit,
    ) {
        when (installStep) {
            null, InstallStep.Idle, InstallStep.Error -> {
                if (extension.hasUpdate) {
                    Button(onClick = { onClickUpdate(extension) }) {
                        Text(stringResource(MR.strings.ext_update))
                    }
                }
            }
            InstallStep.Pending, InstallStep.Downloading -> {
                Button(onClick = { onClickCancel(extension) }) {
                    Text(stringResource(MR.strings.action_cancel))
                }
            }
            InstallStep.Installing -> {
                // TODO: Find a way to show a progress bar
                Button(onClick = {}) {
                    Text(stringResource(MR.strings.ext_installing))
                }
            }
            InstallStep.Installed -> {
                // Do nothing
            }
        }
    }
}
