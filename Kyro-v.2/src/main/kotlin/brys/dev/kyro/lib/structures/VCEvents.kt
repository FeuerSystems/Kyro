package brys.dev.kyro.lib.structures

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import org.json.JSONObject

object VCEvents {
     class VCJOIN(val event: GuildVoiceJoinEvent) {
        override fun toString(): String {
            /**
             * Pre-grab
             */
            val members = ArrayList<JSONObject>()
            for (i in 0 until event.channelJoined.members.size) {
                val member = event.channelJoined.members[i]
                        members.add(JSONObject()
                            .put("id", member.id)
                            .put("name", member.user.name)
                            .put("name_ctx", member.effectiveName)
                            .put("avatar", member.user.effectiveAvatarUrl))
            }
            return JSONObject()
                .put("type", "VC_JOIN")
                .put(
                    "vc", JSONObject()
                        .put("id", event.channelJoined.id)
                        .put("name", event.channelJoined.name)
                        .put("members", members)
                        .put("joinable", if (event.channelJoined.getPermissionOverride(event.channelJoined.guild.selfMember)?.denied?.contains(Permission.VOICE_CONNECT) == null) true else if (event.channelJoined.getPermissionOverride(event.channelJoined.guild.selfMember)?.denied?.contains(Permission.VOICE_CONNECT) == false) true else event.channelJoined.getPermissionOverride(event.channelJoined.guild.selfMember)?.denied?.contains(Permission.VOICE_CONNECT))
                )
                .put(
                    "member", JSONObject()
                        .put("id", event.member.id)
                        .put("name", event.member.user.name)
                        .put("name_ctx", event.member.effectiveName)
                        .put("avatar", event.member.user.effectiveAvatarUrl)
                ).toString()

        }
    }

    class VCLEAVE(val event: GuildVoiceLeaveEvent) {
        override fun toString(): String {
            /**
             * Pre-grab
             */
            val members = ArrayList<JSONObject>()
            for (i in 0 until event.channelLeft.members.size) {
                val member = event.channelLeft.members[i]
                members.add(
                    JSONObject()
                        .put("id", member.id)
                        .put("name", member.user.name)
                        .put("name_ctx", member.effectiveName)
                        .put("avatar", member.user.effectiveAvatarUrl)
                )
            }
            return JSONObject()
                .put("type", "VC_LEAVE")
                .put(
                    "vc", JSONObject()
                        .put("id", event.channelLeft.id)
                        .put("name", event.channelLeft.name)
                        .put("members", members)
                        .put(
                            "joinable",
                            if (event.channelLeft.getPermissionOverride(event.channelLeft.guild.selfMember)?.denied?.contains(
                                    Permission.VOICE_CONNECT
                                ) == null
                            ) true else if (event.channelLeft.getPermissionOverride(event.channelLeft.guild.selfMember)?.denied?.contains(
                                    Permission.VOICE_CONNECT
                                ) == false
                            ) true else event.channelLeft.getPermissionOverride(event.channelLeft.guild.selfMember)?.denied?.contains(
                                Permission.VOICE_CONNECT
                            )
                        )
                )
                .put(
                    "member", JSONObject()
                        .put("id", event.member.id)
                        .put("name", event.member.user.name)
                        .put("name_ctx", event.member.effectiveName)
                        .put("avatar", event.member.user.effectiveAvatarUrl)
                ).toString()
        }
    }
    class VCCHANGE(val event: GuildVoiceMoveEvent) {
        override fun toString(): String {
            /**
             * Pre-grab
             */
            val membersLeft = ArrayList<JSONObject>()
            val membersJoined = ArrayList<JSONObject>()
            for (i in 0 until event.channelJoined.members.size) {
                val member = event.channelJoined.members[i]
                membersJoined.add(JSONObject()
                    .put("id", member.id)
                    .put("name", member.user.name)
                    .put("name_ctx", member.effectiveName)
                    .put("avatar", member.user.effectiveAvatarUrl))
            }
            for (i in 0 until event.channelLeft.members.size) {
                val member = event.channelJoined.members[i]
                membersJoined.add(JSONObject()
                    .put("id", member.id)
                    .put("name", member.user.name)
                    .put("name_ctx", member.effectiveName)
                    .put("avatar", member.user.effectiveAvatarUrl))
            }
            return JSONObject()
                .put("type", "VC_CHANGE")
                .put(
                    "vc_joined", JSONObject()
                        .put("id", event.channelJoined.id)
                        .put("name", event.channelJoined.name)
                        .put("members", membersJoined)
                        .put("joinable", if (event.channelJoined.getPermissionOverride(event.channelJoined.guild.selfMember)?.denied?.contains(Permission.VOICE_CONNECT) == null) true else if (event.channelJoined.getPermissionOverride(event.channelJoined.guild.selfMember)?.denied?.contains(Permission.VOICE_CONNECT) == false) true else event.channelJoined.getPermissionOverride(event.channelJoined.guild.selfMember)?.denied?.contains(Permission.VOICE_CONNECT))
                )
                .put(
                    "vc_left", JSONObject()
                        .put("id", event.channelLeft.id)
                        .put("name", event.channelLeft.name)
                        .put("members", membersLeft)
                        .put("joinable", if (event.channelLeft.getPermissionOverride(event.channelLeft.guild.selfMember)?.denied?.contains(Permission.VOICE_CONNECT) == null) true else if (event.channelLeft.getPermissionOverride(event.channelLeft.guild.selfMember)?.denied?.contains(Permission.VOICE_CONNECT) == false) true else event.channelLeft.getPermissionOverride(event.channelLeft.guild.selfMember)?.denied?.contains(Permission.VOICE_CONNECT))
                )
                .put(
                    "member", JSONObject()
                        .put("id", event.member.id)
                        .put("name", event.member.user.name)
                        .put("name_ctx", event.member.effectiveName)
                        .put("avatar", event.member.user.effectiveAvatarUrl)
                ).toString()

        }
    }
}