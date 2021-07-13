package brys.dev.kyro.commands.standard.info

import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.modules.command.AddCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import brys.dev.kyro.lib.types.ICommand
import brys.dev.kyro.lib.classes.db.FindServerSetting
import brys.dev.kyro.lib.structures.Pages
import brys.dev.kyro.lib.model.Page
import brys.dev.kyro.lib.types.PageType
import net.dv8tion.jda.api.entities.MessageEmbed
import java.util.concurrent.TimeUnit


class Help: ICommand {
    @AddCommand(["help", "h"])
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        val pages = ArrayList<Page>()
        val home = EmbedBuilder()
            .setAuthor("Help", null, "https://cdn.discordapp.com/emojis/817070617865224192.png?v=1")
            .addField("Prefix", "Current prefix for this server is **${FindServerSetting(event.guild.id).prefix}**", false)
            .addField("Reactions", ":arrow_backward: <- Go Back to the last page\n:arrow_forward: <- Go Forward to the next page", false)
            .setColor(FindServerSetting(event.guild.id).color)
            .setFooter("Page Home (0/4) START • ${event.author.asTag}", event.author.effectiveAvatarUrl)
        val settings = EmbedBuilder()
            .setAuthor("Settings", null, "https://cdn.discordapp.com/emojis/585767366743293952.png?v=1")
            .addField("Server Settings","Gets you the current settings the bot is using for the guild\n **Aliases** [`si` `serversettings` `serversetting`]",false)
            .addField("DJ Only", "Set's DJ only mode true being music commands may only be executed by a member with the role and false making almost all music commands usable by anyone.\n **Aliases** [`djonly` `adminonly`]\n **Arguments** [`True/False`]\n **Permission Required** [`Manage_Server`]", false)
            .addField("Set DJ", "Sets the DJ role for this guild\n **Aliases** [`setdj` `dj`]\n **Arguments** [`MentionRole/RoleName`]\n **Permission Required** [`Manage_Server`]", false)
            .addField("Set Color", "Sets the color for the embeds the bot uses\n **Aliases** [`SetColor` `color`]\n **Arguments** [`HexColorCode`]\n **Permission Required** [`Manage_Server`]", false)
            .addField("Set Prefix", "Sets the prefix for the bot\n **Aliases** [`setp` `SetPrefix`]\n **Arguments** [`Prefix`]\n **Permission Required** [`Manage_Server`]", false)
            .setColor(FindServerSetting(event.guild.id).color)
            .setFooter("Page Settings (1/4) • ${event.author.asTag}", event.author.effectiveAvatarUrl)
        val info = EmbedBuilder()
            .setAuthor("Info", null, "https://cdn.discordapp.com/emojis/817084560105275443.png?v=1")
            .addField("Help", "Gets you the help for the bot\n **Aliases** [`help`, `h`]",false)
            .addField("Ping", "Gets you the RTT (<:rtt:816185247522881576>, Round Trip Time)\n Gateway Ping (<:gateway:816186931586924584>, Discord's Gateway)\n And REST Ping (<:rest:816190314498359316>, [Link](https://en.wikipedia.org/wiki/Representational_state_transfer))\n **Aliases** [`Ping` `Time`]", false)
            .addField("Invite", "Generates a Bot invite for ${event.jda.selfUser.name}\n **Aliases** [`invite` `inv`]", false)
            .addField("Info", "Shows the bots contributors, source, and creator\n **Aliases** [`info` `i`]", false)
            .addField("Stats", "Displays the bots statistics, such as threads, memory usage, guilds, and more\n **Aliases** [`stats` `stat`]", false)
            .setColor(FindServerSetting(event.guild.id).color)
            .setFooter("Page Info (2/4) • ${event.author.asTag}", event.author.effectiveAvatarUrl)
        val music = EmbedBuilder()
            .setAuthor("Music", null, "https://i.brys.tk/FZ0o.gif")
            .addField("Play", "Plays a song or playlist\n **Aliases** [`play` `p`]\n **Arguments** [`SongName/SongURL/PlaylistURL`]", false)
            .addField("Queue", "Shows the current queue, if any\n **Aliases** [`queue` `q`]", false)
            .addField("Playlist", "Make a playlist then save songs to it\n **Aliases** [`playlist` `pp`]\n **Arguments** [`Create/Delete`] [`PlaylistName`]", false)
            .addField("Seek", "Seek in the given playing track\n **Aliases** [`seek` `setposition` `sp`]\n **Arguments** [`Position`]", false)
            .addField("Skip", "Skip the current track that's playing if you played it, or vote to skip it.\n **Aliases** [`skip` `next` `s`]", false)
            .setColor(FindServerSetting(event.guild.id).color)
            .setFooter("Page Music (3/4) • ${event.author.asTag}", event.author.effectiveAvatarUrl)
            val dev = EmbedBuilder()
                .setAuthor("Developer", null, "https://cdn.discordapp.com/emojis/697686848545488986.png?v=1")
                .addField("Eval", "Evaluate some kotlin code, or try to break stuff\n **Aliases** [`eval` `e`]\n **Arguments** [`KotlinCode`]\n **Owner Required**", false)
                .addField("Restart", "Restarts the bot and attempts to join back in every channel it was in and re-queue the playing song\n **Aliases** [`Restart`]\n **Owner/CoOwner Required**", false)
                .addField("Contributor", "Add a contributor to be shown in the info command\n **Aliases** [`Contrib` `Contributor`]\n **Arguments** [`add/remove`] [`ContribName`]\n **Owner Required**", false)
                .setColor(FindServerSetting(event.guild.id).color)
                .setFooter("Page Developer (4/4) END • ${event.author.asTag}", event.author.effectiveAvatarUrl)
            pages.add(Page(PageType.EMBED, home.build()))
            pages.add(Page(PageType.EMBED, settings.build()))
            pages.add(Page(PageType.EMBED, info.build()))
            pages.add(Page(PageType.EMBED, music.build()))
            pages.add(Page(PageType.EMBED, dev.build()))
            event.channel.sendMessage(pages[0].content as MessageEmbed).queue { success -> Pages.paginate(success, pages, 300, TimeUnit.SECONDS, event.author.id) }
        return
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }
    override fun setDescripton(): String {
        return "Pretty self explanatory"
    }
}

