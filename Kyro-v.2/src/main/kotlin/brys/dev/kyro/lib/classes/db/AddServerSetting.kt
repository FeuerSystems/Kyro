package brys.dev.kyro.lib.classes.db

import brys.dev.kyro.lib.structures.MongoDatabase
import com.mongodb.BasicDBObject
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.litote.kmongo.*

/**
 * Add a setting to the server [prefix] [DJ] [color]
 */
class AddServerSetting(private val guildID: String, private val value: String) {
    private val database = MongoDatabase.serverDB()
    fun prefix() {
       return addSetting("Prefix")
    }
    fun DJ() {
        return addSetting("DJ")
    }
    fun color(){
        return addSetting("Color")
    }
    fun contributors() {
        return addSetting("Contributors")
    }
    fun djOnly() {
        return addSetting("DJOnly")
    }
    private fun addSetting(name: String) {
        val obj = BasicDBObject()
        obj["Guild"] = guildID
        obj[name] = value
        when (FindServerSetting(guildID).collection?.get("Guild") == null) {
            true ->  database.insertOne(obj.json)
            false -> {
                val filter = Filters.eq("Guild", FindServerSetting(guildID).findSettingCollection()?.get("Guild"))
                val update = Updates.set(name, value)
                database.updateOne(filter, update)
            }
        }
    }
}