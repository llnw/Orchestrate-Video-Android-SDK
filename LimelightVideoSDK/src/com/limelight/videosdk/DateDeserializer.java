package com.limelight.videosdk;

import java.lang.reflect.Type;
import java.util.Date;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * This class is the Deserializer for date data received in Json response.
 * @author kanchan
 *
 */
class DateDeserializer implements JsonDeserializer<Date>, JsonSerializer<Date> {

    @Override
    public JsonElement serialize(Date src, Type type,JsonSerializationContext ctx) {
        // We need the time interval as seconds dividing with 1000 to convert it
        // to seconds
        return src == null ? null : new JsonPrimitive(src.getTime() / 1000);
    }

    @Override
    public Date deserialize(JsonElement json, Type type,JsonDeserializationContext ctx){
        // We get the time interval as seconds multiplying with 1000 to convert
        // it to milliseconds
        return json == null ? null : new Date(json.getAsLong() * 1000);
    }
}
