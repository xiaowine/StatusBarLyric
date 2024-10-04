package statusbar.lyric.ui.page

import android.app.Activity
import android.content.ComponentName
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import statusbar.lyric.BuildConfig
import statusbar.lyric.MainActivity.Companion.context
import statusbar.lyric.R
import statusbar.lyric.config.ActivityOwnSP
import statusbar.lyric.config.ActivityOwnSP.config
import statusbar.lyric.tools.ActivityTools
import statusbar.lyric.tools.BackupTools
import statusbar.lyric.tools.Tools
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.LazyColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.extra.SuperSwitch
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.ArrowBack
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtil.Companion.dismissDialog
import top.yukonga.miuix.kmp.utils.MiuixPopupUtil.Companion.showDialog
import top.yukonga.miuix.kmp.utils.getWindowSize
import java.util.Locale

@Composable
fun MenuPage(navController: NavController) {
    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())
    val ac = LocalContext.current as Activity
    val outLog = remember { mutableStateOf(config.outLog) }
    val showLauncherIcon = remember { mutableStateOf(config.showLauncherIcon) }
    val showDialog = remember { mutableStateOf(false) }
    val showResetDialog = remember { mutableStateOf(false) }

    Column {
        TopAppBar(
            title = stringResource(R.string.menu_page),
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                IconButton(
                    modifier = Modifier.padding(start = 18.dp),
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
        LazyColumn(
            modifier = Modifier
                .height(getWindowSize().height.dp)
                .background(MiuixTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
            enableOverScroll = true,
            topAppBarScrollBehavior = scrollBehavior
        ) {
            item {
                Column(Modifier.padding(top = 18.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Column {
                            SuperSwitch(
                                title = stringResource(R.string.show_launcher_icon),
                                checked = showLauncherIcon.value,
                                onCheckedChange = {
                                    showLauncherIcon.value = it
                                    config.showLauncherIcon = it
                                    context.packageManager.setComponentEnabledSetting(
                                        ComponentName(context, "statusbar.lyric.AliasActivity"), if (it) {
                                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                        } else {
                                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                                        }, PackageManager.DONT_KILL_APP
                                    )
                                }
                            )
                            SuperSwitch(
                                title = stringResource(R.string.show_logcat),
                                checked = outLog.value,
                                onCheckedChange = {
                                    outLog.value = it
                                    config.outLog = it
                                }
                            )
                        }
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column {
                        SuperArrow(
                            title = stringResource(R.string.backup_config),
                            onClick = {
                                BackupTools.backup(ac, ActivityOwnSP.ownSP)
                            }
                        )
                        SuperArrow(
                            title = stringResource(R.string.recovery_config),
                            onClick = {
                                BackupTools.recovery(ac, ActivityOwnSP.ownSP)
                            }
                        )
                        SuperArrow(
                            title = stringResource(R.string.clear_config),
                            onClick = {
                                showResetDialog.value = true
                            }
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    BasicComponent(
                        title = stringResource(R.string.reset_system_ui),
                        titleColor = Color.Red,
                        onClick = {
                            showDialog.value = true
                        }
                    )
                }
                SmallTitle(
                    text = "Info",
                    modifier = Modifier.padding(top = 6.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 6.dp)
                ) {
                    Column {
                        val buildTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(BuildConfig.BUILD_TIME)
                        Text(
                            text = "Version Code:",
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp)
                        )
                        Text(
                            text = "${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE} (${BuildConfig.BUILD_TYPE})",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Text(
                            text = "Build Time:",
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp)
                        )
                        Text(
                            text = buildTime,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Text(
                            text = "Current Device:",
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp)
                        )
                        Text(
                            text = "${Build.BRAND} ${Build.MODEL} (${Build.DEVICE}) SDK${Build.VERSION.SDK_INT}",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Text(
                            text = "Lyric Getter:",
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp)
                        )
                        Text(
                            text = "API${BuildConfig.API_VERSION} \uD83D\uDE0A",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                        )
                    }
                }
                Spacer(Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
            }
        }
    }
    RestartDialog(showDialog = showDialog)
    ResetDialog(showDialog = showResetDialog)
}


@Composable
fun ResetDialog(showDialog: MutableState<Boolean>) {
    if (!showDialog.value) return
    showDialog(
        content = {
            SuperDialog(
                title = stringResource(R.string.clear_config),
                summary = stringResource(R.string.clear_config_tips),
                show = showDialog,
                onDismissRequest = {
                    showDialog.value = false
                },
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.cancel),
                        onClick = {
                            dismissDialog()
                            showDialog.value = false
                        }
                    )
                    Spacer(Modifier.width(20.dp))
                    Button(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.ok),
                        submit = true,
                        onClick = {
                            config.clear()
                            dismissDialog()
                            showDialog.value = false
                            Thread {
                                Thread.sleep(500)
                                ActivityTools.restartApp()
                            }.start()
                        }
                    )
                }
            }
        }
    )
}


@Composable
fun RestartDialog(showDialog: MutableState<Boolean>) {
    if (!showDialog.value) return
    showDialog(
        content = {
            SuperDialog(
                title = stringResource(R.string.reset_system_ui),
                summary = stringResource(R.string.restart_systemui_tips),
                show = showDialog,
                onDismissRequest = {
                    showDialog.value = false
                },
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.ok),
                        onClick = {
                            Tools.shell("killall com.android.systemui", true)
                            dismissDialog()
                            showDialog.value = false
                        }
                    )
                    Spacer(Modifier.width(20.dp))
                    Button(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.cancel),
                        submit = true,
                        onClick = {
                            dismissDialog()
                            showDialog.value = false
                        }
                    )
                }
            }
        }
    )
}