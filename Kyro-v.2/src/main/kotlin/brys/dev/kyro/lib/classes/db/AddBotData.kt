package brys.dev.kyro.lib.classes.db

import brys.dev.kyro.lib.structures.MongoDatabase
import com.mongodb.BasicDBObject
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.litote.kmongo.insertOne
import org.litote.kmongo.json


class AddBotData(private val value: String) {
    private val database = MongoDatabase.botDB()
    fun contributors() {
        return addSetting("Contributors")
    }

    private fun addSetting(name: String) {
        val obj = BasicDBObject()
        obj["Kyro"] = "Kyro"
        obj[name] = value
        when (FindBotData().collection?.get("Kyro") == null) {
            true -> database.insertOne(obj.json)
            false -> {
                val filter = Filters.eq("Kyro", FindBotData().collection?.get("Kyro"))
                val update = Updates.set(name, value)
                database.updateOne(filter, update)
            }
        }
    }
    companion object {
        private val database = MongoDatabase.botDB()
        fun addSetting(name: String) {
            val obj = BasicDBObject()
            obj["Kyro"] = "Kyro"
            obj[name] = "e"
            when (FindBotData().collection?.get("Kyro") == null) {
                true -> database.insertOne(obj.json)
                false -> {
                    val filter = Filters.eq("Kyro", FindBotData().collection?.get("Kyro"))
                    val update = Updates.set(name, "E")
                    database.updateOne(filter, update)
                }
            }
        }
    }
}