package brys.dev.kyro.lib.structures.config.data

import org.json.simple.JSONArray


data class BotJSON(val token: String, val prefix: String, val owner: String, val owners: JSONArray, val errorLog: String, val supportServer: String)