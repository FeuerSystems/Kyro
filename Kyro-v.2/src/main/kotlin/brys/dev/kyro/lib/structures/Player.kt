package brys.dev.kyro.lib.structures


import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents
import brys.dev.kyro.lib.model.PlayerManager
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User

/**
 * Player object for queueing tracks
 */
object Player {
    fun queue(channel: TextChannel, requester: User, now: Boolean, message: Message?, embed: MessageEmbed?,  url: List<String>, wsEvents: WSEvents) {
        val playerManager = PlayerManager.instance
        val musicManager = PlayerManager.instance!!.getGuildManager(channel.guild, message, requester)
            if (url.isEmpty()) {
                return channel.sendMessage("No track was found with that name.").queue()
            }
            if (now) {
                musicManager.player.destroy(); playerManager!!.play(channel, requester, message, embed, url, wsEvents)
                return
            } else
                playerManager!!.play(channel, requester, message, embed, url, wsEvents)
        }

    }

