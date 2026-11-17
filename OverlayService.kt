package com.ks.recoil

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var fireBtn: View? = null
    private var panel: LinearLayout? = null
    private var panelParams: WindowManager.LayoutParams? = null

    // refs para atualizar no painel
    private var tvWeaponName: TextView? = null
    private var tvMult: TextView? = null
    private var tvToggle: TextView? = null
    private var tvVisToggle: TextView? = null
    private var weaponListContainer: LinearLayout? = null
    private var weaponListVisible = false

    // botão de disparo começa 100% invisível (apenas área de toque);
    // só liga visual temporário pra reposicionar
    private var fireBtnVisible = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(1, buildNotification())
        createFireButton()
        createPanel()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        fireBtn?.let { wm.removeView(it) }
        panel?.let { wm.removeView(it) }
    }

    // ── Notificação ────────────────────────────────────────────────────────────

    /**
     * Notificação exigida pelo foreground service. Canal e texto genéricos —
     * "Serviço de acessibilidade ativo" é o que qualquer app com accessibility
     * service legítimo mostra, não chama atenção específica pro app.
     */
    private fun buildNotification(): Notification {
        val ch = "accessibility_svc"
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(
                NotificationChannel(ch, "Serviço do sistema", NotificationManager.IMPORTANCE_MIN)
            )
        return Notification.Builder(this, ch)
            .setContentTitle("Serviço de acessibilidade ativo")
            .setContentText("Em execução")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()
    }

    // ── Botão de disparo ───────────────────────────────────────────────────────

    private fun createFireButton() {
        val params = WindowManager.LayoutParams(
            dp(72), dp(72),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            x = dp(80); y = dp(180)
        }

        val btn = View(this).apply {
            // invisível por padrão — é só área de toque, sem nenhum pixel desenhado.
            // fireBtnVisible liga um contorno fraco temporário só pra reposicionar.
            setBackgroundColor(Color.TRANSPARENT)
        }

        var dragX = 0; var dragY = 0; var dragging = false

        btn.setOnTouchListener { _, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragX = ev.rawX.toInt() - params.x
                    dragY = ev.rawY.toInt() - params.y
                    dragging = false
                    RecoilAccessibilityService.instance?.startRecoil()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = ev.rawX.toInt() - dragX - params.x
                    val dy = ev.rawY.toInt() - dragY - params.y
                    if (!dragging && (Math.abs(dx) > 8 || Math.abs(dy) > 8)) {
                        dragging = true
                        RecoilAccessibilityService.instance?.stopRecoil()
                    }
                    if (dragging) {
                        params.x = ev.rawX.toInt() - dragX
                        params.y = ev.rawY.toInt() - dragY
                        wm.updateViewLayout(btn, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    RecoilAccessibilityService.instance?.stopRecoil()
                    true
                }
                else -> false
            }
        }

        wm.addView(btn, params)
        fireBtn = btn
    }

    // ── Painel de controle ─────────────────────────────────────────────────────

    private fun createPanel() {
        val params = WindowManager.LayoutParams(
            dp(160),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(12); y = dp(100)
        }
        panelParams = params

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.argb(220, 15, 15, 15))
            setPadding(dp(10), dp(8), dp(10), dp(8))
        }

        // ── Header ──
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(6))
        }

        val tvTitle = TextView(this).apply {
            text = "KS"
            textSize = 10f
            setTextColor(Color.parseColor("#FF6B00"))
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        tvToggle = TextView(this).apply {
            val on = RecoilAccessibilityService.enabled
            text = if (on) "●ON" else "○OFF"
            textSize = 10f
            setTextColor(if (on) Color.parseColor("#00C853") else Color.parseColor("#FF4444"))
            setOnClickListener {
                RecoilAccessibilityService.enabled = !RecoilAccessibilityService.enabled
                val enabled = RecoilAccessibilityService.enabled
                text = if (enabled) "●ON" else "○OFF"
                setTextColor(if (enabled) Color.parseColor("#00C853") else Color.parseColor("#FF4444"))
            }
        }

        tvVisToggle = TextView(this).apply {
            text = "👁"
            textSize = 10f
            alpha = 0.5f
            setPadding(dp(6), 0, 0, 0)
            setOnClickListener { toggleFireBtnVisibility() }
        }

        header.addView(tvTitle)
        header.addView(tvToggle)
        header.addView(tvVisToggle)
        root.addView(header)

        // ── Arma atual ──
        val activeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(4))
        }

        tvWeaponName = TextView(this).apply {
            text = RecoilAccessibilityService.activeProfile?.name ?: "— sem arma —"
            textSize = 11f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvArrowToggle = TextView(this).apply {
            text = "▾"
            textSize = 10f
            setTextColor(Color.parseColor("#888888"))
            setOnClickListener { toggleWeaponList(root) }
        }

        activeRow.addView(tvWeaponName)
        activeRow.addView(tvArrowToggle)
        root.addView(activeRow)

        // ── Lista de armas (oculta por padrão) ──
        weaponListContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
        buildWeaponList()
        root.addView(weaponListContainer)

        // ── Divisor ──
        root.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).apply {
                setMargins(0, dp(6), 0, dp(6))
            }
            setBackgroundColor(Color.parseColor("#2A2A2A"))
        })

        // ── Multiplicador ──
        val multRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val tvMultLabel = TextView(this).apply {
            text = "×"
            textSize = 10f
            setTextColor(Color.parseColor("#888888"))
        }

        val btnMinus = TextView(this).apply {
            text = "−"
            textSize = 16f
            setTextColor(Color.parseColor("#AAAAAA"))
            setPadding(dp(4), 0, dp(4), 0)
            setOnClickListener {
                val cur = RecoilAccessibilityService.multiplier
                if (cur > 0.1f) {
                    RecoilAccessibilityService.multiplier = (cur - 0.1f).coerceAtLeast(0.1f)
                    updateMultLabel()
                }
            }
        }

        tvMult = TextView(this).apply {
            text = formatMult()
            textSize = 12f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            minWidth = dp(32)
        }

        val btnPlus = TextView(this).apply {
            text = "+"
            textSize = 16f
            setTextColor(Color.parseColor("#AAAAAA"))
            setPadding(dp(4), 0, dp(4), 0)
            setOnClickListener {
                val cur = RecoilAccessibilityService.multiplier
                if (cur < 3.0f) {
                    RecoilAccessibilityService.multiplier = (cur + 0.1f).coerceAtMost(3.0f)
                    updateMultLabel()
                }
            }
        }

        multRow.addView(tvMultLabel)
        multRow.addView(btnMinus)
        multRow.addView(tvMult)
        multRow.addView(btnPlus)
        root.addView(multRow)

        // ── Drag do painel ──
        var dragX = 0; var dragY = 0
        root.setOnTouchListener { _, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragX = ev.rawX.toInt() - params.x
                    dragY = ev.rawY.toInt() - params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = ev.rawX.toInt() - dragX
                    params.y = ev.rawY.toInt() - dragY
                    wm.updateViewLayout(root, params)
                    true
                }
                else -> false
            }
        }

        wm.addView(root, params)
        panel = root
    }

    private fun buildWeaponList() {
        val container = weaponListContainer ?: return
        container.removeAllViews()

        listOf("AR", "SMG", "LMG").forEach { cat ->
            val catLabel = TextView(this).apply {
                text = cat
                textSize = 9f
                setTextColor(Color.parseColor("#FF6B00"))
                setPadding(0, dp(4), 0, dp(2))
            }
            container.addView(catLabel)

            WeaponProfiles.byCategory(cat).forEach { profile ->
                val item = TextView(this).apply {
                    text = profile.name
                    textSize = 11f
                    setTextColor(Color.parseColor("#CCCCCC"))
                    setPadding(dp(4), dp(3), dp(4), dp(3))
                    setOnClickListener {
                        RecoilAccessibilityService.activeProfile = profile
                        tvWeaponName?.text = profile.name
                        // colapsa lista
                        weaponListContainer?.visibility = View.GONE
                        weaponListVisible = false
                    }
                }
                container.addView(item)
            }
        }
    }

    /**
     * O botão de disparo é invisível por padrão (apenas área de toque, sem
     * pixel desenhado). Esse toggle liga um contorno fraco temporário — só
     * pra facilitar reposicionar sobre o botão de fire do FF — e desliga
     * sozinho depois de alguns segundos.
     */
    private fun toggleFireBtnVisibility() {
        val btn = fireBtn ?: return
        fireBtnVisible = !fireBtnVisible
        if (fireBtnVisible) {
            btn.background = resources.getDrawable(android.R.drawable.btn_default, null)
                .also { it.setTint(Color.argb(70, 255, 107, 0)) }
            tvVisToggle?.alpha = 1f
            handler.postDelayed({
                if (fireBtnVisible) {
                    fireBtnVisible = false
                    btn.setBackgroundColor(Color.TRANSPARENT)
                    tvVisToggle?.alpha = 0.5f
                }
            }, 6000L)
        } else {
            btn.setBackgroundColor(Color.TRANSPARENT)
            tvVisToggle?.alpha = 0.5f
        }
    }

    private fun toggleWeaponList(root: LinearLayout) {
        val container = weaponListContainer ?: return
        weaponListVisible = !weaponListVisible
        container.visibility = if (weaponListVisible) View.VISIBLE else View.GONE
        panelParams?.let { wm.updateViewLayout(root, it) }
    }

    private fun updateMultLabel() {
        tvMult?.text = formatMult()
    }

    private fun formatMult() = "%.1f".format(RecoilAccessibilityService.multiplier)

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
