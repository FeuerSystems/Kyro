package brys.dev.kyro.lib.structures

import brys.dev.kyro.lib.classes.api.YouTube
import brys.dev.kyro.lib.classes.db.FindUserData
import brys.dev.kyro.lib.classes.events.Events
import brys.dev.kyro.lib.classes.events.RouterEvents
import brys.dev.kyro.lib.classes.events.dashevents.core.WSEvents
import brys.dev.kyro.lib.classes.events.dashevents.events.UserJoinedChannelEvent
import brys.dev.kyro.lib.interfaces.QueueWSChangeEvent
import brys.dev.kyro.lib.interfaces.TrackWSChangeEvent
import brys.dev.kyro.lib.model.DataPlaylist
import brys.dev.kyro.lib.model.PlayerManager
import brys.dev.kyro.lib.structures.Util.Misc.retrieveVideoJSON
import brys.dev.kyro.lib.structures.config.Config
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import net.dv8tion.jda.api.JDA
import java.io.File
import java.util.*
import com.github.ajalt.mordant.TermColors
import com.google.gson.Gson
import io.ktor.features.*
import io.ktor.request.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.dv8tion.jda.api.Permission
import org.json.JSONObject
import org.json.simple.parser.JSONParser
import java.lang.Exception
import java.time.Duration
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Router(val wsEvents: WSEvents) {
    val queueC = HashMap<String, QueueWSChangeEvent.QueueWSChangeEvent>()
    val nowPlaying = HashMap<String, TrackWSChangeEvent.TrackWSChangeEvent>()
    val queueListeners = mutableListOf<QueueWSChangeEvent>()
    val nowPlayingListeners = mutableListOf<TrackWSChangeEvent>()
    val userJoinedEvents = mutableListOf<UserJoinedChannelEvent>()

    private val gson = Gson()

    @ObsoleteCoroutinesApi
    fun createAPI(arr: MutableList<JDA>, auth: String, events: Events) {
        val router = RouterEvents()
        val c = TermColors()
        val info = StringBuilder()
        val guilds = StringBuilder()
        val queueJSON = StringBuilder()
        val nowPlayingJSON = StringBuilder()
        val isComma = if (arr.size >= 0) "," else ""
        val trackCache = HashMap<String, JSONObject>()
        fun flush() {
            info.setLength(0); guilds.setLength(0); queueJSON.setLength(0); nowPlayingJSON.setLength(0)
        }
        embeddedServer(Netty, Config.API.port.toInt()) {
            routing {
                install(WebSockets)
                install(CORS) {
                    method(HttpMethod.Get)
                    method(HttpMethod.Post)
                    method(HttpMethod.Put)
                    method(HttpMethod.Delete)
                    method(HttpMethod.Patch)
                    header(HttpHeaders.AccessControlAllowHeaders)
                    header(HttpHeaders.ContentType)
                    header(HttpHeaders.AccessControlAllowOrigin)
                    allowCredentials = true
                    anyHost()
                    maxAge = Duration.ofDays(1)
                }
                get("/api/status") {
                    info.append("{")
                    for (i in 0 until arr.size) {
                        val instance = arr[i]
                        for (d in 0 until instance.guildCache.size().toInt()) {
                            val guild = instance.guildCache.asList()[d]
                            val isGuildComma = if (guild == instance.guildCache.last()) "" else ","
                            guilds.append("\"${guild.id}\"$isGuildComma")
                        }
                        if (i == arr.size - 1) {
                            info.append(
                                "\"$i\": [{\"status\":\"${instance.status.name}\", \"guilds\": [$guilds], \"cache_users\": ${instance.userCache.size()}, \"init_users\": ${events.members},  \"requests\": ${instance.responseTotal}, \"vc\": ${instance.audioManagerCache.size()}, \"ping\": ${instance.gatewayPing}, \"ram_used\": \"${
                                    Util.Misc.getSize(
                                        Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                                    )
                                }\"}]"
                            )
                            guilds.setLength(0);
                        } else
                            info.append(
                                "\"$i\": [{\"status\":\"${instance.status.name}\", \"guilds\": [$guilds], \"cache_users\": ${instance.userCache.size()}, \"init_users\": ${events.members}, \"requests\": ${instance.responseTotal}}, \"vc\": ${instance.audioManagerCache.size()}, \"ping\": ${instance.gatewayPing}, \"ram_used\": \"${
                                    Util.Misc.getSize(
                                        Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                                    )
                                }\"]$isComma"
                            )
                        guilds.setLength(0);
                    }
                    info.append("}")
                    call.respondText(
                        status = HttpStatusCode.OK,
                        contentType = ContentType.Application.Json,
                        provider = { info.toString() }); flush()
                }
                get("/bot/log") {
                    val authentication = call.parameters["auth"]
                    router.log("${c.brightBlue.bg} ${c.black}- ${c.reset} ${c.white}- Request was made for ${c.brightMagenta}/bot/log${c.reset}")
                    if (authentication == null) {
                        return@get call.respond(HttpStatusCode.BadRequest, "Authentication Required.")
                    } else if (authentication != auth) {
                        return@get call.respond(HttpStatusCode.BadRequest, "Incorrect authentication")
                    }
                    call.respondText(
                        File("assets/html/log.html").readText().replace("DATA", File("./bot/log.txt").readText()),
                        ContentType.Text.Html
                    )
                }
                get("/user/playlists") {
                    val id = call.parameters["id"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        JSONObject().put("error", "User id must be a requirement").toString()
                    )
                    val user = FindUserData(id).collection ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        JSONObject().put("error", "User was not found on server").toString()
                    )
                    val playlist = user["playlists"]
                    val array = playlist as ArrayList<*>
                    val playlists = mutableListOf<DataPlaylist>()
                    val rawPlaylists = mutableListOf<JSONObject>()
                    val obj = JSONObject()
                    for (i in 0 until array.size) {
                        val arr = array[i]
                        val json = gson.toJson(arr)
                        val data = gson.fromJson(json, DataPlaylist::class.java)
                        playlists.add(data)
                    }
                    for (pl in playlists) {
                        val playlistData = JSONObject()
                        playlistData.put("img", pl.thumbnail)
                        playlistData.put("songs", pl.songs)
                        playlistData.put("name", pl.name)
                        rawPlaylists.add(playlistData)
                    }
                    obj.put("playlists", rawPlaylists)
                    call.respondText(
                        status = HttpStatusCode.Accepted,
                        provider = { obj.toString() },
                        contentType = ContentType.Application.Json
                    )
                }
                get("/context/vc") {
                    val uid = call.parameters["id"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        JSONObject().put("error", "User id must be a requirement").toString()
                    )
                    val obj = JSONObject()
                    var exists: Boolean = false
                    var botConnection: Boolean = false
                    for (i in arr.first().guildCache) {
                        val member = i.memberCache.getElementById(uid)
                        if (member?.voiceState?.inVoiceChannel() == true) {
                            exists = true
                            if (member.voiceState!!.channel!!.members.contains(member.guild.selfMember)) botConnection =
                                true
                            obj.put("guild", i.id).put(
                                "vc",
                                JSONObject().put("name", member.voiceState!!.channel!!.name)
                                    .put("id", member.voiceState!!.channel!!.id).put("user", member.id)
                            )
                        }
                    }
                    obj.put("exists", exists)
                    obj.put("connected", botConnection)
                    call.respondText(
                        status = HttpStatusCode.Accepted,
                        provider = { obj.toString() },
                        contentType = ContentType.Application.Json
                    )
                }
                get("/can/vc") {
                    val vcID = call.parameters["vc"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        JSONObject().put("error", "Voice channel must be a requirement").toString()
                    )
                    val voiceChannel = arr.first().voiceChannelCache.getElementById(vcID)
                    var canJoin = false
                    val obj = JSONObject()
                    if (voiceChannel?.permissionOverrides?.isNotEmpty() == true) {
                        voiceChannel.permissionOverrides.forEach { p ->
                            if (p.allowed.contains(Permission.VOICE_SPEAK)) {
                                canJoin = true
                            }
                        }
                    }
                    if (voiceChannel?.guild?.selfMember?.hasPermission(Permission.VOICE_SPEAK) == true && voiceChannel.guild.selfMember.hasPermission(
                            Permission.VOICE_CONNECT
                        )
                    ) {
                        canJoin = true
                    }
                    obj.put("vc", vcID).put("joinable", canJoin).put("guild", voiceChannel?.guild?.id)
                    call.respondText(
                        status = HttpStatusCode.Accepted,
                        provider = { obj.toString() },
                        contentType = ContentType.Application.Json
                    )
                }
                post("/vc/join") {
                    val vc = call.parameters["vc"] ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        JSONObject().put("error", "Voice channel must be a requirement").toString()
                    )
                    val user = call.parameters["uid"] ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        JSONObject().put("error", "User id must be a requirement").toString()
                    )
                    val vcObj = arr.first().voiceChannelCache.getElementById(vc) ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        JSONObject().put("error", "Voice channel does not exist").toString()
                    )
                    val audioManager = vcObj.guild.audioManager
                    val members = ArrayList<JSONObject>()
                    if (vcObj.guild.memberCache.getElementById(user) == null) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            JSONObject().put("error", "User doesn't exist in this guild").toString()
                        )
                    }
                    if (vcObj.guild.memberCache.getElementById(user)!!.voiceState?.channel == null || vcObj.guild.memberCache.getElementById(
                            user
                        )!!.voiceState?.channel != vcObj
                    ) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            JSONObject().put("error", "User must be connected to channel").toString()
                        )
                    }
                    if (audioManager.connectedChannel != vcObj) {
                        try {
                            audioManager.openAudioConnection(vcObj)
                            audioManager.isSelfDeafened = true
                        } catch (audioException: Exception) {
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                JSONObject().put("error", "Something broke when trying to connect").toString()
                            )
                        }
                    }
                    for (m in vcObj.members) {
                        members.add(
                            JSONObject().put("name", m.effectiveName).put("id", m.id)
                                .put("avatar", m.user.effectiveAvatarUrl).put(
                                "color",
                                String.format(
                                    "#%02x%02x%02x",
                                    m.roles.first().color?.red,
                                    m.roles.first().color?.green,
                                    m.roles.first().color?.green
                                )
                            )
                        )
                    }
                    call.respondText(
                        status = HttpStatusCode.Accepted,
                        provider = {
                            JSONObject().put(
                                "vc",
                                JSONObject().put("id", vcObj.id).put("name", vcObj.name).put("bitrate", vcObj.bitrate)
                                    .put("members", members)
                            ).toString()
                        },
                        contentType = ContentType.Application.Json
                    )
                }
                get("/stats") {
                    val id = call.parameters["id"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        JSONObject().put("error", "User id must be a requirement").toString()
                    )
                    val obj = JSONObject()
                    val totalTracks =
                        if (FindUserData(id).collection?.get("t_tracks") == null) 0 else FindUserData(id).collection?.get(
                            "t_tracks"
                        ) as Int
                    val totalQueued =
                        if (FindUserData(id).collection?.get("t_queued") == null) 0 else FindUserData(id).collection?.get(
                            "t_queued"
                        ) as Int
                    obj.put("total_tracks", totalTracks).put("total_queued", totalQueued)
                    call.respondText(
                        status = HttpStatusCode.Accepted,
                        provider = { obj.toString() },
                        contentType = ContentType.Application.Json
                    )
                }

                get("/player") {
                    val guild = call.parameters["id"] ?: return@get call.respond(
                        status = HttpStatusCode.BadRequest,
                        JSONObject().put("message", "guild id required!").toString()
                    )
                    val member = call.parameters["m"] ?: return@get call.respond(
                        status = HttpStatusCode.BadRequest,
                        JSONObject().put("message", "member id required!").toString()
                    )
                    val guildObj = arr.first().getGuildById(guild) ?: return@get call.respond(
                        status = HttpStatusCode.BadRequest,
                        JSONObject().put("message", "guild was not found!").toString()
                    )
                    val memberObj = guildObj.getMemberById(member) ?: return@get call.respond(
                        status = HttpStatusCode.BadRequest,
                        JSONObject().put("message", "member was not found!").toString()
                    )
                    val token = call.parameters["auth"] ?: return@get call.respond(
                        status = HttpStatusCode.Forbidden,
                        JSONObject().put("message", "Authorization header required!").toString()
                    )
                    if (!Util.Misc.validateUser(
                            token,
                            member
                        )
                    ) return@get call.respond(
                        status = HttpStatusCode.Forbidden,
                        JSONObject().put("message", "Authorization invalid!").toString()
                    )
                    val player = PlayerManager.instance?.getGuildManager(guildObj, null, memberObj.user)
                    val genericPlayer =
                        PlayerEvents.GenericPlayer(player!!.player, player.trackManager!!.queue, guildObj)
                    call.respond(HttpStatusCode.Found, genericPlayer.toString())
                }
                post("/player/play") {
                    val guild = call.parameters["id"] ?: return@post call.respond(status = HttpStatusCode.BadRequest, JSONObject().put("message", "guild id required!").toString())
                    val member = call.parameters["m"] ?: return@post call.respond(status = HttpStatusCode.BadRequest, JSONObject().put("message", "member id required!").toString())
                    val playRequest = call.receiveText()
                    val guildObj = arr.first().getGuildById(guild) ?: return@post call.respond(status = HttpStatusCode.BadRequest, JSONObject().put("message", "guild was not found!").toString())
                    val memberObj = guildObj.getMemberById(member) ?: return@post call.respond(status = HttpStatusCode.BadRequest, JSONObject().put("message", "member was not found!").toString())
                    val player = PlayerManager.instance?.getGuildManager(guildObj, null, memberObj.user)
                    val token = call.parameters["auth"] ?: return@post call.respond(status = HttpStatusCode.Forbidden, JSONObject().put("message", "Authorization header required!").toString())
                    if (!Util.Misc.validateUser(token, member)) return@post call.respond(status = HttpStatusCode.Forbidden, JSONObject().put("message", "Authorization invalid!").toString())
                    if (playRequest.isEmpty()) {
                        if (player?.player?.isPaused == true && player.player.playingTrack != null) {
                            player.player.isPaused = false
                            return@post call.respond(HttpStatusCode.OK, PlayerEvents.PlayerEvent(player.player, "resume", guildObj.voiceChannelCache.first(), guildObj).toString())
                        } else {
                            return@post call.respond(
                                HttpStatusCode.InternalServerError,
                                JSONObject().put("message", "Can't do that").toString()
                            )
                        }
                    } else if (playRequest.isNotEmpty()) {
                        val data = JSONParser().parse(playRequest) as org.json.simple.JSONObject
                        val urlObj = data["url"] ?: return@post call.respond(HttpStatusCode.BadRequest, JSONObject().put("message", "url can't be null!").toString())
                        val url = urlObj as String
                        Player.queue(guildObj.textChannelCache.first(), memberObj.user, false, null, null, listOf(url), wsEvents)
                        return@post call.respond(HttpStatusCode.OK,  JSONObject().put("message", "playing").toString())
                    }
                }
                post("/player/pause") {
                    val guild = call.parameters["id"] ?: return@post call.respond(
                        status = HttpStatusCode.BadRequest,
                        JSONObject().put("message", "guild id required!").toString()
                    )
                    val member = call.parameters["m"] ?: return@post call.respond(
                        status = HttpStatusCode.BadRequest,
                        JSONObject().put("message", "member id required!").toString()
                    )
                    val guildObj = arr.first().getGuildById(guild) ?: return@post call.respond(
                        status = HttpStatusCode.NotFound,
                        JSONObject().put("message", "guild was not found!").toString()
                    )
                    val memberObj = guildObj.getMemberById(member) ?: return@post call.respond(
                        status = HttpStatusCode.NotFound,
                        JSONObject().put("message", "member was not found!").toString()
                    )
                    val token = call.parameters["auth"] ?: return@post call.respond(
                        status = HttpStatusCode.Forbidden,
                        JSONObject().put("message", "Authorization header required!").toString()
                    )
                    if (!Util.Misc.validateUser(
                            token,
                            member
                        )
                    ) return@post call.respond(
                        status = HttpStatusCode.Forbidden,
                        JSONObject().put("message", "Authorization invalid!").toString()
                    )
                    val player = PlayerManager.instance?.getGuildManager(guildObj, null, memberObj.user)
                    player?.player?.isPaused = true;
                    return@post call.respond(HttpStatusCode.OK, JSONObject().put("message", "paused").toString())
                }
                get("/track") {
                    val track = call.parameters["query"] ?: return@get call.respond(status = HttpStatusCode.BadRequest, JSONObject().put("message", "Track query required!").toString());
                    val authentication = call.parameters["auth"] ?: return@get call.respond(status = HttpStatusCode.Forbidden, JSONObject().put("message", "Authentication required!").toString())
                    val user = call.parameters["user"] ?: call.respond(status = HttpStatusCode.Forbidden, JSONObject().put("message", "User id required!").toString())
                    if (!Util.Misc.validateUser(authentication, user as String)) return@get call.respond(status = HttpStatusCode.Unauthorized, JSONObject().put("message", "Authentication invalid").toString())
                    if (trackCache.containsKey(track)) return@get call.respondText(status = HttpStatusCode.Found, provider = { trackCache[track].toString() }, contentType = ContentType.Application.Json)
                    val yt = YouTube(Config.music.YTToken)
                    val tracks = yt.rawQuery(track, 5);

                    val tracksObjects = ArrayList<JSONObject>()
                    if (tracks != null) {
                        for (ta in tracks) {
                            val duration = yt.temp?.videos()?.list("id")?.setId(ta.id.videoId)?.setFields("items(contentDetails)")?.setMaxResults(1)?.setKey(Config.music.YTToken)?.execute()
                            tracksObjects.add(
                                JSONObject()
                                    .put("title", ta.snippet.title)
                                    .put("channel_name", ta.snippet.channelTitle)
                                    .put("url", "https://www.youtube.com/watch?v=${ta.id.videoId}")
                                    .put("thumbnail_720", "https://i.ytimg.com/vi/${ta.id.videoId}/hq720.jpg")
                                    .put("thumbnail_480", "https://i.ytimg.com/vi/${ta.id.videoId}/sddefault.jpg")
                                    .put("thumbnail_1080", "https://i.ytimg.com/vi/${ta.id.videoId}/maxresdefault.jpg")
                                    .put("duration", "duration=${duration?.items?.get(0)?.contentDetails?.duration}")
                            )
                        }
                    }
                    trackCache[track] = JSONObject().put("tracks", tracksObjects)
                    call.respondText(
                        status = HttpStatusCode.Found,
                        provider = { JSONObject().put("tracks", tracksObjects).toString() },
                        contentType = ContentType.Application.Json
                    )
                }
                val registrators = ArrayList<String>()
                val connections = ArrayList<String>()
            }
        }.start(wait = true)
    }
    inner class RouterConstructor {
        var Bot: MutableList<JDA> = mutableListOf()
        var event: Events? = null
        private lateinit var auth: String
        fun setBot(bot: JDA) {
            Bot.add(bot)
        }

        fun setAuth(str: String) {
            auth = str
        }

        fun setEvents(events: Events) {
            event = events
        }
        @ObsoleteCoroutinesApi
        fun build() {
            if (Bot.isEmpty()) {
                throw NegativeArraySizeException("The JDAStatus server must have at least 1 bot in order to build.")
            }
            if (auth.isEmpty()) {
                throw InstantiationException("The JDAStatus server requires unique authentication string.")
            }
            if (event == null) {
                throw NullPointerException("Events must not be null!")
            }
            createAPI(Bot, auth, event!!)
        }
    }
}


