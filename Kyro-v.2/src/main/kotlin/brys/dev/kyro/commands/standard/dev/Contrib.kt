package brys.dev.kyro.commands.standard.dev

import brys.dev.kyro.lib.classes.db.AddBotData
import brys.dev.kyro.lib.classes.db.FindBotData
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import java.lang.StringBuilder
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
class Contrib: ICommand {
    @AddCommand(["contrib","contributor"], owner = true)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        if (args[1] == null || args[0] == null) {
            return event.reply("You need to have the name of the contributor and need to have a action (add/remove)").mentionRepliedUser(false).queue()
        }
            if (args[0] == "add") {
                val isPrevious = if (FindBotData().contributors == null) "" else FindBotData().contributors
                    AddBotData("$isPrevious${args[1]}|").contributors()
                return event.reply("Contributor added ${args[1]}").mentionRepliedUser(false).queue()
            } else if (args[0] == "remove") {
                val contrib = FindBotData().contributors.split("|").toMutableList()
                val index = contrib.indexOf(args[1])
                val contributors = StringBuilder()
                if (index == -1) {
                    return event.reply("You need to have a valid name that exists in the contributor list.").mentionRepliedUser(false).queue()
                }
                contrib.removeAt(index)
                for (i in contrib) {
                   contributors.append("$i|")
                }
                AddBotData(contributors.toString()).contributors()
                return event.reply("**${args[1]}** was removed from the contributors").mentionRepliedUser(false).queue()
            }

    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }

    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}