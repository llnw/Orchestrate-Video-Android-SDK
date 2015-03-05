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
        if (null == jsonElement) {
            // The thumbnail element is null.
            return null;
        }
        MediaThumbnail mediaThumbnail = null;
        if (jsonElement.isJsonArray()) {
            final JsonArray thummbnailListArray = jsonElement.getAsJsonArray();
            JsonObject maxWidthThumbnailObj = null, tmpThumbnail = null;
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
                        Logger logger = LoggerUtil.getLogger(null,LoggerUtil.sLoggerName);
                        logger.debug("In MediaThumbnail deserialize, exception raised when creating integer from json");
                    } catch (IllegalStateException ex) {
                        widthOfMaxWidthThumbnail = 0;
                        Logger logger = LoggerUtil.getLogger(null,LoggerUtil.sLoggerName);
                        logger.debug("In MediaThumbnail deserialize, exception raised when creating integer from json");
                    } catch (Exception ex) {
                        widthOfMaxWidthThumbnail = 0;
                        Logger logger = LoggerUtil.getLogger(null,LoggerUtil.sLoggerName);
                        logger.debug("In MediaThumbnail deserialize, exception raised when creating integer from json");
                    }
                } else {
                    JsonElement objTempJsonElement = tmpThumbnail.get(Constants.WIDTH);

                    if (false == objTempJsonElement.isJsonNull()) {
                        int width = 0;
                        try {
                            width = objTempJsonElement.getAsInt();
                        } catch (ClassCastException ex) {
                            width = 0;
                            Logger logger = LoggerUtil.getLogger(null,LoggerUtil.sLoggerName);
                            logger.debug("In MediaThumbnail deserialize, exception raised when creating integer from json");
                        } catch (IllegalStateException ex) {
                            width = 0;
                            Logger logger = LoggerUtil.getLogger(null,LoggerUtil.sLoggerName);
                            logger.debug("In MediaThumbnail deserialize, exception raised when creating integer from json");
                        } catch (Exception ex) {
                            width = 0;
                            Logger logger = LoggerUtil.getLogger(null,LoggerUtil.sLoggerName);
                            logger.debug("In MediaThumbnail deserialize, exception raised when creating integer from json");
                        }
                        if (width > widthOfMaxWidthThumbnail) {
                            maxWidthThumbnailObj = tmpThumbnail;
                            try {
                                widthOfMaxWidthThumbnail = maxWidthThumbnailObj.get(Constants.WIDTH).getAsInt();
                            } catch (ClassCastException ex) {
                                widthOfMaxWidthThumbnail = 0;
                                Logger logger =  LoggerUtil.getLogger(null,LoggerUtil.sLoggerName);
                                logger.debug("In MediaThumbnail deserialize, exception raised when creating integer from json");
                            } catch (IllegalStateException ex) {
                                widthOfMaxWidthThumbnail = 0;
                                Logger logger = LoggerUtil.getLogger(null,LoggerUtil.sLoggerName);
                                logger.debug("In MediaThumbnail deserialize, exception raised when creating integer from json");
                            } catch (Exception ex) {
                                widthOfMaxWidthThumbnail = 0;
                                Logger logger = LoggerUtil.getLogger(null,LoggerUtil.sLoggerName);
                                logger.debug("In MediaThumbnail deserialize, exception raised when creating integer from json");
                            }
                        }
                    } else {
                        Logger logger = LoggerUtil.getLogger(null,LoggerUtil.sLoggerName);
                        logger.error("Received null json element");
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
                    Logger logger = LoggerUtil.getLogger(null,LoggerUtil.sLoggerName);
                    logger.debug("In MediaThumbnail deserialize, exception raised for fromJson call");
                }
            }
        }
        return mediaThumbnail;
    }
}
