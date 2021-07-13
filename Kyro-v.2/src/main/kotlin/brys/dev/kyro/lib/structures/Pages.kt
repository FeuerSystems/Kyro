package brys.dev.kyro.lib.structures

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import kotlin.Throws
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.PermissionException
import java.util.concurrent.TimeUnit
import brys.dev.kyro.lib.classes.exceptions.InvalidStateException
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import java.util.concurrent.Future
import java.lang.Void
import brys.dev.kyro.lib.classes.EmojiUtils
import java.util.function.BiConsumer
import java.lang.NullPointerException
import brys.dev.kyro.lib.classes.exceptions.NullPageException
import brys.dev.kyro.lib.types.PageType
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import brys.dev.kyro.lib.model.Page
import brys.dev.kyro.lib.types.Emote
import listener.MessageHandler
import net.dv8tion.jda.api.Permission
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import java.util.function.Predicate
import javax.annotation.Nonnull

/**
 * Dw i'm not insane this was stolen :3
 */
object Pages {
    var api: JDA? = null
    var handler = MessageHandler()
    /**
     * Adds navigation buttons to the specified Message/MessageEmbed which will
     * navigate through a given List of pages. You must specify how long the
     * listener will stay active before shutting down itself after a no-activity
     * interval.
     *
     * @param msg   The message sent which will be paginated.
     * @param pages The pages to be shown. The order of the array will define the
     * order of the pages.
     * @param time  The time before the listener automatically stop listening for
     * further events. (Recommended: 60)
     * @param unit  The time's time unit. (Recommended: TimeUnit.SECONDS)
     * @throws ErrorResponseException Thrown if the message no longer exists or
     * cannot be accessed when triggering a
     * GenericMessageReactionEvent
     * @throws PermissionException    Thrown if this library cannot remove reactions due to lack of bot permission
     * @throws InvalidStateException  Thrown if no JDA client was set with activate()
     */
    @Throws(ErrorResponseException::class, PermissionException::class)
    fun paginate(msg: Message, pages: List<Page?>, time: Int, unit: TimeUnit?, user: String) {
        if (api == null) throw InvalidStateException()
        if (!msg.guild.selfMember.hasPermission(Permission.MESSAGE_MANAGE)) return msg.reply("I don't have permission to manage messages, therefore I can't use pagination.").mentionRepliedUser(false).queue()
        msg.addReaction(Emote.PREVIOUS.code).submit()
        msg.addReaction(Emote.NEXT.code).submit()
        handler.addEvent(
            (if (msg.channelType.isGuild) msg.guild.id else msg.privateChannel.id) + msg.id,
            object : Consumer<MessageReactionAddEvent> {
                private val maxP = pages.size - 1
                private var p = 0
                private var timeout: Future<*>? = null
                private val success = Consumer { s: Void? -> handler.removeEvent(msg) }
                override fun accept(event: MessageReactionAddEvent) {
                    event.retrieveUser().submit().thenAccept { u: User ->
                        if (timeout == null) try {
                            timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                        } catch (ignore: PermissionException) {
                        }
                        if (Objects.requireNonNull(u).isBot || event.messageId != msg.id || u.id != user) return@thenAccept
                        if (timeout != null) timeout!!.cancel(true)
                        try {
                            timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                        } catch (ignore: PermissionException) {
                        }
                        if (event.reactionEmote.name == Emote.PREVIOUS.code) {
                            if (p > 0) {
                                p--
                                val pg = pages[p]
                                updatePage(msg, pg)
                            }
                        } else if (event.reactionEmote.name == Emote.NEXT.code) {
                            if (p < maxP) {
                                p++
                                val pg = pages[p]
                                updatePage(msg, pg)
                            }
                        } else if (event.reactionEmote.name == Emote.CANCEL.code) {
                            try {
                                msg.clearReactions().submit()
                                    .thenAccept(success)
                                    .exceptionally { s: Throwable? ->
                                        msg.reactions.forEach(Consumer { r: MessageReaction ->
                                            r.removeReaction().submit()
                                        })
                                        success.accept(null)
                                        null
                                    }
                            } catch (e: PermissionException) {
                                msg.reactions.forEach(Consumer { r: MessageReaction -> r.removeReaction().submit() })
                                success.accept(null)
                            }
                        }
                        try {
                            event.reaction.removeReaction(u).submit()
                        } catch (ignore: PermissionException) {
                        } catch (ignore: ErrorResponseException) {
                        }
                    }
                }

                init {
                    timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                }
            })
    }

