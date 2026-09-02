package dev.anmitali.nook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import dev.anmitali.nook.core.designsystem.NookTheme
import dev.anmitali.nook.core.navigation.ArchiveRoute
import dev.anmitali.nook.core.navigation.BrowseRoute
import dev.anmitali.nook.feature.archive.ArchiveContentsScreen
import dev.anmitali.nook.feature.browse.BrowseScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NookTheme {
                NookApp()
            }
        }
    }
}

@Composable
private fun NookApp() {
    val backStack = rememberNavBackStack(BrowseRoute())

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<BrowseRoute> {
                BrowseScreen(
                    onOpenFile = {},
                    onOpenArchive = { path -> backStack.add(ArchiveRoute(path)) },
                )
            }
            entry<ArchiveRoute> { route ->
                ArchiveContentsScreen(
                    archivePath = route.archivePath,
                    onNavigateUp = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
