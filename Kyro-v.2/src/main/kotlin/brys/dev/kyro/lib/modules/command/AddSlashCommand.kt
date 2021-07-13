package brys.dev.kyro.lib.modules.command

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

annotation class AddSlashCommand(
    val name: String,
    /**
     * Description of this command
     */
    val description: String = "",
    /**
     * Usage of this command
     */
    /**
     * Require the member to have a track playing for this command to work (true/false)
     */
    val playing: Boolean = false,
    /**
     * Is this command disabled for DJ only mode? (true/false)
     */
    val dj: Boolean = false,
    /**
     * Require the member to be in a vc the bot can access otherwise returns an error message (true/false)
     */
    val vc: Boolean = false,
    /**
     * Owner only command (true/false)
     */
    val owner: Boolean = false,
    /**
     * Require the member to have a certain [net.dv8tion.jda.api.Permission]
     */
    val permission: Permission = Permission.UNKNOWN,
    /**
     * Additional Co Owners
     */
    val owners: Boolean = false,
)


