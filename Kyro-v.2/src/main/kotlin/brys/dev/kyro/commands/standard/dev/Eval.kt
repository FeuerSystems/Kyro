package brys.dev.kyro.commands.standard.dev

import brys.dev.kyro.lib.classes.CommandHandler
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.classes.api.YouTube
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents
import brys.dev.kyro.lib.classes.music.GuildManager
import brys.dev.kyro.lib.model.PlayerManager
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.structures.Util
import brys.dev.kyro.lib.structures.config.Config
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.managers.AudioManager
import java.lang.Exception
import java.util.concurrent.Executors
import javax.script.ScriptEngineManager
import brys.dev.kyro.lib.methods.Logger
import brys.dev.kyro.lib.structures.Player
import javax.script.ScriptEngine

class Eval(val wsEvents: WSEvents): ICommand {
    private var first = true
    private val cachePool = Executors.newCachedThreadPool()
    @AddCommand(["eval","e"], owner = true)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        val audioManager = event.guild.audioManager
        val playerIMPL = PlayerManager.instance!!
        val player = PlayerManager.instance!!.getGuildManager(event.guild, event, event.author)
        val client = event.jda
        val core = Core(client, audioManager, event, Util, YouTube(Config.music.YTToken), CommandHandler, playerIMPL, player, Player)
        val shell = createExecutor(core)
        val import = "import net.dv8tion.jda.*\nimport brys.dev.kyro.*\n" + args.joinToString(" ")
        val logger = Logger(this.javaClass)
        event.addReaction("\uD83D\uDFE1").queue()
        cachePool.execute {
            try {
                if (first) event.channel.sendMessage("Booting internals, this may take a minute.").queue { m ->
                    logger.info("Running startup for internals...")
                    shell.eval("")
                    logger.info("Internal scripting active!")
                    m.editMessage("Internal Scripting has booted and will remain active until next restart").mentionRepliedUser(false).queue()
                }
                    val timeBefore = System.currentTimeMillis()
                    val res = shell.eval(import)
                    val timeAfter = System.currentTimeMillis()
                    event.addReaction("\uD83D\uDFE2").queue()
                    first = false
                    if (res == null) {
                        return@execute
                    }
                    val data = res.toString()
                    if (data.length > 1900) {
                        println(data)
                        event.reply("Check console!").mentionRepliedUser(false).queue()
                    } else
                        event.reply("Evaulation \n```$import\n```\n```groovy\n${data}\n```\n ⌚ Executed in ${timeAfter - timeBefore}ms").queue()

                } catch (e: Exception) {
                    if (e.stackTraceToString().length > 1900) {
                        e.printStackTrace()
                        event.addReaction("\uD83D\uDED1").queue()
                        event.reply("Exception too long, check console.").mentionRepliedUser(false).queue()
                        return@execute
                    }
                    event.addReaction("\uD83D\uDED1").queue()
                    event.reply("Evaulation\n```$import\n```\n Error occured\n```kt\n${e.message}\n```").mentionRepliedUser(false).queue()
                }
            }
    }



    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }

    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}
private fun createExecutor(core: Core): ScriptEngine {
    val shell = ScriptEngineManager().getEngineByExtension("kts")!!
    shell.put("Core", core)
    return shell
}
data class Core(val Client: JDA, val Audio: AudioManager, val Message: Message, val Util: Util, val Youtube: YouTube, val Handler: CommandHandler.Companion, val PlayerImpl: PlayerManager, val Manager: GuildManager, val Player: Player) {
    override fun toString(): String {
        return "Client = ${Client.selfUser.asTag}\nAudio = AudioController(${Audio.guild.name})\nMessage = ${Message.contentRaw}\nUtil = KyroUTIL\nYouTube = KyroYouTubeController(Config.music.YTToken)\nHandler = KyroCommandHandler\nPlayerImpl = PlayerManager\nManager = GuildManager(${Message.guild.name})\n Player = ${Player}"
    }
}