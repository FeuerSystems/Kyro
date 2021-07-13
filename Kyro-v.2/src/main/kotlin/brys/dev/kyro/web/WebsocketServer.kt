package brys.dev.kyro.web

import brys.dev.kyro.lib.interfaces.WS.SubscribeToEvent
import brys.dev.kyro.lib.interfaces.WS.enum.SubscribableEvents
import brys.dev.kyro.lib.methods.Logger
import brys.dev.kyro.lib.structures.PlayerEvents
import brys.dev.kyro.lib.structures.Util
import brys.dev.kyro.lib.structures.VCEvents
import com.google.gson.Gson
import io.javalin.Javalin
import io.javalin.websocket.WsConnectContext
import me.kosert.flowbus.EventsReceiver
import me.kosert.flowbus.subscribe
import org.json.JSONObject
import org.json.simple.parser.JSONParser
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class WebsocketServer {
    var server: Javalin? = null;
    val logger = Logger(this.javaClass)
    val r = EventsReceiver()
    val events = VCEvents
    val clients = ConcurrentLinkedQueue<WsConnectContext>()
    val clientCtx = HashMap<String, ClientContext>()
    val gson = Gson()
    fun start(port: Int): WebsocketServer {
        server = server!!.start(port)
        return this;
    }
    fun initwebsocket() {

        server!!.ws("/dus") { handler ->
            handler.onConnect { client ->
                client.send(JSONObject().put("message", "Your connected to Kyro!").toString())
                logger.info("Client connected to websocket -> ${client.session.remoteAddress}")
                clients.add(client)
            }
            handler.onClose { client ->
                logger.info("Client disconnected -> ${client.reason()}")
                clientCtx.remove(client.sessionId)
            }
            handler.onMessage { msg ->
                // If this isn't JSON lets ignore it..
                if (!Util.Misc.isJSON(msg.message())) {
                    msg.send(JSONObject().put("message", "This websocket only supports JSON type responses!").toString())
                    return@onMessage
                }
                val rawJson = JSONParser().parse(msg.message()) as org.json.simple.JSONObject
                if (rawJson["type"] == "ping") {
                    msg.send(JSONObject().put("type", "pong").toString())
                    return@onMessage
                }
                if (rawJson["special"] != null) {
                    val client = clientCtx[msg.sessionId]
                    client?.specials?.add(rawJson["special"] as String)
                    client?.user = rawJson["user"] as String
                    client?.guild = rawJson["guild"] as String
                    msg.send(JSONObject().put("type", "Special Registered").put("embed", "${rawJson["special"]} was registered to the websocket!").toString())
                    return@onMessage
                }
                /**
                 * Luckily, we have a stupid simple websocket handler for messages.
                 * Since this only allows *one* type of object to be sent (**[SubscribeToEvent]**) we can just only look for that type!
                 */
                val event = gson.fromJson(msg.message(), SubscribeToEvent::class.java)
                /**
                 * If the event type is invalid for what we have we'll send the client a message along with the types it can pick..
                 */
                if (!getNames(SubscribableEvents::class.java).contains(event.events.first())) {
                    msg.send(JSONObject().put("message", "That Event type doesn't exist!").put("types", getNames(SubscribableEvents::class.java)).toString())
                    return@onMessage
                }
                /**
                 * Now lets check this authenication is *actually* valid
                 */

                    if (!Util.Misc.validateUser(event.token, event.user)) {
                        msg.send(JSONObject().put("message", "INVALID AUTH").toString())
                       return@onMessage
                    }

                /**
                 * Lets look for existing events if any lets keep them and add the rest!
                 */
                clientCtx[msg.sessionId] = ClientContext(event.events, event.token, event.user, clientCtx[msg.sessionId]?.specials, null)
                msg.send(JSONObject().put("Register", "Events '${event.events}' registered!").toString())
            }
        }
    }

    init {
         server = Javalin.create { config ->
            run {
                config.enableCorsForAllOrigins()
                config.showJavalinBanner = false
            }
        }
        /**
         * JDA Events, USER SPECIFIC EVENTS
         */
        r.subscribe { event: VCEvents.VCJOIN ->
                for (client in clients) {
                    if (clientCtx[client.sessionId]?.events?.contains("VC") == true && event.event.member.id == clientCtx[client.sessionId]?.user) {
                        client.send(event.toString())
                        return@subscribe
                    }
                }
            }
        r.subscribe { event: VCEvents.VCLEAVE ->
            for (client in clients) {
                if (clientCtx[client.sessionId]?.events?.contains("VC") == true && event.event.member.id == clientCtx[client.sessionId]?.user) {
                    client.send(event.toString())
                    return@subscribe
                }
            }
        }
        r.subscribe { event: VCEvents.VCCHANGE ->
            for (client in clients) {
                if (event.event.member.id == clientCtx[client.sessionId]?.user && clientCtx[client.sessionId]?.specials?.contains("VC") == true) {
                    client.send(event.toString())
                    return@subscribe
                }
            }
        }
        r.subscribe { event: PlayerEvents.PlayerEvent ->
            for (client in clients) {
                if (clientCtx[client.sessionId]?.events?.contains("PLAYER") == true && clientCtx[client.sessionId]?.user?.let {
                        event.guild.getMemberById(
                            it
                        )
                    } != null) {
                    client.send(event.toString())
                    return@subscribe
                }
            }
        }
    }
}

fun getNames(e: Class<out Enum<*>>): MutableList<String> {
    val names = mutableListOf<String>()
    for (enum in e.enumConstants) {
        names.add(enum.name)
    }
    return names
}
data class ClientContext(var events: ArrayList<String>, var auth: String, var user: String, var specials: ArrayList<String>?, var guild: String?)
