package brys.dev.kyro.lib.classes.events.dashevents.events

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

data class TrackChangeEvent(val prev: AudioTrack?, val post: AudioTrack)
