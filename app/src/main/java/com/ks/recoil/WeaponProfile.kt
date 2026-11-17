package com.ks.recoil

/**
 * Recoil compensation profile per weapon.
 * verticalPx: pixels to drag DOWN per step (compensates upward kick)
 * horizontalPx: pixels to drag per step (negative = left, positive = right)
 * stepMs: interval between gesture steps in milliseconds
 * steps: total number of compensation steps to apply
 */
data class RecoilStep(val dx: Float, val dy: Float)

data class WeaponProfile(
    val name: String,
    val category: String,
    val pattern: List<RecoilStep>,   // full pattern sequence
    val stepMs: Long = 60L,          // ms between steps
    val enabled: Boolean = true
)

object WeaponProfiles {

    /**
     * Padrões baseados em dados OB53 / outubro 2026.
     * dy positivo = arrastar para baixo (cancela subida do recoil)
     * dx positivo = arrastar para a direita
     *
     * Escala: ~1px por unidade em 1080p. Ajuste com o multiplicador global na UI.
     */
    val profiles: List<WeaponProfile> = listOf(

        // ── ASSAULT RIFLES ──────────────────────────────────────────────────

        WeaponProfile(
            name = "AK47",
            category = "AR",
            stepMs = 65,
            pattern = listOf(
                // primeiros 5 tiros: kick forte vertical
                RecoilStep(0f, 9f),
                RecoilStep(1f, 10f),
                RecoilStep(2f, 10f),
                RecoilStep(1f, 9f),
                RecoilStep(0f, 8f),
                // 6-12: deriva leve para a direita
                RecoilStep(2f, 7f),
                RecoilStep(3f, 7f),
                RecoilStep(2f, 6f),
                RecoilStep(1f, 6f),
                RecoilStep(0f, 5f),
                RecoilStep(-1f, 5f),
                RecoilStep(-1f, 5f),
                // 13-20: estabiliza com pequena deriva esquerda
                RecoilStep(-2f, 4f),
                RecoilStep(-2f, 4f),
                RecoilStep(-1f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 3f),
                RecoilStep(1f, 3f),
                RecoilStep(1f, 3f),
                RecoilStep(0f, 3f),
            )
        ),

        WeaponProfile(
            name = "M4A1",
            category = "AR",
            stepMs = 60,
            pattern = listOf(
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(1f, 5f),
                RecoilStep(1f, 5f),
                RecoilStep(1f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(-1f, 4f),
                RecoilStep(-1f, 4f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
            )
        ),

        WeaponProfile(
            name = "SCAR",
            category = "AR",
            stepMs = 60,
            pattern = listOf(
                RecoilStep(0f, 6f),
                RecoilStep(0f, 6f),
                RecoilStep(1f, 5f),
                RecoilStep(1f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(-1f, 4f),
                RecoilStep(-1f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
            )
        ),

        WeaponProfile(
            name = "GROZA",
            category = "AR",
            stepMs = 55,
            pattern = listOf(
                RecoilStep(0f, 8f),
                RecoilStep(1f, 8f),
                RecoilStep(1f, 7f),
                RecoilStep(0f, 7f),
                RecoilStep(-1f, 6f),
                RecoilStep(-1f, 6f),
                RecoilStep(0f, 6f),
                RecoilStep(1f, 5f),
                RecoilStep(1f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 4f),
                RecoilStep(-1f, 4f),
                RecoilStep(-1f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
            )
        ),

        WeaponProfile(
            name = "AN94",
            category = "AR",
            stepMs = 65,
            pattern = listOf(
                RecoilStep(0f, 11f),
                RecoilStep(1f, 11f),
                RecoilStep(2f, 10f),
                RecoilStep(2f, 9f),
                RecoilStep(1f, 8f),
                RecoilStep(0f, 7f),
                RecoilStep(-1f, 7f),
                RecoilStep(-2f, 6f),
                RecoilStep(-1f, 6f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(1f, 5f),
                RecoilStep(1f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
            )
        ),

        WeaponProfile(
            name = "M7",
            category = "AR",
            stepMs = 60,
            pattern = listOf(
                RecoilStep(0f, 7f),
                RecoilStep(0f, 7f),
                RecoilStep(0f, 7f),
                RecoilStep(0f, 7f),
                RecoilStep(0f, 7f),
                RecoilStep(0f, 7f),
                RecoilStep(0f, 7f),
                RecoilStep(0f, 7f),
                RecoilStep(0f, 7f),
                RecoilStep(0f, 7f),
                RecoilStep(0f, 6f),
                RecoilStep(0f, 6f),
                RecoilStep(0f, 6f),
                RecoilStep(0f, 6f),
                RecoilStep(0f, 6f),
                RecoilStep(0f, 6f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
            )
        ),

        WeaponProfile(
            name = "AUG",
            category = "AR",
            stepMs = 55,
            pattern = listOf(
                RecoilStep(0f, 6f),
                RecoilStep(1f, 6f),
                RecoilStep(1f, 6f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(-1f, 5f),
                RecoilStep(-1f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
            )
        ),

        WeaponProfile(
            name = "XM8",
            category = "AR",
            stepMs = 60,
            pattern = listOf(
                RecoilStep(0f, 6f),
                RecoilStep(0f, 6f),
                RecoilStep(1f, 5f),
                RecoilStep(1f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(-1f, 4f),
                RecoilStep(-1f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
            )
        ),

        WeaponProfile(
            name = "PARAFAL",
            category = "AR",
            stepMs = 70,
            pattern = listOf(
                RecoilStep(0f, 9f),
                RecoilStep(1f, 9f),
                RecoilStep(2f, 8f),
                RecoilStep(1f, 7f),
                RecoilStep(0f, 7f),
                RecoilStep(-1f, 6f),
                RecoilStep(-2f, 6f),
                RecoilStep(-1f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
            )
        ),

        // ── SMGs ────────────────────────────────────────────────────────────

        WeaponProfile(
            name = "MP40",
            category = "SMG",
            stepMs = 45,
            pattern = listOf(
                RecoilStep(0f, 5f),
                RecoilStep(1f, 5f),
                RecoilStep(1f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(-1f, 4f),
                RecoilStep(-1f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(1f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
            )
        ),

        WeaponProfile(
            name = "UMP",
            category = "SMG",
            stepMs = 60,
            pattern = listOf(
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 2f),
            )
        ),

        WeaponProfile(
            name = "MP5",
            category = "SMG",
            stepMs = 50,
            pattern = listOf(
                RecoilStep(0f, 4f),
                RecoilStep(0f, 5f),
                RecoilStep(1f, 5f),
                RecoilStep(1f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(-1f, 3f),
                RecoilStep(-1f, 3f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
            )
        ),

        WeaponProfile(
            name = "SKORP",
            category = "SMG",
            stepMs = 45,
            pattern = listOf(
                RecoilStep(0f, 6f),
                RecoilStep(0f, 6f),
                RecoilStep(0f, 6f),
                RecoilStep(0f, 6f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 3f),
                RecoilStep(0f, 3f),
            )
        ),

        WeaponProfile(
            name = "Thompson",
            category = "SMG",
            stepMs = 55,
            pattern = listOf(
                RecoilStep(0f, 5f),
                RecoilStep(1f, 5f),
                RecoilStep(2f, 5f),
                RecoilStep(2f, 4f),
                RecoilStep(2f, 4f),
                RecoilStep(2f, 4f),
                RecoilStep(2f, 4f),
                RecoilStep(2f, 4f),
                RecoilStep(2f, 4f),
                RecoilStep(2f, 4f),
                RecoilStep(2f, 4f),
                RecoilStep(2f, 4f),
                RecoilStep(2f, 4f),
                RecoilStep(2f, 4f),
                RecoilStep(1f, 3f),
                RecoilStep(3f, 4f),
                RecoilStep(3f, 4f),
                RecoilStep(3f, 4f),
                RecoilStep(2f, 3f),
                RecoilStep(2f, 3f),
            )
        ),

        // ── LMGs ────────────────────────────────────────────────────────────

        WeaponProfile(
            name = "M60",
            category = "LMG",
            stepMs = 70,
            pattern = listOf(
                RecoilStep(0f, 8f),
                RecoilStep(1f, 8f),
                RecoilStep(1f, 8f),
                RecoilStep(0f, 7f),
                RecoilStep(-1f, 7f),
                RecoilStep(-1f, 7f),
                RecoilStep(0f, 6f),
                RecoilStep(1f, 6f),
                RecoilStep(1f, 6f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
            )
        ),

        WeaponProfile(
            name = "RPK",
            category = "LMG",
            stepMs = 65,
            pattern = listOf(
                RecoilStep(0f, 9f),
                RecoilStep(-1f, 8f),
                RecoilStep(-2f, 7f),
                RecoilStep(-1f, 7f),
                RecoilStep(0f, 7f),
                RecoilStep(1f, 6f),
                RecoilStep(2f, 6f),
                RecoilStep(1f, 6f),
                RecoilStep(0f, 5f),
                RecoilStep(-1f, 5f),
                RecoilStep(-1f, 5f),
                RecoilStep(0f, 5f),
                RecoilStep(1f, 4f),
                RecoilStep(1f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
                RecoilStep(0f, 4f),
            )
        ),
    )

    fun byName(name: String) = profiles.find { it.name == name }
    fun byCategory(cat: String) = profiles.filter { it.category == cat }
}
