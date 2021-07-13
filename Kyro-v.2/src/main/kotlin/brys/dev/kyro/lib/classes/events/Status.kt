package brys.dev.kyro.lib.classes.events

import brys.dev.kyro.lib.methods.Logger
import net.dv8tion.jda.api.events.*
import net.dv8tion.jda.api.hooks.SubscribeEvent
import brys.dev.kyro.lib.structures.GuiModule
import java.awt.Color

/**
 * Log and show status on gui.
 */
class Status(private val gui: Boolean) {
    @SubscribeEvent
    fun onEvent(event: GenericEvent) {
        when (event) {
            is DisconnectEvent -> { Logger(this.javaClass).info("Bot has disconnected"); if (gui) { GuiModule.warn("Bot has disconnected"); GuiModule.grabStatus().text = "<html><p>┌─────────────────┐ <br>│  Guilds: ${event.jda.guildCache.size()}<br>│  Ping: ${event.jda.gatewayPing}ms<br> └─────────────────┘</p><html>"; Color.decode("#d9970f"); GuiModule.grabLog().text + "Bot has been reconncted \n"}}
            is ReconnectedEvent -> { Logger(this.javaClass).info("Bot has reconnected"); if (gui) {  GuiModule.grabStatus().text = "<html><p>┌─────────────────┐ <br>│  Guilds: ${event.jda.guildCache.size()}<br>│  Ping: ${event.jda.gatewayPing}ms<br> └─────────────────┘</p><html>"; GuiModule.grabStatus().foreground = Color.decode("#b94644"); GuiModule.grabLog().text + "Bot has been disconnected \n"}}
            is ReadyEvent -> {
                if (gui) {
                    GuiModule.initGUI().text =
                        "<html><p>┌─────────────────┐ <br>│  Guilds: ${event.jda.guildCache.size()}<br>│  Ping: ${event.jda.gatewayPing}ms<br> └─────────────────┘</p><html>";
                    GuiModule.grabLog().text + "✅ Bot is ready. \n"
                }
            }
            is ResumedEvent -> Logger(this.javaClass).info("Bot has resumed");
            is ShutdownEvent -> { Logger(this.javaClass).info("Bot has been shutdown"); if (gui) { GuiModule.grabStatus().foreground = Color.gray; GuiModule.warn("Bot has been shutdown"); GuiModule.grabLog().text = GuiModule.grabLog().text + "Bot has been shutdown. \n"}}
        }
    }
}