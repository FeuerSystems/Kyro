package brys.dev.kyro.lib.model

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import javax.annotation.Nonnull

class KyroMessageReceived(
    @Nonnull api: JDA?, responseNumber: Long,
    /**
     * The received [Message][net.dv8tion.jda.api.entities.Message] object.
     *
     * @return The received [Message][net.dv8tion.jda.api.entities.Message] object.
     */
    @get:Nonnull
    @param:Nonnull val message: Message
) :
    GenericGuildMessageEvent(api!!, responseNumber, message.idLong, message.textChannel) {

    /**
     * The Author of the Message received as [User][net.dv8tion.jda.api.entities.User] object.
     * <br></br>This will be never-null but might be a fake User if Message was sent via Webhook.
     * See [Webhook.getDefaultUser].
     *
     * @return The Author of the Message.
     *
     * @see .isWebhookMessage
     */
    @get:Nonnull
    val author: User
        get() = message.author

    /**
     * The Author of the Message received as [Member][net.dv8tion.jda.api.entities.Member] object.
     * <br></br>This will be `null` in case of [isWebhookMessage()][.isWebhookMessage] returning `true`.
     *
     * @return The Author of the Message as Member object.
     *
     * @see .isWebhookMessage
     */
    val member: Member?
        get() = message.member

    /**
     * Whether or not the Message received was sent via a Webhook.
     * <br></br>This is a shortcut for `getMessage().isWebhookMessage()`.
     *
     * @return Whether or not the Message was sent via Webhook
     */
    val isWebhookMessage: Boolean
        get() = message.isWebhookMessage

}
