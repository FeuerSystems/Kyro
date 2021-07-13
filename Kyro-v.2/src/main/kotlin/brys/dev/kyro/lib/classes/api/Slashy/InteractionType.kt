package brys.dev.kyro.lib.classes.api.Slashy

object InteractionType {
    /**
     * Tell the api you received the interaction
     */
    const val pong = 1

    /**
     * Tell the api you received the interaction and don't send the user input in the channel
     */
    const val acknowledge = 2

    /**
     * Respond with a message and don't send the user input in the channel
     */
    const val channelMessage = 3

    /**
     * Respond with a message and send the user input in the channel
     */
    const val channelMessageWithSource = 4

    /**
     * Tell the api you received the interaction and send the user input in the channel
     */
    const val acknowledgeWithSource = 5
}