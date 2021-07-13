package brys.dev.kyro.commands.standard.music

import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import brys.dev.kyro.lib.model.PlayerManager
import brys.dev.kyro.lib.model.Skipper
import brys.dev.kyro.lib.structures.Util
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User

import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents

class Skip(val wsEvents: WSEvents): ICommand {
    @AddCommand(["skip","s","next"], playing = true, vc = true, dj = true)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        return event.reply("Temporaily disabled.").mentionRepliedUser(false).queue()
        val listeners = Util.Voice.getActiveListeners(event.member!!.voiceState!!)
        val manager = PlayerManager.instance!!.getGuildManager(event.guild, event, event.author)
        val player = manager.player
        val meta = player.playingTrack.userData as User
        var needed = 0
        val skipEmbed = EmbedBuilder().setAuthor("Skips")
        if (args.isNotEmpty() && args[0] == "--skips") {
            for (i in manager.skips) {
                skipEmbed.addField("${i.user}", "${i.skipped}", true)
            }
            return event.reply(skipEmbed.build()).mentionRepliedUser(false).queue()
        }
        if (meta == event.author) { event.reply("Track skipped!").mentionRepliedUser(false).queue();  manager.trackManager?.nextTrack();  return}
        if (manager.skips.contains(Skipper(event.author.id, true))) return event.reply("You've already voted to skip this song!").mentionRepliedUser(false).queue()
        // Assemble Data
        if (manager.skips.isNullOrEmpty()) {
            for (i in Util.Voice.getActiveListenersMember(event.member!!.voiceState!!)) {
                if (i.user == event.author) {
                    manager.skips.add(Skipper(event.author.id, true))
                }
                 manager.skips.add(Skipper(i.user.id, false))
            }
        }
        for (i in manager.skips) {
            if (i.skipped) needed += 1
        }
        event.reply("You've voted to skip this song (**${needed}** of **${listeners}**)").mentionRepliedUser(false).queue()
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }

    override fun setDescripton(): String {
         return "Skips a track."
    }
}