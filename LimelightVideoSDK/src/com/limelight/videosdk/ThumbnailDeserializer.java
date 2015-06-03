package com.limelight.videosdk;

import java.lang.reflect.Type;
import org.apache.log4j.Logger;
import android.net.Uri;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.limelight.videosdk.model.Media.MediaThumbnail;
/**
 * This class is the Deserializer for thumbnail data received in JSON response.
 * @author Nagaraju
 *
 */
class ThumbnailDeserializer implements JsonDeserializer<MediaThumbnail>,JsonSerializer<MediaThumbnail> {
    final String TAG = ThumbnailDeserializer.class.getSimpleName();

    @Override
    public JsonElement serialize(final MediaThumbnail thumbnail,final Type type,final JsonSerializationContext context) {
        return null;
    }

    @Override
    public MediaThumbnail deserialize(final JsonElement jsonElement,final Type type, final JsonDeserializationContext context){
        if (null == jsonElement) {
            // The thumbnail element is null.
            return null;
        }
        MediaThumbnail mediaThumbnail = null;
        final Logger logger = LoggerUtil.getLogger(null);//It is hack,since we dont have context here.
        if (jsonElement.isJsonArray()) {
            final JsonArray thummbnailList = jsonElement.getAsJsonArray();
            JsonObject maxWidthThumb = null;
            JsonObject tmpThumbnail = null;
            int maxWidth = 0;
            for (int j = 0; j < thummbnailList.size(); j++) {
                tmpThumbnail = thummbnailList.get(j).getAsJsonObject();
                if (null == maxWidthThumb) {
                    maxWidthThumb = tmpThumbnail;
                    // when assigning object to maxWidthThumbnailObj preserving
                    // widthOfMaxWidthThumbnail
                    try {
                        maxWidth = maxWidthThumb.get(Constants.WIDTH).getAsInt();
                    } catch (ClassCastException ex) {
                        maxWidth = 0;
                        if(logger != null){
                            logger.debug(TAG+" ClassCastException "+Constants.DESERIALIZE_ERROR);
                        }
                    } catch (IllegalStateException ex) {
                        maxWidth = 0;
                        if(logger != null){
                            logger.debug(TAG+" IllegalStateException "+Constants.DESERIALIZE_ERROR);
                        }
                    } catch (Exception ex) {
                        maxWidth = 0;
                        if(logger != null){
                            logger.debug(TAG+" Exception "+Constants.DESERIALIZE_ERROR);
                        }
                    }
                } else {
                    final JsonElement objTempJsonElement = tmpThumbnail.get(Constants.WIDTH);

                    if (!objTempJsonElement.isJsonNull()) {
                        int width = 0;
                        try {
                            width = objTempJsonElement.getAsInt();
                        } catch (ClassCastException ex) {
                            width = 0;
                            if(logger != null){
                                logger.debug(TAG+" ClassCastException "+Constants.DESERIALIZE_ERROR);
                            }
                        } catch (IllegalStateException ex) {
                            width = 0;
                            logger.debug(TAG+" IllegalStateException "+Constants.DESERIALIZE_ERROR);
                        } catch (Exception ex) {
                            width = 0;
                            if(logger != null){
                                logger.debug(TAG+" Exception "+Constants.DESERIALIZE_ERROR);
                            }
                        }
                        if (width > maxWidth) {
                            maxWidthThumb = tmpThumbnail;
                            try {
                                maxWidth = maxWidthThumb.get(Constants.WIDTH).getAsInt();
                            } catch (ClassCastException ex) {
                                maxWidth = 0;
                                if(logger != null){
                                    logger.debug(TAG+" ClassCastException "+Constants.DESERIALIZE_ERROR);
                                }
                            } catch (IllegalStateException ex) {
                                maxWidth = 0;
                                if(logger != null){
                                    logger.debug(TAG+" IllegalStateException "+Constants.DESERIALIZE_ERROR);
                                }
                            } catch (Exception ex) {
                                maxWidth = 0;
                                if(logger != null){
                                    logger.debug(TAG+" Exception "+Constants.DESERIALIZE_ERROR);
                                }
                            }
                        }
                    } else {
                        if(logger != null){
                            logger.error(TAG + " Received null json element");
                        }
                    }
                }// end of else
            }// end of for

            if (maxWidthThumb.isJsonObject()) {
                final GsonBuilder myGsonBuilder = new GsonBuilder();
                myGsonBuilder.registerTypeAdapter(Uri.class,new UriDeserializer());
                final Gson myGson = myGsonBuilder.create();
                try {
                    mediaThumbnail = myGson.fromJson(maxWidthThumb, MediaThumbnail.class);
                } catch (JsonSyntaxException ex) {
                    mediaThumbnail = null;
                    if(logger != null){
                        logger.debug(TAG+" JsonSyntaxException "+Constants.DESERIALIZE_ERROR);
                    }
                }
            }
        }
        return mediaThumbnail;
    }
}
