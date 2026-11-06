package com.frost.tool

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.TextView
import android.view.Gravity

/** Minimal launcher screen. Real behaviour lives in the Xposed hooks. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this).apply {
            text = "FrostTool · TWS\n\n这是一个由 AI 生成的 Xposed / LSPosed 模块。\n请在 LSPosed 中启用本模块并打开 tws_device_spoof_enabled 开关。\n\n本项目采用 AGPL-3.0 协议。"
            setTextColor(Color.parseColor("#202124"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }
        setContentView(tv)
    }
}
