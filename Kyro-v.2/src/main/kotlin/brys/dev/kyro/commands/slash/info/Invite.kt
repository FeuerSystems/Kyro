package brys.dev.kyro.commands.slash.info
import brys.dev.kyro.lib.classes.KyroSlashEvent
import brys.dev.kyro.lib.classes.SlashOption
import brys.dev.kyro.lib.modules.command.AddSlashCommand
import brys.dev.kyro.lib.types.ISlashCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.commands.OptionType

class Invite: ISlashCommand {
    @AddSlashCommand("invite")
    override fun message(event: KyroSlashEvent) {
        val invite = EmbedBuilder()
            .setTitle("${event.jda.selfUser.name} Invites")
            .addField("Based On","[Kyro](https://git.brys.tk/Brys/Kyro-v.2)", false)
            .addField("Invite", "[Invite ${event.jda.selfUser.name}!](${event.jda.getInviteUrl()})", false)
        event.replyEmbeds(invite.build()).setEphemeral(false).queue()
    }
}
