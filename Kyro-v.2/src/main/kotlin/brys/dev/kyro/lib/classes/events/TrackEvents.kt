package brys.dev.kyro.lib.classes.events

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import brys.dev.kyro.lib.classes.db.FindServerSetting
import brys.dev.kyro.lib.methods.Logger
import brys.dev.kyro.lib.structures.Util
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import java.util.*

class TrackEvents(private val player: AudioPlayer, private val track: AudioTrack, private val message: Message?) {
    private val time = Util.Time
    fun trackStart(queue: Queue<AudioTrack>) {
        val logger = Logger(this.javaClass)
                val live = if(track.info.isStream) "LIVE" else time.formatMili(track.duration)
                val next = if(queue.peek() == null) "None" else "[${queue.peek().info.title}](${queue.peek().info.uri})"
                val picture = "https://i.ytimg.com/vi/${track.identifier}/maxresdefault.jpg"
                val meta = track.userData as User
                val nowPlaying = EmbedBuilder()
                    .setAuthor("Now playing")
                    .setTitle(track.info.title,track.info.uri)
                    .setColor(message?.guild?.id?.let { FindServerSetting(it).color })
                    .addField("Duration",live,true)
                    .addField("Channel",track.info.author,true)
                    .addField("Next Up",next,true)
                    .setThumbnail(picture)
                    .setFooter("Requested By: ${meta.asTag}", meta.effectiveAvatarUrl)
                    .build()
        message?.channel?.sendMessage(nowPlaying)?.queue()

         logger.info("Now playing ${track.info.title} Requested by: ${meta.id} | In Guild Channel ${message?.channel?.id} | In Guild ${message?.guild?.id}")
            }
        }