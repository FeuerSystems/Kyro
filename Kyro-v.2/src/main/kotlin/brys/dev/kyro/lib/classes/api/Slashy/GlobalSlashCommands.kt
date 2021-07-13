package brys.dev.kyro.lib.classes.api.Slashy

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class GlobalSlashCommands(val builder: SlashCommandBuilder, bot: String, private val token: String) {

    private val okhttp = OkHttpClient()
    private val url = "https://discord.com/api/v8/applications/$bot/commands"
    private val JSON = "application/json; charset=utf-8".toMediaType()

    val commands: ArrayList<SlashCommand>
        get() {
            val commands = ArrayList<SlashCommand>()
            val builder = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bot $token")
                    .get()
                    .build()
            val result = okhttp.newCall(builder).execute()
            val string = result.body!!.string()
            this.builder.checkIfError(string)
            val array = JSONArray(string)
            result.close()
            for (any in array) {
                val json = any as JSONObject
                val command = SlashCommand(json.getString("name"), json.getString("description"))
                command.id = json.getLong("id").toString()
                commands.add(command)
            }
            return commands
        }


    fun registerCommand(command: SlashCommand) {
        val request = Request.Builder()
                .addHeader("Authorization", "Bot $token")
                .url(url)
                .post(command.toRequestBody())
        val result = okhttp.newCall(request.build()).execute()
        val string = result.body!!.string()
        builder.checkIfError(string)
        result.close()
    }

    fun registerCommands(vararg commands: SlashCommand) {
        for (command in commands) {
            registerCommand(command)
        }
    }

    fun deleteCommand(id: String) {
        val request = Request.Builder()
                .url("$url/$id")
                .addHeader("Authorization", "Bot $token")
                .delete()
        val result = okhttp.newCall(request.build()).execute()
        val string = result.body!!.string()
        builder.checkIfError(string)
        result.close()
    }

    fun deleteAllCommands() {
        for (command in commands) {
            deleteCommand(command.id)
        }
    }

    fun editCommand(id: String, newSlashCommand: SlashCommand) {
        val request = Request.Builder()
                .url("$url/$id")
                .addHeader("Authorization", "Bot $token")
                .patch(newSlashCommand.toRequestBody())
        val result = okhttp.newCall(request.build()).execute()
        val string = result.body!!.string()
        builder.checkIfError(string)
        result.close()
    }


    fun getCommand(id: String): SlashCommand? {
        for (command in commands) {
            if (command.id == id) {
                return command
            }
        }
        return null
    }
}