package brys.dev.kyro.lib.methods

import org.slf4j.LoggerFactory
import java.lang.Exception
import kotlin.system.exitProcess





/**
 * Logger class
 */
class Logger(private val c: Class<Any>) {
    var logger: org.slf4j.Logger = LoggerFactory.getLogger(c)
    fun debug(s: String) {
        return this.logger.debug(s)
    }
    fun info(s: String) {
        return this.logger.info(s)
    }
    fun warning(s: String) {
        return this.logger.warn(s)
    }
    fun severe(err: Exception, fatal: Boolean) {
        this.logger.error("\u001B[31m┌────────────────────────────⚠️─────────────────────────┐")
        this.logger.error("  Message  │ ${err.message} \u001B[0m")
        this.logger.error("\u001B[33m  Type     │ ${err.javaClass.simpleName} \u001B[0m")
        this.logger.error("\u001B[32m  Location │ ${this.c.name} (${err.stackTrace.last()})\u001B[0m")
        this.logger.error("\u001B[34m  Fatal    │ $fatal\u001B[0m")
        this.logger.error("\u001B[31m└──────────────────────────────────────────────────────────┘\u001B[0m")
        when (fatal) {
            true -> exitProcess(1)
            false -> return
        }
    }
}

