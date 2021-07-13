package brys.dev.kyro.commands.standard.info

import brys.dev.kyro.lib.classes.db.FindBotData
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import java.io.IOException
import java.net.URL
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
class Info: ICommand {
    @AddCommand(["i","info"])
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        val contrib = FindBotData().contributors.split("|").toMutableList()
        val info = EmbedBuilder()
            .setAuthor("${event.jda.selfUser.name} Info")
            .setDescription("While this Bot was independently coded by me, it couldn't have been done without the people that helped me along the way **[Br4d](https://github.com/Iskawo)** & **[Bradyn](https://github.com/Bradyn1710)**\n")
            .appendDescription("Massive shout out to you guys :heart:. That being said, it just takes one good idea :)\n")
            .appendDescription("**Contributors**\n")
        for (i in 0 until contrib.size-1) {
            val c = contrib[i]
            info.appendDescription("${c}\n")
        }
        var spotifyStatus = true
        try {
            URL("https://s.brys.tk").readText()
        } catch (e: IOException) {
            spotifyStatus = false
        }
        val spotifyAPICheck = if (!spotifyStatus) "<:crossed:817047355102593024><:unchecked:817047466946986005> No `spotify` related commands will work." else "<:uncrossed:817047502271283281><:checked:817047537004970014>"
        info.addField("API Status", "Spotify $spotifyAPICheck", false)
        info.addField("Info", "This bot was written in <:kotlin:818170981990137897>, and uses <:jda:818171819001511936> as its Wrapper for the [`Discord API`](https://discord.com/developers/docs). It plays music with [`LavaPlayer`](https://github.com/sedmelluq/lavaplayer). With a custom version of [`LavaWrap`](https://github.com/brys0/LavaWrap) Framework", false)
        event.reply(info.build()).mentionRepliedUser(false).queue()
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }


    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}


