package brys.dev.kyro.lib.classes.exceptions

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import brys.dev.kyro.lib.structures.Util
import net.dv8tion.jda.api.entities.Message

class TrackExceptions(private val player: AudioPlayer, private val track: AudioTrack, private val tx: Message?) {
    private val time = Util.Time
    fun trackException(e: FriendlyException, message: String?): Unit? {
        return when (message.isNullOrEmpty()) {
            true -> tx?.editMessage("Track encountered an exception. (${e.message}")?.queue()
            false -> tx?.editMessage(message)?.queue()
        }
    }
    fun trackStuck(ms: Long, message: String?): Unit? {
        val format = time.formatMili(ms)
        return when (message.isNullOrEmpty()) {
            true -> tx?.editMessage("Track got stuck at `$format`")?.queue()
            false -> tx?.editMessage(message)?.queue()
        }
    }

}