package brys.dev.kyro.lib.classes.music

import brys.dev.kyro.lib.classes.db.AddUserData
import brys.dev.kyro.lib.classes.db.FindUserData
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import brys.dev.kyro.lib.classes.events.TrackEvents
import brys.dev.kyro.lib.classes.exceptions.TrackExceptions
import brys.dev.kyro.lib.structures.PlayerEvents
import me.kosert.flowbus.GlobalBus
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import java.util.*

/**
 * The Lavawrap track manager class managing all track events and exceptions / providing util for other methods
 * [nextTrack] [queueRepeat] [trackRepeat] [queueRepeating] [trackRepeating] [shuffleQueue] [removeTrack] [skipTracks]
 * @author [Brys](http://brys.tk)
 */
class TrackManager(private val player: AudioPlayer, private val guild: Guild, private val tx: Message?, private val requester: User): AudioEventAdapter(),
    AudioEventListener {
    private var lastPlay: AudioTrack? = null
    var queue: Queue<AudioTrack> = LinkedList()
    var currentTrack: AudioTrack? = null
    private var trackRepeat: Boolean = false
    private var queueRepeat: Boolean = false
    /**
     * The queue method for tracks either loads now or places the track in queue
     */
    fun queue(track: AudioTrack, channel: TextChannel) {
         val previousTotalQueued = if (FindUserData(requester.id).collection?.get("t_queued") == null) 0 else FindUserData(requester.id).collection?.get("t_queued") as Int
        AddUserData(requester.id, previousTotalQueued + 1).totalQueued()
        try {
            if (!player.startTrack(track, true)) {
                queue.offer(track)
            }
        } catch (e: FriendlyException) {
            channel.sendMessage("An error occurred most likely this is because the requested song is age restricted.").queue()
        }
    }
    /**
     * The next track method for playing the next track in queue
     */
    fun nextTrack(): Boolean {
        val data = queue.peek()
         player.startTrack(queue.poll(), false)
        return true
    }
    /**
     * Catch exceptions...
     */
    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        GlobalBus.post(PlayerEvents.PlayerEvent(player, "PLAYER_EXCEPTION", guild.audioManager.connectedChannel, guild))
        TrackExceptions(player, track, tx).trackException(exception,null)
    }
    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        GlobalBus.post(PlayerEvents.PlayerEvent(player, "PLAYER_STUCK", guild.audioManager.connectedChannel, guild))
        TrackExceptions(player, track, tx).trackStuck(thresholdMs,null)
    }

    override fun onPlayerPause(player: AudioPlayer) {
        GlobalBus.post(PlayerEvents.PlayerEvent(player, "PLAYER_PAUSE", guild.audioManager.connectedChannel, guild))
    }

    override fun onPlayerResume(player: AudioPlayer) {
        GlobalBus.post(PlayerEvents.PlayerEvent(player, "PLAYER_RESUME", guild.audioManager.connectedChannel, guild))
    }
    /**
     * Provides either the next track to be played or loops the current track
     */
    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        GlobalBus.post(PlayerEvents.PlayerEvent(player, "PLAYER_TRACK_END", guild.audioManager.connectedChannel, guild))
        this.lastPlay = track
        val previousTotal = if (FindUserData(requester.id).collection?.get("t_tracks") == null) 0 else FindUserData(requester.id).collection?.get("t_tracks") as Int
        val listening = guild.audioManager.connectedChannel!!.members
        for (m in listening) {
            val previousTime =  if (FindUserData(m.id).collection?.get("listening") == null) 0 else FindUserData(m.id).collection?.get("listening") as Int
            AddUserData(m.id, previousTime + track.duration)
        }
          AddUserData(requester.id, previousTotal + 1).totalTracks()
        if (endReason.mayStartNext) {
            // ws.post(TrackChangeEvent(lastPlay, queue.peek()))
            if (trackRepeat)
                player!!.startTrack(lastPlay!!.makeClone(),false)
            else
                nextTrack()
        }
    }
    /**
     * Track events...
     */
    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
      GlobalBus.post(
          PlayerEvents.PlayerEvent(player, "PLAYER_START", guild.audioManager.connectedChannel, guild))
            TrackEvents(player, track, tx).trackStart(queue)
    }
    /**
     * Sets for the queue to repeat or not.
     */
    fun queueRepeat(repeating: Boolean) {
        this.queueRepeat = repeating
    }
    /**
     * Sets for a track to repeat or not
     */
    fun trackRepeat(repeating: Boolean) {
        this.trackRepeat = repeating
    }
    /**
     * Returns if the queue is being repeated
     */
    fun queueRepeating(): Boolean {
        return queueRepeat
    }
    /**
     * Returns if a track is being repeated
     */
    fun trackRepeating(): Boolean {
        return trackRepeat
    }
    /**
     * Shuffles the current queue
     */
    fun shuffleQueue(): Queue<AudioTrack> {
        val list = queue.toList().shuffled()
        queue.clear()
        queue.addAll(list)
        return queue
    }
    /**
     * Removes a track from the queue
     */
    fun removeTrack(i: Int): AudioTrack {
        val list = queue.toMutableList()
        val removedTrack = list.removeAt(i-1)
        queue.clear()
        queue.addAll(list)
        return removedTrack
    }
    /**
     * Skips a set number of tracks from the queue
     */
    fun skipTracks(number: Int) {
        for (i in 0 until number) removeElement(0)
    }
    /** Clears the queue
     *
     */
    fun clearQueue() {
        queue.clear()
    }
    /**
     * Removes the set element from a queue and returns it
     */
    private fun removeElement(index: Int): AudioTrack? {
        val list = queue.toMutableList()
        val removeElement = list.removeAt(index)
        queue.clear()
        queue.addAll(list)
        return removeElement
    }
}