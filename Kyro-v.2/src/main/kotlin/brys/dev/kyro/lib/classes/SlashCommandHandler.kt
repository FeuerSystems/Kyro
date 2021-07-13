package brys.dev.kyro.lib.classes

import brys.dev.kyro.lib.modules.command.AddSlashCommand
import brys.dev.kyro.lib.types.ISlashCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.hooks.SubscribeEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.internal.interactions.ButtonImpl
import net.dv8tion.jda.internal.interactions.CommandInteractionImpl
import net.dv8tion.jda.internal.interactions.SelectionMenuImpl
import java.lang.reflect.Method
import java.util.concurrent.Executors
import java.util.function.Consumer
import java.util.logging.Logger
import javax.naming.InvalidNameException
import kotlin.reflect.full.declaredMemberProperties

class SlashCommandHandler(val api: JDA) {
    private val logger = brys.dev.kyro.lib.methods.Logger(this.javaClass)
    val commands = HashMap<String, SlashCommand>()
    private val interactionCallback = HashMap<String, (interaction: KyroInteractionEvent) -> Unit>()
    private val interactionClearAfterExecution = HashMap<String, Boolean>()
    @SubscribeEvent
    fun onInteraction(event: SlashCommandEvent) {
        logger.debug("Received event {${event.name}} with options ${event.options.forEach { n -> n.name }}")
        if (!event.isFromGuild) return
        val callback =  commands[event.name]!!
        val annotation = callback.commandAnnotation
        if (annotation.vc && !event.member?.voiceState!!.inVoiceChannel() || annotation.vc && !event.guild?.selfMember?.hasPermission(Permission.VOICE_CONNECT)!!) return event.reply("You are not in a channel or I cannot see or speak in it.").mentionRepliedUser(false).queue()
         async.submit {
             invokeMethod(callback, event)
         }
    }
    @SubscribeEvent
    fun onButtonEvent(event: ButtonClickEvent) {
        logger.debug("Null check {${event.button?.id}} callback {${interactionCallback["button:${event.button?.id}"]}} interaction list {${interactionCallback}}")
        if (interactionCallback["button:${event.button?.id}"] != null) {
            val button = interactionCallback["button:${event.button?.id}"]
            if (interactionClearAfterExecution["button:${event.button?.id}"] == true) {
                val updatedList = mutableListOf<Button>()
                for (but in event.message?.buttons!!) {
                    if (but == event.button) {
                        updatedList.add(but.asDisabled())
                        break
                    }
                    updatedList.add(but)
                }
                event.editMessage(event.message!!).setActionRow(updatedList).queue()
            }
            button?.invoke(KyroInteractionEvent(event, null))
        }
    }
    @SubscribeEvent
    fun onMenuEvent(event: SelectionMenuEvent) {
        logger.debug("Null check {${event.componentId}} callback {${interactionCallback["menu:${event.componentId}"]}} interaction list {${interactionCallback}}")
       if (interactionCallback["menu:${event.componentId}"] != null) {
           for (selection in event.selectedOptions!!) {
               val menu = interactionCallback["menu:${event.componentId}"]
               menu?.invoke(KyroInteractionEvent(null, event))
           }
           if (interactionClearAfterExecution["menu:${event.componentId}"] == true) interactionCallback.remove("menu:${event.componentId}")
       }
    }
    fun registerSlashCommand(command: ISlashCommand, guild: String? ) {
        val options = command.javaClass.kotlin.declaredMemberProperties.toList().first().get(command) as List<*>?
        for (method in command.javaClass.methods) {
            val annotation = method.getAnnotation(AddSlashCommand::class.java) ?: continue
            val simpleSlashCommand: SlashCommand = SlashCommand(annotation, method, command, null)
            val mutableCollection = mutableListOf<OptionData>()
            commands[annotation.name] = simpleSlashCommand

            if (options != null) {
                for (element in options) {
                    val option = element as SlashOption
                    mutableCollection.add(OptionData(option.type, option.name, option.description, option.required))
                }
            }
            if (guild == null) {
                val globalCommands = api.updateCommands()
                globalCommands.addCommands(CommandData(annotation.name, annotation.description).addOptions(mutableCollection)).queue{
                    println("Global command added ")
                }
            }
            if (guild != null) {
                val guildObject = api.guildCache.getElementById(guild) ?: throw NullPointerException("This guild doesn't exist!")
                val guildCommands = guildObject.updateCommands()
                guildCommands.addCommands(CommandData(annotation.name, annotation.description).addOptions(mutableCollection))
                println(commands)
                guildCommands.queue{
                    println("Guild command added.")
                }

            }

        }

    }
    private fun invokeMethod(command: SlashCommand, event: SlashCommandEvent) {
        val m = command!!.method
        try {
            command.executor.message(KyroSlashEvent(event, interactionClearAfterExecution, interactionCallback))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    inner class SlashCommand internal constructor(
        val commandAnnotation: AddSlashCommand,
        val method: Method,
        val executor: ISlashCommand,
        val options: List<*>?,
    )
    companion object {
        private val async = Executors.newCachedThreadPool()
    }
}
 class KyroSlashEvent(val event: SlashCommandEvent, private val interactionClearAfterExecution: HashMap<String, Boolean>, private val interactionCallback: HashMap<String, (interaction: KyroInteractionEvent) -> Unit>): SlashCommandEvent(event.jda, event.responseNumber, event.interaction as CommandInteractionImpl) {
    val util = Util()
    val button = Button()
    val menu = Menu()
    inner class Util {
        fun connect() {
            val audioManager = event.guild!!.audioManager
            if (!audioManager.isConnected) {
                try {
                    audioManager.openAudioConnection(event.member!!.voiceState!!.channel)
                    audioManager.isSelfDeafened = true
                } catch (audioException: Exception) {
                    when (audioException) {
                        IllegalArgumentException() -> event.reply("Looks like something broke when I tried to connect to your channel.")
                            .mentionRepliedUser(false).queue()
                        UnsupportedOperationException() -> event.reply("Something internally broke when trying to connect to your channel.")
                            .mentionRepliedUser(false).queue()
                        InsufficientPermissionException(event.guild!!, Permission.VOICE_CONNECT) -> {
                            event.reply("Please give me permissions to connect.").mentionRepliedUser(false).queue()
                        }
                    }
                }
            }
        }
    }
     inner class Button {
         fun create(message: Message, clearAfterExecution: Boolean = true, vararg buttons: ButtonImpl): ((KyroInteractionEvent) -> Unit) -> Unit {
             val msg = event.reply(message)
             for (button in buttons) {
                 if (button.id?.startsWith("button:") == true) throw InvalidNameException("Button id can't contain button: at start!")
                 msg.addActionRow(button)
             }
             msg.queue()
             return fun(listener: (KyroInteractionEvent) -> Unit) {
                     for (button in buttons) {
                         interactionClearAfterExecution["button:${button.id}"] = clearAfterExecution
                         interactionCallback["button:${button.id}"] = listener
                     }
             }
         }
         fun remove(vararg buttons: String) {
             for (button in buttons) {
                 interactionCallback.remove("button:${button}")
             }
         }
     }
    init {
     brys.dev.kyro.lib.methods.Logger(this.javaClass).info("Slash command handler initialized")
 }
     inner class Menu {
         fun create(message: Message, clearAfterExecution: Boolean = true, menu: SelectionMenuImpl): ((KyroInteractionEvent) -> Unit) -> Unit {
             println("helLLO???")
             val msg = event.reply(message)
             println(msg)
             if (menu.id?.startsWith("menu:") == true) throw InvalidNameException("Menu id can't contain menu: at start!")
             msg.addActionRow(menu)
             msg.queue{
                 event.reply("hmmmmmmmmmm")
             }
             return fun (listener: (KyroInteractionEvent) -> Unit) {
                 interactionClearAfterExecution["menu:${menu.id}"] = clearAfterExecution
                 interactionCallback["menu:${menu.id}"] = listener
             }
         }
     }
}
data class SlashOption(val type: OptionType, val name: String, val description: String, val required: Boolean)
data class KyroInteractionEvent(val buttonInteraction: ButtonClickEvent?, val menuSelectInteraction: SelectionMenuEvent?)
