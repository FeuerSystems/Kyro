package brys.dev.kyro.lib.structures
import brys.dev.kyro.lib.structures.config.Config
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import org.bson.Document
import org.bson.codecs.pojo.PojoCodecProvider

import com.mongodb.MongoClientSettings
import org.bson.codecs.configuration.CodecRegistries


/**
 * Our MongoDatabase object handling connecting to the database itself and grabbing the right collections
 */
object MongoDatabase {
    private var conf = Config
    fun runStartup() {
            val mongoClient = MongoClient(conf.db.ip, conf.db.port.toInt())
            val db = mongoClient.getDatabase("Kyro")
            db.getCollection("GuildSettings")
            db.getCollection("UserSettings")
    }
    private var mongoClient = MongoClient(conf.db.ip, conf.db.port.toInt())
    fun serverDB(): MongoCollection<Document> {
        val database = mongoClient.getDatabase("Kyro")
        return  database.getCollection("GuildSettings")
    }
    fun userDB(): MongoCollection<Document> {
        val database = mongoClient.getDatabase("Kyro")
        val pojoCodecRegistry = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        )
        return database.getCollection("UserSettings").withCodecRegistry(pojoCodecRegistry)

    }
    fun botDB(): MongoCollection<Document> {
        val database = mongoClient.getDatabase("Kyro")
        return database.getCollection("BotData")
    }
}