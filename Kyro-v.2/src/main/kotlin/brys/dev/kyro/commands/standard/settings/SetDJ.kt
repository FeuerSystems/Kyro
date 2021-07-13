package brys.dev.kyro.commands.standard.settings

import brys.dev.kyro.lib.classes.db.AddServerSetting
import brys.dev.kyro.lib.classes.db.FindServerSetting
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
class SetDJ: ICommand {
    @AddCommand(["setdj","dj"], permission = Permission.MANAGE_SERVER)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        val color = FindServerSetting(event.guild.id).color
        if (args.isEmpty() && event.mentionedRoles.isEmpty()) return event.reply("A valid role is required. (Either mention the role, or the name of the role.)").mentionRepliedUser(false).queue()
        val role = if(event.mentionedRoles.isEmpty()) event.guild.getRolesByName(args[0], true)[0] else event.mentionedRoles[0]
        return event.reply(EmbedBuilder().setAuthor("Adding DJ Role", null, "https://i.brys.tk/FZ0o.gif").setColor(color).build()).mentionRepliedUser(false)
            .queue { m ->
                AddServerSetting(event.guild.id, role.id).DJ()
                m.editMessage(EmbedBuilder().setAuthor("DJ Role").setDescription("DJ Role is now **${role.name}**").setColor(color).build()).mentionRepliedUser(false).queue()
            }
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}