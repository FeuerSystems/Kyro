package brys.dev.kyro.lib.classes
import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandListener
import brys.dev.kyro.lib.structures.config.Config
import brys.dev.kyro.lib.classes.db.FindServerSetting
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents
import brys.dev.kyro.lib.interfaces.KyroMessage
import brys.dev.kyro.lib.methods.Logger
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.model.PlayerManager
import brys.dev.kyro.lib.types.ICommand
import com.google.gson.Gson
import net.dv8tion.jda.api.EmbedBuilder
import java.util.HashMap
import net.dv8tion.jda.api.hooks.SubscribeEvent
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import java.lang.NumberFormatException
import java.lang.Exception
import java.lang.NullPointerException
import java.lang.reflect.Method
import java.util.concurrent.Executors
@SuppressWarnings
/**
 * Handles the fucking commands
 */
class CommandHandler(val wsevents: WSEvents) {
    val logger = Logger(this.javaClass)
    val commands = HashMap<String, Command>()
    val prefixes = HashMap<String, String>()
    var api: JDA? = null
    var kyro = KyroMessage
    val gson = Gson()
    val wsEvents = wsevents
    /**
     * The handling method using jda annotation methods.
     */
    @SubscribeEvent
    fun handle(event: GuildMessageReceivedEvent) {
        kyro.message = event
        val prefix = this.retrievePrefix(event.guild.id)
        if (event.message.contentRaw.startsWith("<@!750368143209267281>")) return event.message.reply("My prefix in this guild is set to `${prefix}`").queue()
        if (!event.message.contentRaw.startsWith(prefix!!)) return
        if (event.author.isBot || event.message.isWebhookMessage) return
        val splitContent = event.message.contentRaw.removePrefix(prefix).split(" ").toTypedArray()
        if (!commands.containsKey(splitContent[0])) return
        val command = commands[splitContent[0]]
        val annotation = command!!.commandAnnotation
        if (annotation.permission != Permission.UNKNOWN && !event.member!!.hasPermission(annotation.permission)) return event.message.reply(
            "You lack the permission to use this command."
        ).mentionRepliedUser(false).queue()
        if (annotation.playing && PlayerManager.instance!!.getGuildManager(
                event.guild,
                event.message,
                event.author).player.playingTrack == null
        ) return event.message.reply("A track must be playing in order to use this command.").mentionRepliedUser(false)
            .queue()
        if (annotation.dj && FindServerSetting(event.guild.id).djOnly.toString()
                .toBoolean() && event.member!!.roles.stream()
                .noneMatch { r: Role -> r.id == FindServerSetting(event.guild.id).DJ }
        ) return event.message.reply("You must have the DJ role in order to use this command.")
            .mentionRepliedUser(false).queue()
        if (annotation.vc && !event.member?.voiceState!!.inVoiceChannel() || annotation.vc && !event.guild.selfMember.hasPermission(Permission.VOICE_CONNECT)) return event.message.reply("You are not in a channel or I cannot see or speak in it.").mentionRepliedUser(false).queue()
        if (annotation.owners && Config.bot.owner != event.author.id && !Config.bot.owners.contains(event.author.id)) return event.message.reply(
            "You weren't found to be the owner or any of the Co Owners of this Bot."
        ).mentionRepliedUser(false).queue()
        async.submit {
            invokeMethod(command, getParameters(splitContent, command, event.message, event.jda), event)
        }
    }


    /**
     * Register the shite
     */
    fun registerCommand(command: ICommand) {
        for (method in command.javaClass.methods) {
            val annotation = method.getAnnotation(AddCommand::class.java) ?: continue
            require(annotation.aliases.isNotEmpty()) { "No aliases have been defined!" }
            val simpleCommand: Command = Command(annotation, method, command)
            for (alias in annotation.aliases) {
                commands[alias.toLowerCase()] = simpleCommand
            }
        }
    }

