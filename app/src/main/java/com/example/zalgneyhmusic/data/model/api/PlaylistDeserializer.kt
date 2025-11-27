//package com.example.zalgneyhmusic.data.model.api
//
//import com.google.gson.*
//import java.lang.reflect.Type
//
///**
// * Custom deserializer for PlaylistDTO to support songs being array of IDs or array of Song objects
// */
//class PlaylistDeserializer : JsonDeserializer<PlaylistDTO> {
//    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PlaylistDTO {
//        val obj = json.asJsonObject
//
//        fun JsonElement?.asStringOrNull(): String? =
//            if (this == null || isJsonNull) null else try { asString } catch (_: Exception) { null }
//
//        val id = obj.get("_id").asString
//        val name = obj.get("name").asString
//        val description = obj.get("description").asStringOrNull()
//        val imageUrl = obj.get("imageUrl").asStringOrNull()
//        val isPublic = obj.get("isPublic")?.let { if (it.isJsonNull) true else it.asBoolean } ?: true
//        val createdBy = obj.get("createdBy").asStringOrNull()
//        val createdAt = obj.get("createdAt").asStringOrNull()
//        val updatedAt = obj.get("updatedAt").asStringOrNull()
//
//        // songs field can be list of strings (ids) or list of objects { _id: ... }
//        val songsElement = obj.get("songs")
//        val songIds: MutableList<String> = mutableListOf()
//        if (songsElement != null && songsElement.isJsonArray) {
//            val arr = songsElement.asJsonArray
//            for (el in arr) {
//                when {
//                    el.isJsonPrimitive && el.asJsonPrimitive.isString -> songIds.add(el.asString)
//                    el.isJsonObject -> {
//                        val songObj = el.asJsonObject
//                        val sid = songObj.get("_id").asStringOrNull()
//                            ?: songObj.get("id").asStringOrNull()
//                        if (sid != null) songIds.add(sid)
//                    }
//                }
//            }
//        }
//
//        return PlaylistDTO(
//            id = id,
//            name = name,
//            description = description,
//            songIds = songIds,
//            imageUrl = imageUrl,
//            isPublic = isPublic,
//            createdBy = createdBy,
//            createdAt = createdAt,
//            updatedAt = updatedAt
//        )
//    }
//}
//
