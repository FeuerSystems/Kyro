package brys.dev.kyro.commands.standard.settings

import brys.dev.kyro.lib.classes.db.AddServerSetting
import brys.dev.kyro.lib.classes.db.FindServerSetting
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.structures.Util
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
class DJOnly: ICommand {
    @AddCommand(["djonly","adminonly"], permission = Permission.MANAGE_SERVER)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        val color = FindServerSetting(event.guild.id).color
        if (args.isEmpty() || Util.Strings.stringAsBoolean(args[0]) == null) return event.reply("A boolean argument is required (true/false)").mentionRepliedUser(false).queue()
        val boolean = Util.Strings.stringAsBoolean(args[0])
        return event.reply(EmbedBuilder().setAuthor("Setting DJ Only", null, "https://i.brys.tk/FZ0o.gif").setColor(color).build()).mentionRepliedUser(false)
            .queue { m ->
                AddServerSetting(event.guild.id, boolean.toString()).djOnly()
                m.editMessage(EmbedBuilder().setAuthor("DJ Role").setDescription("DJ Only is set to: ${boolean.toString()}").setColor(color).build()).mentionRepliedUser(false).queue()
            }
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}