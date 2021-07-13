package brys.dev.kyro.lib.interfaces

import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.requests.restaction.MessageAction
import javax.annotation.Nonnull

interface KyroMessages: Message {
    val message: Message?
    val interaction: SlashCommandEvent?
    override fun reply(@Nonnull content: CharSequence): MessageAction {
        if (message == null && interaction == interaction!!) {
            return channel.sendMessage(content)
        }
        return channel.sendMessage(content).reference(message!!)
    }
}