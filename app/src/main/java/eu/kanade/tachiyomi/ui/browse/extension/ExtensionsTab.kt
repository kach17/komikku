package eu.kanade.tachiyomi.ui.browse.extension

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TravelExplore
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.GlobalSearchScreen
import androidx.compose.material.icons.outlined._18UpRating
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.domain.source.model.installedExtension
import eu.kanade.presentation.browse.ExtensionScreen
import eu.kanade.presentation.browse.SourceCategoriesDialog
import eu.kanade.presentation.browse.SourceOptionsDialog
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.TabContent
import eu.kanade.presentation.more.settings.screen.browse.ExtensionReposScreen
import eu.kanade.tachiyomi.extension.model.Extension
import eu.kanade.tachiyomi.ui.browse.extension.ExtensionUiModel
import eu.kanade.tachiyomi.ui.browse.extension.details.ExtensionDetailsScreen
import eu.kanade.tachiyomi.ui.browse.migration.manga.MigrateMangaScreen
import eu.kanade.tachiyomi.ui.browse.source.SourcesScreenModel
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceScreen
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceScreenModel.Listing
import eu.kanade.tachiyomi.ui.webview.WebViewScreen
import eu.kanade.tachiyomi.util.system.isPackageInstalled
import kotlinx.collections.immutable.persistentListOf
import tachiyomi.i18n.MR
import tachiyomi.i18n.kmk.KMR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun extensionsTab(
    extensionsScreenModel: ExtensionsScreenModel,
    sourcesScreenModel: SourcesScreenModel,
): TabContent {
    val navigator = LocalNavigator.currentOrThrow
    val context = LocalContext.current

    val state by extensionsScreenModel.state.collectAsState()
    val sourcesState by sourcesScreenModel.state.collectAsState()
    var privateExtensionToUninstall by remember { mutableStateOf<Extension?>(null) }

    return TabContent(
        titleRes = MR.strings.label_sources,
        badgeNumber = state.updates.takeIf { it > 0 },
        searchEnabled = true,
        actions = persistentListOf(
            AppBar.Action(
                title = stringResource(MR.strings.action_global_search),
                icon = Icons.Outlined.TravelExplore,
                onClick = { navigator.push(GlobalSearchScreen()) },
            ),
            // KMK -->
            AppBar.Action(
                title = stringResource(KMR.strings.action_toggle_nsfw_only),
                icon = Icons.Outlined._18UpRating,
                iconTint = if (state.nsfwOnly) MaterialTheme.colorScheme.error else LocalContentColor.current,
                onClick = { extensionsScreenModel.toggleNsfwOnly() },
            ),
            AppBar.OverflowAction(
                title = stringResource(MR.strings.action_webview_refresh),
                onClick = extensionsScreenModel::findAvailableExtensions,
            ),
            // KMK <--
            AppBar.OverflowAction(
                title = stringResource(MR.strings.action_filter),
                onClick = { navigator.push(ExtensionFilterScreen()) },
            ),
            AppBar.OverflowAction(
                title = stringResource(MR.strings.label_extension_repos),
                onClick = { navigator.push(ExtensionReposScreen()) },
            ),
        ),
        content = { contentPadding, _ ->
            BackHandler(enabled = state.searchQuery != null) {
                extensionsScreenModel.search(null)
            }
            // Filter out installed group since sources section handles it
            val filteredState = if (!sourcesState.isEmpty) {
                state.copy(
                    items = state.items.filter { (header, _) ->
                        header !is ExtensionUiModel.Header.Resource ||
                            (header.textRes != MR.strings.ext_installed &&
                                header.textRes != MR.strings.ext_updates_pending)
                    },
                )
            } else state

            ExtensionScreen(
                state = filteredState,
                contentPadding = contentPadding,
                searchQuery = state.searchQuery,
                onLongClickItem = { extension ->
                    when (extension) {
                        is Extension.Installed -> extensionsScreenModel.showDialog(extension)
                        is Extension.Available -> extensionsScreenModel.installExtension(extension)
                        else -> {
                            if (context.isPackageInstalled(extension.pkgName)) {
                                extensionsScreenModel.uninstallExtension(extension)
                            } else {
                                privateExtensionToUninstall = extension
                            }
                        }
                    }
                },
                onClickItemCancel = extensionsScreenModel::cancelInstallUpdateExtension,
                onClickUpdateAll = extensionsScreenModel::updateAllExtensions,
                onOpenWebView = { extension ->
                    when (extension) {
                        is Extension.Available -> extension.sources.getOrNull(0)?.let {
                            navigator.push(WebViewScreen(url = it.baseUrl, initialTitle = it.name, sourceId = it.id))
                        }
                        is Extension.Installed -> (extension.sources.getOrNull(0) as? eu.kanade.tachiyomi.source.online.HttpSource)?.let {
                            navigator.push(WebViewScreen(url = it.baseUrl, initialTitle = it.name, sourceId = it.id))
                        }
                        else -> {}
                    }
                },
                onInstallExtension = extensionsScreenModel::installExtension,
                onOpenExtension = { extension ->
                    extension.sources.getOrNull(0)?.let { navigator.push(BrowseSourceScreen(it.id, null)) }
                        ?: navigator.push(ExtensionDetailsScreen(extension.pkgName))
                },
                sourcesState = sourcesState,
                onClickSourceItem = { source, listing -> navigator.push(BrowseSourceScreen(source.id, listing.query)) },
                onClickSourcePin = sourcesScreenModel::togglePin,
                onLongClickSourceItem = sourcesScreenModel::showSourceDialog,
                onChangeSourceSearchQuery = sourcesScreenModel::search,
                onTrustExtension = { extensionsScreenModel.trustExtension(it) },
                onUninstallExtension = { extensionsScreenModel.uninstallExtension(it) },
                onUpdateExtension = extensionsScreenModel::updateExtension,
                onRefresh = extensionsScreenModel::findAvailableExtensions,
            )

            privateExtensionToUninstall?.let { extension ->
                ExtensionUninstallConfirmation(
                    extensionName = extension.name,
                    onClickConfirm = {
                        extensionsScreenModel.uninstallExtension(extension)
                    },
                    onDismissRequest = {
                        privateExtensionToUninstall = null
                    },
                )
            }

            state.dialog?.let { extension ->
                ExtensionOptionsDialog(
                    extension = extension,
                    onClickPin = {
                        extensionsScreenModel.togglePin(extension)
                        extensionsScreenModel.closeDialog()
                    },
                    onClickDisable = {
                        extensionsScreenModel.toggleSource(extension)
                        extensionsScreenModel.closeDialog()
                    },
                    onClickMigrate = {
                        extension.sources.getOrNull(0)?.let { navigator.push(MigrateMangaScreen(it.id)) }
                        extensionsScreenModel.closeDialog()
                    }.takeIf { (state.favoriteCountByPkgName[extension.pkgName] ?: 0L) > 0L },
                    onClickUninstall = {
                        extensionsScreenModel.uninstallExtension(extension)
                        extensionsScreenModel.closeDialog()
                    },
                    onClickSettings = {
                        navigator.push(ExtensionDetailsScreen(extension.pkgName))
                        extensionsScreenModel.closeDialog()
                    },
                    onDismiss = extensionsScreenModel::closeDialog,
                )
            }

            when (val dialog = sourcesState.dialog) {
                is SourcesScreenModel.Dialog.SourceLongClick -> {
                    val source = dialog.source
                    SourceOptionsDialog(
                        source = source,
                        onClickPin = { sourcesScreenModel.togglePin(source); sourcesScreenModel.closeDialog() },
                        onClickDisable = { sourcesScreenModel.toggleSource(source); sourcesScreenModel.closeDialog() },
                        onClickSetCategories = { sourcesScreenModel.showSourceCategoriesDialog(source) }.takeIf { sourcesState.categories.isNotEmpty() },
                        onClickToggleDataSaver = { sourcesScreenModel.toggleExcludeFromDataSaver(source); sourcesScreenModel.closeDialog() }.takeIf { sourcesState.dataSaverEnabled },
                        onDismiss = sourcesScreenModel::closeDialog,
                        onClickSettings = {
                            source.installedExtension?.let { navigator.push(ExtensionDetailsScreen(it.pkgName)) }
                            sourcesScreenModel.closeDialog()
                        },
                    )
                }
                is SourcesScreenModel.Dialog.SourceCategories -> {
                    val source = dialog.source
                    SourceCategoriesDialog(
                        source = source,
                        categories = sourcesState.categories,
                        onClickCategories = { categories -> sourcesScreenModel.setSourceCategories(source, categories); sourcesScreenModel.closeDialog() },
                        onDismissRequest = sourcesScreenModel::closeDialog,
                    )
                }
                null -> Unit
            }
        },
    )
}

