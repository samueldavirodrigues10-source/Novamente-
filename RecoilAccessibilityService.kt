package com.ks.recoil

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import kotlin.math.max
import kotlin.random.Random
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Serviço de acessibilidade que injeta gestos de compensação de recoil
 * enquanto o jogador segura o botão de disparo.
 *
 * Fluxo:
 *  1. OverlayService recebe toque no botão de disparo (área configurada)
 *  2. Chama startRecoil() com o profile selecionado
 *  3. Gestos de arrasto são injetados step a step via dispatchGesture()
 *  4. stopRecoil() cancela a sequência ao soltar o botão
 *
 * Humanização (anti-padrão):
 *  - path curvo (ponto de controle quadrático) em vez de linha reta
 *  - jitter percentual em dx/dy por step (varia a magnitude sem perder a direção)
 *  - jitter no stepMs (varia o timing dentro de uma janela plausível)
 *  - âncora com deslocamento pequeno e aleatório por sessão de disparo,
 *    mais um micro-jitter por step, pra não repetir exatamente o mesmo ponto de toque
 */
class RecoilAccessibilityService : AccessibilityService() {

    companion object {
        var instance: RecoilAccessibilityService? = null
            private set

        // estado compartilhado com OverlayService
        @Volatile var activeProfile: WeaponProfile? = null
        @Volatile var multiplier: Float = 1.0f
        @Volatile var sensitivityScale: Float = 1.0f
        @Volatile var enabled: Boolean = true

        // 0f = desligado, 1f = jitter "normal" (±~12% mag, ±~15% timing)
        @Volatile var humanization: Float = 1.0f
    }

    private val handler = Handler(Looper.getMainLooper())
    private val isRunning = AtomicBoolean(false)
    private val stepIndex = AtomicInteger(0)
    private val rng = Random(System.nanoTime())

    private var screenWidth = 1080
    private var screenHeight = 2400

    // deslocamento de âncora fixo para a sessão de disparo atual
    private var sessionAnchorOffsetX = 0f
    private var sessionAnchorOffsetY = 0f

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(dm)
        screenWidth = dm.widthPixels
        screenHeight = dm.heightPixels
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() { stopRecoil() }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        stopRecoil()
    }

    fun startRecoil() {
        if (!enabled) return
        val profile = activeProfile ?: return
        if (isRunning.getAndSet(true)) return

        stepIndex.set(0)
        // nova âncora "pessoal" pra essa rajada — pequeno, não desloca o polegar
        // de forma perceptível, mas evita repetir px exatos a cada disparo
        val spread = screenWidth * 0.01f * humanization
        sessionAnchorOffsetX = randRange(-spread, spread)
        sessionAnchorOffsetY = randRange(-spread, spread)

        scheduleNextStep(profile)
    }

    fun stopRecoil() {
        isRunning.set(false)
        handler.removeCallbacksAndMessages(null)
    }

    private fun scheduleNextStep(profile: WeaponProfile) {
        val idx = stepIndex.get()
        if (!isRunning.get()) return

        // loop infinito sobre o padrão enquanto o disparo continua
        val step = profile.pattern[idx % profile.pattern.size]

        // jitter de magnitude: ±(humanization * 12%) em cada eixo, independentes
        val magJitterX = 1f + randRange(-0.12f, 0.12f) * humanization
        val magJitterY = 1f + randRange(-0.12f, 0.12f) * humanization

        val dx = step.dx * multiplier * sensitivityScale * magJitterX
        val dy = step.dy * multiplier * sensitivityScale * magJitterY

        // jitter de timing: ±(humanization * 15%), nunca abaixo de 16ms (~1 frame a 60Hz)
        val timingJitter = 1f + randRange(-0.15f, 0.15f) * humanization
        val stepDuration = max(16L, (profile.stepMs * timingJitter).toLong())

        injectDrag(dx, dy, stepDuration)

        stepIndex.incrementAndGet()

        // o próximo agendamento também varia — timing do loop não é o mesmo
        // valor fixo de profile.stepMs toda vez
        handler.postDelayed({
            scheduleNextStep(profile)
        }, stepDuration)
    }

    /**
     * Injeta um arrasto relativo ao centro inferior da tela
     * (aproximação para onde o polegar costuma estar no Free Fire).
     * Path curvo + âncora com jitter — evita a assinatura de "linha reta,
     * ponto fixo, duração exata" que caracteriza input sintético.
     */
    private fun injectDrag(dx: Float, dy: Float, durationMs: Long) {
        val anchorX = screenWidth * 0.75f + sessionAnchorOffsetX + randRange(-2f, 2f) * humanization
        val anchorY = screenHeight * 0.75f + sessionAnchorOffsetY + randRange(-2f, 2f) * humanization

        val endX = anchorX + dx
        val endY = anchorY + dy

        // ponto de controle fora da reta anchor→end, desloca o traço pra uma curva suave
        val midX = (anchorX + endX) / 2f + randRange(-1.5f, 1.5f) * humanization * (if (dx == 0f && dy == 0f) 0f else 1f)
        val midY = (anchorY + endY) / 2f + randRange(-1.5f, 1.5f) * humanization * (if (dx == 0f && dy == 0f) 0f else 1f)

        val path = Path().apply {
            moveTo(anchorX, anchorY)
            quadTo(midX, midY, endX, endY)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(
                    path,
                    0L,
                    durationMs.coerceAtLeast(16L),
                    true  // continuable — mantém contato
                )
            )
            .build()

        dispatchGesture(gesture, null, null)
    }

    private fun randRange(min: Float, max: Float): Float =
        if (max <= min) min else min + rng.nextFloat() * (max - min)
}
