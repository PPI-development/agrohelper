package com.example.a123

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class FloatArrayAdapter : JsonDeserializer<FloatArray> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): FloatArray {
        val jsonArray = json.asJsonArray
        val floatArray = FloatArray(jsonArray.size())
        for (i in 0 until jsonArray.size()) {
            floatArray[i] = jsonArray[i].asFloat
        }
        return floatArray
    }
}
