package brys.dev.kyro.commands.standard.music

import brys.dev.kyro.lib.model.PlayerManager
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents

class Leave(val wsEvents: WSEvents): ICommand {
    @AddCommand(["leave","l","fuckoff"], vc = true, dj = true)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        val audioManager = event.guild.audioManager
        val guildManager = PlayerManager.instance!!.getGuildManager(event.guild, event, event.author)
        if (!audioManager.isConnected) {
            return event.reply("No. :joy:").mentionRepliedUser(false).queue()
        }
        audioManager.closeAudioConnection()
        guildManager.player.destroy()
        guildManager.trackManager!!.clearQueue()
       return event.reply("Goodbye D:").mentionRepliedUser(false).queue()
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}