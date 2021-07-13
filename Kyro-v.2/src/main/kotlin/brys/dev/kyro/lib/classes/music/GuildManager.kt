package brys.dev.kyro.lib.classes.music

import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import brys.dev.kyro.lib.model.AudioSendHandler
import brys.dev.kyro.lib.model.Skipper
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User

/**
 * Our guild manager managing music for Discord Servers
 */
class GuildManager(val manager: AudioPlayerManager, guild: Guild, message: Message?, requester: User) {
    val player: AudioPlayer = manager.createPlayer()
    var trackManager: TrackManager? = null
    val sendHandler: AudioSendHandler
    var skips = mutableListOf<Skipper>()

    init {
        trackManager = TrackManager(player, guild, message, requester)
        player.addListener(this.trackManager)
        sendHandler = AudioSendHandler(this.player)
    }
}