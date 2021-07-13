package brys.dev.kyro.lib.classes

import brys.dev.kyro.lib.structures.Util
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent

class JiroClient(private val jda: JDA, private val prefix: String) {
private val start = System.currentTimeMillis()
    private val sharded = if(jda.shardInfo.shardTotal <= 1) "not sharded" else "sharded"
    @SubscribeEvent
    fun jic(event: MessageReceivedEvent)  {
        if (event.message.contentRaw != "${prefix}jic") return
        return event.message.reply("Jiro v0.0.1, JDA **${JDAInfo.VERSION_MAJOR}.${JDAInfo.VERSION_MINOR}** **${Util.Time.getDurationBreakdown(System.currentTimeMillis() - start)}**ago\n" +
                "This bot is $sharded and can see ${jda.guildCache.size()} guild(s) and ${jda.userCache.size()} cached user(s)\n" +
                "Avg Gateway ping: `${jda.gatewayPing}ms`").mentionRepliedUser(false).queue()
    }
}