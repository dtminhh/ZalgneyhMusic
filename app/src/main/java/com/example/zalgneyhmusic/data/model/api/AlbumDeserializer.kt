package com.example.zalgneyhmusic.data.model.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class AlbumDeserializer : JsonDeserializer<AlbumDTO> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): AlbumDTO {

// 1. Safety check: If Album is String ID -> Return empty Album to avoid crash
        if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
            return AlbumDTO(
                id = json.asString,
                title = "",
                artist = ArtistDTO(
                    id = "",
                    name = "Unknown Artist",
                    imageUrl = null
                ),
                songs = emptyList() // Trả về list rỗng
            )
        }

        val obj = json.asJsonObject

        fun JsonElement?.asStringOrNull(): String? =
            if (this == null || isJsonNull) null else try {
                asString
            } catch (_: Exception) {
                null
            }

        fun JsonElement?.asIntOrNull(): Int? {
            if (this == null || isJsonNull) return null
            return try {
                if (asJsonPrimitive.isNumber) asInt else asString.toIntOrNull()
            } catch (_: Exception) {
                null
            }
        }

        val id = obj.get("_id").asString
        val title = obj.get("title").asString

        // Handle artist field (string id or object)
        val artistElement = obj.get("artist")
        val artistDto: ArtistDTO = when {
            artistElement == null || artistElement.isJsonNull -> ArtistDTO(
                id = "",
                name = "Unknown",
                imageUrl = null
            )

            artistElement.isJsonPrimitive && artistElement.asJsonPrimitive.isString -> ArtistDTO(
                id = artistElement.asString,
                name = "Unknown",
                imageUrl = null
            )

            artistElement.isJsonObject -> context.deserialize(artistElement, ArtistDTO::class.java)
                ?: ArtistDTO(id = "", name = "Unknown", imageUrl = null)

            else -> ArtistDTO(id = "", name = "Unknown", imageUrl = null)
        }

        val releaseYear = obj.get("releaseYear").asIntOrNull()
        val coverImage = obj.get("coverImage").asStringOrNull()
        val imageUrl = obj.get("imageUrl").asStringOrNull()
        val description = obj.get("description").asStringOrNull()
        val totalTracks = obj.get("totalTracks").asIntOrNull() ?: 0
        val createdAt = obj.get("createdAt").asStringOrNull()
        val updatedAt = obj.get("updatedAt").asStringOrNull()

        // Parse the song list ("songs")
        val songsElement = obj.get("songs")
        val songsList: List<SongDTO> = if (songsElement != null && songsElement.isJsonArray) {
            // Gson automatically parses each element in the array into a SongDTO
            val listType = object : TypeToken<List<SongDTO>>() {}.type
            context.deserialize(songsElement, listType)
        } else {
            emptyList()
        }

        return AlbumDTO(
            id = id,
            title = title,
            artist = artistDto,
            releaseYear = releaseYear,
            coverImage = coverImage,
            imageUrl = imageUrl,
            description = description,
            totalTracks = totalTracks,
            songs = songsList,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
