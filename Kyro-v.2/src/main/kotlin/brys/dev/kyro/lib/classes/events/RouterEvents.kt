package brys.dev.kyro.lib.classes.events

import brys.dev.kyro.lib.methods.Logger

class RouterEvents {
    private val logger = Logger(this.javaClass)
    fun log(string: String) {
        logger.info(string)
    }
}