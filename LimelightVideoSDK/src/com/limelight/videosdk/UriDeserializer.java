package com.limelight.videosdk;

import java.lang.reflect.Type;
import android.net.Uri;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * This class is the Deserializer for URI data received in Json response.
 * @author Nagaraju
 *
 */
class UriDeserializer implements JsonDeserializer<Uri>,JsonSerializer<Uri> {
    @Override
    public JsonElement serialize(Uri src, Type type,JsonSerializationContext context) {
        return src == null ? null : new JsonPrimitive(src.toString());
    }

    @Override
    public Uri deserialize(JsonElement json, Type type,JsonDeserializationContext context){
        return json==null?null:Uri.parse(json.getAsString());
    }
}
