package brys.dev.kyro.commands.standard.settings

import brys.dev.kyro.lib.classes.db.FindServerSetting
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import brys.dev.kyro.lib.structures.Util
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
class ServerSettings: ICommand {
    @AddCommand(["si","serversettings","serversetting"], permission = Permission.MANAGE_SERVER)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
         val djOnly = if (FindServerSetting(event.guild.id).djOnly == null || !Util.Strings.stringAsBoolean(FindServerSetting(event.guild.id).djOnly.toString())!!) "<:crossed:817047355102593024><:unchecked:817047466946986005> **False**" else "<:uncrossed:817047502271283281><:checked:817047537004970014> **True**"
         val prefix = "<:uncrossed:817047502271283281><:checked:817047537004970014> **${FindServerSetting(event.guild.id).prefix}**"
         val dj = if (FindServerSetting(event.guild.id).DJ == null) "<:crossed:817047355102593024><:unchecked:817047466946986005>" else "<:uncrossed:817047502271283281><:checked:817047537004970014> **${event.guild.getRoleById(FindServerSetting(event.guild.id).DJ.toString())?.name}**"
         val color = FindServerSetting(event.guild.id).color
        event.reply(EmbedBuilder().setTitle("${event.guild.name} Settings").addField("DJ Only", djOnly, false).addField("Prefix", prefix, false).addField("DJ Role", dj, false).addField("Color", "<:uncrossed:817047502271283281><:checked:817047537004970014> **${String.format("#%02x%02x%02x", color.red, color.green, color.blue)}**", false).setColor(color).setThumbnail(event.guild.iconUrl).build()).mentionRepliedUser(false).queue()
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}