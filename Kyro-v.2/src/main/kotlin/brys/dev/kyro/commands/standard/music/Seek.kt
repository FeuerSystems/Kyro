package brys.dev.kyro.commands.standard.music

import brys.dev.kyro.lib.structures.Util
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import brys.dev.kyro.lib.model.PlayerManager.Companion.instance
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import java.lang.Exception
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents

class Seek(val wsEvents: WSEvents): ICommand {
    @AddCommand(["seek","setposition","sp"], vc = true, dj = true)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        val playerManager = instance!!.getGuildManager(event.guild, event, event.author)
        val player = playerManager.player
        if (playerManager.player.playingTrack == null) {
            return event.reply("A track needs to be playing in order to use this command").mentionRepliedUser(false)
                .queue()
        }
        if (args.isEmpty()) {
            return event.reply("A time is required in order to seek a track").mentionRepliedUser(false).queue()
        }
        try {
            Util.Time.seekTime(args[0])
        } catch (e: Exception) {
            return event.reply("You need to have a valid time!").mentionRepliedUser(false).queue()
        }
        val time = Util.Time.seekTime(args[0])
        player.playingTrack.position =  if (player.playingTrack.duration < time) player.playingTrack.duration else if (time < 0) 0 else time
        return event.reply(" Seeking to `${Util.Time.formatMili(player.playingTrack.position)}` :fast_forward:").mentionRepliedUser(false).queue()
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        return "Seeks to a part in the track that's currently being played."
    }
}