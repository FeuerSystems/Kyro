package brys.dev.kyro

import brys.dev.kyro.commands.standard.dev.Restart
import brys.dev.kyro.commands.standard.info.*
import brys.dev.kyro.commands.standard.music.*
import brys.dev.kyro.commands.standard.settings.*
import brys.dev.kyro.lib.structures.config.Config
import brys.dev.kyro.lib.classes.JiroClient
import brys.dev.kyro.lib.structures.MongoDatabase
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.AnnotatedEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import brys.dev.kyro.lib.classes.events.EventThread
import brys.dev.kyro.lib.classes.events.Status
import brys.dev.kyro.lib.classes.CommandHandler
import brys.dev.kyro.lib.classes.events.Events

import brys.dev.kyro.commands.standard.dev.Eval
import brys.dev.kyro.lib.classes.SlashCommandHandler
import brys.dev.kyro.lib.classes.events.dashevents.core.GenericListener
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents
import brys.dev.kyro.lib.classes.events.dashevents.events.TestEvent
import brys.dev.kyro.lib.classes.events.dashevents.events.TrackChangeEvent
import brys.dev.kyro.lib.classes.events.dashevents.events.UserJoinedChannelEvent
import brys.dev.kyro.lib.structures.Router
import brys.dev.kyro.lib.structures.Pages
import brys.dev.kyro.lib.structures.Util
import brys.dev.kyro.unsafe.EXEventBus
import brys.dev.kyro.web.WebsocketServer
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.ajalt.mordant.TermColors
import com.google.common.eventbus.Subscribe
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.LoggerFactory
import sun.misc.Unsafe
import java.lang.Exception
import java.lang.NullPointerException
import java.lang.reflect.Field

/**
 * The main run method for the Music Bot
 * @author Brys
 */
val color = TermColors()
@ObsoleteCoroutinesApi
suspend fun main(args: Array<String>) {
    val ws = WebsocketServer()
    ws.start(7569)
    ws.initwebsocket()
    val bus = EXEventBus
    val wsEvents = WSEvents(bus)
    val ch = CommandHandler(wsEvents)
    disableWarning()
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
     loggerContext.getLogger("net.dv8tion.jda").level = Level.OFF
    val auth = Util.Strings.makeRandomString(20)
    val server = Router(wsEvents)
    val authConstruct = server.RouterConstructor()
    println("Log Server Online At http://localhost:${Config.API.port}/bot/log?auth=${auth}")
    println("Status API Online At: http://localhost:${Config.API.port}/api/status")
    /**
     * Controls the gui if its enabled or disabled
     */
    val gui: Boolean = when (args.isEmpty()) {
        true -> true
        false -> gui(args[0])
    }
    val waiter = EventWaiter()
    val events = Events(wsEvents)
    /**
     * Loads commands
     */
    ch.registerCommand(SetPrefix(ch.prefixes))
    ch.registerCommand(Ping())
    ch.registerCommand(SetColor())
    ch.registerCommand(Help())
    ch.registerCommand(Play(wsEvents))
    ch.registerCommand(Seek(wsEvents))
    ch.registerCommand(Queue(wsEvents))
    ch.registerCommand(Skip(wsEvents))
    ch.registerCommand(Restart(wsEvents))
    ch.registerCommand(Invite())
    ch.registerCommand(ServerSettings())
    ch.registerCommand(Info())
    ch.registerCommand(Playlist())
    ch.registerCommand(Data())
    ch.registerCommand(Save(wsEvents))
    ch.registerCommand(SetDJ())
    ch.registerCommand(DJOnly())
    ch.registerCommand(Join())
    ch.registerCommand(Leave(wsEvents))
    ch.registerCommand(Search(waiter, wsEvents))
    ch.registerCommand(Stats(events))
    ch.registerCommand(Volume(wsEvents))
    ch.registerCommand(Karaoke(waiter))
    ch.registerCommand(Eval(wsEvents))
    ch.registerCommand(ForceSkip(wsEvents))
    ch.registerCommand(Pause())
    ch.registerCommand(Lyrics())
    /**
     * Init JDA
     */
    val bot = JDABuilder.createDefault(
        Config.bot.token,
    GatewayIntent.GUILD_MEMBERS,
    GatewayIntent.GUILD_MESSAGES,
    GatewayIntent.GUILD_VOICE_STATES,
    GatewayIntent.DIRECT_MESSAGES,
    GatewayIntent.GUILD_MESSAGE_REACTIONS,
    ).disableCache(CacheFlag.EMOTE).setMemberCachePolicy(MemberCachePolicy.ALL).enableCache(CacheFlag.VOICE_STATE).setRawEventsEnabled(true)
        .build()
  val sch = SlashCommandHandler(bot)
    bot.selfUser
        Pages.api = bot
        bot.setEventManager(AnnotatedEventManager())
        bot.addEventListener(ch, Status(gui), Pages.handler, JiroClient(bot, ">>"), waiter, events, CustomEventListener(bot, sch), sch)
       // routerConstructor.setBot(bot)
       // routerConstructor.setAuth(auth)
       // routerConstructor.setEvents(events)
        EventThread(bot, gui).run()
    ch.api = bot
    bot.selfUser
    authConstruct.setBot(bot)
    authConstruct.setAuth(Util.Strings.makeRandomString(20))
    authConstruct.setEvents(events)
    authConstruct.build()
    /**
     * Connect to MongoDB
     */
    MongoDatabase.runStartup()
    /**
     * Starts event thread for updating gui [brys.dev.kyro.lib.GUI.GuiModule]
     */
    /**
     * Add our event listeners
     */





}
/**
 * The parser for seeing if nogui mode is active
 */
private fun gui(args: String): Boolean {
    return !args.contentEquals("--nogui")
}
fun disableWarning() {
    try {
        val theUnsafe: Field = Unsafe::class.java.getDeclaredField("theUnsafe")
        theUnsafe.isAccessible = true
        val u: Unsafe = theUnsafe.get(null) as Unsafe
        val cls = Class.forName("jdk.internal.module.IllegalAccessLogger")
        val logger: Field = cls.getDeclaredField("logger")
        u.putObjectVolatile(cls, u.staticFieldOffset(logger), null)
    } catch (e: Exception) {
        // ignore
    }
}

class CustomEventListener(val bot: JDA, val sch: SlashCommandHandler) {
    @SubscribeEvent
   fun awaitReady(event: ReadyEvent) {
            sch.registerSlashCommand(brys.dev.kyro.commands.slash.music.Play(), null)
        }
    @SubscribeEvent
    fun onMessage(event: GuildMessageReceivedEvent) {
        if (event.author.id != "443166863996878878") return
        if (event.message.contentRaw != "slash add") return
        event.message.reply("Adding slash...").queue()
        val commandList = event.guild.updateCommands()
       commandList.addCommands(CommandData("Play", "Play a URL or song").addOptions(OptionData(OptionType.STRING, "Song/URL", "Play a song name or URL with this argument").setRequired(true)))
      commandList.queue{
          println("AAAa")
      }
    }
   }
