package brys.dev.kyro.lib.classes.events
import brys.dev.kyro.lib.structures.config.Config
import brys.dev.kyro.lib.classes.db.RemoveServer
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents
import brys.dev.kyro.lib.classes.events.dashevents.events.UserJoinedChannelEvent
import brys.dev.kyro.lib.methods.Logger
import brys.dev.kyro.lib.structures.VCEvents
import com.github.ajalt.mordant.TermColors
import com.google.common.eventbus.EventBus
import io.ktor.http.cio.websocket.*
import me.kosert.flowbus.GlobalBus
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.events.RawGatewayEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.update.GuildUpdateRegionEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import org.json.JSONObject

/**
 * Use events for more efficent operation and cleanup
 */
class Events(val ws: WSEvents) {
    var members: Int = 0
    val c = TermColors()

    /**
     * Event a guild join and send a embed to servers default channel
     */
    @SubscribeEvent
    fun guildJoin(event: GuildJoinEvent) {
        if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_WRITE) or !event.guild.selfMember.hasPermission(
                Permission.MESSAGE_WRITE
            )
        ) return
        val inviteEmbed = EmbedBuilder()
            .setAuthor(event.jda.selfUser.name)
            .addField("Data Collection", "By using this 'bot' you agree that: This bot will collect your User ID. This is needed due to Playlist intergration.", true)
            .addField("Use of my data", "The access of your data is limited to you, unless **told** otherwise. OR set otherwise your: Playlists, Commands, Use case. Is all information that cannot be accessed by anyone.", true)
            .addField("Removal of my data", "You have the right to clear your data at anytime.", true)
            .setDescription("Your prefix is __${Config.bot.prefix}__")
        inviteEmbed.setThumbnail(event.jda.selfUser.effectiveAvatarUrl)
        event.guild.textChannelCache.first().sendMessage(inviteEmbed.build()).queue()
    }

    /**
     * Event for a region change to notify listeners about the change of regions
     */
    @SubscribeEvent
    fun guildRegionChange(event: GuildUpdateRegionEvent) {
        if (event.guild.audioManager.connectedChannel == null) return
        if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_WRITE) or !event.guild.selfMember.hasPermission(
                Permission.MESSAGE_WRITE
            )
        ) return
        val regionEmbed = EmbedBuilder()
        regionEmbed.setAuthor("Region change")
        regionEmbed.setDescription("Old region (${event.oldRegionRaw}) -> New region (${event.newRegionRaw})\n ${event.oldRegion.emoji} -> ${event.newRegion.emoji}")
        event.guild.defaultChannel!!.sendMessage(regionEmbed.build()).queue()
    }

    /**
     * When leaving a guild clean up all Database assets for the guild
     */
    @SubscribeEvent
    fun guildLeave(event: GuildLeaveEvent) {
        RemoveServer(event.guild.id).remove()
    }

    /**
     * Load members
     */
    @SubscribeEvent
    fun loadStartup(event: ReadyEvent) {
        for (i in event.jda.guildCache) {
            i.loadMembers().onSuccess { m -> members += m.size }
        }
        Logger(this.javaClass).info("${c.brightGreen.bg} ${c.black}~ ${c.reset} \u001B[36m${event.jda.selfUser.name}\u001B[0m is \u001B[32mReady\u001B[0m, responding to \u001B[33m${event.jda.guildCache.size()}\u001B[0m guilds, at \u001B[31m${event.jda.gatewayPing}ms\u001B[0m")
    }

    @SubscribeEvent
    fun loadSlashys(event: ReadyEvent) {

    }
    @SubscribeEvent
    fun onUserConnect(event: GuildVoiceJoinEvent) {
     GlobalBus.post(VCEvents.VCJOIN(event))
    }
   @SubscribeEvent
   fun onUserLeave(event: GuildVoiceLeaveEvent) {
       GlobalBus.post(VCEvents.VCLEAVE(event))
   }
    @SubscribeEvent
    fun userChange(event: GuildVoiceMoveEvent) {
        GlobalBus.post(VCEvents.VCCHANGE(event))
    }
}