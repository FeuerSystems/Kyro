package brys.dev.kyro.lib.classes.api.Slashy

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

open class SlashCommand {

    var options: Array<SlashCommandOption>
        private set
    var id: String = ""
    var name: String
        private set
    var description: String
        private set

    constructor(name: String, description: String, vararg command_options: SlashCommandOption) {
        this.name = name
        this.description = description
        this.options = command_options.toList().toTypedArray()
    }

    constructor(command: SlashCommand) {
        this.options = command.options.toList().toTypedArray()
        this.name = command.name
        this.description = command.description
        this.id = command.id
    }

    fun toRequestBody() : RequestBody {
        val JSON = "application/json; charset=utf-8".toMediaType()
        val commandObject  = JSONObject()
        commandObject.put("name", name)
        commandObject.put("description", description)

        val commandOptions = JSONArray()

        for (command_option in options) {
            val option = JSONObject()
            option.put("name", command_option.name)
            option.put("description", command_option.description)
            option.put("type", command_option.type)
            if(command_option.type != 1 && command_option.type != 2) {
                option.put("required", command_option.required)
            }

            if(!command_option.hasSubOptions && command_option.choices.size != 0) { //check if it's just a command
                val choices = JSONArray()

                for (choice in command_option.choices) {
                    val new_choice = JSONObject()
                    new_choice.put("name", choice.name)
                    new_choice.put("value", choice.value)
                    choices.put(new_choice)
                }
                option.put("choices", choices)
            } else if(command_option.type == 1) { //check if its a subcommand
                val suboptions = JSONArray() //the options from the command
                for (s in command_option.suboptions) { //iterate through the options of the subcommand
                    val n = JSONObject()
                    n.put("name", s.name)
                    n.put("description", s.description)
                    n.put("type", s.type)

                    if(!s.hasSubOptions && s.choices.size != 0) { //check if the option has choices
                        val subChoices = JSONArray()

                        for (choice in command_option.choices) {
                            val newChoice = JSONObject()
                            newChoice.put("name", choice.name)
                            newChoice.put("value", choice.value)
                            subChoices.put(newChoice)
                        }
                        n.put("choices", subChoices)
                    }
                    suboptions.put(n)
                }
                option.put("options", suboptions)
            } else if(command_option.type == 2) { //check if its a subcommand group
                val subcmds = JSONArray()
                for (subcommand in command_option.suboptions) { //iterate through the subcommands of the subcommand group
                    //create a new command object
                    val cmd = JSONObject()
                    cmd.put("name", subcommand.name)
                    cmd.put("description", subcommand.description)
                    cmd.put("type", subcommand.type)
                    val subargs = JSONArray() //the options of the subcommand
                    for (arg in subcommand.suboptions) { //iterate through the options of the subcommand
                        //add the option
                        val argument = JSONObject()
                        argument.put("name", arg.name)
                        argument.put("description", arg.required)
                        argument.put("type", arg.type)
                        argument.put("required", arg.required)
                        val choices = JSONArray()
                        for (choice in arg.choices) { //check if the option has choices
                            val new_choice = JSONObject()
                            new_choice.put("name", choice.name)
                            new_choice.put("value", choice.value)
                            choices.put(new_choice)
                        }
                        argument.put("choices", choices)
                        subargs.put(argument)
                    }
                    cmd.put("options", subargs)
                    subcmds.put(cmd)
                }
                option.put("options", subcmds)
            }
            commandOptions.put(option)
        }
        commandObject.put("options", commandOptions)
        return commandObject.toString().toRequestBody(JSON)
    }

    class Builder {

        var name: String? = null
            private set
        var description: String? = null
            private set
        val options: ArrayList<SlashCommandOption> = ArrayList()

        fun setName(name: String) : Builder {
            this.name = name
            return this
        }

        fun setDescription(description: String) : Builder {
            this.description = description
            return this
        }

        fun appendDescription(description: String) : Builder {
            this.description += description
            return this
        }

        fun addOption(option: SlashCommandOption) : Builder {
            options.add(option)
            return this
        }

        fun removeOption(option: SlashCommandOption) : Builder {
            options.remove(option)
            return this
        }

        fun build() : SlashCommand? {
            if(name != null && description != null) {
                return SlashCommand(name!!, description!!, *options.toTypedArray())
            } else {
                return null
            }
        }

    }

}