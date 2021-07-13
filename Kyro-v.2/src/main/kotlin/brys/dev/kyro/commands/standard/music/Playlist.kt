package brys.dev.kyro.commands.standard.music

import brys.dev.kyro.lib.classes.db.AddUserData
import brys.dev.kyro.lib.model.DataPlaylist
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.structures.MongoDatabase
import brys.dev.kyro.lib.structures.Util
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.classes.db.FindServerSetting
import brys.dev.kyro.lib.classes.db.FindUserData
import brys.dev.kyro.lib.model.Page
import brys.dev.kyro.lib.structures.Pages
import brys.dev.kyro.lib.types.PageType
import com.google.gson.Gson
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URL
import java.time.Instant
import java.util.concurrent.TimeUnit

class Playlist: ICommand {
    val db = MongoDatabase.userDB()
    val gson = Gson()
    private val database = MongoDatabase.userDB()
    @AddCommand(["playlist", "pp", "playlists"])
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        if (args.isEmpty()) {
            val user = FindUserData(event.author.id).collection
            val playlist = user?.get("playlists")
            val array = playlist as ArrayList<*>
            val playlists = mutableListOf<DataPlaylist>()
            if (array.isEmpty()) {
                return event.reply("No playlists were found").mentionRepliedUser(false).queue()
            }
            for (i in 0 until array.size) {
                val arr = array[i]
                val json = gson.toJson(arr)
                val data = gson.fromJson(json, DataPlaylist::class.java)
                playlists.add(data)
            }
            val chunks = playlists.chunked(5)
            val onePage = EmbedBuilder()
                .setTitle(event.author.asTag)
                .setDescription("`Name` **->** __songs__ total")
            if (chunks.size == 1) {
                val dataBuilder = StringBuilder()
                for (i in playlists) {
                    dataBuilder.append("`${i.name}` **->** __${i.songs.size}__ total\n")
                }
                onePage.addField("Playlists", dataBuilder.toString(), false)
                return event.reply(onePage.build()).mentionRepliedUser(false).queue()
            }
            if (chunks.size >= 2) {
                val dataBuilder = StringBuilder()
                val pages = ArrayList<Page>()
                val multiPage = EmbedBuilder()
                for (chunked in chunks.indices) {
                    val chunk = chunks[chunked]
                    multiPage.fields.clear()
                    dataBuilder.setLength(0)
                    for (i in chunk.indices) {
                        val playlistS = chunk[i]
                        dataBuilder.append("`${playlistS.name}` **->** __${playlistS.songs.size}__ total\n")
                    }
                    multiPage.setTitle(event.author.asTag)
                    multiPage.setDescription("`Name` **->** __songs__ total")
                    multiPage.addField("Playlists", dataBuilder.toString(), false)
                    pages.add(Page(PageType.EMBED, multiPage.build()))
                }
                return event.channel.sendMessage(pages.firstOrNull()?.content as MessageEmbed)
                    .queue { success -> Pages.paginate(success, pages, 300, TimeUnit.SECONDS, event.author.id);  }
            }
        }
        if (args.isNotEmpty() && args[0].toLowerCase() == "delete") {
            if(args.size == 1) {
                return event.reply("You need to enter a vaild playlist name").mentionRepliedUser(false).queue()
            }
            val user = FindUserData(event.author.id).collection
            val playlist = user?.get("playlists")
            val array = playlist as ArrayList<*>
            if(args[1] == "all" || args[1] == "everything") {
                val filter = Filters.eq("user", event.author.id)
                val update = Updates.set("playlists", mutableListOf<DataPlaylist>())
                database.updateOne(filter, update)
                return event.reply("All playlists were cleared!").mentionRepliedUser(false).queue()
            }
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
            val list = mutableListOf<DataPlaylist>()
            val filter = Filters.eq("user", event.author.id)
            for (i in array.indices) {
                val arr = array[i]
                val json = gson.toJson(arr)
                val data = gson.fromJson(json, DataPlaylist::class.java)
                list.add(data)
            }
            val arr = array[location]
            val json = gson.toJson(arr)
            val data = gson.fromJson(json, DataPlaylist::class.java)
            list.remove(data)
            val update = Updates.set("playlists", list)
            database.updateOne(filter, update)
            return event.reply("Playlist ${data.name} was deleted!").mentionRepliedUser(false).queue()
        }
        if (args.isNotEmpty() && args[0].toLowerCase() == "view") {
            if(args.size == 1) {
                return event.reply("You need to enter a vaild playlist name").mentionRepliedUser(false).queue()
            }
            val user = FindUserData(event.author.id).collection
            val playlist = user?.get("playlists")
            val array = playlist as ArrayList<*>
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
        val arr = array[location]
        val json = gson.toJson(arr)
        val data = gson.fromJson(json, DataPlaylist::class.java)
        if (data.name == args[1]) {
            val songs = data.songs
            val thumbnail = data.thumbnail ?: event.jda.selfUser.effectiveAvatarUrl
            val tracks = mutableListOf<String>()
            val playlistEmbed = EmbedBuilder()
            for (s in 0 until songs.size) {
                val song = songs[s]
                tracks.add("`[${s + 1}]` - [${song.name}](${song.url})\n")
            }
            val chunks = tracks.chunked(5)
            val pages = ArrayList<Page>()
            if (chunks.size <= 1) {
                playlistEmbed.setTitle(event.author.asTag)
                    .setThumbnail(thumbnail)
                    .setColor(FindServerSetting(event.guild.id).color)
                    .setDescription("Your playlist '**${data.name}**'")
                    .setTimestamp(Instant.now())
                playlistEmbed.addField("Tracks (${songs.size} total) ", tracks.joinToString(""), false)
                return event.reply(playlistEmbed.build()).mentionRepliedUser(false).queue()
            }
            for (chunked in chunks.indices) {
                val chunk = chunks[chunked]
                val str = if (chunk.joinToString("").isEmpty()) "None" else chunk.joinToString("")
                playlistEmbed.fields.clear()
                playlistEmbed.setTitle(event.author.asTag)
                    .setThumbnail(thumbnail)
                    .setColor(FindServerSetting(event.guild.id).color)
                    .setDescription("Your playlist '**${data.name}**'")
                    .setTimestamp(Instant.now())
                playlistEmbed.addField("Tracks (${songs.size} total)", str, false).setFooter("Page ${chunked + 1}")
                pages.add(Page(PageType.EMBED, playlistEmbed.build()))
            }
            return event.channel.sendMessage(pages.firstOrNull()?.content as MessageEmbed)
                .queue { success -> Pages.paginate(success, pages, 300, TimeUnit.SECONDS, event.author.id) }
        }
    }
    if (args.isNotEmpty() && args[0].toLowerCase() == "create")
    {
        if (args.size == 1) {
            return event.reply("You need to have an argument for this playlist's name").mentionRepliedUser(false)
                .queue()
        }
        if (Util.UserData.sameNamePlaylist(
                event.author.id,
                args[1]
            )
        ) return event.reply("You can't have a playlist with the same name!").mentionRepliedUser(false).queue()
        if (Util.UserData.playlistSize(event.author.id, args[1]) == 25) return event.reply("You can't have more than 25 playlists for your account.").mentionRepliedUser(false).queue()
        if (args.size <= 2) {
            AddUserData(event.author.id, DataPlaylist(args[1], mutableListOf(), null)).playlist()
            return event.reply("Okay your playlist **${args[1]}** was created!").mentionRepliedUser(false).queue()
        }
        try {
            val conn = URL(args[2]).openConnection()
            if (!conn.getHeaderField("Content-Type").contains("image/")) {
                return event.reply("Seems like the URL you sent isn't a valid image URL!").mentionRepliedUser(false)
                    .queue()
            }

        } catch (e: Exception) {
            return event.reply("Make sure the Thumbnail URL of this playlist is vaild!").mentionRepliedUser(false)
                .queue()
        }
        AddUserData(event.author.id, DataPlaylist(args[1], mutableListOf(), args[2])).playlist()
        return event.reply("Okay your playlist **${args[1]}** was created!").mentionRepliedUser(false).queue()
    }
}


    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}


