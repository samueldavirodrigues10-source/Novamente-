package com.ks.recoil

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color

class MainActivity : AppCompatActivity() {

    private lateinit var dotOverlay: View
    private lateinit var dotAccess: View
    private lateinit var tvOverlayStatus: TextView
    private lateinit var tvAccessStatus: TextView
    private lateinit var tvActiveWeapon: TextView
    private lateinit var btnStart: Button
    private lateinit var rowAR: LinearLayout
    private lateinit var rowSMG: LinearLayout
    private lateinit var rowLMG: LinearLayout

    private val chipViews = mutableMapOf<String, TextView>()
    private var selectedProfile: WeaponProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dotOverlay     = findViewById(R.id.dotOverlay)
        dotAccess      = findViewById(R.id.dotAccess)
        tvOverlayStatus = findViewById(R.id.tvOverlayStatus)
        tvAccessStatus  = findViewById(R.id.tvAccessStatus)
        tvActiveWeapon  = findViewById(R.id.tvActiveWeapon)
        btnStart        = findViewById(R.id.btnStart)
        rowAR           = findViewById(R.id.rowAR)
        rowSMG          = findViewById(R.id.rowSMG)
        rowLMG          = findViewById(R.id.rowLMG)

        findViewById<Button>(R.id.btnOverlay).setOnClickListener { requestOverlayPermission() }
        findViewById<Button>(R.id.btnAccessibility).setOnClickListener { openAccessibilitySettings() }
        btnStart.setOnClickListener { startOverlayService() }
        findViewById<Button>(R.id.btnStop).setOnClickListener { stopOverlayService() }

        populateWeaponRows()
        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val overlayOk = Settings.canDrawOverlays(this)
        val accessOk  = isAccessibilityEnabled()

        dotOverlay.setBackgroundResource(if (overlayOk) R.drawable.dot_active else R.drawable.dot_inactive)
        dotAccess.setBackgroundResource(if (accessOk) R.drawable.dot_active else R.drawable.dot_inactive)

        tvOverlayStatus.text      = if (overlayOk) "Ativo" else "Pendente"
        tvOverlayStatus.setTextColor(if (overlayOk) Color.parseColor("#00C853") else Color.parseColor("#888888"))
        tvAccessStatus.text       = if (accessOk) "Ativo" else "Pendente"
        tvAccessStatus.setTextColor(if (accessOk) Color.parseColor("#00C853") else Color.parseColor("#888888"))

        btnStart.isEnabled = overlayOk && accessOk
        btnStart.setTextColor(if (overlayOk && accessOk) Color.BLACK else Color.parseColor("#555555"))
    }

    private fun populateWeaponRows() {
        val rowMap = mapOf("AR" to rowAR, "SMG" to rowSMG, "LMG" to rowLMG)
        val dp = resources.displayMetrics.density

        rowMap.forEach { (cat, row) ->
            row.removeAllViews()
            WeaponProfiles.byCategory(cat).forEach { profile ->
                val chip = TextView(this).apply {
                    text = profile.name
                    textSize = 11f
                    setTextColor(Color.parseColor("#BBBBBB"))
                    setBackgroundResource(R.drawable.weapon_chip_default)
                    setPadding(
                        (10 * dp).toInt(), (6 * dp).toInt(),
                        (10 * dp).toInt(), (6 * dp).toInt()
                    )
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 0, (6 * dp).toInt(), 0) }
                    layoutParams = lp
                    setOnClickListener { selectWeapon(profile, this) }
                }
                chipViews[profile.name] = chip
                row.addView(chip)
            }
        }
    }

    private fun selectWeapon(profile: WeaponProfile, chip: TextView) {
        selectedProfile?.let { prev ->
            chipViews[prev.name]?.apply {
                setBackgroundResource(R.drawable.weapon_chip_default)
                setTextColor(Color.parseColor("#BBBBBB"))
            }
        }
        selectedProfile = profile
        RecoilAccessibilityService.activeProfile = profile
        chip.setBackgroundResource(R.drawable.weapon_chip_active)
        chip.setTextColor(Color.parseColor("#FF6B00"))
        tvActiveWeapon.text = profile.name
    }

    private fun requestOverlayPermission() {
        startActivity(
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        )
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun isAccessibilityEnabled(): Boolean {
        val service = "$packageName/${RecoilAccessibilityService::class.java.canonicalName}"
        val enabled = try {
            Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
        } catch (e: Exception) { "" }
        return TextUtils.SimpleStringSplitter(':').apply { setString(enabled) }
            .asSequence()
            .any { it.equals(service, ignoreCase = true) }
    }

    private fun startOverlayService() {
        startForegroundService(Intent(this, OverlayService::class.java))
    }

    private fun stopOverlayService() {
        stopService(Intent(this, OverlayService::class.java))
    }
}
