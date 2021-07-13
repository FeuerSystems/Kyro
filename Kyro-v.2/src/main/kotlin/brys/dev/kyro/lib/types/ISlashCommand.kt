package brys.dev.kyro.lib.types

import brys.dev.kyro.lib.classes.KyroSlashEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

 interface ISlashCommand {
    fun message(event: KyroSlashEvent)
}