package brys.dev.kyro.commands.slash.music

import brys.dev.kyro.commands.slash.core.KyroButton
import brys.dev.kyro.lib.classes.KyroSlashEvent
import brys.dev.kyro.lib.classes.SlashOption
import brys.dev.kyro.lib.modules.command.AddSlashCommand
import brys.dev.kyro.lib.types.ISlashCommand
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.Component
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu
import net.dv8tion.jda.internal.interactions.ButtonImpl
import net.dv8tion.jda.internal.interactions.SelectionMenuImpl

class Play: ISlashCommand {
    val options = listOf(SlashOption(OptionType.STRING, "Song/URL", "Play a song name or URL with this argument", true))
    @AddSlashCommand("play", "Play a URL or song")
    override fun message(event: KyroSlashEvent) {
      /*  val messageSelection = MessageBuilder("Selection menus mmmm").build()
        val menu = SelectionMenu.create("uwu")
            .addOption(
            "Cute",
            "c",
            "Are u CUTE???",
            Emoji.fromEmote("NC_hug",842261072718856222,false)
            )
            .addOption(
                "Not Cute",
                "nc",
                "Are u not cute??? :c",
                Emoji.fromEmote("NC_O2blush",841509413269667840,false)
            )
            .build() as SelectionMenuImpl
           event.menu.create(messageSelection, false, menu).invoke { event ->
            val interaction = event.menuSelectInteraction!!
            when (interaction.selectedOptions!!.first().value) {
                "c" -> return@invoke interaction.reply("${interaction.interaction.member?.asMention}, You are CUTE!!! <a:NC_love:830701610929094677>").queue()
                "nc" -> return@invoke interaction.reply("${interaction.interaction.member?.asMention}, :c... You're cute shush :(").queue()
            }
        }*/
        val message = MessageBuilder().setContent("Hi!").build()
        val button = Button.danger("emojitest", "Emoji!").withEmoji(Emoji.fromEmote("owoSmile", 670364989269213204, true)) as ButtonImpl
        val button2 = Button.primary("kekek", "uwu") as ButtonImpl
        event.button.create(message, true, button, button2).invoke { btEvent ->
            val buttonName = btEvent.buttonInteraction!!.button?.id
            val msg = btEvent.buttonInteraction
            when(buttonName) {
                "emojitest" -> {
                    msg.reply("uwu!").queue()
                }
                "kekek" -> {
                    msg.editMessage("End button").queue()
                }
            }
        }
        val messageSelection = MessageBuilder("Selection menus mmmm").build()
        val menu = SelectionMenu.create("uwu").addOption(":heart:", "❤️").build() as SelectionMenuImpl

    }
}