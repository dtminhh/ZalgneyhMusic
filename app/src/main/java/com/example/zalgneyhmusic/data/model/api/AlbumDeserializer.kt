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

        // 1. [FIX] Handle JsonArray: Backend returns [] (empty) or [{...}]
        if (json.isJsonArray) {
            val array = json.asJsonArray
            return if (array.size() > 0) {
                // Array has elements, deserialize first element
                context.deserialize(array[0], AlbumDTO::class.java)
            } else {
                // Empty array [], return empty Album to avoid crash
                createEmptyAlbum()
            }
        }

        // 2. Handle Null explicitly
        if (json.isJsonNull) {
            return createEmptyAlbum()
        }

        // 3. Safety check: If Album is String ID -> Return empty Album to avoid crash
        if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
            return createEmptyAlbum().copy(id = json.asString)
        }

        // 4. Main logic: Handle JsonObject
        return try {
            val obj = json.asJsonObject

            // Helper functions for safe extraction
            fun JsonElement?.asStringOrNull(): String? =
                if (this == null || isJsonNull) null else try {
                    asString
                } catch (_: Exception) { null }

            fun JsonElement?.asIntOrNull(): Int? {
                if (this == null || isJsonNull) return null
                return try {
                    if (asJsonPrimitive.isNumber) asInt else asString.toIntOrNull()
                } catch (_: Exception) { null }
            }

            val id = if (obj.has("_id")) obj.get("_id").asString else ""
            val title = if (obj.has("title")) obj.get("title").asString else ""

            // Handle artist field (string id or object)
            val artistElement = obj.get("artist")
            val artistDto: ArtistDTO = when {
                artistElement == null || artistElement.isJsonNull -> ArtistDTO(id = "", name = "Unknown", imageUrl = null)

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

            AlbumDTO(
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
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback if JSON object parsing fails
            createEmptyAlbum()
        }
    }

    /**
     * Helper function to create an empty Album (default).
     */
    private fun createEmptyAlbum(): AlbumDTO {
        return AlbumDTO(
            id = "",
            title = "",
            artist = ArtistDTO(
                id = "",
                name = "Unknown Artist",
                imageUrl = null
            ),
            songs = emptyList()
        )
    }
}