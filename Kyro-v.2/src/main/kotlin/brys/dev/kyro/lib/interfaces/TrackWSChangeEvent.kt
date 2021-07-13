package brys.dev.kyro.lib.interfaces

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

interface TrackWSChangeEvent {
   fun track(before: AudioTrack?, after: AudioTrack?)
    data class TrackWSChangeEvent(var before: List<AudioTrack>, var after: List<AudioTrack>)
}