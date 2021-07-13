package brys.dev.kyro.lib.interfaces.WS

  data class SubscribeToEvent(val user: String, val token: String, val events: ArrayList<String>)
