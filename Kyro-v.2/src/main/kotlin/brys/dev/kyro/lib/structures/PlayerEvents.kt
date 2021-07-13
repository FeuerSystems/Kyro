package brys.dev.kyro.lib.structures

import com.google.gson.Gson
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import org.json.JSONObject
import java.util.*

object PlayerEvents {
    class GenericPlayer(val player: AudioPlayer, val queue: Queue<AudioTrack>, val guild: Guild) {
        override fun toString(): String {
            val queueObj = ArrayList<JSONObject>()
            for (song in queue) {
                queueObj.add(JSONObject()
                    .put("internal_id", song.identifier)
                    .put("position", song.position)
                    .put("duration", song.duration)
                    .put("info",
                        JSONObject()
                            .put("id", song.info.identifier)
                            .put("author", song.info.author)
                            .put("stream", song.info.isStream)
                            .put("length", song.info.length)
                            .put("title", song.info.title)
                            .put("url", song.info.uri)
                            .put("img", "https://i.ytimg.com/vi/${song.identifier}/maxresdefault.jpg")
                    )
                )
            }
            return JSONObject()
                .put("guild", guild.id)
                .put("player", JSONObject()
                    .put("paused", player.isPaused)
                    .put("queue", queueObj)
                    .put("playing", if (player.playingTrack != null)
                        JSONObject()
                            .put("internal_id", player.playingTrack.identifier)
                            .put("position", player.playingTrack.position)
                            .put("duration", player.playingTrack.duration)
                            .put("info",
                                JSONObject()
                                    .put("id", player.playingTrack.info.identifier)
                                    .put("author", player.playingTrack.info.author)
                                    .put("stream", player.playingTrack.info.isStream)
                                    .put("length", player.playingTrack.info.length)
                                    .put("title", player.playingTrack.info.title)
                                    .put("url", player.playingTrack.info.uri)
                                    .put("img", "https://i.ytimg.com/vi/${player.playingTrack.identifier}/maxresdefault.jpg")
                                    .put("lyrics", if (Util.Misc.getLyrics("${player.playingTrack.info.title} - ${player.playingTrack.info.author}") == null) "No lyrics found :(" else  Util.Misc.getLyrics("${player.playingTrack.info.title} - ${player.playingTrack.info.author}") )
                            ) else null
                    )).toString()
        }
    }
 class PlayerEvent(val player: AudioPlayer, val type: String, val channel: VoiceChannel?, val guild: Guild) {
     override fun toString(): String {
         return JSONObject()
             .put("guild", guild.id)
             .put("channel", channel?.id)
             .put("type", type)
             .put("player",
                 JSONObject()
                     .put("paused", player.isPaused)
                     .put("playing", if (player.playingTrack != null)
                         JSONObject()
                             .put("internal_id", player.playingTrack.identifier)
                             .put("position", player.playingTrack.position)
                             .put("duration", player.playingTrack.duration)
                             .put("info",
                                 JSONObject()
                                     .put("id", player.playingTrack.info.identifier)
                                     .put("author", player.playingTrack.info.author)
                                     .put("stream", player.playingTrack.info.isStream)
                                     .put("length", player.playingTrack.info.length)
                                     .put("title", player.playingTrack.info.title)
                                     .put("url", player.playingTrack.info.uri)
                                     .put("img", "https://i.ytimg.com/vi/${player.playingTrack.identifier}/maxresdefault.jpg")
                                     .put("lyrics", if (Util.Misc.getLyrics("${player.playingTrack.info.title} - ${player.playingTrack.info.author}") == null) "No lyrics found :(" else  Util.Misc.getLyrics("${player.playingTrack.info.title} - ${player.playingTrack.info.author}") )
                             ) else null
                     )
             ).toString()

     }
 }
}