@Composable
private fun ExtensionUninstallConfirmation(
    extensionName: String,
    onClickConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(MR.strings.ext_confirm_remove))
        },
        text = {
            Text(text = stringResource(MR.strings.remove_private_extension_message, extensionName))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onClickConfirm()
                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(MR.strings.ext_remove))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(MR.strings.action_cancel))
            }
        },
        onDismissRequest = onDismissRequest,
    )
}

@Composable
private fun ExtensionOptionsDialog(
    extension: Extension.Installed,
    onClickPin: () -> Unit,
    onClickDisable: () -> Unit,
    onClickMigrate: (() -> Unit)?,
    onClickUninstall: () -> Unit,
    onClickSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = { Text(text = extension.name) },
        text = {
            Column {
                Text(
                    text = stringResource(MR.strings.action_pin),
                    modifier = Modifier.clickable(onClick = onClickPin).fillMaxWidth().padding(vertical = 16.dp),
                )
                Text(
                    text = stringResource(MR.strings.action_disable),
                    modifier = Modifier.clickable(onClick = onClickDisable).fillMaxWidth().padding(vertical = 16.dp),
                )
                if (onClickMigrate != null) {
                    Text(
                        text = stringResource(MR.strings.action_migrate),
                        modifier = Modifier.clickable(onClick = onClickMigrate).fillMaxWidth().padding(vertical = 16.dp),
                    )
                }
                Text(
                    text = stringResource(MR.strings.action_settings),
                    modifier = Modifier.clickable(onClick = onClickSettings).fillMaxWidth().padding(vertical = 16.dp),
                )
                Text(
                    text = stringResource(MR.strings.ext_uninstall),
                    modifier = Modifier.clickable(onClick = onClickUninstall).fillMaxWidth().padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {},
    )
}