    /**
     * Adds navigation buttons to the specified Message/MessageEmbed which will
     * navigate through a given List of pages. You must specify how long the
     * listener will stay active before shutting down itself after a no-activity
     * interval.
     *
     * @param msg         The message sent which will be paginated.
     * @param pages       The pages to be shown. The order of the array will define the
     * order of the pages.
     * @param time        The time before the listener automatically stop listening for
     * further events. (Recommended: 60)
     * @param unit        The time's time unit. (Recommended: TimeUnit.SECONDS)
     * @param canInteract Predicate to determine whether the user that pressed the button can or cannot interact with the buttons
     * @throws ErrorResponseException Thrown if the message no longer exists or
     * cannot be accessed when triggering a
     * GenericMessageReactionEvent
     * @throws PermissionException    Thrown if this library cannot remove reactions due to lack of bot permission
     * @throws InvalidStateException  Thrown if no JDA client was set with activate()
     */
    @Throws(ErrorResponseException::class, PermissionException::class)
    fun paginate(
        msg: Message,
        pages: List<Page?>,
        time: Int,
        unit: TimeUnit?,
        canInteract: Predicate<User?>,
        user: String
    ) {
        if (api == null) throw InvalidStateException()
        msg.addReaction(Emote.PREVIOUS.code).submit()
        msg.addReaction(Emote.CANCEL.code).submit()
        msg.addReaction(Emote.NEXT.code).submit()
        handler.addEvent(
            (if (msg.channelType.isGuild) msg.guild.id else msg.privateChannel.id) + msg.id,
            object : Consumer<MessageReactionAddEvent> {
                private val maxP = pages.size - 1
                private var p = 0
                private var timeout: Future<*>? = null
                private val success = Consumer { s: Void? -> handler.removeEvent(msg) }
                override fun accept(event: MessageReactionAddEvent) {
                    event.retrieveUser().submit().thenAccept { u: User ->
                        if (canInteract.test(u)) {
                            if (timeout == null) try {
                                timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success);

                            } catch (ignore: PermissionException) {
                            }
                            if (Objects.requireNonNull(u).isBot || event.messageId != msg.id || u.id != user) return@thenAccept
                            if (timeout != null) timeout!!.cancel(true)
                            try {
                                timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                            } catch (ignore: PermissionException) {
                            }
                            if (event.reactionEmote.name == Emote.PREVIOUS.code) {
                                if (p > 0) {
                                    p--
                                    val pg = pages[p]
                                    updatePage(msg, pg)
                                }
                            } else if (event.reactionEmote.name == Emote.NEXT.code) {
                                if (p < maxP) {
                                    p++
                                    val pg = pages[p]
                                    updatePage(msg, pg)
                                }
                            } else if (event.reactionEmote.name == Emote.CANCEL.code) {
                                try {
                                    msg.clearReactions().submit()
                                        .thenAccept(success)
                                        .exceptionally { s: Throwable? ->
                                            msg.reactions.forEach(Consumer { r: MessageReaction ->
                                                r.removeReaction().submit()
                                            })
                                            success.accept(null)
                                            null
                                        }
                                } catch (e: PermissionException) {
                                    msg.reactions.forEach(Consumer { r: MessageReaction ->
                                        r.removeReaction().submit()
                                    })
                                    success.accept(null)
                                }
                            }
                            try {
                                event.reaction.removeReaction(u).submit()
                            } catch (ignore: PermissionException) {
                            } catch (ignore: ErrorResponseException) {
                            }
                        }
                    }
                }

                init {
                    timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                }
            })
    }

    /**
     * Adds navigation buttons to the specified Message/MessageEmbed which will
     * navigate through a given List of pages. You must specify how long the
     * listener will stay active before shutting down itself after a no-activity
     * interval.
     *
     * @param msg        The message sent which will be paginated.
     * @param pages      The pages to be shown. The order of the array will define the
     * order of the pages.
     * @param time       The time before the listener automatically stop listening for
     * further events. (Recommended: 60)
     * @param unit       The time's time unit. (Recommended: TimeUnit.SECONDS)
     * @param skipAmount The amount of pages to be skipped when clicking SKIP buttons
     * @throws ErrorResponseException Thrown if the message no longer exists or
     * cannot be accessed when triggering a
     * GenericMessageReactionEvent
     * @throws PermissionException    Thrown if this library cannot remove reactions due to lack of bot permission
     * @throws InvalidStateException  Thrown if no JDA client was set with activate()
     */
    @Throws(ErrorResponseException::class, PermissionException::class)
    fun paginate(msg: Message, pages: List<Page?>, time: Int, unit: TimeUnit?, skipAmount: Int, user: String) {
        if (api == null) throw InvalidStateException()
        msg.addReaction(Emote.SKIP_BACKWARD.code).submit()
        msg.addReaction(Emote.PREVIOUS.code).submit()
        msg.addReaction(Emote.CANCEL.code).submit()
        msg.addReaction(Emote.NEXT.code).submit()
        msg.addReaction(Emote.SKIP_FORWARD.code).submit()
        handler.addEvent(
            (if (msg.channelType.isGuild) msg.guild.id else msg.privateChannel.id) + msg.id,
            object : Consumer<MessageReactionAddEvent> {
                private val maxP = pages.size - 1
                private var p = 0
                private var timeout: Future<*>? = null
                private val success = Consumer { s: Void? -> handler.removeEvent(msg) }
                override fun accept(event: MessageReactionAddEvent) {
                    event.retrieveUser().submit().thenAccept { u: User ->
                        if (timeout == null) try {
                            timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                        } catch (ignore: PermissionException) {
                        }
                        if (Objects.requireNonNull(u).isBot || event.messageId != msg.id || u.id != user) return@thenAccept
                        if (timeout != null) timeout!!.cancel(true)
                        try {
                            timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                        } catch (ignore: PermissionException) {
                        }
                        if (event.reactionEmote.name == Emote.PREVIOUS.code) {
                            if (p > 0) {
                                p--
                                val pg = pages[p]
                                updatePage(msg, pg)
                            }
                        } else if (event.reactionEmote.name == Emote.NEXT.code) {
                            if (p < maxP) {
                                p++
                                val pg = pages[p]
                                updatePage(msg, pg)
                            }
                        } else if (event.reactionEmote.name == Emote.SKIP_BACKWARD.code) {
                            if (p > 0) {
                                p -= if (p - skipAmount < 0) p else skipAmount
                                val pg = pages[p]
                                updatePage(msg, pg)
                            }
                        } else if (event.reactionEmote.name == Emote.SKIP_FORWARD.code) {
                            if (p < maxP) {
                                p += if (p + skipAmount > maxP) maxP - p else skipAmount
                                val pg = pages[p]
                                updatePage(msg, pg)
                            }
                        } else if (event.reactionEmote.name == Emote.CANCEL.code) {
                            try {
                                msg.clearReactions().submit()
                                    .thenAccept(success)
                                    .exceptionally { s: Throwable? ->
                                        msg.reactions.forEach(Consumer { r: MessageReaction ->
                                            r.removeReaction().submit()
                                        })
                                        success.accept(null)
                                        null
                                    }
                            } catch (e: PermissionException) {
                                msg.reactions.forEach(Consumer { r: MessageReaction -> r.removeReaction().submit() })
                                success.accept(null)
                            }
                        }
                        try {
                            event.reaction.removeReaction(u).submit()
                        } catch (ignore: PermissionException) {
                        } catch (ignore: ErrorResponseException) {
                        }
                    }
                }

                init {
                    timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                }
            })
    }

    /**
     * Adds navigation buttons to the specified Message/MessageEmbed which will
     * navigate through a given List of pages. You must specify how long the
     * listener will stay active before shutting down itself after a no-activity
     * interval.
     *
     * @param msg         The message sent which will be paginated.
     * @param pages       The pages to be shown. The order of the array will define the
     * order of the pages.
     * @param time        The time before the listener automatically stop listening for
     * further events. (Recommended: 60)
     * @param unit        The time's time unit. (Recommended: TimeUnit.SECONDS)
     * @param skipAmount  The amount of pages to be skipped when clicking SKIP buttons
     * @param canInteract Predicate to determine whether the user that pressed the button can or cannot interact with the buttons
     * @throws ErrorResponseException Thrown if the message no longer exists or
     * cannot be accessed when triggering a
     * GenericMessageReactionEvent
     * @throws PermissionException    Thrown if this library cannot remove reactions due to lack of bot permission
     * @throws InvalidStateException  Thrown if no JDA client was set with activate()
     */
    @Throws(ErrorResponseException::class, PermissionException::class)
    fun paginate(
        msg: Message,
        pages: List<Page?>,
        time: Int,
        unit: TimeUnit?,
        skipAmount: Int,
        canInteract: Predicate<User?>,
        user: String
    ) {
        if (api == null) throw InvalidStateException()
        msg.addReaction(Emote.SKIP_BACKWARD.code).submit()
        msg.addReaction(Emote.PREVIOUS.code).submit()
        msg.addReaction(Emote.CANCEL.code).submit()
        msg.addReaction(Emote.NEXT.code).submit()
        msg.addReaction(Emote.SKIP_FORWARD.code).submit()
        handler.addEvent(
            (if (msg.channelType.isGuild) msg.guild.id else msg.privateChannel.id) + msg.id,
            object : Consumer<MessageReactionAddEvent> {
                private val maxP = pages.size - 1
                private var p = 0
                private var timeout: Future<*>? = null
                private val success = Consumer { s: Void? -> handler.removeEvent(msg) }
                override fun accept(event: MessageReactionAddEvent) {
                    event.retrieveUser().submit().thenAccept { u: User ->
                        if (canInteract.test(u)) {
                            if (timeout == null) try {
                                timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                            } catch (ignore: PermissionException) {
                            }
                            if (Objects.requireNonNull(u).isBot || event.messageId != msg.id || u.id != user) return@thenAccept
                            if (timeout != null) timeout!!.cancel(true)
                            try {
                                timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                            } catch (ignore: PermissionException) {
                            }
                            if (event.reactionEmote.name == Emote.PREVIOUS.code) {
                                if (p > 0) {
                                    p--
                                    val pg = pages[p]
                                    updatePage(msg, pg)
                                }
                            } else if (event.reactionEmote.name == Emote.NEXT.code) {
                                if (p < maxP) {
                                    p++
                                    val pg = pages[p]
                                    updatePage(msg, pg)
                                }
                            } else if (event.reactionEmote.name == Emote.SKIP_BACKWARD.code) {
                                if (p > 0) {
                                    p -= if (p - skipAmount < 0) p else skipAmount
                                    val pg = pages[p]
                                    updatePage(msg, pg)
                                }
                            } else if (event.reactionEmote.name == Emote.SKIP_FORWARD.code) {
                                if (p < maxP) {
                                    p += if (p + skipAmount > maxP) maxP - p else skipAmount
                                    val pg = pages[p]
                                    updatePage(msg, pg)
                                }
                            } else if (event.reactionEmote.name == Emote.CANCEL.code) {
                                try {
                                    msg.clearReactions().submit()
                                        .thenAccept(success)
                                        .exceptionally { s: Throwable? ->
                                            msg.reactions.forEach(Consumer { r: MessageReaction ->
                                                r.removeReaction().submit()
                                            })
                                            success.accept(null)
                                            null
                                        }
                                } catch (e: PermissionException) {
                                    msg.reactions.forEach(Consumer { r: MessageReaction ->
                                        r.removeReaction().submit()
                                    })
                                    success.accept(null)
                                }
                            }
                            try {
                                event.reaction.removeReaction(u).submit()
                            } catch (ignore: PermissionException) {
                            } catch (ignore: ErrorResponseException) {
                            }
                        }
                    }
                }

                init {
                    timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                }
            })
    }

    /**
     * Adds menu-like buttons to the specified Message/MessageEmbed which will
     * browse through a given Map of pages. You may specify one Page per button,
     * adding another button with an existing unicode will overwrite the current
     * button's Page. You must specify how long the listener will stay active before
     * shutting down itself after a no-activity interval.
     *
     * @param msg        The message sent which will be categorized.
     * @param categories The categories to be shown. The categories are defined by a
     * Map containing emote unicodes as keys and Pages as values.
     * @param time       The time before the listener automatically stop listening
     * for further events. (Recommended: 60)
     * @param unit       The time's time unit. (Recommended: TimeUnit.SECONDS)
     * @throws ErrorResponseException Thrown if the message no longer exists or
     * cannot be accessed when triggering a
     * GenericMessageReactionEvent
     * @throws PermissionException    Thrown if this library cannot remove reactions due to lack of bot permission
     * @throws InvalidStateException  Thrown if no JDA client was set with activate()
     */
    @Throws(ErrorResponseException::class, PermissionException::class)
    fun categorize(msg: Message, categories: Map<String?, Page?>, time: Int, unit: TimeUnit?, user: String) {
        if (api == null) throw InvalidStateException()
        categories.keys.forEach(Consumer { k: String? ->
            if (EmojiUtils.containsEmoji(k)) msg.addReaction(
                k!!
            ).submit() else msg.addReaction(Objects.requireNonNull(api!!.getEmoteById(k!!))!!).submit()
        })
        msg.addReaction(Emote.CANCEL.code).submit()
        handler.addEvent(
            (if (msg.channelType.isGuild) msg.guild.id else msg.privateChannel.id) + msg.id,
            object : Consumer<MessageReactionAddEvent> {
                private var currCat = ""
                private var timeout: Future<*>? = null
                private val success = Consumer { s: Void? -> handler.removeEvent(msg) }
                override fun accept(@Nonnull event: MessageReactionAddEvent) {
                    event.retrieveUser().submit().thenAccept { u: User ->
                        if (timeout == null) try {
                            timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                        } catch (ignore: PermissionException) {
                        }
                        if (Objects.requireNonNull(u).isBot || event.reactionEmote.name == currCat || event.messageId != msg.id || u.id != user) return@thenAccept else if (event.reactionEmote.name == Emote.CANCEL.code) {
                            try {
                                msg.clearReactions().submit()
                                    .thenAccept(success)
                                    .exceptionally { s: Throwable? ->
                                        msg.reactions.forEach(Consumer { r: MessageReaction ->
                                            r.removeReaction().submit()
                                        })
                                        success.accept(null)
                                        null
                                    }
                            } catch (e: PermissionException) {
                                msg.reactions.forEach(Consumer { r: MessageReaction -> r.removeReaction().submit() })
                                success.accept(null)
                            }
                            return@thenAccept
                        }
                        if (timeout != null) timeout!!.cancel(true)
                        try {
                            timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                        } catch (ignore: PermissionException) {
                        }
                        val pg =
                            categories[if (event.reactionEmote.isEmoji) event.reactionEmote.name else event.reactionEmote.id]
                        currCat = updateCategory(event, msg, pg)
                        try {
                            event.reaction.removeReaction(u).submit()
                        } catch (ignore: PermissionException) {
                        } catch (ignore: ErrorResponseException) {
                        }
                    }
                }

                init {
                    timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                }
            })
    }

    /**
     * Adds menu-like buttons to the specified Message/MessageEmbed which will
     * browse through a given Map of pages. You may specify one Page per button,
     * adding another button with an existing unicode will overwrite the current
     * button's Page. You must specify how long the listener will stay active before
     * shutting down itself after a no-activity interval.
     *
     * @param msg         The message sent which will be categorized.
     * @param categories  The categories to be shown. The categories are defined by a
     * Map containing emote unicodes as keys and Pages as values.
     * @param time        The time before the listener automatically stop listening
     * for further events. (Recommended: 60)
     * @param unit        The time's time unit. (Recommended: TimeUnit.SECONDS)
     * @param canInteract Predicate to determine whether the user that pressed the button can or cannot interact with the buttons
     * @throws ErrorResponseException Thrown if the message no longer exists or
     * cannot be accessed when triggering a
     * GenericMessageReactionEvent
     * @throws PermissionException    Thrown if this library cannot remove reactions due to lack of bot permission
     * @throws InvalidStateException  Thrown if no JDA client was set with activate()
     */
    @Throws(ErrorResponseException::class, PermissionException::class)
    fun categorize(
        msg: Message,
        categories: Map<String?, Page?>,
        time: Int,
        unit: TimeUnit?,
        canInteract: Predicate<User?>,
        user: String
    ) {
        if (api == null) throw InvalidStateException()
        categories.keys.forEach(Consumer { k: String? ->
            if (EmojiUtils.containsEmoji(k)) msg.addReaction(
                k!!
            ).submit() else msg.addReaction(Objects.requireNonNull(api!!.getEmoteById(k!!))!!).submit()
        })
        msg.addReaction(Emote.CANCEL.code).submit()
        handler.addEvent(
            (if (msg.channelType.isGuild) msg.guild.id else msg.privateChannel.id) + msg.id,
            object : Consumer<MessageReactionAddEvent> {
                private var currCat = ""
                private var timeout: Future<*>? = null
                private val success = Consumer { s: Void? -> handler.removeEvent(msg) }
                override fun accept(@Nonnull event: MessageReactionAddEvent) {
                    event.retrieveUser().submit().thenAccept { u: User ->
                        if (canInteract.test(u)) {
                            if (timeout == null) try {
                                timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                            } catch (ignore: PermissionException) {
                            }
                            if (Objects.requireNonNull(u).isBot || event.reactionEmote.name == currCat || event.messageId != msg.id || u.id != user) return@thenAccept else if (event.reactionEmote.name == Emote.CANCEL.code) {
                                try {
                                    msg.clearReactions().submit()
                                        .thenAccept(success)
                                        .exceptionally { s: Throwable? ->
                                            msg.reactions.forEach(Consumer { r: MessageReaction ->
                                                r.removeReaction().submit()
                                            })
                                            success.accept(null)
                                            null
                                        }
                                } catch (e: PermissionException) {
                                    msg.reactions.forEach(Consumer { r: MessageReaction ->
                                        r.removeReaction().submit()
                                    })
                                    success.accept(null)
                                }
                                return@thenAccept
                            }
                            if (timeout != null) timeout!!.cancel(true)
                            try {
                                timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                            } catch (ignore: PermissionException) {
                            }
                            val pg =
                                categories[if (event.reactionEmote.isEmoji) event.reactionEmote.name else event.reactionEmote.id]
                            currCat = updateCategory(event, msg, pg)
                            try {
                                event.reaction.removeReaction(u).submit()
                            } catch (ignore: PermissionException) {
                            } catch (ignore: ErrorResponseException) {
                            }
                        }
                    }
                }

                init {
                    timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                }
            })
    }

    /**
     * Adds buttons to the specified Message/MessageEmbed, with each executing a
     * specific task on click. Each button's unicode must be unique, adding another
     * button with an existing unicode will overwrite the current button's Runnable.
     *
     * @param msg              The message sent which will be buttoned.
     * @param buttons          The buttons to be shown. The buttons are defined by a Map
     * containing emote unicodes as keys and BiConsumer<Member></Member>, Message> containing
     * desired behavior as value.
     * @param showCancelButton Should the cancel button be created automatically?
     * @throws ErrorResponseException Thrown if the message no longer exists or
     * cannot be accessed when triggering a
     * GenericMessageReactionEvent
     * @throws PermissionException    Thrown if this library cannot remove reactions due to lack of bot permission
     * @throws InvalidStateException  Thrown if no JDA client was set with activate()
     */
    @Throws(ErrorResponseException::class, PermissionException::class)
    fun buttonize(
        msg: Message,
        buttons: Map<String?, BiConsumer<Member?, Message?>>,
        showCancelButton: Boolean,
        user: String
    ) {
        if (api == null) throw InvalidStateException()
        buttons.keys.forEach(Consumer { k: String? ->
            if (EmojiUtils.containsEmoji(k)) msg.addReaction(
                k!!
            ).submit() else msg.addReaction(Objects.requireNonNull(api!!.getEmoteById(k!!))!!).submit()
        })
        if (!buttons.containsKey(Emote.CANCEL.code) && showCancelButton) msg.addReaction(Emote.CANCEL.code).submit()
        handler.addEvent(
            (if (msg.channelType.isGuild) msg.guild.id else msg.privateChannel.id) + msg.id,
            object : Consumer<MessageReactionAddEvent> {
                private val success = Consumer { s: Void? -> handler.removeEvent(msg) }
                override fun accept(@Nonnull event: MessageReactionAddEvent) {
                    event.retrieveUser().submit().thenAccept { u: User ->
                        if (Objects.requireNonNull(u).isBot || event.messageId != msg.id || u.id != user) return@thenAccept
                        try {
                            if (event.reactionEmote.isEmoji) buttons[event.reactionEmote.name]!!
                                .accept(event.member, msg) else buttons[event.reactionEmote.id]!!
                                .accept(event.member, msg)
                        } catch (ignore: NullPointerException) {
                        }
                        if (!buttons.containsKey(Emote.CANCEL.code) && showCancelButton && event.reactionEmote.name == Emote.CANCEL.code) {
                            try {
                                msg.clearReactions().submit()
                                    .thenAccept(success)
                                    .exceptionally { s: Throwable? ->
                                        msg.reactions.forEach(Consumer { r: MessageReaction ->
                                            r.removeReaction().submit()
                                        })
                                        success.accept(null)
                                        null
                                    }
                            } catch (e: PermissionException) {
                                msg.reactions.forEach(Consumer { r: MessageReaction -> r.removeReaction().submit() })
                                success.accept(null)
                            }
                        }
                        try {
                            event.reaction.removeReaction(u).submit()
                        } catch (ignore: PermissionException) {
                        } catch (ignore: ErrorResponseException) {
                        }
                    }
                }
            })
    }

    /**
     * Adds buttons to the specified Message/MessageEmbed, with each executing a
     * specific task on click. Each button's unicode must be unique, adding another
     * button with an existing unicode will overwrite the current button's Runnable.
     * You can specify the time in which the listener will automatically stop itself
     * after a no-activity interval.
     *
     * @param msg              The message sent which will be buttoned.
     * @param buttons          The buttons to be shown. The buttons are defined by a Map
     * containing emote unicodes as keys and BiConsumer<Member></Member>, Message> containing
     * desired behavior as value.
     * @param showCancelButton Should the cancel button be created automatically?
     * @param time             The time before the listener automatically stop listening for
     * further events. (Recommended: 60)
     * @param unit             The time's time unit. (Recommended: TimeUnit.SECONDS)
     * @throws ErrorResponseException Thrown if the message no longer exists or
     * cannot be accessed when triggering a
     * GenericMessageReactionEvent
     * @throws PermissionException    Thrown if this library cannot remove reactions due to lack of bot permission
     * @throws InvalidStateException  Thrown if no JDA client was set with activate()
     */
    @Throws(ErrorResponseException::class, PermissionException::class)
    fun buttonize(
        msg: Message,
        buttons: Map<String?, BiConsumer<Member?, Message?>>,
        showCancelButton: Boolean,
        time: Int,
        unit: TimeUnit?,
        user: String
    ) {
        if (api == null) throw InvalidStateException()
        buttons.keys.forEach(Consumer { k: String? ->
            if (EmojiUtils.containsEmoji(k)) msg.addReaction(
                k!!
            ).submit() else msg.addReaction(Objects.requireNonNull(api!!.getEmoteById(k!!))!!).submit()
        })
        if (!buttons.containsKey(Emote.CANCEL.code) && showCancelButton) msg.addReaction(Emote.CANCEL.code).submit()
        handler.addEvent(
            (if (msg.channelType.isGuild) msg.guild.id else msg.privateChannel.id) + msg.id,
            object : Consumer<MessageReactionAddEvent> {
                private var timeout: Future<*>? = null
                private val success = Consumer { s: Void? -> handler.removeEvent(msg) }
                override fun accept(@Nonnull event: MessageReactionAddEvent) {
                    event.retrieveUser().submit().thenAccept { u: User ->
                        if (timeout == null) try {
                            timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                        } catch (ignore: PermissionException) {
                        }
                        if (Objects.requireNonNull(u).isBot || event.messageId != msg.id || u.id != user) return@thenAccept
                        try {
                            if (event.reactionEmote.isEmoji) buttons[event.reactionEmote.name]!!
                                .accept(event.member, msg) else buttons[event.reactionEmote.id]!!
                                .accept(event.member, msg)
                        } catch (ignore: NullPointerException) {
                        }
                        if (!buttons.containsKey(Emote.CANCEL.code) && showCancelButton && event.reactionEmote.name == Emote.CANCEL.code) {
                            try {
                                msg.clearReactions().submit()
                                    .thenAccept(success)
                                    .exceptionally { s: Throwable? ->
                                        msg.reactions.forEach(Consumer { r: MessageReaction ->
                                            r.removeReaction().submit()
                                        })
                                        success.accept(null)
                                        null
                                    }
                            } catch (e: PermissionException) {
                                msg.reactions.forEach(Consumer { r: MessageReaction -> r.removeReaction().submit() })
                                success.accept(null)
                            }
                        }
                        if (timeout != null) timeout!!.cancel(true)
                        try {
                            timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                        } catch (ignore: PermissionException) {
                        }
                        try {
                            event.reaction.removeReaction(u).submit()
                        } catch (ignore: PermissionException) {
                        } catch (ignore: ErrorResponseException) {
                        }
                    }
                }

                init {
                    timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                }
            })
    }

    /**
     * Adds buttons to the specified Message/MessageEmbed, with each executing a
     * specific task on click. Each button's unicode must be unique, adding another
     * button with an existing unicode will overwrite the current button's Runnable.
     * You can specify the time in which the listener will automatically stop itself
     * after a no-activity interval.
     *
     * @param msg              The message sent which will be buttoned.
     * @param buttons          The buttons to be shown. The buttons are defined by a Map
     * containing emote unicodes as keys and BiConsumer<Member></Member>, Message> containing
     * desired behavior as value.
     * @param showCancelButton Should the cancel button be created automatically?
     * @param time             The time before the listener automatically stop listening for
     * further events. (Recommended: 60)
     * @param unit             The time's time unit. (Recommended: TimeUnit.SECONDS)
     * @param canInteract      Predicate to determine whether the user that pressed the button can or cannot interact with the buttons
     * @throws ErrorResponseException Thrown if the message no longer exists or
     * cannot be accessed when triggering a
     * GenericMessageReactionEvent
     * @throws PermissionException    Thrown if this library cannot remove reactions due to lack of bot permission
     * @throws InvalidStateException  Thrown if no JDA client was set with activate()
     */
    @Throws(ErrorResponseException::class, PermissionException::class)
    fun buttonize(
        msg: Message,
        buttons: Map<String?, BiConsumer<Member?, Message?>>,
        showCancelButton: Boolean,
        time: Int,
        unit: TimeUnit?,
        canInteract: Predicate<User?>,
        user: String
    ) {
        if (api == null) throw InvalidStateException()
        buttons.keys.forEach(Consumer { k: String? ->
            if (EmojiUtils.containsEmoji(k)) msg.addReaction(
                k!!
            ).submit() else msg.addReaction(Objects.requireNonNull(api!!.getEmoteById(k!!))!!).submit()
        })
        if (!buttons.containsKey(Emote.CANCEL.code) && showCancelButton) msg.addReaction(Emote.CANCEL.code).submit()
        handler.addEvent(
            (if (msg.channelType.isGuild) msg.guild.id else msg.privateChannel.id) + msg.id,
            object : Consumer<MessageReactionAddEvent> {
                private var timeout: Future<*>? = null
                private val success = Consumer { s: Void? -> handler.removeEvent(msg) }
                override fun accept(@Nonnull event: MessageReactionAddEvent) {
                    event.retrieveUser().submit().thenAccept { u: User ->
                        if (canInteract.test(u)) {
                            if (timeout == null) try {
                                timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                            } catch (ignore: PermissionException) {
                            }
                            if (Objects.requireNonNull(u).isBot || event.messageId != msg.id || u.id != user) return@thenAccept
                            try {
                                if (event.reactionEmote.isEmoji) buttons[event.reactionEmote.name]!!
                                    .accept(event.member, msg) else buttons[event.reactionEmote.id]!!
                                    .accept(event.member, msg)
                            } catch (ignore: NullPointerException) {
                            }
                            if (!buttons.containsKey(Emote.CANCEL.code) && showCancelButton && event.reactionEmote.name == Emote.CANCEL.code) {
                                try {
                                    msg.clearReactions().submit()
                                        .thenAccept(success)
                                        .exceptionally { s: Throwable? ->
                                            msg.reactions.forEach(Consumer { r: MessageReaction ->
                                                r.removeReaction().submit()
                                            })
                                            success.accept(null)
                                            null
                                        }
                                } catch (e: PermissionException) {
                                    msg.reactions.forEach(Consumer { r: MessageReaction ->
                                        r.removeReaction().submit()
                                    })
                                    success.accept(null)
                                }
                            }
                            if (timeout != null) timeout!!.cancel(true)
                            try {
                                timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                            } catch (ignore: PermissionException) {
                            }
                            try {
                                event.reaction.removeReaction(u).submit()
                            } catch (ignore: PermissionException) {
                            } catch (ignore: ErrorResponseException) {
                            }
                        }
                    }
                }

                init {
                    timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                }
            })
    }

    /**
     * Adds buttons to the specified Message/MessageEmbed, with each executing a
     * specific task on click. Each button's unicode must be unique, adding another
     * button with an existing unicode will overwrite the current button's Runnable.
     * You can specify the time in which the listener will automatically stop itself
     * after a no-activity interval.
     *
     * @param msg              The message sent which will be buttoned.
     * @param buttons          The buttons to be shown. The buttons are defined by a Map
     * containing emote unicodes as keys and BiConsumer<Member></Member>, Message> containing
     * desired behavior as value.
     * @param showCancelButton Should the cancel button be created automatically?
     * @param time             The time before the listener automatically stop listening for
     * further events. (Recommended: 60)
     * @param unit             The time's time unit. (Recommended: TimeUnit.SECONDS)
     * @param canInteract      Predicate to determine whether the user that pressed the button can or cannot interact with the buttons
     * @param onCancel         Action to be ran after the listener is removed
     * @throws ErrorResponseException Thrown if the message no longer exists or
     * cannot be accessed when triggering a
     * GenericMessageReactionEvent
     * @throws PermissionException    Thrown if this library cannot remove reactions due to lack of bot permission
     * @throws InvalidStateException  Thrown if no JDA client was set with activate()
     */
    @Throws(ErrorResponseException::class, PermissionException::class)
    fun buttonize(
        msg: Message,
        buttons: Map<String?, BiConsumer<Member?, Message?>>,
        showCancelButton: Boolean,
        time: Int,
        unit: TimeUnit?,
        canInteract: Predicate<User?>,
        onCancel: Consumer<Message?>?,
        user: String
    ) {
        if (api == null) throw InvalidStateException()
        buttons.keys.forEach(Consumer { k: String? ->
            if (EmojiUtils.containsEmoji(k)) msg.addReaction(
                k!!
            ).submit() else msg.addReaction(Objects.requireNonNull(api!!.getEmoteById(k!!))!!).submit()
        })
        if (!buttons.containsKey(Emote.CANCEL.code) && showCancelButton) msg.addReaction(Emote.CANCEL.code).submit()
        handler.addEvent(
            (if (msg.channelType.isGuild) msg.guild.id else msg.privateChannel.id) + msg.id,
            object : Consumer<MessageReactionAddEvent> {
                private var timeout: Future<*>? = null
                private val success = Consumer { s: Void? ->
                    handler.removeEvent(msg)
                    onCancel?.accept(msg)
                }

                override fun accept(@Nonnull event: MessageReactionAddEvent) {
                    event.retrieveUser().submit().thenAccept { u: User ->
                        if (canInteract.test(u)) {
                            if (timeout == null) try {
                                timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                            } catch (ignore: PermissionException) {
                            }
                            if (Objects.requireNonNull(u).isBot || event.messageId != msg.id || u.id != user) return@thenAccept
                            try {
                                if (event.reactionEmote.isEmoji) buttons[event.reactionEmote.name]!!
                                    .accept(event.member, msg) else buttons[event.reactionEmote.id]!!
                                    .accept(event.member, msg)
                            } catch (ignore: NullPointerException) {
                            }
                            if (!buttons.containsKey(Emote.CANCEL.code) && showCancelButton && event.reactionEmote.name == Emote.CANCEL.code) {
                                try {
                                    msg.clearReactions().submit()
                                        .thenAccept(success)
                                        .exceptionally { s: Throwable? ->
                                            msg.reactions.forEach(Consumer { r: MessageReaction ->
                                                r.removeReaction().submit()
                                            })
                                            success.accept(null)
                                            null
                                        }
                                } catch (e: PermissionException) {
                                    msg.reactions.forEach(Consumer { r: MessageReaction ->
                                        r.removeReaction().submit()
                                    })
                                    success.accept(null)
                                }
                            }
                            if (timeout != null) timeout!!.cancel(true)
                            try {
                                timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                            } catch (ignore: PermissionException) {
                            }
                            try {
                                event.reaction.removeReaction(u).submit()
                            } catch (ignore: PermissionException) {
                            } catch (ignore: ErrorResponseException) {
                            }
                        }
                    }
                }

                init {
                    timeout = msg.clearReactions().submitAfter(time.toLong(), unit!!).thenAccept(success)
                }
            })
    }

    private fun updatePage(msg: Message, p: Page?) {
        if (p == null) throw NullPageException()
        if (p.type == PageType.TEXT) {
            msg.editMessage((p.content as Message)).submit()
        } else {
            msg.editMessage((p.content as MessageEmbed)).submit()
        }
    }

    private fun updateCategory(event: GenericMessageReactionEvent, msg: Message, p: Page?): String {
        val out = AtomicReference("")
        if (p == null) throw NullPageException()
        if (p.type == PageType.TEXT) {
            msg.editMessage((p.content as Message))
                .submit()
                .thenAccept { s: Message? -> out.set(event.reactionEmote.name) }
        } else {
            msg.editMessage((p.content as MessageEmbed))
                .submit()
                .thenAccept { s: Message? -> out.set(event.reactionEmote.name) }
        }
        return out.get()
    }
}