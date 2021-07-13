import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.interactions.components.ButtonStyle
import net.dv8tion.jda.api.interactions.components.Component
import net.dv8tion.jda.internal.interactions.ButtonImpl
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

interface Button : Component {
    /**
     * The visible text on the button.
     *
     * @return The button label
     */
    @get:Nonnull
    val label: String?

    /**
     * The style of this button.
     *
     * @return [ButtonStyle]
     */
    @get:Nonnull
    val style: ButtonStyle

    /**
     * The target URL for this button, if it is a [LINK][ButtonStyle.LINK]-Style Button.
     *
     * @return The target URL or null
     */
    val url: String?

    /**
     * The emoji attached to this button.
     * <br></br>This can be either [unicode][Emoji.isUnicode] or [Emoji.isCustom] custom.
     *
     *
     * You can use [.withEmoji] to create a button with an Emoji.
     *
     * @return [Emoji] for this button
     */
    val emoji: Emoji?

    /**
     * Whether this button is disabled.
     *
     *
     * You can use [.asDisabled] or [.asEnabled] to create enabled/disabled instances.
     *
     * @return True, if this button is disabled
     */
    val isDisabled: Boolean

    /**
     * Returns a copy of this button with [.isDisabled] set to true.
     *
     * @return New disabled button instance
     */
    @Nonnull
    @CheckReturnValue
    fun asDisabled(): ButtonImpl {
        return ButtonImpl(id, label, style, url, true, emoji)
    }

    /**
     * Returns a copy of this button with [.isDisabled] set to false.
     *
     * @return New enabled button instance
     */
    @Nonnull
    @CheckReturnValue
    fun asEnabled(): ButtonImpl {
        return ButtonImpl(id, label, style, url, false, emoji)
    }

    /**
     * Returns a copy of this button with [.isDisabled] set to the provided value.
     *
     * @param  disabled
     * True, if this button should be disabled
     *
     * @return New enabled/disabled button instance
     */
    @Nonnull
    @CheckReturnValue
    fun withDisabled(disabled: Boolean): Button? {
        return ButtonImpl(id, label, style, url, disabled, emoji) as Button?
    }

    /**
     * Returns a copy of this button with the attached Emoji.
     *
     * @param  emoji
     * The emoji to use
     *
     * @return New button with emoji
     */
    @Nonnull
    @CheckReturnValue
    fun withEmoji(emoji: Emoji?): Button? {
        return ButtonImpl(id, label, style, url, isDisabled, emoji) as Button?
    }

    /**
     * Returns a copy of this button with the provided label.
     *
     * @param  label
     * The label to use
     *
     * @throws IllegalArgumentException
     * If the label is not between 1-80 characters
     *
     * @return New button with the changed label
     */
    @Nonnull
    @CheckReturnValue
    fun withLabel(@Nonnull label: String?): Button? {
        Checks.notEmpty(label, "Label")
        Checks.notLonger(label, 80, "Label")
        return ButtonImpl(id, label, style, url, isDisabled, emoji) as Button?
    }

    /**
     * Returns a copy of this button with the provided id.
     *
     * @param  id
     * The id to use
     *
     * @throws IllegalArgumentException
     * If the id is not between 1-100 characters
     *
     * @return New button with the changed id
     */
    @Nonnull
    @CheckReturnValue
    fun withId(@Nonnull id: String?): Button? {
        Checks.notEmpty(id, "ID")
        Checks.notLonger(id, 100, "ID")
        return ButtonImpl(id, label, style, null, isDisabled, emoji) as Button?
    }

    /**
     * Returns a copy of this button with the provided url.
     *
     * @param  url
     * The url to use
     *
     * @throws IllegalArgumentException
     * If the url is null, empty, or longer than 512 characters
     *
     * @return New button with the changed url
     */
    @Nonnull
    @CheckReturnValue
    fun withUrl(@Nonnull url: String?): Button? {
        Checks.notEmpty(url, "URL")
        Checks.notLonger(url, 512, "URL")
        return ButtonImpl(null, label, ButtonStyle.LINK, url, isDisabled, emoji) as Button?
    }

