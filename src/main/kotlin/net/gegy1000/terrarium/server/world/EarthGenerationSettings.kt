package net.gegy1000.terrarium.server.world

import com.google.gson.Gson

data class EarthGenerationSettings(
        var mapFeatures: Boolean = true,
        var buildings: Boolean = true,
        var streets: Boolean = true,
        var decorate: Boolean = true,
        var terrainHeightScale: Double = 1.0,
        var scale: Double = 0.01
) {
    companion object {
        private val GSON = Gson()

        fun deserialize(settings: String): EarthGenerationSettings {
            if (settings.isEmpty()) {
                return EarthGenerationSettings()
            }
            return GSON.fromJson(settings, EarthGenerationSettings::class.java)
        }
    }

    fun serialize() = GSON.toJson(this)
}