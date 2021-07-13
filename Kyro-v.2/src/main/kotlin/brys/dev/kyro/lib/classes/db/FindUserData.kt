package brys.dev.kyro.lib.classes.db

import brys.dev.kyro.lib.structures.MongoDatabase
import com.mongodb.BasicDBObject
import org.bson.Document
import org.litote.kmongo.findOne

class FindUserData(private val user: String) {
    val db = MongoDatabase.userDB()
    val collection = findSettingCollection()
    private fun findSettingCollection(): Document? {
        val query = BasicDBObject()
        query["user"] = user
        return db.findOne(query)
    }
}