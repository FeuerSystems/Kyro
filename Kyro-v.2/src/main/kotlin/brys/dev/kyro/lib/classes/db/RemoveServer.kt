package brys.dev.kyro.lib.classes.db
import brys.dev.kyro.lib.structures.MongoDatabase
import com.mongodb.BasicDBObject

/**
 * Remove a server for more efficent operation
 */
class RemoveServer(private val guildID: String) {
    private val database = MongoDatabase.serverDB()
    fun remove() {
        val query = BasicDBObject()
        query["Guild"] = guildID
        database.deleteOne(query)
    }
}