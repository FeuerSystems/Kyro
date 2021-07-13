package brys.dev.kyro.commands.standard.settings


import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import brys.dev.kyro.lib.classes.db.AddServerSetting
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import java.awt.Color
import java.lang.NumberFormatException
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
class SetColor: ICommand {
    @AddCommand(["setColor","SetColor","color"], permission = Permission.MANAGE_SERVER)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        if (args.isEmpty()) {
            return event.reply("You must have a color to set the bot with.").mentionRepliedUser(false).queue()
        }
       try {
           Color.decode(args[0])
       } catch (e: NumberFormatException) {
          return event.reply("The provided color was invalid. Try again with a valid hex.").mentionRepliedUser(false).queue()
       }
        AddServerSetting(event.guild.id, args[0]).color()
        val color = EmbedBuilder().setAuthor("Color was set to ${args[0]}").setColor(Color.decode(args[0]))
        return event.reply(color.build()).mentionRepliedUser(false).queue()
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        return "Set the color for the bots embeds"
    }
}