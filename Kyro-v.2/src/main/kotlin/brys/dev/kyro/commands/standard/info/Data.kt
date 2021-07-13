package brys.dev.kyro.commands.standard.info

import brys.dev.kyro.lib.classes.db.FindUserData
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import com.mongodb.BasicDBObject
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import org.codehaus.jackson.map.ObjectMapper
import java.time.Instant
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent

class Data: ICommand {
    @AddCommand(["data","d"])
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        if (args.isEmpty()) {
            val obj = FindUserData(event.author.id).collection
            val mapper = ObjectMapper()
            var s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
            s = s.substring(0, s.length.coerceAtMost(1200))
            val data = EmbedBuilder()
                .setAuthor(event.jda.selfUser.name)
                .setTitle("${event.author.asTag}'s Data")
                .setDescription("Because Transparency and Honesty is important to us, you are permitted at anytime to wipe all account Data, from ever existing for our Database.\nAs of right now this is the data we have for you:\n")
                .appendDescription("```json\n$s\n```")
                .setFooter("${event.author.asTag} • If the data says null we have no data of your user.", event.author.effectiveAvatarUrl)
                .setTimestamp(Instant.now())
                .setThumbnail(event.jda.selfUser.effectiveAvatarUrl)
            return event.reply(data.build()).mentionRepliedUser(false).queue()
        }
        if (args[0] == "wipe" || args[0] == "remove") {
            if (FindUserData(event.author.id).collection == null) {
                return event.reply("No data exists for you.").mentionRepliedUser(false).queue()
            }
            val db = FindUserData(event.author.id).db
            val query = BasicDBObject()
            query["user"] = event.author.id
            db.deleteOne(query)
            return event.reply("Your user data has been wiped. You may check this by running the same command with no arguments again. It should return null.").mentionRepliedUser(false).queue()
        }
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }
    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}