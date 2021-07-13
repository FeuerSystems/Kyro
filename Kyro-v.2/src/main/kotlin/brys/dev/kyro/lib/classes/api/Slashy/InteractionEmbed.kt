package brys.dev.kyro.lib.classes.api.Slashy

import org.json.JSONArray
import org.json.JSONObject

class InteractionEmbed(val description: String?,
                       val title: String?,
                       val url: String?,
                       val color: Int?,
                       val timestamp: String?,
                       val footer: Footer?,
                       val image: Image?,
                       val thumbnail: Image?,
                       val author: Author?,
                       val fields: Array<Field>) {

    fun toJSONObject() : JSONObject {
        val embed = JSONObject()
                .put("title", title)
                .put("type", "rich")
                .put("description", description)
                .put("url", url)
                .put("timestamp", timestamp)
                .put("color", color)
        if(footer != null) {
            val footer = JSONObject()
                    .put("text", this.footer.text)
                    .put("icon_url", this.footer.icon_url)
                    .put("proxy_icon_url", this.footer.proxy_icon_url)
            embed.put("footer", footer)
        }
        if(image != null) {
            val image = JSONObject()
                    .put("url", this.image.url)
                    .put("proxy_url", this.image.proxy_url)
                    .put("height", this.image.height)
                    .put("width", this.image.width)
            embed.put("image", image)
        }
        if(thumbnail != null) {
            val thumbnail = JSONObject()
                    .put("url", this.thumbnail.url)
                    .put("proxy_url", this.thumbnail.proxy_url)
                    .put("height", this.thumbnail.height)
                    .put("width", this.thumbnail.width)
            embed.put("thumbnail", thumbnail)
        }
        if(author != null) {
            val author = JSONObject()
                    .put("name", this.author.name)
                    .put("url", this.author.url)
                    .put("icon_url", this.author.icon_url)
                    .put("proxy_icon_url", this.author.proxy_icon_url)
            embed.put("author", author)
        }
        val fields = JSONArray()
        for (field in this.fields) {
            val newField = JSONObject()
                    .put("name", field.name)
                    .put("value", field.value)
                    .put("inline", field.inline)
            fields.put(newField)
        }
        embed.put("fields", fields)
        return embed
    }

    class Footer(val text: String, val icon_url: String?, val proxy_icon_url: String?)
    class Image(val url: String, val proxy_url: String?, val height: Int?, val width: Int?)
    class Author(val name: String, val url: String?, val icon_url: String?, val proxy_icon_url: String?)
    class Field(val name: String, val value: String, val inline: Boolean?)

    class Builder {

        private var description: String? = null
        private var title: String? = null
        private var url: String? = null
        private var color: Int? = null
        private var timestamp: String? = null
        private var footer: Footer? = null
        private var image: Image? = null
        private var thumbnail: Image? = null
        private var author: Author? = null
        private val fields: ArrayList<Field> = ArrayList()

        fun setDescription(description: String) : Builder {
            this.description = description
            return this
        }

        fun setTitle(title: String) : Builder {
            this.title = title
            return this
        }

        fun setURL(url: String) : Builder {
            this.url = url
            return this
        }

        fun setColor(color: Int) : Builder {
            this.color = color
            return this
        }

        fun setTimestamp(timestamp: String) : Builder {
            this.timestamp = timestamp
            return this
        }

        fun setFooter(footer: String) : Builder {
            this.footer = Footer(footer, null, null)
            return this
        }

        fun setFooter(footer: String, icon_url: String) : Builder {
            this.footer = Footer(footer, icon_url, null)
            return this
        }

        fun setFooter(footer: Footer) : Builder {
            this.footer = footer
            return this
        }

        fun setImage(url: String) : Builder {
            this.image = Image(url, null, null, null)
            return this
        }

        fun setImage(image: Image) : Builder {
            this.image = image
            return this
        }

        fun setThumbnail(url: String) : Builder {
            this.thumbnail = Image(url, null, null, null)
            return this
        }

        fun setThumbnail(thumbnail: Image) : Builder {
            this.thumbnail = thumbnail
            return this
        }

        fun setAuthor(text: String) : Builder {
            this.author = Author(text, null, null, null)
            return this
        }

        fun setAuthor(text: String, url: String) : Builder {
            this.author = Author(text, url, null, null)
            return this
        }

        fun setAuthor(author: Author) : Builder {
            this.author = author
            return this
        }

        fun addField(name: String, value: String, inline: Boolean = false) : Builder {
            fields.add(Field(name, value, inline))
            return this
        }

        fun removeField(field: Field) : Builder {
            fields.remove(field)
            return this
        }

        fun build() : InteractionEmbed {
            return InteractionEmbed(description, title, url,
                    color, timestamp, footer, image, thumbnail,
                    author, fields.toTypedArray())
        }
    }
}