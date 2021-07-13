package brys.dev.kyro.lib.classes.api.Slashy

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class SlashCommandGuild(val builder: SlashCommandBuilder, guild_id: String, bot: String, private val token: String) {

    private val okhttp = OkHttpClient()
    private val url = "https://discord.com/api/v8/applications/$bot/guilds/$guild_id/commands"
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
            for (any in array) {
                val json = any as JSONObject
                val command = SlashCommand(json.getString("name"), json.getString("description"))
                command.id = json.getLong("id").toString()
                commands.add(command)
            }
            result.close()
            return commands
        }


    fun registerGuildCommand(command: SlashCommand) {
        val request = Request.Builder()
                .addHeader("Authorization", "Bot $token")
                .url(url)
                .post(command.toRequestBody())

        val result = okhttp.newCall(request.build()).execute()
        val string = result.body!!.string()
        builder.checkIfError(string)
        result.close()
    }

    fun registerGuildCommands(vararg commands: SlashCommand) {
        for (command in commands) {
            registerGuildCommand(command)
        }
    }

    fun deleteGuildCommand(id: String) {
        val request = Request.Builder()
                .url("$url/$id")
                .addHeader("Authorization", "Bot $token")
                .delete()
        val result = okhttp.newCall(request.build()).execute()
        val string = result.body!!.string()
        builder.checkIfError(string)
        result.close()
    }

    fun deleteAllGuildCommands() {
        for (command in commands) {
            deleteGuildCommand(command.id)
        }
    }

    fun editGuildCommand(id: String, newGuildCommand: SlashCommand) {
        val request = Request.Builder()
                .url("$url/$id")
                .addHeader("Authorization", "Bot $token")
                .patch(newGuildCommand.toRequestBody())
        val result = okhttp.newCall(request.build()).execute()
        val string = result.body!!.string()
        builder.checkIfError(string)
        result.close()
    }

    fun getGuildCommand(id: String) : SlashCommand? {
        for (command in commands) {
            if(command.id == id) {
                return command
            }
        }
        return null
    }

}