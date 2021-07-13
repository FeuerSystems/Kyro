package brys.dev.kyro.lib.classes.api.Slashy

/**
 * @param name The name of the argument
 * @param description A short description
 * @param required Whether the argument is required or not
 * @param type The type of the argument (User, Channel, Integer, String...)
 * @param option_choices Specify choices so the user can only select one of them
 */
class SlashCommandOption() {

    var name = ""
        private set
    var description = ""
        private set
    var required = true
        private set
    var type = -1
        private set
    val choices = ArrayList<SlashCommandOptionChoice>()
    val suboptions = ArrayList<SlashCommandOption>()
    var hasSubOptions = false
        private set

    constructor(name: String, description: String, required: Boolean, type: Int, vararg choices: SlashCommandOptionChoice) : this() {
        this.name = name
        this.description = description
        this.required = required
        this.type = type
        this.choices.addAll(choices)
    }

    constructor(name: String, description: String, required: Boolean, type: Int, vararg sub_options: SlashCommandOption) : this() {
        this.name = name
        this.description = description
        this.required = required
        this.type = type
        this.suboptions.addAll(sub_options)
        this.hasSubOptions = true
    }

    constructor(name: String, description: String, required: Boolean, type: Int) : this() {
        this.name = name
        this.description = description
        this.required = required
        this.type = type
        this.hasSubOptions = true
    }

    class Builder {

        var name: String? = null
            private set
        var description: String? = null
            private set
        var required: Boolean = false
            private set
        var type: Int? = null
            private set
        val choices: ArrayList<SlashCommandOptionChoice> = ArrayList()
        val suboptions: ArrayList<SlashCommandOption> = ArrayList()

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

        fun setRequired(required: Boolean) : Builder {
            this.required = required
            return this
        }

        fun setType(type: Int) : Builder {
            this.type = type
            return this
        }

        fun addChoice(choice: SlashCommandOptionChoice) : Builder {
            choices.add(choice)
            return this
        }

        fun removeChoice(choice: SlashCommandOptionChoice) : Builder {
            choices.remove(choice)
            return this
        }

        fun addSubOption(s: SlashCommandOption) : Builder {
            suboptions.add(s)
            return this
        }

        fun removeSubOption(s: SlashCommandOption) : Builder {
            suboptions.add(s)
            return this
        }

        fun build() : SlashCommandOption? {
            if(name != null && description != null && type != null) {
                if(suboptions.size != 0 && choices.size == 0) {
                    return SlashCommandOption(this.name!!, this.description!!, this.required, this.type!!, *suboptions.toTypedArray())
                } else if(choices.size != 0 && suboptions.size == 0) {
                    return SlashCommandOption(this.name!!, this.description!!, this.required, this.type!!, *choices.toTypedArray())
                } else {
                    return SlashCommandOption(this.name!!, this.description!!, this.required, this.type!!, *choices.toTypedArray())
                }
            } else {
                return null
            }
        }

    }

}