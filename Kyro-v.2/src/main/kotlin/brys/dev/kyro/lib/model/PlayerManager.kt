package brys.dev.kyro.lib.model

import brys.dev.kyro.lib.classes.db.AddUserData
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import brys.dev.kyro.lib.classes.db.FindServerSetting
import brys.dev.kyro.lib.classes.db.FindUserData
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents
import brys.dev.kyro.lib.structures.Util
import brys.dev.kyro.lib.classes.music.GuildManager
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import java.awt.Color
import java.util.HashMap
import java.util.function.Consumer


/**
 * Lavawraps player manager handling the playing of the physical tracks.
 * [play]
 */

class PlayerManager private constructor() {
    private val PlayerManager: AudioPlayerManager
    private val guildManagers: MutableMap<Long, GuildManager>
    private val time = Util.Time
    fun getGuildManager(guild: Guild, message: Message?, requester: User): GuildManager {
        val id = guild.idLong
        var guildManager = guildManagers[id]
        when (guildManager) {
            null -> {
                guildManager = GuildManager(PlayerManager, guild, message, requester)
                guildManagers[id] = guildManager
            }
        }
        guild.audioManager.sendingHandler = guildManager!!.sendHandler
        return guildManager
    }

    fun play(channel: TextChannel, requester: User, message: Message?, embed: MessageEmbed?,  url: List<String>, wsEvents: WSEvents) {
        val guildManager = getGuildManager(channel.guild, message,requester)
        for (uri in url) {
            this.PlayerManager.loadItemOrdered(guildManager, uri, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    track.userData = requester
                    play(guildManager, track, channel)
                    if (embed != null) {
                        return channel.sendMessage(embed).queue()
                    }
                    if (guildManager.trackManager?.queue?.toList()?.size == 0 ) {
                        val picture = "https://i.ytimg.com/vi/${track.identifier}/maxresdefault.jpg"
                        val live = if (track.info.isStream) "LIVE" else time.formatMili(track.duration)
                        var untilPlayed = 0L
                        val tempQueue = guildManager.trackManager!!.queue.toMutableList()
                        val trackOrTracks = if (guildManager.trackManager!!.queue.size >= 2) "tracks" else "track"
                        val trackEmbed = embed ?: EmbedBuilder()
                            .setTitle("♪ Now Playing")
                            .addField("Track", "[${track.info.title}](${track.info.uri})", true)
                            .addField("Channel", track.info.author, true)
                            .setThumbnail(picture)
                            .addField("Duration", live, true)
                            .addField(
                                "Estimated Time Until Playing",
                                "${guildManager.trackManager!!.queue.size} ${trackOrTracks}, and ${
                                    Util.Time.getDurationBreakdown(untilPlayed)
                                        ?.replace("/0 (?: Second | Minute |Hour|Day)s/".toRegex(), "")
                                } until played",
                                false
                            )
                            .setColor(FindServerSetting(channel.guild.id).color)
                            .setFooter("Requested By: ${requester.asTag}", requester.effectiveAvatarUrl).build()
                        channel.sendMessage(trackEmbed).queue()
                    }
                    if (guildManager.trackManager?.queue?.peek() == null) {
                        return
                    }
                    if (uri == url.first()) {
                        val picture = "https://i.ytimg.com/vi/${track.identifier}/maxresdefault.jpg"
                        val live = if (track.info.isStream) "LIVE" else time.formatMili(track.duration)
                        var untilPlayed = 0L
                        val tempQueue = guildManager.trackManager!!.queue.toMutableList()
                        val trackOrTracks = if (guildManager.trackManager!!.queue.size >= 2) "tracks" else "track"
                        tempQueue.removeAt(0)
                        for (t in tempQueue) {
                            untilPlayed += t.duration
                        }
                        untilPlayed += guildManager.player.playingTrack.duration - guildManager.player.playingTrack.position
                        val trackEmbed = embed ?: EmbedBuilder()
                            .setTitle("Added Track")
                            .addField("Track", "[${track.info.title}](${track.info.uri})", true)
                            .addField("Channel", track.info.author, true)
                            .setThumbnail(picture)
                            .addField("Duration", live, true)
                            .addField(
                                "Estimated Time Until Playing",
                                "${guildManager.trackManager!!.queue.size} ${trackOrTracks}, and ${
                                    Util.Time.getDurationBreakdown(untilPlayed)
                                        ?.replace("/0 (?: Second | Minute |Hour|Day)s/".toRegex(), "")
                                } until played",
                                false
                            )
                            .setColor(FindServerSetting(channel.guild.id).color)
                            .setFooter("Requested By: ${requester.asTag}", requester.effectiveAvatarUrl).build()
                        return channel.sendMessage(trackEmbed).queue()
                    }
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    var firstTrack = playlist.selectedTrack
                    if (firstTrack == null) {
                        firstTrack = playlist.tracks.removeAt(0)
                    }
                    firstTrack.userData = requester
                    val count = playlist.tracks.size.coerceAtMost(10)
                    val tracks: List<AudioTrack> = ArrayList(playlist.tracks)
                    val picture = "https://i.ytimg.com/vi/${firstTrack.identifier}/maxresdefault.jpg"
                    val playlistEmbed = EmbedBuilder()
                        .setTitle(firstTrack.info.title, firstTrack.info.uri)
                        .setDescription("Showing first **$count** Tracks.\n")
                        .setThumbnail(picture)
                        .setColor(Color.decode("#7289da"))
                        .setFooter("Requested By ${requester.asTag}", requester.effectiveAvatarUrl)
                    for (i in tracks) {
                        i.userData = requester
                    }
                    for (i in 0 until count) {
                        val track = tracks[i]
                        val info = track.info
                        playlistEmbed.appendDescription(
                            String.format(
                                "%s `%s`\n",
                                "`#${i + 1}` - [${info.title}](${info.uri})",
                                time.formatMili(info.length)
                            )
                        )
                    }
                    message?.editMessage(playlistEmbed.build())?.queue()
                    play(guildManager, firstTrack, channel)
                    playlist.tracks.forEach(Consumer { track: AudioTrack ->
                        guildManager.trackManager!!.queue(track, channel)
                    })
                }

                override fun noMatches() {
                    message?.editMessage("Nothing found perhaps this url is hidden $url")?.queue()
                }

                override fun loadFailed(exception: FriendlyException?) {
                    message?.editMessage("Couldn't play the track. (`${exception!!.message}`)")?.queue()
                }
            })
        }

    }
    fun play(guildManager: GuildManager, track: AudioTrack, channel: TextChannel) {
        guildManager.trackManager!!.queue(track, channel)
    }
    companion object {
        private var PLAYERMANAGER: PlayerManager? = null

        @JvmStatic
        @get:Synchronized
        val instance: PlayerManager?
            get() {
                if (PLAYERMANAGER == null) {
                    PLAYERMANAGER = PlayerManager()

                }
                return PLAYERMANAGER
            }
    }
    init {
        guildManagers = HashMap()
        PlayerManager = DefaultAudioPlayerManager()
        AudioSourceManagers.registerRemoteSources(PlayerManager)
        AudioSourceManagers.registerLocalSource(PlayerManager)
        PlayerManager.registerSourceManager(TwitchStreamAudioSourceManager())

    }
}