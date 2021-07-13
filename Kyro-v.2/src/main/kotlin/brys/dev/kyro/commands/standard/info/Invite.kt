package brys.dev.kyro.commands.standard.info

import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
class Invite: ICommand {
    @AddCommand(["inv","invite"])
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
       val invite = EmbedBuilder()
           .setTitle("${event.jda.selfUser.name} Invites")
           .addField("Based On","[Kyro](https://git.brys.tk/Brys/Kyro-v.2)", false)
           .addField("Invite", "[Invite ${event.jda.selfUser.name}!](${event.jda.getInviteUrl()})", false)
        event.reply(invite.build()).mentionRepliedUser(false).queue()
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}