    /**
     * Parameters :kekdead:
     */
    private fun getParameters(splitMessage: Array<String>, command: Command?, message: Message?, jda: JDA): Array<Any?> {
        val args = splitMessage.copyOfRange(1, splitMessage.size)
        val parameterTypes = command!!.method.parameterTypes
        val parameters = arrayOfNulls<Any>(parameterTypes.size)
        var stringCounter = 0
        for (i in parameterTypes.indices) {
            val type = parameterTypes[i]
            if (type == String::class.java) {
                if (stringCounter++ == 0) {
                } else {
                    if (args.size + 2 > stringCounter) {
                        parameters[i] = args[stringCounter - 2]
                    }
                }
            } else if (type == Array<String>::class.java) {
                parameters[i] = args
            } else if (type == Message::class.java) {
                parameters[i] = message
            } else if (type == JDA::class.java) {
                parameters[i] = jda
            } else if (type == TextChannel::class.java) {
                parameters[i] = message?.textChannel
            } else if (type == User::class.java) {
                parameters[i] = message?.author
            } else if (type == MessageChannel::class.java) {
                parameters[i] = message?.channel
            } else if (type == Guild::class.java) {
                if (message?.channelType != ChannelType.TEXT) {
                    parameters[i] = message?.guild
                }
            } else if (type == Array<Any>::class.java) {
                parameters[i] = getObjectsFromString(jda, args)
            } else {
                parameters[i] = null
            }
        }
        return parameters
    }

    /**
     * Do I need to explain this?
     */
    private fun getObjectsFromString(jda: JDA, args: Array<String>): Array<Any?> {
        val objects = arrayOfNulls<Any>(args.size)
        for (i in args.indices) {
            objects[i] = getObjectFromString(jda, args[i])
        }
        return objects
    }

    /**
     * Grab a singular Object from string
     */
    private fun getObjectFromString(jda: JDA, arg: String): Any {
        try {
            return Integer.valueOf(arg)
        } catch (e: NumberFormatException) {
            println("[ERROR] $e")
        }
        if (arg.matches("<@([0-9]*)>".toRegex())) {
            val id = arg.substring(2, arg.length - 1)
            val user = jda.getUserById(id)
            if (user != null) {
                return user
            }
        }
        if (arg.matches("<#([0-9]*)>".toRegex())) {
            val id = arg.substring(2, arg.length - 1)
            val channel = jda.getTextChannelById(id) as Invite.Channel?
            if (channel != null) {
                return channel
            }
        }
        return arg
    }

    /**
     * Invokes (runs) the command
     */
    private fun invokeMethod(command: Command?, paramaters: Array<Any?>, event: GuildMessageReceivedEvent) {
        val m = command!!.method
        try {
            m.invoke(command.executor, *paramaters)
        } catch(e: Exception) {
                this.api?.getGuildById(Config.bot.supportServer)?.getTextChannelById(Config.bot.errorLog)?.sendMessage(
                    EmbedBuilder().setAuthor("Exception")
                        .setDescription("**Command** | `${command.executor.javaClass.name}`\n **Message** | `${e.message}`\n **Type** | `${e.javaClass.name}`\n **Location** | `${e.stackTrace.last().fileName}` (***${e.stackTrace.last().lineNumber}***)\n ```java\n${e.stackTraceToString()}\n```")
                        .build()
                )?.queue()
                event.message.reply(
                    EmbedBuilder().setAuthor("Uh oh! ⚠️").setTitle("${command.commandAnnotation.aliases[0]} Error")
                        .setDescription("```java\nException: ${e.javaClass.simpleName}\n```\n The developers were notified about this issue and will fix it shortly.")
                        .build()
                ).mentionRepliedUser(false).queue()
            e.printStackTrace()
            }
    }

    @SubscribeEvent
    private fun loadPrefixes(event: ReadyEvent) {
        for (i in event.jda.guildCache) {
            prefixes[i.id] = FindServerSetting(i.id).prefix
        }
        logger.info("All prefixes from the guilds have been loaded [\u001B[34m${prefixes.size}\u001B[0m]")
    }

    @SubscribeEvent
    private fun guildJoinSetup(event: GuildJoinEvent) {
        prefixes[event.guild.id] = FindServerSetting(event.guild.id).prefix
    }

    /**
     * Retrieves a prefix that has been cached
     */
    private fun retrievePrefix(guildID: String): String? {
        if (prefixes[guildID] == null) {
            try {
                throw NullPointerException("Something internally broke")
            } catch (e: Exception) {
                logger.severe(e, true)
            }
        }
        return prefixes[guildID]
    }

    /**
     * Command le constructor
     */
    inner class Command internal constructor(
        val commandAnnotation: AddCommand,
        val method: Method,
        val executor: ICommand
    )



    /**
     * Async Support Bitches :cool:
     */
    companion object {
        private val async = Executors.newCachedThreadPool()
    }

}
