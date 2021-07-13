package brys.dev.kyro.commands.standard.music

import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.model.PlayerManager
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel

class Pause: ICommand {
    @AddCommand(["pause","pa"], vc = true, playing = true)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        val player = PlayerManager.instance!!.getGuildManager(event.guild, event, event.author).player
        if (player.isPaused && player.playingTrack != null) {
            player.isPaused = false
            return event.reply("Player resumed!").mentionRepliedUser(false).queue()
        } else if (!player.isPaused && player.playingTrack != null) {
            player.isPaused = true
            return event.reply("Player paused!").mentionRepliedUser(false).queue()
        } else if (player.playingTrack == null) {
            return event.reply("You need to have a track playing to pause/resume the player").mentionRepliedUser(false).queue()
        }
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }

    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}