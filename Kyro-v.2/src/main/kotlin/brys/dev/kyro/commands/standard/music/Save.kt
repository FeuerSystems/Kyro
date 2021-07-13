package brys.dev.kyro.commands.standard.music

import brys.dev.kyro.lib.classes.db.FindUserData
import brys.dev.kyro.lib.model.DataPlaylist
import brys.dev.kyro.lib.model.DataSong
import brys.dev.kyro.lib.model.PlayerManager
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.classes.db.AddUserData
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents
import com.google.gson.Gson

class Save(val wsEvents: WSEvents): ICommand {
    private val gson = Gson()
    @AddCommand(["save","ss"])
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        if(args.isEmpty()) { if (!event.member?.voiceState!!.inVoiceChannel() || !event.guild.selfMember.hasPermission(Permission.VOICE_CONNECT) || !event.guild.selfMember.hasPermission(Permission.VOICE_SPEAK)) return event.reply("You are not in a channel or I cannot see or speak in it.").mentionRepliedUser(false).queue() }
        if(args.isNotEmpty()) {
            val track = PlayerManager.instance!!.getGuildManager(event.guild, event, event.author).player.playingTrack
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
            list.add(DataSong(track.info.title, track.info.uri))
            val newPlaylist = DataPlaylist(iPlaylist.name, list, iPlaylist.thumbnail)
            val finalStruct = mutableListOf<DataPlaylist>()
            old.remove(iPlaylist)
            finalStruct.addAll(old)
            finalStruct.add(newPlaylist)
            AddUserData(event.author.id, finalStruct).addSetting("playlists")
            return event.reply("Alright, Track **${track.info.title}** was added to __${args[0]}__. To check the rest of the tracks in this playlist, just use the `playlist view ${args[0]}`!").mentionRepliedUser(false).queue()
            }
        }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}