@file:Suppress("DEPRECATION")

package statusbar.lyric.activity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import cn.fkj233.ui.activity.view.*
import cn.fkj233.ui.activity.MIUIActivity
import cn.fkj233.ui.activity.data.MIUIPopupData
import cn.fkj233.ui.dialog.MIUIDialog
import com.microsoft.appcenter.analytics.Analytics
import statusbar.lyric.BuildConfig
import statusbar.lyric.R
import statusbar.lyric.config.IconConfig
import statusbar.lyric.utils.ActivityOwnSP
import statusbar.lyric.utils.ActivityUtils
import statusbar.lyric.utils.ShellUtils
import statusbar.lyric.utils.Utils
import kotlin.system.exitProcess

enum class DataItem {
    Custom, Author, CustomIcon
}

class SettingsActivity : MIUIActivity() {
    private val activity = this

    override fun onCreate(savedInstanceState: Bundle?) {
        ActivityOwnSP.activity = this
        if (!checkLSPosed()) isLoad = false
        super.onCreate(savedInstanceState)
        registerReceiver(HookReceiver(), IntentFilter().apply {
            addAction("Hook_Sure")
        })
    }

    inner class HookReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                Handler(Looper.getMainLooper()).post {
                    val message: String = if (intent.getBooleanExtra("hook_ok", false)) {
                        getString(R.string.HookSureSuccess)
                    } else {
                        getString(R.string.HookSureFail)
                    }
                    MIUIDialog(activity).apply {
                        setTitle(getString(R.string.HookSure))
                        setMessage(message)
                        setRButton(getString(R.string.Ok)) { dismiss() }
                        show()
                    }
                    val channelId = "Hook_Ok"
                    (applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).apply {
                        createNotificationChannel(
                            NotificationChannel(
                                channelId,
                                "Hook",
                                NotificationManager.IMPORTANCE_DEFAULT
                            )
                        )
                        notify(0, Notification.Builder(applicationContext).let {
                            it.setChannelId(channelId)
                            it.setSmallIcon(R.mipmap.ic_launcher)
                            it.setContentTitle(getString(R.string.AppName))
                            it.setContentText(message)
                            it.build()
                        })
                    }
                }
            } catch (_: Throwable) {
            }
        }
    }

    private fun checkLSPosed(): Boolean {
        return try {
            Utils.getSP(this, "Lyric_Config")?.let { setSP(it) }
            true
        } catch (e: Throwable) {
            MIUIDialog(this).apply {
                setTitle(R.string.Tips)
                setMessage(R.string.NotSupport)
                setRButton(R.string.ReStart) {
                    val intent = packageManager.getLaunchIntentForPackage(packageName)
                    intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    exitProcess(0)
                }
                show()
            }
            false
        }
    }

    override fun menuName(): String {
        return getString(R.string.AppName)
    }

    override fun menuItems(): ArrayList<BaseView> {
        return arrayListOf<BaseView>().apply {
            add(
                TextWithSwitchV(
                    TextV(resId = R.string.HideDeskIcon),
                    SwitchV("hLauncherIcon", customOnCheckedChangeListener = {
                        packageManager.setComponentEnabledSetting(
                            ComponentName(activity, "${BuildConfig.APPLICATION_ID}.launcher"),
                            if (it) {
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                            } else {
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                            },
                            PackageManager.DONT_KILL_APP
                        )
                    })
                )
            )
            add(TextSummaryV(textId = R.string.ResetModule, onClick = {
                MIUIDialog(activity).apply {
                    setTitle(R.string.ResetModuleDialog)
                    setMessage(R.string.ResetModuleDialogTips)
                    setRButton(R.string.Ok) {
                        ActivityUtils.cleanConfig(
                            activity
                        )
                        dismiss()
                    }
                    setLButton(R.string.Cancel) { dismiss() }
                    show()
                }
            }))
            add(TextSummaryV(textId = R.string.ReStartSystemUI, onClick = {
                MIUIDialog(activity).apply {
                    setTitle(R.string.RestartUI)
                    setMessage(R.string.RestartUITips)
                    setRButton(R.string.Ok) {
                        ShellUtils.voidShell("pkill -f com.android.systemui", true)
                        Analytics.trackEvent("重启SystemUI")
                        dismiss()
                    }
                    setLButton(R.string.Cancel) { dismiss() }
                    show()
                }
            }))
            add(LineV())
            add(TitleTextV("Module Version"))
            add(TextV("${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})-${BuildConfig.BUILD_TYPE}"))
        }
    }

    override fun mainName(): String {
        return getString(R.string.AppName)
    }

    override fun mainItems(): ArrayList<BaseView> {
        return arrayListOf<BaseView>().apply {
            add(TextSummaryV(textId = R.string.UseInfo, onClick = {
                MIUIDialog(activity).apply {
                    setTitle(R.string.VerExplanation)
                    setMessage(
                        String.format(
                            " %s [%s] %s",
                            getString(R.string.CurrentVer),
                            BuildConfig.VERSION_NAME,
                            getString(R.string.VerExp)
                        )
                    )
                    setRButton(R.string.Done) {
                        dismiss()
                    }
                    show()
                }
            }))
            add(TextSummaryV(textId = R.string.WarnExplanation, onClick = {
                MIUIDialog(activity).apply {
                    setTitle(R.string.WarnExplanation)
                    setMessage(
                        String.format(
                            " %s [%s] %s",
                            getString(R.string.CurrentVer),
                            BuildConfig.VERSION_NAME,
                            getString(R.string.WarnExp)
                        )
                    )
                    setRButton(R.string.Done) {
                        dismiss()
                    }
                    show()
                }
            }))
            add(LineV())
            add(TitleTextV(resId = R.string.BaseSetting))
            add(TextWithSwitchV(TextV(resId = R.string.AllSwitch), SwitchV("LService")))
            add(TextWithSwitchV(TextV(resId = R.string.LyricIcon), SwitchV("I", true)))
            add(
                TextSummaryV(
                    textId = R.string.Custom,
                    onClick = { showFragment(getItems(DataItem.Custom), getString(R.string.Custom)) })
            )
            add(LineV())
            add(TitleTextV(resId = R.string.AdvancedSettings))
            add(TextWithSwitchV(TextV(resId = R.string.AbScreen), SwitchV("AntiBurn")))
            add(TextWithSwitchV(TextV(resId = R.string.UseSystemReverseColor), SwitchV("UseSystemReverseColor", true)))
            add(TextWithSwitchV(TextV(resId = R.string.SongPauseCloseLyrics), SwitchV("LAutoOff")))
            add(TextWithSwitchV(TextV(resId = R.string.UnlockShow), SwitchV("LockScreenOff")))
            add(TextWithSwitchV(TextV(resId = R.string.AutoHideNotiIcon), SwitchV("HNoticeIcon")))
            add(TextWithSwitchV(TextV(resId = R.string.HideNetWork), SwitchV("HNetSpeed")))
            add(TextWithSwitchV(TextV(resId = R.string.AutoHideCarrierName), SwitchV("HCuk")))
            add(LineV())
            add(TitleTextV(resId = R.string.Other))
            add(TextSummaryV(textId = R.string.CustomHook, onClick = {
                MIUIDialog(activity).apply {
                    setTitle(R.string.HookSetTips)
                    setEditText(
                        ActivityOwnSP.ownSPConfig.getHook(),
                        getString(R.string.InputCustomHook)
                    )
                    setRButton(R.string.Ok) {
                        ActivityOwnSP.ownSPConfig.setHook(getEditText())
                        ActivityUtils.showToastOnLooper(
                            activity,
                            String.format(
                                "%s %s%s",
                                getString(R.string.HookSetTips),
                                if (ActivityOwnSP.ownSPConfig.getHook() == "") getString(R.string.Default) else ActivityOwnSP.ownSPConfig.getHook(),
                                getString(R.string.RestartSystemUI)
                            )
                        )
                        dismiss()
                    }
                    setLButton(R.string.Cancel) { dismiss() }
                    show()
                }
            }))
            add(TextWithSwitchV(TextV(resId = R.string.DebugMode), SwitchV("Debug")))
            add(TextSummaryV(textId = R.string.Test, onClick = {
                MIUIDialog(activity).apply {
                    setTitle(R.string.Test)
                    setMessage(R.string.TestDialogTips)
                    setRButton(R.string.Start) {
                        ActivityUtils.showToastOnLooper(activity, "尝试唤醒界面")
                        activity.sendBroadcast(
                            Intent().apply {
                                action = "Lyric_Server"
                                putExtra("Lyric_Type", "test")
                            }
                        )
                        dismiss()
                    }
                    setLButton(R.string.Back) { dismiss() }
                    show()
                }
            }))
            add(LineV())
            add(TitleTextV(resId = R.string.About))
            add(TextSummaryV("CheckUpdate (${BuildConfig.VERSION_NAME})", onClick = {
                ActivityUtils.showToastOnLooper(
                    activity,
                    getString(R.string.StartCheckUpdate)
                )
                ActivityUtils.checkUpdate(activity)
            }))
            add(
                TextSummaryV(
                    textId = R.string.AboutModule,
                    onClick = { showFragment(getItems(DataItem.Author), getString(R.string.AboutModule)) })
            )
            add(TextV())
        }
    }

    private fun getItems(dataItem: DataItem): ArrayList<BaseView> {
        return when (dataItem) {
            DataItem.Custom -> arrayListOf<BaseView>().apply {
                add(TextSummaryV(textId = R.string.LyricColor, onClick = {
                    MIUIDialog(activity).apply {
                        setTitle(R.string.LyricColor)
                        setMessage(R.string.LyricColorTips)
                        setEditText(ActivityOwnSP.ownSPConfig.getLyricColor(), "#FFFFFF")
                        setRButton(R.string.Ok) {
                            if (getEditText() == "") {
                                ActivityOwnSP.ownSPConfig.setLyricColor("")
                            } else {
                                try {
                                    Color.parseColor(getEditText())
                                    ActivityOwnSP.ownSPConfig.setLyricColor(getEditText())
                                } catch (e: Throwable) {
                                    ActivityUtils.showToastOnLooper(
                                        activity,
                                        getString(R.string.LyricColorError)
                                    )
                                    ActivityOwnSP.ownSPConfig.setLyricColor("")
                                }
                            }
                            dismiss()
                        }
                        setLButton(R.string.Cancel) { dismiss() }
                        show()
                    }
                }))
                add(TextV(resId = R.string.LyricSize, onClickListener = {
                    MIUIDialog(activity).apply {
                        setTitle(R.string.LyricSize)
                        setMessage(R.string.LyricSizeTips)
                        setEditText(ActivityOwnSP.ownSPConfig.getLyricSize().toString(), "0")
                        setRButton(R.string.Ok) {
                            if (getEditText() == "") {
                                ActivityOwnSP.ownSPConfig.setLyricSize(0)
                            } else {
                                try {
                                    ActivityOwnSP.ownSPConfig.setLyricSize(getEditText().toInt())
                                } catch (e: Throwable) {
                                    ActivityUtils.showToastOnLooper(
                                        activity,
                                        getString(R.string.LyricColorError)
                                    )
                                    ActivityOwnSP.ownSPConfig.setLyricSize(0)
                                }
                            }
                            dismiss()
                        }
                        setLButton(R.string.Cancel) { dismiss() }
                        show()
                    }
                }))
                add(SeekBarWithTextV("LSize", 0, 50))
                val dataBinding = getDataBinding(ActivityOwnSP.ownSPConfig.getLyricWidth()) { view, flags, data ->
                    when (flags) {
                        1 -> view.visibility = if ((data as Int) == -1) View.VISIBLE else View.GONE
                        2 -> view.visibility = if ((data as Int) == -1) View.VISIBLE else View.GONE
                    }
                }
                add(TextV(resId = R.string.LyricWidth, onClickListener = {
                    MIUIDialog(activity).apply {
                        setTitle(R.string.LyricWidth)
                        setMessage(R.string.LyricTips)
                        setEditText(ActivityOwnSP.ownSPConfig.getLyricWidth().toString(), "-1")
                        setRButton(R.string.Ok) {
                            if (getEditText() == "") {
                                ActivityOwnSP.ownSPConfig.setLyricSize(-1)
                            } else {
                                try {
                                    ActivityOwnSP.ownSPConfig.setLyricWidth(getEditText().toInt())
                                } catch (e: Throwable) {
                                    ActivityUtils.showToastOnLooper(
                                        activity,
                                        getString(R.string.LyricColorError)
                                    )
                                    ActivityOwnSP.ownSPConfig.setLyricSize(-1)
                                }
                            }
                            dismiss()
                        }
                        setLButton(R.string.Cancel) { dismiss() }
                        show()
                    }
                }))
                add(SeekBarWithTextV("LWidth", -1, 100, dataBindingSend = dataBinding.bindingSend))
                add(
                    TextV(
                        resId = R.string.LyricAutoMaxWidth,
                        dataBindingRecv = dataBinding.binding.getRecv(1),
                        onClickListener = {
                            MIUIDialog(activity).apply {
                                setTitle(R.string.LyricAutoMaxWidth)
                                setMessage(R.string.LyricTips)
                                setEditText(ActivityOwnSP.ownSPConfig.getLyricMaxWidth().toString(), "-1")
                                setRButton(R.string.Ok) {
                                    if (getEditText() == "") {
                                        ActivityOwnSP.ownSPConfig.setLyricMaxWidth(-1)
                                    } else {
                                        try {
                                            ActivityOwnSP.ownSPConfig.setLyricMaxWidth(getEditText().toInt())
                                        } catch (e: Throwable) {
                                            ActivityUtils.showToastOnLooper(
                                                activity,
                                                getString(R.string.LyricColorError)
                                            )
                                            ActivityOwnSP.ownSPConfig.setLyricMaxWidth(-1)
                                        }
                                    }
                                    dismiss()
                                }
                                setLButton(R.string.Cancel) { dismiss() }
                                show()
                            }
                        })
                )
                add(SeekBarWithTextV("LMaxWidth", -1, 100, dataBindingRecv = dataBinding.binding.getRecv(2)))
                add(TextV(resId = R.string.LyricPos, onClickListener = {
                    MIUIDialog(activity).apply {
                        setTitle(R.string.LyricPos)
                        setMessage(R.string.LyricPosTips)
                        setEditText(ActivityOwnSP.ownSPConfig.getLyricPosition().toString(), "0")
                        setRButton(R.string.Ok) {
                            ActivityOwnSP.ownSPConfig.setLyricPosition(getEditText().toInt())
                            dismiss()
                        }
                        setLButton(R.string.Cancel) { dismiss() }
                        show()
                    }
                }))
                add(SeekBarWithTextV("LPosition", -900, 900))
                add(TextV(resId = R.string.LyricHigh, onClickListener = {
                    MIUIDialog(activity).apply {
                        setTitle(R.string.LyricHigh)
                        setMessage(R.string.LyricHighTips)
                        setEditText(ActivityOwnSP.ownSPConfig.getLyricHigh().toString(), "0")
                        setRButton(R.string.Ok) {
                            ActivityOwnSP.ownSPConfig.setLyricHigh(getEditText().toInt())
                            dismiss()
                        }
                        setLButton(R.string.Cancel) { dismiss() }
                        show()
                    }
                }))
                add(SeekBarWithTextV("LHigh", -100, 100))
                val dict: HashMap<String, String> = hashMapOf()
                dict["off"] = getString(R.string.Off)
                dict["top"] = getString(R.string.top)
                dict["lower"] = getString(R.string.lower)
                dict["left"] = getString(R.string.left)
                dict["right"] = getString(R.string.right)
                dict["random"] = getString(R.string.random)
                add(TextWithSpinnerV(TextV(resId = R.string.LyricsAnimation), Spinner(arrayListOf(
                    MIUIPopupData(getString(R.string.Off)) { ActivityOwnSP.ownSPConfig.setAnim("off") },
                    MIUIPopupData(getString(R.string.top)) { ActivityOwnSP.ownSPConfig.setAnim("top") },
                    MIUIPopupData(getString(R.string.lower)) { ActivityOwnSP.ownSPConfig.setAnim("lower") },
                    MIUIPopupData(getString(R.string.left)) { ActivityOwnSP.ownSPConfig.setAnim("left") },
                    MIUIPopupData(getString(R.string.right)) { ActivityOwnSP.ownSPConfig.setAnim("right") },
                    MIUIPopupData(getString(R.string.random)) { ActivityOwnSP.ownSPConfig.setAnim("random") }
                ), dict[ActivityOwnSP.ownSPConfig.getAnim()]!!)))
                val timeBinding = getDataBinding(ActivityOwnSP.ownSPConfig.getHideTime()) { view, flags, data ->
                    when (flags) {
                        2 -> view.visibility = if (data as Boolean) View.GONE else View.VISIBLE
                    }
                }
                add(
                    TextWithSwitchV(
                        TextV(resId = R.string.HideTime),
                        SwitchV("HideTime", true, dataBindingSend = timeBinding.bindingSend),
                        dataBindingRecv = timeBinding.binding.getRecv(1)
                    )
                )
                add(
                    TextWithSwitchV(
                        TextV(resId = R.string.ClickLyric),
                        SwitchV("LSwitch", false, dataBindingSend = timeBinding.bindingSend),
                        dataBindingRecv = timeBinding.binding.getRecv(2)
                    )
                )
                add(TextWithSwitchV(TextV(resId = R.string.pseudoTime), SwitchV("PseudoTime", true)))
                add(TextSummaryV(textId = R.string.pseudoTimeStyle, onClick = {
                    MIUIDialog(activity).apply {
                        setTitle(R.string.pseudoTimeStyleTips)
                        setEditText(
                            ActivityOwnSP.ownSPConfig.getPseudoTimeStyle(),
                            ""
                        )
                        setRButton(R.string.Ok) {
                            ActivityOwnSP.ownSPConfig.setPseudoTimeStyle(getEditText())
                            dismiss()
                        }
                        setLButton(R.string.Cancel) { dismiss() }
                        show()
                    }
                }))
                val meiZuStyle = getDataBinding(ActivityOwnSP.ownSPConfig.getLyricStyle()) { view, flags, data ->
                    when (flags) {
                        2 -> view.visibility = if (data as Boolean) View.VISIBLE else View.GONE
                    }
                }
                add(
                    TextWithSwitchV(
                        TextV(resId = R.string.MeizuStyle, dataBindingRecv = meiZuStyle.binding.getRecv(1)),
                        SwitchV("LStyle", true, dataBindingSend = meiZuStyle.bindingSend)
                    )
                )
                add(TextSummaryV(textId = R.string.LyricSpeed, onClick = {
                    MIUIDialog(activity).apply {
                        setTitle(R.string.LyricSpeed)
                        setEditText(ActivityOwnSP.ownSPConfig.getLyricSpeed().toString(), "1.0")
                        setRButton(R.string.Ok) {
                            if (getEditText() == "") {
                                ActivityOwnSP.ownSPConfig.setLyricSpeed(1f)
                            } else {
                                ActivityOwnSP.ownSPConfig.setLyricSpeed(getEditText().toFloat())
                            }
                            dismiss()
                        }
                        setLButton(R.string.Cancel) { dismiss() }
                        show()
                    }
                }, dataBindingRecv = meiZuStyle.binding.getRecv(2)))
                add(TitleTextV(resId = R.string.IconSettings))
                add(TextV(resId = R.string.IconSize, onClickListener = {
                    MIUIDialog(activity).apply {
                        setTitle(R.string.IconSize)
                        setMessage(R.string.LyricHighTips)
                        setEditText(ActivityOwnSP.ownSPConfig.getIconSize().toString(), "0")
                        setRButton(R.string.Ok) {
                            ActivityOwnSP.ownSPConfig.setIconSize(getEditText().toInt())
                            dismiss()
                        }
                        setLButton(R.string.Cancel) { dismiss() }
                        show()
                    }
                }))
                add(SeekBarWithTextV("ISize", 0, 50, ActivityOwnSP.ownSPConfig.getIconSize()))
                add(TextV(resId = R.string.IconHigh, onClickListener = {
                    MIUIDialog(activity).apply {
                        setTitle(R.string.IconHigh)
                        setMessage(R.string.LyricSizeTips)
                        setEditText(ActivityOwnSP.ownSPConfig.getIconHigh().toString(), "7")
                        setRButton(R.string.Ok) {
                            ActivityOwnSP.ownSPConfig.setIconHigh(getEditText().toInt())
                            dismiss()
                        }
                        setLButton(R.string.Cancel) { dismiss() }
                        show()
                    }
                }))
                add(SeekBarWithTextV("IHigh", -100, 100, ActivityOwnSP.ownSPConfig.getIconSize()))
                add(TextWithSwitchV(TextV(resId = R.string.IconAutoColors), SwitchV("IAutoColor", true)))
                add(TextSummaryV(textId = R.string.IconSettings, onClick = {
                    showFragment(getItems(DataItem.CustomIcon), getString(R.string.IconSettings))
                }))
                add(TextV())
            }
            DataItem.CustomIcon -> {
                arrayListOf<BaseView>().apply {
                    val iconConfig = IconConfig(Utils.getSP(activity, "Icon_Config"))
                    for (icon in arrayOf("Netease", "KuGou", "QQMusic", "Myplayer", "MiGu", "Default")) {
                        val drawable = BitmapDrawable(Utils.stringToBitmap(iconConfig.getIcon(icon)))
                        drawable.setTint(getColor(android.R.color.background_dark))
                        add(AuthorV(drawable, icon, onClick = {
                            MIUIDialog(activity).apply {
                                setTitle(icon)
                                setMessage(R.string.MakeIconTitle)
                                setEditText(iconConfig.getIcon(icon).toString(), "")
                                setRButton(R.string.Ok) {
                                    iconConfig.setIcon(icon, getEditText())
                                    dismiss()
                                }
                                setLButton(R.string.Cancel) { dismiss() }
                                show()
                            }
                        }))
                    }
                    add(TextV())
                }
            }
            DataItem.Author -> {
                arrayListOf<BaseView>().apply {
                    add(
                        AuthorV(
                            getDrawable(R.drawable.header_577fkj)!!,
                            "577fkj",
                            getString(R.string.AboutTips1),
                            onClick = {
                                ActivityUtils.openUrl(
                                    activity,
                                    "https://github.com/577fkj"
                                )
                            })
                    )
                    add(
                        AuthorV(
                            getDrawable(R.drawable.header_xiaowine)!!,
                            "xiaowine",
                            getString(R.string.AboutTips2),
                            onClick = {
                                ActivityUtils.openUrl(
                                    activity,
                                    "https://github.com/xiaowine"
                                )
                            })
                    )
                    add(LineV())
                    add(TitleTextV(resId = R.string.ThkListTips))
                    add(TextSummaryV(textId = R.string.ThkListTips, onClick = {
                        ActivityUtils.openUrl(
                            activity,
                            "https://github.com/577fkj/StatusBarLyric#%E6%84%9F%E8%B0%A2%E5%90%8D%E5%8D%95%E4%B8%8D%E5%88%86%E5%85%88%E5%90%8E"
                        )
                    }))
                    add(TextSummaryV(textId = R.string.SponsoredList, onClick = {
                        ActivityUtils.openUrl(
                            activity,
                            "https://github.com/577fkj/StatusBarLyric/blob/Dev/doc/SPONSOR.md"
                        )
                    }))
                    add(LineV())
                    add(TitleTextV(resId = R.string.Other))
                    add(TextSummaryV(textId = R.string.PrivacyPolicy, onClick = {
                        ActivityUtils.openUrl(
                            activity,
                            "https://github.com/577fkj/StatusBarLyric/blob/main/EUAL.md"
                        )
                    }))
                    add(TextSummaryV(textId = R.string.Source, onClick = {
                        ActivityUtils.openUrl(
                            activity,
                            "https://github.com/577fkj/StatusBarLyric"
                        )
                    }))
                    add(TextSummaryV(textId = R.string.Donate, onClick = {
                        ActivityUtils.openUrl(
                            activity,
                            "https://fkj2005.gitee.io/merger/"
                        )
                    }))
                    add(TextV())
                }
            }
        }
    }
}