package brys.dev.kyro.commands.standard.info


import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import brys.dev.kyro.lib.structures.Util
import java.time.temporal.ChronoUnit
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
class Ping: ICommand {
    @Override
    @AddCommand(["ping","time"])
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        return event.reply(EmbedBuilder().setAuthor("Loading...", null, "https://i.brys.tk/FZ0o.gif").build()).queue { m ->
            val gatewayPing = event.jda.gatewayPing
            val ping = event.timeCreated.until(m.timeCreated, ChronoUnit.MILLIS)
            event.jda.restPing.queue { time ->
                m.editMessage(
                    EmbedBuilder().addField("<:rtt:816185247522881576>", "${ping}ms", true)
                        .addField("<:gateway:816186931586924584>", "${gatewayPing}ms", false)
                        .addField("<:rest:816190314498359316>", "${time}ms", false)
                        .setColor(Util.Misc.colorStatus(gatewayPing))
                            .build()
                ).queue()
            }
        }
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        return "Gets the bots ping to the Discord's Gateway"
    }
}