package com.limelight.videosdk;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.Logger;
import android.util.Base64;

/**
 * This class helps in signing the URL requests using developer secret and access key.
 * @author kanchan
 *
 */
final class URLAuthenticator {

    private URLAuthenticator(){}
    /**
     * Method to sign the request url with customer application secret and access key.
     * @param httpVerb httpMethod
     * @param resourceUrl requestUrl
     * @param accessKey accessKey
     * @param secret secret
     * @param params map of parameters
     * @return signed Request URL
     * @throws InvalidKeyException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws URISyntaxException
     */
    static String authenticateRequest(final String httpVerb,
            final String resourceUrl, final String accessKey, final String secret,
            final Map<String, String> params) throws InvalidKeyException,
            UnsupportedEncodingException, NoSuchAlgorithmException, URISyntaxException {
        // Creating a sorted map
        SortedMap<String, String> sortedParams = null;
        if(params == null){
            sortedParams = new TreeMap<String, String>();
        }
        else{
            sortedParams = new TreeMap<String, String>(params);
        }

        // Adding necessary items to the dictionary
        sortedParams.put(Constants.ACCESS_KEY, accessKey);
        if (!sortedParams.containsKey(Constants.EXPIRES)) {
            sortedParams.put(Constants.EXPIRES,Long.toString(System.currentTimeMillis() / 1000 + 300));
        }

        // Generating signed url and the string to generate the signature from
        final URI uri = new URI(resourceUrl);

        StringBuilder strToSignBuilder = new StringBuilder(httpVerb.toLowerCase(Locale.ENGLISH));
        strToSignBuilder = strToSignBuilder.append("|");
        strToSignBuilder.append(uri.getHost().toLowerCase(Locale.ENGLISH));
        strToSignBuilder = strToSignBuilder.append("|");
        strToSignBuilder.append(uri.getPath().toLowerCase(Locale.ENGLISH));
        strToSignBuilder = strToSignBuilder.append("|");

        StringBuilder signedUrlBuilder = new StringBuilder(resourceUrl);
        signedUrlBuilder = signedUrlBuilder.append("?");

        // Iterating through keys in sorted order to build up the string to sign
        // and the signed url query string
        for (final Map.Entry<String, String> entry : sortedParams.entrySet()) {
            signedUrlBuilder.append(URLEncoder.encode(entry.getKey(),Constants.ENCODING));
            signedUrlBuilder = signedUrlBuilder.append("=");
            signedUrlBuilder.append(URLEncoder.encode(entry.getValue(),Constants.ENCODING));
            signedUrlBuilder = signedUrlBuilder.append("&");

            strToSignBuilder.append(entry.getKey());
            strToSignBuilder = strToSignBuilder.append("=");
            strToSignBuilder.append(entry.getValue());
            strToSignBuilder = strToSignBuilder.append("&");
        }

        // Removing trailing "&"
        strToSignBuilder.deleteCharAt(strToSignBuilder.length() - 1);

        // Create the signature
        final String signature = signWithKey(secret,strToSignBuilder.toString());
        if(signature == null){
            throw new InvalidKeyException();
        }
        signedUrlBuilder = signedUrlBuilder.append("signature=");
        signedUrlBuilder.append(URLEncoder.encode(signature,Constants.ENCODING));

        return signedUrlBuilder.toString();
    }

    /**
     * This method generates signature to be appended to Request URL using 
     * developer secret and access key.
     * @param key secret
     * @param data data to be signed
     * @return signed data
     */
    static String signWithKey(final String key, final String data){
        final String TAG = AnalyticsReporter.class.getSimpleName();
        final Logger mLogger = LoggerUtil.getLogger(null);//It is hack,since we dont have context here.
        try{
            final Key secretKey = new SecretKeySpec(key.getBytes(), Constants.SHA256_HASH);
            final Mac mac = Mac.getInstance(Constants.SHA256_HASH);
            mac.init(secretKey);
            final byte[] hashValue = mac.doFinal(data.getBytes(Constants.ENCODING));
            return Base64.encodeToString(hashValue, Base64.DEFAULT).trim();
        }catch (IllegalArgumentException e){
            if(mLogger != null){
                mLogger.error(TAG + e==null?Constants.AUTH_ERROR:e.getMessage());
            }
        }
        catch (NoSuchAlgorithmException e){
            if(mLogger != null){
                mLogger.error(TAG + e==null?Constants.AUTH_ERROR:e.getMessage());
            }
        }
        catch (InvalidKeyException e){
            if(mLogger != null){
                mLogger.error(TAG + e==null?Constants.AUTH_ERROR:e.getMessage());
            }
        }
        catch (UnsupportedEncodingException e){
            if(mLogger != null){
                mLogger.error(TAG + e==null?Constants.AUTH_ERROR:e.getMessage());
            }
        }
        return null;
    }
}
