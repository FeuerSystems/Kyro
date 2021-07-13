package brys.dev.kyro.lib.model

import net.dv8tion.jda.api.entities.Message
import brys.dev.kyro.lib.types.PageType
import net.dv8tion.jda.api.entities.MessageEmbed
import javax.annotation.Nonnull

class Page
/**
 * A Page object to be used in this library's methods. Currently only Message
 * and MessageEmbed types are supported.
 *
 * @param type    The type of the content (PageType.TEXT or PageType.EMBED)
 * @param content The Message/MessageEmbed object to be used as pages
 */(@param:Nonnull val type: PageType, @param:Nonnull val content: Any) {
    override fun toString(): String {
        return if (type == PageType.TEXT) {
            (content as Message).contentRaw
        } else {
            (content as MessageEmbed).description!!
        }
    }
}