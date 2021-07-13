package brys.dev.kyro.lib.classes.events

import brys.dev.kyro.lib.methods.Logger
import net.dv8tion.jda.api.JDA
import brys.dev.kyro.lib.structures.GuiModule
import java.awt.Color
import java.util.*


/**
 * The event thread that constructs a new thread with a timer for logging and displaying events for the gui
 */
class EventThread(private val bot: JDA, private val gui: Boolean): Thread() {
    override fun run() {
        name = "Event Thread"
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (!gui) return
                GuiModule.grabStatus().text = "<html><p>Guilds: ${bot.guildCache.size()}<br>  Ping: ${bot.gatewayPing}ms<br> TC: ${bot.textChannels.size}<br> Playing in: ${bot.audioManagers.size}<br> Users: ${bot.userCache.size()}</p><html>"
                when (bot.status) {
                    JDA.Status.DISCONNECTED -> GuiModule.grabStatus().foreground = Color.decode("#b94644")
                    JDA.Status.ATTEMPTING_TO_RECONNECT -> GuiModule.grabStatus().foreground = Color.decode("#d9970f")
                    JDA.Status.SHUTDOWN -> GuiModule.grabStatus().foreground = Color.gray
                    JDA.Status.CONNECTED -> GuiModule.grabStatus().foreground = Color.decode("#47c462")
                    else -> GuiModule.grabStatus().foreground = Color.decode("#46a7db")
               }
                Logger(this.javaClass).debug("Updated Stats")
            }
        }, 0, 10000)
    }
}