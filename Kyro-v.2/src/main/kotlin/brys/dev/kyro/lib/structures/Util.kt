@file:Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")

package brys.dev.kyro.lib.structures


import brys.dev.kyro.lib.classes.api.Slashy.SlashCommand
import brys.dev.kyro.lib.classes.db.FindUserData
import brys.dev.kyro.lib.structures.config.Config
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.explodingbush.ksoftapi.KSoftAPI
import okhttp3.OkHttpClient
import okhttp3.Request
import org.bson.Document
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import oshi.SystemInfo
import oshi.hardware.CentralProcessor
import java.awt.Color
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.math.round


/**
 * All kinds of utils :3
 */
object Util {
    val client = OkHttpClient()
    val lyrics = KSoftAPI(Config.music.ksoftToken).lyrics
    val lyricCache = HashMap<String, String>()
    /**
     * All kinds of Miscellaneous methods
     */
    internal object Misc {
        var members = 0
        fun getLyrics(search:  String): String {
            if (lyricCache[search] != null) return lyricCache[search]!!
            val request = Request.Builder()
                .url("https://api.ksoft.si/lyrics/search?q=${search}&&limit=1&&textonly=true")
                .addHeader("Authorization", "Bearer ${Config.music.ksoftToken}")
                .get()
                .build()
            val response = client.newCall(request).execute()
            val raw: String? = response.body?.string()
            val json: JSONObject = JSONParser().parse(raw) as JSONObject
            var lyrics = "No lyrics found :("
            val data: JSONArray = json["data"] as JSONArray
            for (i in 0 until data.size) {
                val obj: JSONObject = data[i] as JSONObject
                if (obj != null) {
                    lyrics =  obj["lyrics"] as String
                }
            }
            lyricCache[search] = lyrics
            return lyrics
        }
        fun isJSON(js: String): Boolean {
            try {
                JSONParser().parse(js) as org.json.simple.JSONObject
                return true
            } catch (e: ParseException) {
                return false
            }
        }
         fun validateUser(token: String, user: String): Boolean {
                 val request = Request.Builder()
                 .url("https://discord.com/api/users/@me")
                 .addHeader("Authorization", "Bearer $token")
                 .get()
                 .build()
                 val response = client.newCall(request).execute()
                 val raw: String? = response.body?.string()
                 val json: JSONObject = JSONParser().parse(raw) as JSONObject
                 if (response.code != 200 || json["id"] != user) {
                     return false
                 }
                 return true
             }
        fun retrieveVideoJSON(videoID: String, part: String, APIkey: String): String? {
            val postURL = "https://www.googleapis.com/youtube/v3/videos?id=$videoID&part=$part&key=$APIkey"
            var output = ""
            try {
                val url = URL(postURL)
                val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
                val br1 = BufferedReader(
                    InputStreamReader(
                        conn.inputStream
                    )
                )
                var line1: String
                while (br1.readLine().also { line1 = it } != null) {
                    output += line1
                }
                conn.disconnect()
                br1.close()
            } catch (e: IOException) {
                println(
                    """
                
                e = ${e.message}
                
                """.trimIndent()
                )
            }
            return output
        }
        fun colorStatus(ping: Long): Color {
            return when {
                ping <= 150 -> {
                    Color.green
                }
                ping <= 200 -> {
                    Color.YELLOW
                }
                else -> {
                    Color.red
                }
            }
        }

        fun isUrl(input: String): Boolean {
            return try {
                URL(input)
                true
            } catch (ignored: MalformedURLException) {
                false
            }
        }

        fun getOS(): String? {
            return System.getProperty("os.name")
        }

        fun getSize(size: Long): String {
            val n: Long = 1024
            var s = ""
            val kb = size.toDouble() / n
            val mb = kb / n
            val gb = mb / n
            val tb = gb / n
            if (size < n) {
                s = "$size Bytes"
            } else if (size >= n && size < n * n) {
                s = String.format("%.2f", kb) + " KB"
            } else if (size >= n * n && size < n * n * n) {
                s = String.format("%.2f", mb) + " MB"
            } else if (size >= n * n * n && size < n * n * n * n) {
                s = String.format("%.2f", gb) + " GB"
            } else if (size >= n * n * n * n) {
                s = String.format("%.2f", tb) + " TB"
            }
            return s
        }

