package com.example.zalgneyhmusic.data.model.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Custom deserializer for AlbumDTO to handle flexible artist field
 * Backend may send:
 * - artist as STRING (artist ID) - OLD format
 * - artist as OBJECT (full Artist object) - NEW format
 */
class AlbumDeserializer : JsonDeserializer<AlbumDTO> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): AlbumDTO {
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
            artistElement == null || artistElement.isJsonNull -> {
                ArtistDTO(
                    id = "",
                    name = "Unknown Artist",
                    bio = null,
                    imageUrl = null,
                    followers = 0,
                    verified = false,
                    createdAt = null,
                    updatedAt = null
                )
            }

            artistElement.isJsonPrimitive && artistElement.asJsonPrimitive.isString -> {
                val artistId = artistElement.asString
                ArtistDTO(
                    id = artistId,
                    name = "Unknown Artist",
                    bio = null,
                    imageUrl = null,
                    followers = 0,
                    verified = false,
                    createdAt = null,
                    updatedAt = null
                )
            }

            artistElement.isJsonObject -> {
                val deserializedArtist =
                    context.deserialize<ArtistDTO>(artistElement, ArtistDTO::class.java)
                deserializedArtist ?: ArtistDTO(
                    id = "",
                    name = "Unknown Artist",
                    bio = null,
                    imageUrl = null,
                    followers = 0,
                    verified = false,
                    createdAt = null,
                    updatedAt = null
                )
            }
            else -> {
                ArtistDTO(
                    id = "",
                    name = "Unknown Artist",
                    bio = null,
                    imageUrl = null,
                    followers = 0,
                    verified = false,
                    createdAt = null,
                    updatedAt = null
                )
            }
        }

        val releaseYear = obj.get("releaseYear").asIntOrNull()
        val coverImage = obj.get("coverImage").asStringOrNull()
        val imageUrl = obj.get("imageUrl").asStringOrNull()
        val description = obj.get("description").asStringOrNull()
        val totalTracks = obj.get("totalTracks").asIntOrNull() ?: 0
        val createdAt = obj.get("createdAt").asStringOrNull()
        val updatedAt = obj.get("updatedAt").asStringOrNull()

        return AlbumDTO(
            id = id,
            title = title,
            artist = artistDto,
            releaseYear = releaseYear,
            coverImage = coverImage,
            imageUrl = imageUrl,
            description = description,
            totalTracks = totalTracks,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
