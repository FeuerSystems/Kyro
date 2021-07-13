package brys.dev.kyro.commands.standard.music

import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import brys.dev.kyro.lib.structures.config.Config
import brys.dev.kyro.lib.classes.db.FindServerSetting
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import brys.dev.kyro.lib.structures.Util
import brys.dev.kyro.lib.classes.api.YouTube
import brys.dev.kyro.lib.structures.Player
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.lang.UnsupportedOperationException
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.classes.db.FindUserData
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents
import brys.dev.kyro.lib.classes.spotify.SpotifySong
import brys.dev.kyro.lib.model.DataPlaylist
import brys.dev.kyro.lib.model.DataSong
import brys.dev.kyro.lib.model.PlayerManager
import brys.dev.kyro.lib.structures.Util.Misc.round
import com.google.gson.Gson
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.MessageEmbed
import java.net.URL
import kotlin.time.ExperimentalTime


class Play(val wsEvents: WSEvents): ICommand {
    private val spotify = "^(?:https:\\/\\/open\\.spotify\\.com|spotify)([\\/:])user\\1([^\\/]+)\\1playlist\\1([a-z0-9]+)".toRegex()
    private val playlist = "/(open.spotify.com)\\/(?:track)\\/.{0,9}[0-9A-Za-z]\\w+/ig".toRegex()
    private val track = "/(open.spotify.com)\\/(?:playlist)\\/.{0,9}[0-9A-Za-z]\\w+/ig"
    private val gson = Gson()

    @ExperimentalTime
    @AddCommand(aliases = ["play", "p"], vc = true, dj = true)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        if (event.member!!.voiceState!!.isSelfDeafened || event.member!!.voiceState!!.isGuildDeafened) {
            return event.reply("You can't queue songs when deafened, who'd be listening?").mentionRepliedUser(false)
                .queue()
        }
        val sb = StringBuilder()
        for (element in args) {
            sb.append("$element ")
        }
        val audioManager = event.guild.audioManager
        if (!audioManager.isConnected) {
            try {
                audioManager.openAudioConnection(event.member!!.voiceState!!.channel)
                audioManager.isSelfDeafened = true
            } catch (audioException: Exception) {
                when (audioException) {
                    IllegalArgumentException() -> event.reply("Looks like something broke when I tried to connect to your channel.")
                        .mentionRepliedUser(false).queue()
                    UnsupportedOperationException() -> event.reply("Something internally broke when trying to connect to your channel.")
                        .mentionRepliedUser(false).queue()
                    InsufficientPermissionException(event.guild, Permission.VOICE_CONNECT) -> {
                        event.reply("Please give me permissions to connect.").mentionRepliedUser(false).queue()
                    }
                }
            }
        }
        if (args.isNotEmpty() && args[0] == "--file") {
            val file = event.attachments[0].proxyUrl
            return event.reply(EmbedBuilder().setAuthor("Loading: $file", null, "https://i.brys.tk/FZ0o.gif").build())
                .queue { m ->
                    Player.queue(event.textChannel, event.author, false, m, null, listOf(file), wsEvents)
                }
        }
        if (args.isNotEmpty() && args[0] == "--playlist") {
            val name = args[1] ?: event.reply("You need name for the playlist on your account you want to play!").mentionRepliedUser(false).queue()
            val user = FindUserData(event.author.id).collection
            val playlist = user?.get("playlists")
            val array = playlist as ArrayList<*>
            val playlistEmbed = EmbedBuilder()
                .setTitle("Playlist Queued")
            val songs = StringBuilder()
            val urls = mutableListOf<String>()
            var embedField: MessageEmbed.Field? = null
            var location = -1
            for (i in array.indices) {
                val arr = array[i]
                val json = gson.toJson(arr)
                val data = gson.fromJson(json, DataPlaylist::class.java)
                if (data.name == args[1]) {
                    if (location == -1) {
                        location = i
                    }
                }
            }
            if (location == -1) {
                return event.reply("No playlist with that name was found.").mentionRepliedUser(false).queue()
            }
            val json = gson.toJson(location)
            val data = gson.fromJson(json, DataPlaylist::class.java)
                playlistEmbed.setDescription("Your playlist '**${data.name}**'")
                embedField = MessageEmbed.Field("Now Playing", "[${data.songs.first().name}](${data.songs.first().url})", true)
                for (s in data.songs) {
                    urls.add(s.url)
                }
                val value = mutableListOf<DataSong>()
                value.addAll(data.songs)
                for (s in 0 until value.size) {
                    val song = value[s]
                    songs.append("[**${s+1}**] - [${song.name}](${song.url})\n")
                }
            return Player.queue(event.textChannel, event.author, false, null, playlistEmbed.addField("Tracks", songs.toString(), true).addField(embedField!!).build(), urls, wsEvents)
        }
        if (args.isEmpty()) {
            return event.reply("You need to have a search term in order to use this command. (`${FindServerSetting(event.guild.id).prefix}play no friends - cadmium`)")
                .mentionRepliedUser(false).queue()
        }
        if (Util.Misc.isUrl(args[0])) {
            val url = java.lang.String.join(" ", args[0])
                if (args[0].contains("https://open.spotify.com/track")) {
                    val sublist = args[0].split("/")
                    val trackTitle = sublist[4].split("?si")
                    val req = URL("https://sn.brys.tk/track?id=${trackTitle[0]}").readText()
                    val song = SpotifySong.getSong(req)
                    val s = YouTube(Config.music.YTToken).search(song.full_track + " song", 1)?.get(0)!!
                    val playingOrAdded = if (PlayerManager.instance!!.getGuildManager(event.guild, event, event.author).player.playingTrack is AudioTrack) "♪ Now Playing" else "♪ Added Spotify Track"
                    val songEmbed =   EmbedBuilder()
                        .setAuthor("♪ Now Playing")
                        .setTitle("${song.name} by ${song.artist}",  song.url)
                        .addField("Artist", "__${song.artist}__", true)
                        .addField("Duration", "`${Util.Time.formatMili(song.features.duration)}`", true)
                        .addField("Features", "**Energy** ${song.features.energy.round(2).toString().replace("0.","")}\n **Acousticness** ${song.features.acoustic.round(2).toString().replace("0.","")}%\n **Danceibility** ${song.features.dance.round(2).toString().replace("0.","")}", true)
                        .setThumbnail(song.artwork)
                        .setColor(FindServerSetting(event.guild.id).color)
                        .setFooter("Requested by ${event.author.asTag}")
                        .build()
                    return Player.queue(
                        event.textChannel,
                        event.author,
                        false,
                        null,
                        songEmbed, listOf(s), wsEvents)
            }
            return event.reply(
                EmbedBuilder().setAuthor("Loading: Track", url, "https://i.brys.tk/FZ0o.gif").build())
                .queue { m ->
                    Player.queue(event.textChannel, event.author, false, m, null, listOf(url), wsEvents)
                    m.delete().queue()
                }
        }
        val s = YouTube(Config.music.YTToken).search(sb.toString(), 1)?.get(0)

                if (s == null) {
                    event.reply("No results were found!").mentionRepliedUser(false).queue()
                }
                if (s != null) {
                    Player.queue(event.textChannel, event.author, false, event, null, listOf(s), wsEvents)
                }

    }
    override fun executeSlash(interaction: SlashCommandEvent) {
    }

    override fun setDescripton(): String {
        return "Plays a song with a url or search query."
    }
}