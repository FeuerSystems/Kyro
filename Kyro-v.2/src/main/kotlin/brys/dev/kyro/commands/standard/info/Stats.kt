package brys.dev.kyro.commands.standard.info


import brys.dev.kyro.lib.classes.db.FindServerSetting
import brys.dev.kyro.lib.classes.events.Events
import brys.dev.kyro.lib.structures.Util
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
class Stats(val events: Events): ICommand {
    @AddCommand(["stats","stat"])
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        return event.reply(EmbedBuilder().setAuthor( "Grabbing statistics...", null,  "https://i.brys.tk/FZ0o.gif").build()).mentionRepliedUser(false).queue {
             m ->
            val max = Runtime.getRuntime().maxMemory()
            val usable = Runtime.getRuntime().freeMemory()
            val used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            val useable = Runtime.getRuntime().totalMemory() - used
            m.editMessage(EmbedBuilder()
            .setAuthor(event.jda.selfUser.name)
            .setThumbnail(event.jda.selfUser.effectiveAvatarUrl)
            .addField("Memory Stats", "Allocated: `${Util.Misc.getSize(max)}`\nUsable: `${Util.Misc.getSize(useable)}`\nIn Use: `${Util.Misc.getSize(used)}`", true)
            .addField("CPU Stats", "Total Load: `${Util.CPU.getCPULoad().toInt()}%`\n Threads: `${Util.CPU.getLiveThreads()}`\n Parked: `${Util.CPU.getParkedThreads()}`", true)
            .setColor(FindServerSetting(event.guild.id).color)
            .addField("Bot Stats","Guild(s): `${event.jda.guildCache.size()}`\nUser(s): `${events.members}`\nPlaying In: `${event.jda.audioManagerCache.size()}`\nTc(s): `${event.jda.textChannelCache.size()}`",true).build()).queue()

        }
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}