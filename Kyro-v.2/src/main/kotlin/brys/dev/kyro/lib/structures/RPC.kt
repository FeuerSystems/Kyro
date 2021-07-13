package brys.dev.kyro.lib.structures

import brys.dev.kyro.lib.interfaces.TrackWSChangeEvent
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import net.dv8tion.jda.api.entities.User
import org.json.JSONObject

class RPC(val router: Routing): TrackWSChangeEvent{
    override fun track(before: AudioTrack?, after: AudioTrack?) {
        val json = JSONObject()
        val beforeTrack = JSONObject()
        val beforeTrackRequester = before?.userData as User?
        beforeTrack.put("name", before?.info?.title)
        beforeTrack.put("author", before?.info?.author)
        beforeTrack.put("duration", before?.duration)
        beforeTrack.put("requester", beforeTrackRequester?.id)
        val afterTrack = JSONObject()
        val afterTrackRequester = after!!.userData as User
        afterTrack.put("name", after.info.title)
        afterTrack.put("author", after.info.author)
        afterTrack.put("duration", after.duration)
        afterTrack.put("requester", afterTrackRequester.id)
        json.put("op", 2)
        json.put("before", beforeTrack)
        json.put("after", after)
        router {
            webSocket("/rpc") {
                val json = JSONObject()
                json.put("op", 1)
                json.put("data", "HELLO")
                this.send(Frame.Text(json.toString()))
            }
        }
    }
}