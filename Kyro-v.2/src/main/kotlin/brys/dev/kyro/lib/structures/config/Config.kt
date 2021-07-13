package brys.dev.kyro.lib.structures.config
import brys.dev.kyro.lib.methods.Logger
import brys.dev.kyro.lib.structures.config.data.APIJSON
import brys.dev.kyro.lib.structures.config.data.BotJSON
import brys.dev.kyro.lib.structures.config.data.DBJSON
import brys.dev.kyro.lib.structures.config.data.MusicJSON
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter

/**
 * Did someone say, Config Controller 👀
 */
object Config {
   private val botJSON = readJson("bot") as JSONObject
   private val musicJSON = readJson("music") as JSONObject
   private val dbJSON = readJson("db") as JSONObject
   private val apiJSON = readJson("api") as JSONObject
   val bot = BotJSON(
       botJSON["token"] as String,
       botJSON["prefix"] as String,
       botJSON["owner_id"] as String,
       botJSON["coOwners"] as JSONArray,
       botJSON["error_channel"] as String,
       botJSON["support_server"] as String
   )
   val music = MusicJSON(
       musicJSON["ksoft_token"] as String,
       musicJSON["yt_token"] as String
   )
   val db = DBJSON(
       dbJSON["port"] as Long,
       dbJSON["ip"] as String
   )
   val API = APIJSON(
       apiJSON["port"] as Long
   )

    /**
     * Create a Json file if not already present
     */
    private fun createJson() {
        try {
            JSONParser().parse(FileReader("config.json"))
        } catch (e: FileNotFoundException) {
            val uri = "config.json"
           Logger(this.javaClass).warning("$uri file was not found.")
            try {
                File(uri).createNewFile()
            } catch (e: SecurityException) {
                Logger(this.javaClass).severe(e, true)
            }
            val writer = FileWriter(uri)
            writer.write(File("./typings/example.json").readText())
            writer.close()
            Logger(this.javaClass).severe(e, true)
        }
    }
    /**
     * Read the json to be used.
     */
    private fun readJson(str: String): Any? {
        createJson()
        val obj: Any = JSONParser().parse(FileReader("config.json"))
        val jObj: JSONObject = obj as JSONObject
        return jObj[str]
    }
}


