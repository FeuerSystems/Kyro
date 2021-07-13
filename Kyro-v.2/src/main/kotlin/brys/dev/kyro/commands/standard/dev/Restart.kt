package brys.dev.kyro.commands.standard.dev
import brys.dev.kyro.lib.model.PlayerManager
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.structures.Player
import brys.dev.kyro.lib.structures.Util
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import java.awt.Color
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents

class Restart(val wsEvents: WSEvents): ICommand {
    @AddCommand(["restart"], owner = true, owners = true)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        if (args.isEmpty()) {
            var message: Message? = null
            val channels = mutableListOf<VoiceChannel>()
            val managers = mutableListOf<String>()
            val c = event.channel.id
            event.reply(
                EmbedBuilder().setAuthor("Restarting...", null, "https://i.brys.tk/FZ0o.gif")
                    .setColor(Color.decode("#f2816b")).build()
            ).mentionRepliedUser(false).queue { m ->
                message = m
            }
                for (i in 0 until event.jda.audioManagerCache.asList().size) {
                    val manager = event.jda.audioManagerCache.asList()[i]
                    channels.add(manager.connectedChannel!!)
                    val guild = channels[i].guild
                    val player = PlayerManager.instance?.getGuildManager(guild, event, event.author)
                    if (player != null) {
                        managers.add(player.player.playingTrack.info.uri)
                    }
                }
                event.jda.presence.setPresence(OnlineStatus.ONLINE, Activity.listening("Restarting..."))
                event.jda.shutdown()
                val bot = Util.Bot.makeBot()
                bot.awaitReady().getGuildById(message!!.guild.id)!!
                    .getTextChannelById(message!!.textChannel.id)!!
                    .editMessageById(message!!.id, EmbedBuilder().setAuthor("Restarted!").setColor(Color.decode("#94f26b")).build()).queue()
                for (i in 0 until channels.size) {
                    val manager = channels[i]
                    val guild = channels[i].guild
                    val player = PlayerManager.instance?.getGuildManager(guild, event, event.author)
                    val uri = managers[i]
                    bot.awaitReady().getGuildById(guild.id)!!.audioManager.openAudioConnection(manager)
                    if (player != null) {
                        bot.getGuildById(guild.id)?.defaultChannel?.sendMessage("Queueing back up tracks...")?.queue {
                           m -> Player.queue(bot.getGuildById(guild.id)?.defaultChannel!!, bot.selfUser, false, m, null, listOf(uri), wsEvents)
                            player.trackManager?.removeTrack(player.trackManager!!.queue.size)
                        }
                    }
                }
                bot.awaitReady().presence.activity = Activity.streaming("${bot.guildCache.size()} servers | ?play", "https://twitch.tv/monstercat")
            }
        }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}