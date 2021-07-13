package brys.dev.kyro.commands.standard.music

import brys.dev.kyro.lib.structures.Util.Time.formatMili
import brys.dev.kyro.lib.classes.db.FindServerSetting
import brys.dev.kyro.lib.model.Page
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import brys.dev.kyro.lib.model.PlayerManager.Companion.instance
import brys.dev.kyro.lib.structures.Pages
import brys.dev.kyro.lib.types.PageType
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import java.util.concurrent.TimeUnit
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.classes.db.AddUserData
import brys.dev.kyro.lib.classes.db.FindUserData
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents
import brys.dev.kyro.lib.model.DataPlaylist
import brys.dev.kyro.lib.model.DataSong
import com.google.gson.Gson

class Queue(val wsEvents: WSEvents): ICommand {
    private val gson = Gson()
    @AddCommand(["queue","q"])
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        val manager = instance!!.getGuildManager(event.guild, event, event.author)
        val queue = manager.trackManager?.queue
        val player = manager.player
        val s = manager.trackManager
        if (queue.isNullOrEmpty() && player.playingTrack == null) {
            return event.reply("You need to have a queue and have a track playing.").mentionRepliedUser(false).queue()
        }
        if (args.size == 2) {
            val user = FindUserData(event.author.id).collection
            val db = FindUserData(event.author.id).db
            val playlist = user?.get("playlists")
            val array = playlist as ArrayList<*>
            var location = -1
            for (i in array.indices) {
                val arr = array[i]
                val json = gson.toJson(arr)
                val data = gson.fromJson(json, DataPlaylist::class.java)
                if (data.name == args[0]) {
                    if (location == -1) {
                        location = i
                    }
                }
            }
            if (location == -1) {
                return event.reply("You can't add a song to a playlist that doesn't exist!").mentionRepliedUser(false).queue()
            }
            val arr = array[location]
            val json = gson.toJson(arr)
            val iPlaylist = Gson().fromJson(json, DataPlaylist::class.java)
            val songs = iPlaylist.songs
            val list = mutableListOf<DataSong>()
            val old = mutableListOf<DataPlaylist>()
            for (o in 0 until array.size) {
                val oldPlaylists = array[o]
                val json = gson.toJson(oldPlaylists)
                val data = gson.fromJson(json, DataPlaylist::class.java)
                old.add(data)
            }
            list.addAll(songs)
            if (args[1] == "save" || args[1] == "s") {
                for (i in manager.trackManager?.queue!!) {
                    list.add(DataSong(i.info.title, i.info.uri))
                }
                val newPlaylist = DataPlaylist(iPlaylist.name, list, iPlaylist.thumbnail)
                val finalStruct = mutableListOf<DataPlaylist>()
                old.remove(iPlaylist)
                finalStruct.addAll(old)
                finalStruct.add(newPlaylist)
                AddUserData(event.author.id, finalStruct).addSetting("playlists")
                return event.reply("Alright, your **queue** was added to __${args[0]}__. To check the rest of the tracks in this playlist, just use the `playlist view ${args[0]}`!").mentionRepliedUser(false).queue()
            }
        }
        val meta = player.playingTrack.userData as User
        fun isRepeating(): String? {
            if (s!!.trackRepeating()) {
                return "(<:repeat:777273568387530772>) -"
            } else if (!s.trackRepeating()) {
                return ""
            }
            return null
        }
        val trackCount = queue!!.chunked(5)
        val tracks: List<AudioTrack> = ArrayList(queue)
        val infonp = player.playingTrack?.info
        val thumbnail = "https://img.youtube.com/vi/${infonp!!.identifier}/maxresdefault.jpg"
        val itrack = "${isRepeating()}["+infonp.title+"]"+"("+infonp.uri +")" + if (infonp.isStream) " - :red_circle: LIVE\n" else " `" + player.playingTrack?.position?.let { player.playingTrack?.duration?.minus(it) }?.let { formatMili(it) } + "`"
        val queueEmbed = EmbedBuilder()
        val queueBuilder = StringBuilder()
        queueEmbed.setTitle("${event.guild.name} Queue (${queue.size.plus(1)})")
            .setThumbnail(thumbnail)
            .addField("Now Playing", "$itrack - (Requested By: **${meta.asTag}**)", false)
            .setColor(FindServerSetting(event.guild.id).color)
        if (queue.size == 0) {
            queueBuilder.append("Empty")
        }
        val pages = ArrayList<Page>()
        var integer = 0
        for (i in trackCount.indices) {
            val groupTracks = trackCount[i]
            for (o in groupTracks.indices) {
                val track = groupTracks[o]
                val metaData = track.userData as User
                integer = groupTracks.indexOf(track)
                queueBuilder.append(String.format("%s `%s` \n(Requested By: **${metaData.asTag}**)\n",
                    "`#${integer}` - [${track.info.title}](${track.info.uri})",
                    formatMili(track.info.length),))
            }
            pages.add(Page(PageType.EMBED, queueEmbed.addField("Queue", queueBuilder.toString(), false).build()))
            queueBuilder.setLength(0)
        }

        if (pages.isEmpty()) return event.reply(queueEmbed.build()).mentionRepliedUser(false).queue()
        event.channel.sendMessage(pages[0].content as MessageEmbed).queue { success -> Pages.paginate(success, pages, 300, TimeUnit.SECONDS, event.author.id) }
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        return "Gives you the current server queue."
    }
}