package brys.dev.kyro.lib.classes.events.dashevents.events

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.VoiceChannel

data class UserLeftChannelEvent(val member: Member, val channel: VoiceChannel)