package brys.dev.kyro.lib.classes.spotify

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser


data class SpotifySong(val name: String,
                       val artwork: String,
                       val artist: String,
                       val popularity: Long,
                       val explicit: Boolean,
                       val full_track: String,
                       val features: SpotifyTrackFeatures,
                       val url: String)
{
   companion object {
       fun getSong(string: String): SpotifySong {
           val src: Any = JSONParser().parse(string)
           val json: JSONObject = src as JSONObject
           val obj: JSONObject = json["track"] as JSONObject
           val title = obj["name"] as String
           val art = obj["artwork"] as String
           val artist = obj["artist"] as String
           val popularity = obj["popularity"] as Long
           val explicit = obj["explicit"] as Boolean
           val full_track = obj["full_track"] as String
           val url = obj["url"] as String
           val features = SpotifyTrackFeatures.getFeatures(obj["features"] as JSONObject)
           return SpotifySong(title, art, artist, popularity, explicit, full_track, features, url)
       }
    }
}