        val String.isInt: Boolean
            get() {
                return try {
                    this.toInt()
                    true
                } catch (e: NumberFormatException) {
                    false
                }
            }
        fun Double.round(decimals: Int): Double {
            var multiplier = 1.0
            repeat(decimals) { multiplier *= 10 }
            return round(this * multiplier) / multiplier
        }
    }

    /**
     * Methods for providing Time related Functions
     */
    internal object Time {
        private val TIMESTAMP_PATTERN = Pattern.compile("^(\\d?\\d)(?::([0-5]?\\d))?(?::([0-5]?\\d))?$")
        fun formatMili(ms: Long): String {
            val hours = ms / TimeUnit.HOURS.toMillis(1)
            val minutes = ms / TimeUnit.MINUTES.toMillis(1)
            val seconds = ms % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        @Throws(NumberFormatException::class)
        fun seekTime(str: String): Long {
            val millis: Long
            var seconds: Long = 0
            var minutes: Long = 0
            var hours: Long = 0
            val m = TIMESTAMP_PATTERN.matcher(str)
            check(m.find()) { "Unable to match $str" }
            var capturedGroups = 0
            if (m.group(1) != null) capturedGroups++
            if (m.group(2) != null) capturedGroups++
            if (m.group(3) != null) capturedGroups++
            when (capturedGroups) {
                0 -> throw IllegalStateException("Unable to match $str")
                1 -> seconds = m.group(1).toInt().toLong()
                2 -> {
                    minutes = m.group(1).toInt().toLong()
                    seconds = m.group(2).toInt().toLong()
                }
                3 -> {
                    hours = m.group(1).toInt().toLong()
                    minutes = m.group(2).toInt().toLong()
                    seconds = m.group(3).toInt().toLong()
                }
            }
            minutes += hours * 60
            seconds += minutes * 60
            millis = seconds * 1000
            return millis
        }

        fun convertToTitleCase(text: String?): String? {
            if (text == null || text.isEmpty()) {
                return text
            }
            val converted = StringBuilder()
            var convertNext = true
            for (ch: Char in text.toCharArray()) {
                if (Character.isSpaceChar(ch)) {
                    convertNext = true
                } else if (convertNext) {
                    var char = Character.toTitleCase(ch)
                    convertNext = false
                } else {
                    var char = Character.toLowerCase(ch)
                }
                converted.append(ch)
            }
            return converted.toString()
        }

        /**
         * Convert a millisecond duration to a string format
         *
         * @param millis A duration to convert to a string form
         * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
         */
        fun getDurationBreakdown(millis: Long): String? {
            var millis = millis
            require(millis >= 0) { "Duration must be greater than zero!" }
            val days = TimeUnit.MILLISECONDS.toDays(millis)
            millis -= TimeUnit.DAYS.toMillis(days)
            val hours = TimeUnit.MILLISECONDS.toHours(millis)
            millis -= TimeUnit.HOURS.toMillis(hours)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
            millis -= TimeUnit.MINUTES.toMillis(minutes)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
            val sb = StringBuilder(64)
            if (days > 0L) {
                sb.append(days)
                if (days == 1L) {
                    sb.append(" Day ")
                } else {
                    sb.append(" Days ")
                }
            }
            if (hours > 0L) {
                sb.append(hours)
                if (hours == 1L) {
                    sb.append(" Hour ")
                } else {
                    sb.append(" Hours ")
                }
            }
            if (minutes > 0L) {
                sb.append(minutes)
                if (minutes == 1L) {
                    sb.append(" Minute ")
                } else {
                    sb.append(" Minutes ")
                }
            }
            if (seconds > 0L) {
                sb.append(seconds)
                if (seconds == 1L) {
                    sb.append(" Second ")
                } else {
                    sb.append(" Seconds ")
                }
            }
            return sb.toString()
        }
    }

    internal object CPU {
        private val si = SystemInfo()
        private val hal = si.hardware
        private val cpu: CentralProcessor = hal.processor
        private var prevTicks = LongArray(CentralProcessor.TickType.values().size)
        fun getCPULoad(): Double {
            val cpuLoad: Double = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100
            prevTicks = cpu.systemCpuLoadTicks
            return cpuLoad
        }

