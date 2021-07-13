package brys.dev.kyro.lib.modules.command

import net.dv8tion.jda.api.Permission

@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
/**
 * Add this for to any command you've made
 */
annotation class AddCommand(
    /**
     * Aliases of this command [Array]
     */
    val aliases: Array<String>,
    /**
     * Description of this command
     */
    val description: String = "",
    /**
     * Usage of this command
     */
    val usage: String = "",
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