package brys.dev.kyro.commands.standard.settings

import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import brys.dev.kyro.lib.classes.db.AddServerSetting
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
class SetPrefix(private val prefixes: HashMap<String, String>): ICommand {
        @Override
        @AddCommand(aliases = ["setp", "setPrefix", "setprefix"], permission = Permission.MANAGE_SERVER)
        override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
                 if (args.isEmpty()) {
                        return event.reply("You must have a prefix to set the bot with.").mentionRepliedUser(false).queue()
                }
                AddServerSetting(event.guild.id, args[0]).prefix()
                prefixes[event.guild.id] = args[0]
                return event.reply("Prefix is now `${prefixes[event.guild.id]}`").mentionRepliedUser(false).queue()
        }

        override fun executeSlash(interaction: SlashCommandEvent) {
                TODO("Not yet implemented")
        }


        override fun setDescripton(): String {
                return "Set the prefix for the server to use the bot."
        }

}