        fun getLiveThreads(): Int {
            return Thread.getAllStackTraces().size
        }

        fun getParkedThreads(): Int {
            return Thread.getAllStackTraces().size - Thread.activeCount()
        }
    }

    internal object Voice {
        fun getActiveListeners(voice: GuildVoiceState): Int {
            var listeners = 0
            for (i in voice.channel!!.members) {
                if (i.voiceState!!.isGuildDeafened || i.voiceState!!.isSelfDeafened) listeners -= 1
                listeners += 1
            }
            return listeners
        }

        fun getActiveListenersMember(voice: GuildVoiceState): List<Member> {
            val listeners = mutableListOf<Member>()
            for (i in voice.channel!!.members) {
                if (i.voiceState!!.isGuildDeafened || i.voiceState!!.isSelfDeafened) listeners.remove(i)
                listeners.add(i)
            }
            listeners.remove(voice.guild.selfMember)
            return listeners
        }
    }

    internal object UserData {
        fun sameNamePlaylist(user: String, playlistName: String): Boolean {
            val data =
                if (FindUserData(user).collection?.get("playlists") == null) listOf() else FindUserData(user).collection?.get(
                    "playlists"
                ) as ArrayList<*>
            var sameName = false
            for (element in data) {
                val playlist = element as Document
                if (playlist["name"] == playlistName) {
                    sameName = true
                }
            }
            return sameName
        }
        fun playlistSize(user: String, playlistName: String): Int {
            val data =
                if (FindUserData(user).collection?.get("playlists") == null) listOf() else FindUserData(user).collection?.get(
                    "playlists"
                ) as ArrayList<*>
            return data.size
        }
    }

    internal object Strings {
        fun makeRandomString(length: Int): String {
            val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
            return (1..length)
                .map { charset.random() }
                .joinToString("")
        }

        fun stringAsBoolean(str: String): Boolean? {
            return if (str.startsWith("true", true)) {
                true
            } else if (str.startsWith("false", true)) {
                false
            } else
                null
        }

        fun flags(content: String, vararg flags: String) {
            val data = mutableMapOf<String, String>()
            for (s in flags.indices) {
                val unparsed = content.substringAfter("--${s}")
                var parsed = StringBuilder()
                for (i in 0 until s) {
                    val toBeRemoved = flags[i]
                    val removed = unparsed.substringBefore("--${toBeRemoved}")
                parsed.append(removed.removePrefix("--"))
            }
            println(parsed)
            }

        }
    }

    internal object Bot {
        fun makeBot(): JDA {
            val bot = JDABuilder.createDefault(
                Config.bot.token,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS
            ).disableCache(CacheFlag.EMOTE)
                .build()
            return bot
        }

        fun getAppID(bot: JDA): String {
            var id = ""
            bot.retrieveApplicationInfo().queue { a -> id = a.id }
            return id
        }

        fun getSlashCommands(): List<SlashCommand> {
            val list = mutableListOf<SlashCommand>()
            val help = SlashCommand.Builder()
                .setName("Help")
                .setDescription("Sends the help for the bot's commands ℹ️")
                .build()!!
            val DJOnly = SlashCommand.Builder().setName("DJ Only")
                .setDescription("Whether or not you want only the DJ role to control music commands \uD83D\uDCA1")
                .build()!!
            val DJ = SlashCommand.Builder().setName("DJ")
                .setDescription("Whether or not you want only the DJ role to control music commands \uD83D\uDCA1")
                .build()!!
            list.add(help)
            list.add(DJOnly)
            list.add(DJ)
            return list
        }
    }

    internal object Arrays {
        fun spotifyUrl(url: String): String {
            var path = url.split("/".toRegex())
            for (i in path) {
                path = i.split("?")
            }
            return path[0]
        }

        fun loopTrimArray(list: List<Any>): String {
            val converted = list.toMutableList()
            for (i in converted.indices) {
                val element = list[i]
                if (element != converted.first()) {
                    converted.remove(element)
                }
            }
            return converted.joinToString()
        }
    }
}


