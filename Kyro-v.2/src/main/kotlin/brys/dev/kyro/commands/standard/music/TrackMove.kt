package brys.dev.kyro.commands.standard.music

import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents
import brys.dev.kyro.lib.model.PlayerManager
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel

class TrackMove(val wsEvents: WSEvents): ICommand {
    @AddCommand(["mv","move"], vc = true, dj = true)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        val manager = PlayerManager.instance
        val guildManager = manager!!.getGuildManager(event.guild, event, event.author)
        val queue = guildManager.trackManager?.queue
        if (args.first().isEmpty()) return event.reply("A target track is required!").mentionRepliedUser(false).queue()
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }

    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}