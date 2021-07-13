package brys.dev.kyro.lib.classes.db

import brys.dev.kyro.lib.structures.MongoDatabase
import com.mongodb.BasicDBObject
import org.bson.Document
import org.litote.kmongo.findOne

class FindBotData {
    private val database = MongoDatabase.botDB()
    val collection = findSettingCollection()
    val contributors = findSettingCollection()?.get("Contributors").toString()
    private fun findSettingCollection(): Document? {
        val query = BasicDBObject()
        query["Kyro"] = "Kyro"
        return database.findOne(query)
    }
}