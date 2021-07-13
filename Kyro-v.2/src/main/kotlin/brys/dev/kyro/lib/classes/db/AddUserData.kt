package brys.dev.kyro.lib.classes.db

import brys.dev.kyro.lib.structures.MongoDatabase
import com.mongodb.BasicDBObject
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.bson.Document
import org.litote.kmongo.insertOne
import org.litote.kmongo.json

class AddUserData(private val user: String, private val value: Any) {
    val db = MongoDatabase.userDB()
    fun playlist() {
       addONToSetting("playlists")
    }
    fun listeningTime() {
        addSetting("listening")
    }
    fun totalTracks() {
        addSetting("t_tracks")
    }
    fun totalQueued() {
        addSetting("t_queued")
    }

     fun addSetting(name: String) {
        val obj = BasicDBObject()
        obj["user"] = user
        obj[name] = value
        when (FindUserData(user).collection?.get("user") == null) {
            true -> db.insertOne(obj.json)
            false -> {
                val filter = Filters.eq("user", FindUserData(user).collection?.get("user"))
                val update = Updates.set(name, value)
                db.updateOne(filter, update)
            }
        }
    }

    private fun addONToSetting(name: String) {
        val obj = BasicDBObject()
        val list = mutableListOf<Any>()
        list.add(value)
        obj["user"] = user
        obj[name] = list
        when (FindUserData(user).collection?.get("user") == null) {
            true -> db.insertOne(obj.json)
            false -> {
                if (FindUserData(user).collection?.get(name) == null) {
                    val new = mutableListOf<Any>()
                    new.add(value)
                    val filter = Filters.eq("user", FindUserData(user).collection?.get("user"))
                    val update = Updates.set(name, new)
                     db.updateOne(filter, update)
                    return
                }
                val previous = FindUserData(user).collection?.get(name) as ArrayList<*>
                 val new = mutableListOf<Any>()
                for (i in 0 until previous.size) {
                    val doc = previous[i] as Document
                    new.add(doc)
                }
               new.add(value)
                val filter = Filters.eq("user", FindUserData(user).collection?.get("user"))
                val update = Updates.set(name, new)
                db.updateOne(filter, update)
            }
        }
    }
}