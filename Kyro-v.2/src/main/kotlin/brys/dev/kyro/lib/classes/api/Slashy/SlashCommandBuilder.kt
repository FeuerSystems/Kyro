package brys.dev.kyro.lib.classes.api.Slashy

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.RawGatewayEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import org.json.JSONException
import org.json.JSONObject

class SlashCommandBuilder (val jda: JDA, val botID: String, val token: String) {

    val listeners = ArrayList<SlashCommandListener>()

    fun getGuildCommandsFor(id: String) : SlashCommandGuild {
        return SlashCommandGuild(this, id, botID, token)
    }

    fun getGlobalCommands() : GlobalSlashCommands {
        return GlobalSlashCommands(this, botID, token)
    }

    fun addListener(listener: SlashCommandListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: SlashCommandListener) {
        listeners.remove(listener)
    }

    fun build() {
        jda.addEventListener(Listener(this))
    }

    fun checkIfError(string: String) {
        try {
            val ob = JSONObject(string)
            if(ob.has("code")) {
                throw SlashCommandError(string)
            }
        } catch(e: JSONException) {

        }

    }

    private class Listener(private val builder: SlashCommandBuilder)  {
        @SubscribeEvent
         fun onRawGateway(event: RawGatewayEvent) {
            if(event.type == "INTERACTION_CREATE") {
                val data = JSONObject(event.`package`.toString()).getJSONObject("d")
                val interactionToken = data.getString("token")
                val id = data.getString("id")
                val interaction = Interaction(builder, interactionToken, builder.token, builder.botID, id).callback(
                    5
                )
                for (listener in builder.listeners) {
                    val guild = builder.jda.getGuildById(data.getLong("guild_id")) ?: continue
                    var member: Member? = null
                    guild.retrieveMemberById(data.getJSONObject("member").getJSONObject("user").getLong("id")).queue() {m ->
                        member = m
                    }
                    val channel = guild.getTextChannelById(data.getLong("channel_id"))
                    val command = builder.getGuildCommandsFor(guild.id).getGuildCommand(data.getJSONObject("data").getString("id").toLong().toString())
                    val args = ArrayList<SlashCommandArgument>()
                    var subcommand: SlashSubCommand? = null
                    try {
                        val options = data.getJSONObject("data").getJSONArray("options")
                        for (option in options) {
                            val op = option as JSONObject
                            if(op.has("value")) { //check if the option is not a subcommand or subcommand group
                                args.add(SlashCommandArgument(op.getString("name"), op.get("value")))
                            } else { //the option is a subcommand or subcommand group
                                for (sub in op.getJSONArray("options")) {
                                    val s = sub as JSONObject
                                    if(s.has("value")) { //check if option is a subcommand
                                        subcommand = SlashSubCommand(op.getString("name"), null)
                                        args.add(SlashCommandArgument(s.getString("name"), s.get("value"))) //add the arguments of the command
                                    } else { //the option must be a subcommand group
                                        for (any in s.getJSONArray("options")) { //iterate through the arguments of the command in the command group
                                            val g = any as JSONObject
                                            args.add(SlashCommandArgument(g.getString("name"), g.get("value")))
                                        }
                                        subcommand = SlashSubCommand(s.getString("name"), op.getString("name"))
                                    }
                                }
                            }
                        }
                    } catch(ex: JSONException) {

                    }
                    if(member != null && channel != null && command != null) {
                        listener.run(SlashCommandEvent(member!!, channel, command, args, subcommand, interaction))
                    }
                }
            }
        }
    }

}