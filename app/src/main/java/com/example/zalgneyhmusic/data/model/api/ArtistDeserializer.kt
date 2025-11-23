package com.example.zalgneyhmusic.data.model.api

import com.google.gson.*
import java.lang.reflect.Type

class ArtistDeserializer : JsonDeserializer<ArtistDTO?> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ArtistDTO? {
        if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
            return ArtistDTO(
                id = json.asString,
                name = "Unknown Artist",
                imageUrl = null
            )
        }
        return Gson().fromJson(json, ArtistDTO::class.java)
    }
}