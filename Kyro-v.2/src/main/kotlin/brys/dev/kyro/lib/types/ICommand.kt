package brys.dev.kyro.lib.types



import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel

/**
 * Implement this for making your own commands
 */
interface ICommand {
    fun execute(event: Message, channel: MessageChannel, args: Array<String>)
    fun executeSlash(interaction: SlashCommandEvent)
    fun setDescripton(): String
}