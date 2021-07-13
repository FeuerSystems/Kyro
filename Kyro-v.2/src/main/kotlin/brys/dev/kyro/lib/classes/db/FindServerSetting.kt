package brys.dev.kyro.lib.classes.db

import brys.dev.kyro.lib.structures.config.Config
import brys.dev.kyro.lib.structures.MongoDatabase
import com.mongodb.BasicDBObject
import org.bson.Document
import org.litote.kmongo.findOne
import java.awt.Color

/**
 * Find a server setting for a guild the bot has access to
 */
class FindServerSetting(private val guildID: String) {
    private val database = MongoDatabase.serverDB()
    val prefix = if (findSetting()?.get("Prefix") == null) Config.bot.prefix else findSetting()?.get("Prefix").toString()
    val DJ = findSetting()?.get("DJ")
    val color = if (findSetting()?.get("Color") == null) Color.decode("#e64c51") else Color.decode(findSetting()?.get("Color").toString())
    val djOnly = findSetting()?.get("DJOnly")
    val contributors = findSetting()?.get("Contributors").toString()
    val collection = findSettingCollection()
    private fun findSetting(): Document? {
        val query = BasicDBObject()
        query["Guild"] = guildID
        return database.findOne(query)
    }
      fun findSettingCollection(): Document? {
        val query = BasicDBObject()
        query["Guild"] = guildID
        return database.findOne(query)
    }
}