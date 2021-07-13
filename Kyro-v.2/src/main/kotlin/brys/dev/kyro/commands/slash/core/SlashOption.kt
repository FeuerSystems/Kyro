package brys.dev.kyro.commands.slash.core

import net.dv8tion.jda.api.interactions.commands.OptionType

data class SlashOption(val type: OptionType, val name: String, val description: String, val required: Boolean)