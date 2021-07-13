package brys.dev.kyro.lib.interfaces

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

interface QueueWSChangeEvent {
    fun queue(before: List<AudioTrack>, after: List<AudioTrack>)
     data class QueueWSChangeEvent(var before: List<AudioTrack>, var after: List<AudioTrack>)
}