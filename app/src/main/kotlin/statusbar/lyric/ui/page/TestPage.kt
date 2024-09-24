package statusbar.lyric.ui.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import getWindowSize
import statusbar.lyric.MainActivity.Companion.context
import statusbar.lyric.R
import statusbar.lyric.config.ActivityOwnSP.config
import statusbar.lyric.tools.ActivityTestTools.getClass
import statusbar.lyric.tools.ActivityTestTools.waitResponse
import statusbar.lyric.tools.Tools
import top.yukonga.miuix.kmp.MiuixScrollBehavior
import top.yukonga.miuix.kmp.MiuixSuperArrow
import top.yukonga.miuix.kmp.MiuixSuperDialog
import top.yukonga.miuix.kmp.MiuixSuperSwitch
import top.yukonga.miuix.kmp.MiuixTopAppBar
import top.yukonga.miuix.kmp.basic.MiuixBasicComponent
import top.yukonga.miuix.kmp.basic.MiuixBox
import top.yukonga.miuix.kmp.basic.MiuixButton
import top.yukonga.miuix.kmp.basic.MiuixCard
import top.yukonga.miuix.kmp.basic.MiuixLazyColumn
import top.yukonga.miuix.kmp.basic.MiuixScaffold
import top.yukonga.miuix.kmp.basic.MiuixSmallTitle
import top.yukonga.miuix.kmp.basic.MiuixText
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.ArrowBack
import top.yukonga.miuix.kmp.rememberMiuixTopAppBarState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtil.Companion.dismissDialog
import top.yukonga.miuix.kmp.utils.MiuixPopupUtil.Companion.showDialog

@Composable
fun TestPage(navController: NavController) {
    val scrollBehavior = MiuixScrollBehavior(rememberMiuixTopAppBarState())
    val showDialog = remember { mutableStateOf(false) }
    val testMode = remember { mutableStateOf(config.testMode) }
    val relaxConditions = remember { mutableStateOf(config.relaxConditions) }
    MiuixScaffold(
        modifier = Modifier
            .imePadding()
            .fillMaxSize(),
        topBar = {
            MiuixTopAppBar(
                title = stringResource(R.string.hook_page),
                color = Color.Transparent,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.padding(start = 12.dp),
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = MiuixTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) {
        MiuixBox {
            MiuixLazyColumn(
                modifier = Modifier
                    .height(getWindowSize().height.dp)
                    .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
                contentPadding = it,
                enableOverScroll = true,
                topAppBarScrollBehavior = scrollBehavior
            ) {
                item {
                    Column {
                        MiuixCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            insideMargin = DpSize(0.dp, 0.dp)
                        ) {
                            MiuixSuperSwitch(
                                title = stringResource(R.string.test_mode),
                                checked = testMode.value,
                                onCheckedChange = {
                                    testMode.value = it
                                    config.testMode = it
                                },
                                insideMargin = DpSize(16.dp, 16.dp)
                            )
                            AnimatedVisibility(testMode.value) {
                                Column {
                                    MiuixSuperSwitch(
                                        title = stringResource(R.string.relax_conditions),
                                        summary = stringResource(R.string.relax_conditions_tips),
                                        checked = relaxConditions.value,
                                        onCheckedChange = {
                                            relaxConditions.value = it
                                            config.relaxConditions = it
                                        },
                                        insideMargin = DpSize(16.dp, 16.dp)
                                    )
                                    MiuixSuperArrow(
                                        title = stringResource(R.string.get_hook),
                                        onClick = {
                                            waitResponse()
                                            context.getClass()
                                        },
                                        insideMargin = DpSize(16.dp, 16.dp)
                                    )
                                }
                            }
                        }
                        MiuixCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            insideMargin = DpSize(0.dp, 0.dp)
                        ) {
                            MiuixBasicComponent(
                                leftAction = {
                                    MiuixText(
                                        text = stringResource(R.string.reset_system_ui),
                                        color = Color.Red
                                    )
                                },
                                onClick = {
                                    showDialog.value = true
                                },
                                insideMargin = DpSize(16.dp, 16.dp)
                            )
                        }
                        MiuixSmallTitle(
                            text = stringResource(R.string.test_mode_tips).split("\n")[1]
                        )
                    }
                    RestartDialog(showDialog)
                }
            }
        }
    }
}

@Composable
fun RestartDialog(showDialog: MutableState<Boolean>) {
    if (!showDialog.value) return
    showDialog(
        content = {
            MiuixSuperDialog(
                title = stringResource(R.string.reset_system_ui),
                summary = stringResource(R.string.restart_systemui_tips),
                onDismissRequest = {
                    showDialog.value = false
                },
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiuixButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.cancel),
                        onClick = {
                            dismissDialog()
                            showDialog.value = false
                        }
                    )
                    Spacer(Modifier.width(20.dp))
                    MiuixButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.ok),
                        submit = true,
                        onClick = {
                            Tools.shell("killall com.android.systemui", true)
                            dismissDialog()
                            showDialog.value = false
                        }
                    )
                }
            }
        }
    )
}