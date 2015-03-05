package com.limelight.videosdk;

import java.lang.reflect.Type;
import java.sql.Time;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * This class is the Deserializer for time data received in Json response.
 * @author kanchan
 *
 */
class TimeDeserializer implements JsonDeserializer<Time>,JsonSerializer<Time> {
    @Override
    public JsonElement serialize(Time src, Type type, JsonSerializationContext context) {
        return src == null ? null : new JsonPrimitive(src.getTime());
    }

    @Override
    public Time deserialize(JsonElement json, Type type,JsonDeserializationContext context) {
        return json == null ? null : new Time(json.getAsLong());
    }
}
