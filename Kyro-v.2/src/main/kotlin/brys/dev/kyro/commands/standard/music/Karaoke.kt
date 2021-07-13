package brys.dev.kyro.commands.standard.music

import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.model.KaraokeUser
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit


class Karaoke(private val waiter: EventWaiter): ICommand {
    var list: ArrayList<Member> = ArrayList()
    var players: ArrayList<KaraokeUser> = ArrayList()


    @AddCommand(["k","karaoke"], vc = true, owner = true)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        event.reply("Alright first lets set some things up!\n What people do you want to play (Mention these people)")
            .mentionRepliedUser(false).queue {
                val memberWait = waiter.waitForEvent(
                    GuildMessageReceivedEvent::class.java,
                    { e: GuildMessageReceivedEvent ->
                        e.author == event.author
                                && e.channel == event.channel
                                && e.message != event
                    },
                    { e: GuildMessageReceivedEvent ->
                        if (e.message.mentionedMembers.isEmpty()) return@waitForEvent event.reply("No members were mentioned. Try this command again mentioning these members!")
                            .mentionRepliedUser(false).queue()
                        list.add(event.member!!)
                        list.addAll(e.message.mentionedMembers)
                        val members = StringBuilder("The following members are planning on doing Karaoke: ")
                        for (i in list) {
                            members.append("\n`${i.effectiveName}`")
                        }
                        event.reply(members).mentionRepliedUser(false).queue()
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