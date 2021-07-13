package brys.dev.kyro.lib.interfaces

import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

 data class KyroMessage(var message: GuildMessageReceivedEvent?, var interaction: SlashCommandEvent?) {
     companion object {
       var message: GuildMessageReceivedEvent? = null
        var interaction: SlashCommandEvent? = null
       fun init() {
           KyroMessage(message, interaction)
       }
    }
 }