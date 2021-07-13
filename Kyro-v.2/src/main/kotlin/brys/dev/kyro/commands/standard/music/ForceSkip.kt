package brys.dev.kyro.commands.standard.music

import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.classes.db.FindServerSetting
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents
import brys.dev.kyro.lib.model.PlayerManager
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel

class ForceSkip(val wsEvents: WSEvents): ICommand {
    @AddCommand(["forceskip","fs","force-skip"], playing = true, vc = true, dj = true)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        val manager = PlayerManager.instance!!.getGuildManager(event.guild, event, event.author)
        val trackManager = manager.trackManager
        val role = event.guild.getRoleById(FindServerSetting(event.guild.id).DJ.toString())

        if (!event.member!!.hasPermission(Permission.ADMINISTRATOR) || !event.member!!.hasPermission(Permission.VOICE_MUTE_OTHERS) || !event.member!!.isOwner || !event.member!!.roles.contains(role)) {
            return event.reply("You can't force skip a song if you don't have mute permission or the DJ role!").mentionRepliedUser(false).queue()
        }
        event.reply("Track skipped!").mentionRepliedUser(false).queue()
        trackManager?.nextTrack()
        return
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }

    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}