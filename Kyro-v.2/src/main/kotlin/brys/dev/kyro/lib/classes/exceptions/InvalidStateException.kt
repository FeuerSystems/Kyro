package brys.dev.kyro.lib.classes.exceptions

import java.lang.RuntimeException

class InvalidStateException : RuntimeException("No active JDA client has been set")