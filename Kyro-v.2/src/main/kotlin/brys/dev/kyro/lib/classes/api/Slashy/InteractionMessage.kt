package brys.dev.kyro.lib.classes.api.Slashy

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class InteractionMessage(var content: Any, val id: String, private val builder: SlashCommandBuilder, val interaction: Interaction, val callback: Boolean) {


    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    fun edit(value: String) {
        this.content = value
        var createURL = "https://discord.com/api/v8/webhooks/${builder.botID}/${interaction.interactionToken}/messages/$id"
        if(callback) {
            createURL = "https://discord.com/api/v8/webhooks/${builder.botID}/${interaction.interactionToken}/messages/@original"
        }
        val data = JSONObject()
                .put("tts", false)
                .put("content", value)
                .put("embeds", JSONArray())
                .put("allowed_mentions", JSONArray())
        val r = Request.Builder()
                .url(createURL)
                .addHeader("Authorization", "Bot ${builder.token}")
                .patch(data.toString().toRequestBody(JSON))
        val call = client.newCall(r.build()).execute()
        val result = call.body!!.string()
        builder.checkIfError(result)
    }

    fun edit(value: InteractionEmbed) {
        this.content = value
        var createURL = "https://discord.com/api/v8/webhooks/${builder.botID}/${interaction.interactionToken}/messages/$id"
        if(callback) {
            createURL = "https://discord.com/api/v8/webhooks/${builder.botID}/${interaction.interactionToken}/messages/@original"
        }
        val data = JSONObject()
                .put("tts", false)
                .put("embeds", JSONArray().put(value.toJSONObject()))
                .put("allowed_mentions", JSONArray())
        val r = Request.Builder()
                .url(createURL)
                .addHeader("Authorization", "Bot ${builder.token}")
                .patch(data.toString().toRequestBody(JSON))
        val call = client.newCall(r.build()).execute()
        val result = call.body!!.string()
        builder.checkIfError(result)
    }

    fun delete() {
        var createURL = "https://discord.com/api/v8/webhooks/${builder.botID}/${interaction.interactionToken}/messages/$id"
        if(callback) {
            createURL = "https://discord.com/api/v8/webhooks/${builder.botID}/${interaction.interactionToken}/messages/@original"
        }
        val r = Request.Builder()
                .url(createURL)
                .addHeader("Authorization", "Bot ${builder.token}")
                .delete()
        val call = client.newCall(r.build()).execute()
        val result = call.body!!.string()
        builder.checkIfError(result)
    }

}