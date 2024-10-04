package statusbar.lyric

import android.content.res.Configuration
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import statusbar.lyric.ui.page.ChoosePage
import statusbar.lyric.ui.page.ExtendPage
import statusbar.lyric.ui.page.HomePage
import statusbar.lyric.ui.page.IconPage
import statusbar.lyric.ui.page.LyricPage
import statusbar.lyric.ui.page.MenuPage
import statusbar.lyric.ui.page.SystemSpecialPage
import statusbar.lyric.ui.page.TestPage
import statusbar.lyric.ui.theme.AppTheme
import top.yukonga.miuix.kmp.basic.Box
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.getWindowSize

@Composable
fun App() {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    AppTheme {
        Scaffold {
            BoxWithConstraints {
                if (isLandscape || maxWidth > 768.dp) {
                    LandscapeLayout()
                } else {
                    PortraitLayout()
                }
            }
        }
    }
}

@Composable
fun PortraitLayout() {
    val windowWidth = getWindowSize().width
    val easing = CubicBezierEasing(0.12f, 0.38f, 0.2f, 1f)
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "HomePage",
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { windowWidth },
                animationSpec = tween(durationMillis = 500, easing = easing)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -windowWidth / 5 },
                animationSpec = tween(durationMillis = 500, easing = easing)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -windowWidth / 5 },
                animationSpec = tween(durationMillis = 500, easing = easing)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { windowWidth },
                animationSpec = tween(durationMillis = 500, easing = easing)
            )
        }
    ) {
        composable("HomePage") { HomePage(navController) }
        composable("MenuPage") { MenuPage(navController) }
        composable("ChoosePage") { ChoosePage(navController) }
        composable("TestPage") { TestPage(navController) }
        composable("LyricPage") { LyricPage(navController) }
        composable("IconPage") { IconPage(navController) }
        composable("ExtendPage") { ExtendPage(navController) }
        composable("SystemSpecialPage") { SystemSpecialPage(navController) }
    }
}

@Composable
fun LandscapeLayout() {
    val windowWidth = getWindowSize().width
    val easing = CubicBezierEasing(0.12f, 0.88f, 0.2f, 1f)
    val navController = rememberNavController()
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background)
            .padding(horizontal = 12.dp)
    ) {
        Box(
            modifier = Modifier.weight(0.88f)
        ) {
            HomePage(navController)
        }
        NavHost(
            navController = navController,
            startDestination = "EmptyPage",
            modifier = Modifier.weight(1f),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { windowWidth },
                    animationSpec = tween(durationMillis = 500, easing = easing)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { windowWidth / 5 },
                    animationSpec = tween(durationMillis = 500, easing = easing)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 200)
                )
            },
        ) {
            composable("EmptyPage", exitTransition = { fadeOut() }, popEnterTransition = { fadeIn()}) { EmptyPage() }
            composable("MenuPage") { MenuPage(navController) }
            composable("ChoosePage") { ChoosePage(navController) }
            composable("TestPage") { TestPage(navController) }
            composable("LyricPage") { LyricPage(navController) }
            composable("IconPage") { IconPage(navController) }
            composable("ExtendPage") { ExtendPage(navController) }
            composable("SystemSpecialPage") { SystemSpecialPage(navController) }
        }
    }
}

@Composable
fun EmptyPage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            tint = MiuixTheme.colorScheme.secondary,
            modifier = Modifier.size(256.dp)
        )
    }
}