    /**
     * Returns a copy of this button with the provided style.
     *
     *
     * You cannot use this convert link buttons.
     *
     * @param  style
     * The style to use
     *
     * @throws IllegalArgumentException
     * If the style is null or tries to change whether this button is a LINK button
     *
     * @return New button with the changed style
     */
    @Nonnull
    @CheckReturnValue
    fun withStyle(@Nonnull style: ButtonStyle): Button? {
        Checks.notNull(style, "Style")
        Checks.check(style != ButtonStyle.UNKNOWN, "Cannot make button with unknown style!")
        require(!(style == ButtonStyle.LINK && style != ButtonStyle.LINK)) { "You cannot change a link button to another style!" }
        require(!(style != ButtonStyle.LINK && style == ButtonStyle.LINK)) { "You cannot change a styled button to a link button!" }
        return ButtonImpl(id, label, style, url, isDisabled, emoji) as Button?
    }

    companion object {
        /**
         * Creates a button with [PRIMARY][ButtonStyle.PRIMARY] Style.
         * <br></br>The button is enabled and has no emoji attached by default.
         * You can use [.asDisabled] and [.withEmoji] to further configure it.
         *
         * @param  id
         * The custom button ID
         * @param  label
         * The text to display on the button
         *
         * @throws IllegalArgumentException
         * If any argument is empty or null, the label is longer than 80 characters, or the id is longer than 100 characters
         *
         * @return The button instance
         */
        @Nonnull
        fun primary(@Nonnull id: String?, @Nonnull label: String?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notEmpty(label, "Label")
            Checks.notLonger(id, 100, "Id")
            Checks.notLonger(label, 80, "Label")
            return ButtonImpl(id, label, ButtonStyle.PRIMARY, false, null) as Button?
        }

        /**
         * Creates a button with [PRIMARY][ButtonStyle.PRIMARY] Style.
         * <br></br>The button is enabled and has no text label.
         * To use labels you can use `primary(id, label).withEmoji(emoji)`
         *
         *
         * To disable the button you can use [.asDisabled].
         *
         * @param  id
         * The custom button ID
         * @param  emoji
         * The emoji to use as the button label
         *
         * @throws IllegalArgumentException
         * If any argument is empty or null or the id is longer than 100 characters
         *
         * @return The button instance
         */
        @Nonnull
        fun primary(@Nonnull id: String?, @Nonnull emoji: Emoji?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notNull(emoji, "Emoji")
            Checks.notLonger(id, 100, "Id")
            return ButtonImpl(id, "", ButtonStyle.PRIMARY, false, emoji) as Button?
        }

        /**
         * Creates a button with [SECONDARY][ButtonStyle.SECONDARY] Style.
         * <br></br>The button is enabled and has no emoji attached by default.
         * You can use [.asDisabled] and [.withEmoji] to further configure it.
         *
         * @param  id
         * The custom button ID
         * @param  label
         * The text to display on the button
         *
         * @throws IllegalArgumentException
         * If any argument is empty or null, the label is longer than 80 characters, or the id is longer than 100 characters
         *
         * @return The button instance
         */
        @Nonnull
        fun secondary(@Nonnull id: String?, @Nonnull label: String?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notEmpty(label, "Label")
            Checks.notLonger(id, 100, "Id")
            Checks.notLonger(label, 80, "Label")
            return ButtonImpl(id, label, ButtonStyle.SECONDARY, false, null) as Button?
        }

        /**
         * Creates a button with [SECONDARY][ButtonStyle.SECONDARY] Style.
         * <br></br>The button is enabled and has no text label.
         * To use labels you can use `secondary(id, label).withEmoji(emoji)`
         *
         *
         * To disable the button you can use [.asDisabled].
         *
         * @param  id
         * The custom button ID
         * @param  emoji
         * The emoji to use as the button label
         *
         * @throws IllegalArgumentException
         * If any argument is empty or null or the id is longer than 100 characters
         *
         * @return The button instance
         */
        @Nonnull
        fun secondary(@Nonnull id: String?, @Nonnull emoji: Emoji?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notNull(emoji, "Emoji")
            Checks.notLonger(id, 100, "Id")
            return ButtonImpl(id, "", ButtonStyle.SECONDARY, false, emoji) as Button?
        }

        /**
         * Creates a button with [SUCCESS][ButtonStyle.SUCCESS] Style.
         * <br></br>The button is enabled and has no emoji attached by default.
         * You can use [.asDisabled] and [.withEmoji] to further configure it.
         *
         * @param  id
         * The custom button ID
         * @param  label
         * The text to display on the button
         *
         * @throws IllegalArgumentException
         * If any argument is empty or null, the label is longer than 80 characters, or the id is longer than 100 characters
         *
         * @return The button instance
         */
        @Nonnull
        fun success(@Nonnull id: String?, @Nonnull label: String?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notEmpty(label, "Label")
            Checks.notLonger(id, 100, "Id")
            Checks.notLonger(label, 80, "Label")
            return ButtonImpl(id, label, ButtonStyle.SUCCESS, false, null) as Button?
        }

        /**
         * Creates a button with [SUCCESS][ButtonStyle.SUCCESS] Style.
         * <br></br>The button is enabled and has no text label.
         * To use labels you can use `success(id, label).withEmoji(emoji)`
         *
         *
         * To disable the button you can use [.asDisabled].
         *
         * @param  id
         * The custom button ID
         * @param  emoji
         * The emoji to use as the button label
         *
         * @throws IllegalArgumentException
         * If any argument is empty or null or the id is longer than 100 characters
         *
         * @return The button instance
         */
        @Nonnull
        fun success(@Nonnull id: String?, @Nonnull emoji: Emoji?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notNull(emoji, "Emoji")
            Checks.notLonger(id, 100, "Id")
            return ButtonImpl(id, "", ButtonStyle.SUCCESS, false, emoji) as Button?
        }

        /**
         * Creates a button with [DANGER][ButtonStyle.DANGER] Style.
         * <br></br>The button is enabled and has no emoji attached by default.
         * You can use [.asDisabled] and [.withEmoji] to further configure it.
         *
         * @param  id
         * The custom button ID
         * @param  label
         * The text to display on the button
         *
         * @throws IllegalArgumentException
         * If any argument is empty or null, the label is longer than 80 characters, or the id is longer than 100 characters
         *
         * @return The button instance
         */
        @Nonnull
        fun danger(@Nonnull id: String?, @Nonnull label: String?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notEmpty(label, "Label")
            Checks.notLonger(id, 100, "Id")
            Checks.notLonger(label, 80, "Label")
            return ButtonImpl(id, label, ButtonStyle.DANGER, false, null) as Button?
        }

        /**
         * Creates a button with [DANGER][ButtonStyle.DANGER] Style.
         * <br></br>The button is enabled and has no text label.
         * To use labels you can use `danger(id, label).withEmoji(emoji)`
         *
         *
         * To disable the button you can use [.asDisabled].
         *
         * @param  id
         * The custom button ID
         * @param  emoji
         * The emoji to use as the button label
         *
         * @throws IllegalArgumentException
         * If any argument is empty or null or the id is longer than 100 characters
         *
         * @return The button instance
         */
        @Nonnull
        fun danger(@Nonnull id: String?, @Nonnull emoji: Emoji?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notNull(emoji, "Emoji")
            Checks.notLonger(id, 100, "Id")
            return ButtonImpl(id, "", ButtonStyle.DANGER, false, emoji) as Button?
        }

        /**
         * Creates a button with [LINK][ButtonStyle.LINK] Style.
         * <br></br>The button is enabled and has no emoji attached by default.
         * You can use [.asDisabled] and [.withEmoji] to further configure it.
         *
         *
         * Note that link buttons never send a [ButtonClickEvent][net.dv8tion.jda.api.events.interaction.ButtonClickEvent].
         * These buttons only open a link for the user.
         *
         * @param  url
         * The target URL for this button
         * @param  label
         * The text to display on the button
         *
         * @throws IllegalArgumentException
         * If any argument is empty or null, the label is longer than 80 characters, or the url is longer than 512 characters
         *
         * @return The button instance
         */
        @Nonnull
        fun link(@Nonnull url: String?, @Nonnull label: String?): Button {
            Checks.notEmpty(url, "URL")
            Checks.notEmpty(label, "Label")
            Checks.notLonger(url, 512, "URL")
            Checks.notLonger(label, 80, "Label")
            return ButtonImpl(null, label, ButtonStyle.LINK, url, false, null) as Button
        }

        /**
         * Creates a button with [LINK][ButtonStyle.LINK] Style.
         * <br></br>The button is enabled and has no text label.
         * To use labels you can use `link(url, label).withEmoji(emoji)`
         *
         *
         * To disable the button you can use [.asDisabled].
         *
         *
         * Note that link buttons never send a [ButtonClickEvent][net.dv8tion.jda.api.events.interaction.ButtonClickEvent].
         * These buttons only open a link for the user.
         *
         * @param  url
         * The target URL for this button
         * @param  emoji
         * The emoji to use as the button label
         *
         * @throws IllegalArgumentException
         * If any argument is empty or null or the url is longer than 512 characters
         *
         * @return The button instance
         */
        @Nonnull
        fun link(@Nonnull url: String?, @Nonnull emoji: Emoji?): Button? {
            Checks.notEmpty(url, "URL")
            Checks.notNull(emoji, "Emoji")
            Checks.notLonger(url, 512, "URL")
            return ButtonImpl(null, "", ButtonStyle.LINK, url, false, emoji) as Button?
        }

        /**
         * Create a button with the provided [style][ButtonStyle], URL or ID, and label.
         * <br></br>The button is enabled and has no emoji attached by default.
         * You can use [.asDisabled] and [.withEmoji] to further configure it.
         *
         *
         * See [.link] or [.primary] for more details.
         *
         * @param  style
         * The button style
         * @param  idOrUrl
         * Either the ID or URL for this button
         * @param  label
         * The text to display on the button
         *
         * @throws IllegalArgumentException
         * If any argument is empty or null, the label is longer than 80 characters, the id is longer than 100 characters, or the url is longer than 512 characters
         *
         * @return The button instance
         */
        @Nonnull
        fun of(@Nonnull style: ButtonStyle, @Nonnull idOrUrl: String?, @Nonnull label: String?): Button {
            Checks.check(style != ButtonStyle.UNKNOWN, "Cannot make button with unknown style!")
            Checks.notNull(style, "Style")
            Checks.notNull(label, "Label")
            Checks.notLonger(label, 80, "Label")
            if (style == ButtonStyle.LINK) return link(idOrUrl, label)
            Checks.notEmpty(idOrUrl, "Id")
            Checks.notLonger(idOrUrl, 100, "Id")
            return ButtonImpl(idOrUrl, label, style, false, null) as Button
        }

        /**
         * Create a button with the provided [style][ButtonStyle], URL or ID, and [emoji][Emoji].
         * <br></br>The button is enabled and has no text label.
         * To use labels you can use `of(style, idOrUrl, label).withEmoji(emoji)`
         *
         *
         * See [.link] or [.primary] for more details.
         *
         * @param  style
         * The button style
         * @param  idOrUrl
         * Either the ID or URL for this button
         * @param  emoji
         * The emoji to use as the button label
         *
         * @throws IllegalArgumentException
         * If any argument is empty or null, the id is longer than 100 characters, or the url is longer than 512 characters
         *
         * @return The button instance
         */
        @Nonnull
        fun of(@Nonnull style: ButtonStyle, @Nonnull idOrUrl: String?, @Nonnull emoji: Emoji?): Button? {
            Checks.check(style != ButtonStyle.UNKNOWN, "Cannot make button with unknown style!")
            Checks.notNull(style, "Style")
            Checks.notNull(emoji, "Emoji")
            if (style == ButtonStyle.LINK) return link(idOrUrl, emoji)
            Checks.notEmpty(idOrUrl, "Id")
            Checks.notLonger(idOrUrl, 100, "Id")
            return ButtonImpl(idOrUrl, "", style, false, emoji) as Button?
        }

        /**
         * Create an enabled button with the provided [style][ButtonStyle], URL or ID, label and [emoji][Emoji].
         *
         *
         * You can use [.asDisabled] to disable it.
         *
         *
         * See [.link] or [.primary] for more details.
         *
         * @param  style
         * The button style
         * @param  idOrUrl
         * Either the ID or URL for this button
         * @param  label
         * The text to display on the button
         * @param  emoji
         * The emoji to use as the button label
         *
         * @throws IllegalArgumentException
         * If any of the following scenarios occurs:
         *
         *  * The style is null
         *  * You provide a URL that is null, empty or longer than 512 characters, or you provide an ID that is null, empty or longer than 100 characters
         *  * The label is non-null and longer than 80 characters
         *  * The label is null/empty, and the emoji is also null
         *
         *
         * @return The button instance
         */
        @Nonnull
        fun of(@Nonnull style: ButtonStyle, @Nonnull idOrUrl: String?, label: String?, emoji: Emoji?): Button? {
            if (label != null) return of(
                style,
                idOrUrl,
                label
            ).withEmoji(emoji) else if (emoji != null) return of(style, idOrUrl, emoji)
            throw IllegalArgumentException("Cannot build a button without a label and emoji. At least one has to be provided as non-null.")
        }
    }
}