package brys.dev.kyro.commands.standard.music

import brys.dev.kyro.lib.classes.api.Slashy.SlashCommandEvent
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents
import brys.dev.kyro.lib.model.PlayerManager
import brys.dev.kyro.lib.modules.command.AddCommand
import brys.dev.kyro.lib.types.ICommand
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import java.lang.NumberFormatException

class Volume(val wsEvents: WSEvents): ICommand {
    private val loud = listOf("I probably sound like a jet engine.","Are you trying to be your father? Turn that shit down!","You have hearing loss now  c o o l","<:qhy:817958684243787797>.","BRRBRBRRMRNRRMRBRBRBBRRRQRRRRRNEJKHJEJKRRNBRBRBRBRBR","Compression and high volume don't work well together...","I probably sound worst than the kid with the shitty mic.","Discords voice regions are so shit you're just going to hear static.","Imagine having ears..")
    private val quiet = listOf("Is your nan listening? Better turn it up (She's deaf as hell).","This isn't a study room, let others hear!",":books: Library mode active")
    @AddCommand(["volume","v","vol"], vc = true, dj = true, playing = true)
    override fun execute(event: Message, channel: MessageChannel, args: Array<String>) {
        val manager = PlayerManager.instance?.getGuildManager(event.guild, event, event.author);
        if (args.isEmpty()) {
            return event.reply("Current volume is ${manager!!.player.volume}").mentionRepliedUser(false).queue()
        }
        try {
            args[0].toInt()
        } catch (e: NumberFormatException) {
            return event.reply("That is not a valid volume!").mentionRepliedUser(false).queue()
        }
        val n = args[0].toInt()
        if (n == 420) return event.reply("<a:elonsmoke:824753212279095317> :level_slider: Volume is now __**420**__\n Just kidding it doesn't go that high <:troled:818503377624629259>").mentionRepliedUser(false).queue()
        if (n >= 200) return event.reply("Volume can not go above 200.").mentionRepliedUser(false).mentionRepliedUser(false).queue()
        if (n <= 10) return event.reply("Volume can not go below 100.").mentionRepliedUser(false).mentionRepliedUser(false).queue()
        if (n == 69){
            manager!!.player.volume = n
            return event.reply("No one thinks your funny, including me. Volume is now __**${n}**__").mentionRepliedUser(false).queue()
        } else if (n >= 120) {
            event.reply("${loud.last()} :level_slider: Volume is now __**${n}**__").mentionRepliedUser(false).queue()
            manager!!.player.volume = n
            return
        } else if (n <= 40) {
            event.reply("${quiet.random()} :level_slider: Volume is now __**${n}**__").mentionRepliedUser(false).queue()
            manager!!.player.volume = n
            return
        } else {
            event.reply(":level_slider: Volume is now __**${n}**__").mentionRepliedUser(false).queue()
            manager!!.player.volume = n
            return
        }
    }

    override fun executeSlash(interaction: SlashCommandEvent) {
        TODO("Not yet implemented")
    }

    override fun setDescripton(): String {
        TODO("Not yet implemented")
    }
}