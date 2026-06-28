import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.singleWindowApplication
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loc.hexis.shared.ui.LocalWindowSizeClass
import com.loc.hexis.shared.ui.app.MainApp
import com.loc.hexis.shared.ui.theme.HexisTheme
import com.loc.hexis.shared.ui.viewmodel.MainViewModel
import com.loc.hexis.web_demo.di.AppModule
import org.koin.compose.viewmodel.koinViewModel
import org.koin.plugin.module.dsl.startKoin

fun main() {
    startKoin<AppModule>()

    singleWindowApplication(title = "Hexis") {
        val windowSizeClass = calculateWindowSizeClass()
        val viewmodel = koinViewModel<MainViewModel>()
        val state by viewmodel.state.collectAsStateWithLifecycle()

        CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
            HexisTheme(state.theme) { MainApp(state = state) }
        }
    }
}