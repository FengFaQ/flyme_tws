package github.forest.tool.tws.io

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import io.github.libxposed.service.XposedService

/**
 * Launcher/control UI. The switch writes the module's REAL remote preferences
 * through XposedService; the hook process reads the same group through
 * XposedModule.getRemotePreferences(). This is a functional end-to-end switch,
 * not a decorative screen.
 */
class MainActivity : Activity(), App.ServiceStateListener {

    private var status: TextView? = null
    private var toggle: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 64, 48, 64)
        }

        root.addView(TextView(this).apply {
            text = "FrostTool · TWS\nLSPosed 模块"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#202124"))
            gravity = Gravity.CENTER
        })

        status = TextView(this).apply {
            text = "正在连接 Xposed 服务…"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(Color.parseColor("#5f6368"))
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 32)
        }
        root.addView(status)

        toggle = Button(this).apply {
            text = "启用 / 关闭 伪装"
            isEnabled = false
            setOnClickListener { onToggle() }
        }
        root.addView(toggle)

        root.addView(TextView(this).apply {
            text = ("本项目完全由 AI 生成 · AGPL-3.0\n" +
                    "包名 github.forest.tool.tws.io\n" +
                    "需在 LSPosed 中勾选本模块并授权作用域")
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(Color.parseColor("#9aa0a6"))
            gravity = Gravity.CENTER
            setPadding(0, 48, 0, 0)
        })
        setContentView(root)
    }

    override fun onStart() {
        super.onStart()
        App.addListener(this, true)
    }

    override fun onStop() {
        App.removeListener(this)
        super.onStop()
    }

    override fun onServiceStateChanged(service: XposedService?) {
        runOnUiThread {
            if (service == null) {
                status?.text = "未检测到 Xposed 服务（请先安装/启用 LSPosed）"
                toggle?.isEnabled = false
            } else {
                toggle?.isEnabled = true
                refresh(service)
            }
        }
    }

    private fun refresh(service: XposedService) {
        val enabled = try {
            service.getRemotePreferences(ModuleSettings.PREFS_GROUP)
                .getBoolean(ModuleSettings.KEY_SPOOF_ENABLED, true)
        } catch (t: Throwable) {
            true
        }
        status?.text = ("Xposed 服务已连接（API ${service.apiVersion} / ${service.frameworkName}）\n" +
                "当前伪装开关：" + if (enabled) "已启用" else "已关闭")
    }

    private fun onToggle() {
        val service = App.service ?: return
        try {
            val prefs = service.getRemotePreferences(ModuleSettings.PREFS_GROUP)
            val next = !prefs.getBoolean(ModuleSettings.KEY_SPOOF_ENABLED, true)
            prefs.edit()?.putBoolean(ModuleSettings.KEY_SPOOF_ENABLED, next)?.apply()
            Toast.makeText(this, if (next) "已启用（重启目标进程生效）" else "已关闭", Toast.LENGTH_SHORT).show()
            refresh(service)
        } catch (t: Throwable) {
            Toast.makeText(this, "写入失败：${t.message}", Toast.LENGTH_LONG).show()
        }
    }
}
