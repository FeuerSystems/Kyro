package brys.dev.kyro.lib.classes.spotify

import org.json.simple.JSONObject


data class SpotifyTrackFeatures(val dance: Double,
                                val energy: Double,
                                val key: Long,
                                val loud: Double,
                                val mode: Long,
                                val speech: Double,
                                val acoustic: Double,
                                val instrument: Double,
                                val live: Double,
                                val tempo: Double,
                                val duration: Long) {
    companion object {
        fun getFeatures(obj: JSONObject): SpotifyTrackFeatures {
            return SpotifyTrackFeatures(
                obj["dance"] as Double,
                obj["energy"] as Double,
                obj["key"] as Long,
                obj["loud"] as Double,
                obj["mode"] as Long,
                obj["speech"] as Double,
                obj["acoustic"] as Double,
                obj["instrument"] as Double,
                obj["live"] as Double,
                obj["tempo"] as Double,
                obj["duration"] as Long
            )
        }
    }
}