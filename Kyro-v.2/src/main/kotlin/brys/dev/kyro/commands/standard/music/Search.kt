package brys.dev.kyro.commands.standard.music

import brys.dev.kyro.lib.classes.api.YouTube
import brys.dev.kyro.lib.classes.db.FindServerSetting
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.structures.config.Config
import brys.dev.kyro.lib.structures.Player
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException
import java.lang.UnsupportedOperationException
import java.util.concurrent.TimeUnit
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents
import com.jagrosh.jdautilities.commons.waiter.EventWaiter

class Search(private val waiter: EventWaiter, val wsEvents: WSEvents): ICommand {
    private val baseUrl = "https://www.youtube.com/watch?v="
    @AddCommand(["search","se"], vc = true, dj = true)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        if (args.isEmpty()) {
            return event.reply("Search term required.").mentionRepliedUser(false).queue()
        }
        val audioManager = event.guild.audioManager
        val results = YouTube(Config.music.YTToken).rawQuery(args[0], 10)
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
        val searchEm = EmbedBuilder().setDescription("Search Results for **${args[0]}**\n──────────────────────────").setColor(FindServerSetting(event.guild.id).color)
        for (i in 0 until results?.size!!) {
            val result = results[i]
            searchEm.appendDescription("\n`[${i + 1}]` **-** [${result.snippet.title}]($baseUrl${result.id.videoId})")
        }
        searchEm.setThumbnail("https://i.ytimg.com/vi/${results[0].id.videoId}/maxresdefault.jpg")
            .setFooter("Type an number in chat to play the track in the search list. (Timeout 1 min)")
        event.reply(searchEm.build()).mentionRepliedUser(false).queue { m ->
            waiter.waitForEvent(
                GuildMessageReceivedEvent::class.java,
                { e: GuildMessageReceivedEvent ->
                    e.author == event.author
                            && e.channel == event.channel
                            && e.message != event
                },
                { e: GuildMessageReceivedEvent ->
                    try {
                        e.message.contentRaw.toInt()
                        if (e.message.contentRaw.toInt() > 10) {
                            event.reply("Number must be less than 10.").mentionRepliedUser(false).queue()
                            return@waitForEvent
                        }
                    } catch (e: NumberFormatException) {
                        event.reply("Must contain a number.").mentionRepliedUser(false).queue()
                        return@waitForEvent
                    };
                    m.delete().queue()
                    Player.queue(
                        event.textChannel,
                        e.author,
                        false,
                        event
                    , null,
                        listOf(baseUrl + results[e.message.contentRaw.toInt() - 1].id.videoId), wsEvents)
                },
                1,
                TimeUnit.MINUTES,
                { event.reply("You took too long, command canceled.").mentionRepliedUser(false).queue() })
        }
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }

}