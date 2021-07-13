package brys.dev.kyro.lib.classes.api.Slashy

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel


/**
 * @param sender The member who sent the slash command
 * @param channel The channel where the slash command was executed
 * @param command The command which was executed
 * @param args The arguments which were used
 * @param sub The sub command, if the sender used one
 */
class SlashCommandEvent(val member: Member, val channel: TextChannel, val command: SlashCommand, val args: ArrayList<SlashCommandArgument>, val subCommand: SlashSubCommand?, val interaction: Interaction) {
}