package brys.dev.kyro.commands.standard.music

import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.structures.Util
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.interactions.components.Button

class Lyrics: ICommand {
    @AddCommand(["lyrics","l","lyric"])
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        event.reply("Ur mum").setActionRow(
            Button.primary(Util.Strings.makeRandomString(10),"Is fat ")
        ).queue()
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }

    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}