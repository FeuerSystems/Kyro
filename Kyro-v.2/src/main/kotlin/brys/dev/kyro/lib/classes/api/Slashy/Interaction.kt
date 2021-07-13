package brys.dev.kyro.lib.classes.api.Slashy

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class Interaction(private val builder: SlashCommandBuilder, val interactionToken: String, private  val token: String, private val id: String, private val interactionID: String) {

    private val callback_url = "https://discord.com/api/v8/interactions/$interactionID/$interactionToken/callback"
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private var callback = false

    fun callback(type: Int, value: String) : InteractionMessage? {
        if(callback) {
            return null
        }
        callback = true
        val i = JSONObject()
        i.put("type", type)
        if(value != null) {
            val data = JSONObject()
                    .put("tts", false)
                    .put("content", value)
                    .put("embeds", JSONArray())
                    .put("allowed_mentions", JSONArray())
            i.put("data", data)
        }
        val r = Request.Builder()
                .url(callback_url)
                .addHeader("Authorization", "Bot $token")
                .post(i.toString().toRequestBody(JSON))
        val call = client.newCall(r.build()).execute()
        val result = call.body!!.string()
        builder.checkIfError(result)
        val message = JSONObject(result)
        if(value != null) {
            return InteractionMessage(value, message.getString("id"), builder, this, true)
        } else {
            return null
        }
    }

    fun callback(type: Int) : Interaction {
        callback = true
        val i = JSONObject()
        i.put("type", type)
        val r = Request.Builder()
                .url(callback_url)
                .addHeader("Authorization", "Bot $token")
                .post(i.toString().toRequestBody(JSON))
        val call = client.newCall(r.build()).execute()
        val result = call.body!!.string()
        builder.checkIfError(result)
        return this
    }


    fun callback(type: Int, value: InteractionEmbed) : InteractionMessage? {
        if(callback) {
            return null
        }
        callback = true
        val i = JSONObject()
        i.put("type", type)
        if(value != null) {
            val data = JSONObject()
                    .put("tts", false)
                    .put("embeds", JSONArray().put(value.toJSONObject()))
                    .put("allowed_mentions", JSONArray())
            i.put("data", data)
        }
        val r = Request.Builder()
                .url(callback_url)
                .addHeader("Authorization", "Bot $token")
                .post(i.toString().toRequestBody(JSON))
        val call = client.newCall(r.build()).execute()
        val result = call.body!!.string()
        builder.checkIfError(result)
        if(value != null) {
            return InteractionMessage("", "", builder, this, true)
        } else {
            return null
        }
    }

    fun sendMessage(value: String) : InteractionMessage {
        val createURL = "https://discord.com/api/v8/webhooks/${builder.botID}/$interactionToken"
            val data = JSONObject()
                    .put("tts", false)
                    .put("content", value)
                    .put("embeds", JSONArray())
                    .put("allowed_mentions", JSONArray())
        val r = Request.Builder()
                .url(createURL)
                .addHeader("Authorization", "Bot $token")
                .post(data.toString().toRequestBody(JSON))
        val call = client.newCall(r.build()).execute()
        val result = call.body!!.string()
        builder.checkIfError(result)
        val message = JSONObject(result)
        return InteractionMessage(message.getString("content"), message.getString("id"), builder, this, false)
    }

    fun sendMessage(value: InteractionEmbed) : InteractionMessage {
        val createURL = "https://discord.com/api/v8/webhooks/${builder.botID}/$interactionToken"
        val data = JSONObject()
                .put("tts", false)
                .put("embeds", JSONArray().put(value.toJSONObject()))
                .put("allowed_mentions", JSONArray())
        val r = Request.Builder()
                .url(createURL)
                .addHeader("Authorization", "Bot $token")
                .post(data.toString().toRequestBody(JSON))
        val call = client.newCall(r.build()).execute()
        val result = call.body!!.string()
        builder.checkIfError(result)
        val message = JSONObject(result)
        return InteractionMessage(message.getString("content"), message.getString("id"), builder, this, false)
    }

}