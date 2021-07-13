package brys.dev.kyro.commands.standard.music

import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent


class Join: ICommand {
    @AddCommand(["join", "j", "fuckon"], vc = true, dj = true)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        val audioManager = event.guild.audioManager
        if (!audioManager.isConnected) {
            try {
                audioManager.openAudioConnection(event.member!!.voiceState!!.channel)
                audioManager.isSelfDeafened = true
            } catch (audioException: Exception) {
                when (audioException) {
                    IllegalArgumentException() -> event.reply("Looks like something broke when I tried to connect to your channel.")
                        .mentionRepliedUser(false).queue()
                    UnsupportedOperationException() -> event.reply("Something internally broke when trying to connect to your channel.")
                        .mentionRepliedUser(false).queue()
                    InsufficientPermissionException(event.guild, Permission.VOICE_CONNECT) -> {
                        event.reply("Please give me permissions to connect.").mentionRepliedUser(false).queue()
                    }
                }
            }
        }
        val gasm = if (event.contentRaw.contains("fuckon")) "<:2bgasm:818311211975835681>" else ""
        event.reply("$gasm Connected to `${event.member!!.voiceState!!.channel!!.name}` with **${event.member!!.voiceState!!.channel!!.members.size}** Members!").mentionRepliedUser(false).queue()
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}