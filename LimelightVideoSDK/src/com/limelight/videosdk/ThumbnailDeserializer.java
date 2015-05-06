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
 * This class is the Deserializer for thumbnail data received in Json response.
 * @author kanchan
 *
 */
class ThumbnailDeserializer implements JsonDeserializer<MediaThumbnail>,JsonSerializer<MediaThumbnail> {

    @Override
    public JsonElement serialize(MediaThumbnail thumbnail, Type type,JsonSerializationContext context) {
        return null;
    }

    @Override
    public MediaThumbnail deserialize(JsonElement jsonElement,Type type, JsonDeserializationContext context){
        final String TAG = ThumbnailDeserializer.class.getSimpleName();
        if (null == jsonElement) {
            // The thumbnail element is null.
            return null;
        }
        MediaThumbnail mediaThumbnail = null;
        Logger logger = LoggerUtil.getLogger(null);//It is hack,since we dont have context here.
        if (jsonElement.isJsonArray()) {
            final JsonArray thummbnailListArray = jsonElement.getAsJsonArray();
            JsonObject maxWidthThumbnailObj = null;
            JsonObject tmpThumbnail = null;
            int widthOfMaxWidthThumbnail = 0;
            for (int j = 0; j < thummbnailListArray.size(); j++) {
                tmpThumbnail = thummbnailListArray.get(j).getAsJsonObject();
                if (null == maxWidthThumbnailObj) {
                    maxWidthThumbnailObj = tmpThumbnail;
                    // when assigning object to maxWidthThumbnailObj preserving
                    // widthOfMaxWidthThumbnail
                    try {
                        widthOfMaxWidthThumbnail = maxWidthThumbnailObj.get(Constants.WIDTH).getAsInt();
                    } catch (ClassCastException ex) {
                        widthOfMaxWidthThumbnail = 0;
                        if(logger != null){
                            logger.debug(TAG+" ClassCastException "+Constants.DESERIALIZE_ERROR);
                        }
                    } catch (IllegalStateException ex) {
                        widthOfMaxWidthThumbnail = 0;
                        if(logger != null){
                            logger.debug(TAG+" IllegalStateException "+Constants.DESERIALIZE_ERROR);
                        }
                    } catch (Exception ex) {
                        widthOfMaxWidthThumbnail = 0;
                        if(logger != null){
                            logger.debug(TAG+" Exception "+Constants.DESERIALIZE_ERROR);
                        }
                    }
                } else {
                    final JsonElement objTempJsonElement = tmpThumbnail.get(Constants.WIDTH);

                    if (false == objTempJsonElement.isJsonNull()) {
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
                        if (width > widthOfMaxWidthThumbnail) {
                            maxWidthThumbnailObj = tmpThumbnail;
                            try {
                                widthOfMaxWidthThumbnail = maxWidthThumbnailObj.get(Constants.WIDTH).getAsInt();
                            } catch (ClassCastException ex) {
                                widthOfMaxWidthThumbnail = 0;
                                if(logger != null){
                                    logger.debug(TAG+" ClassCastException "+Constants.DESERIALIZE_ERROR);
                                }
                            } catch (IllegalStateException ex) {
                                widthOfMaxWidthThumbnail = 0;
                                if(logger != null){
                                    logger.debug(TAG+" IllegalStateException "+Constants.DESERIALIZE_ERROR);
                                }
                            } catch (Exception ex) {
                                widthOfMaxWidthThumbnail = 0;
                                if(logger != null){
                                    logger.debug(TAG+" Exception "+Constants.DESERIALIZE_ERROR);
                                }
                            }
                        }
                    } else {
                        logger.error(TAG + " Received null json element");
                    }
                }// end of else
            }// end of for

            if (maxWidthThumbnailObj.isJsonObject()) {
                GsonBuilder myGsonBuilder = new GsonBuilder();
                myGsonBuilder.registerTypeAdapter(Uri.class,new UriDeserializer());
                Gson myGson = myGsonBuilder.create();
                try {
                    mediaThumbnail = myGson.fromJson(maxWidthThumbnailObj, MediaThumbnail.class);
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
