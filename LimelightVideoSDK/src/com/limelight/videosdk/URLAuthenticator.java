package com.limelight.videosdk;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;

/**
 * This class helps in signing the URL requests using developer secret and access key.
 * @author kanchan
 *
 */
class URLAuthenticator {

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
    static String authenticateRequest(String httpVerb,
            String resourceUrl, String accessKey, String secret,
            Map<String, String> params) throws InvalidKeyException,
            UnsupportedEncodingException, NoSuchAlgorithmException, URISyntaxException {
        // Creating a sorted map
        SortedMap<String, String> sortedParams = null;
        if(params != null)
            sortedParams = new TreeMap<String, String>(params);
        else
            sortedParams = new TreeMap<String, String>();

        // Adding necessary items to the dictionary
        sortedParams.put(Constants.ACCESS_KEY, accessKey);
        if (!sortedParams.containsKey(Constants.EXPIRES)) {
            sortedParams.put(Constants.EXPIRES,Long.toString((System.currentTimeMillis() / 1000) + 300));
        }

        // Generating signed url and the string to generate the signature from
        URI uri = new URI(resourceUrl);

        StringBuilder strToSignBuilder = new StringBuilder(httpVerb.toLowerCase());
        strToSignBuilder.append("|");
        strToSignBuilder.append(uri.getHost().toLowerCase());
        strToSignBuilder.append("|");
        strToSignBuilder.append(uri.getPath().toLowerCase());
        strToSignBuilder.append("|");

        StringBuilder signedUrlBuilder = new StringBuilder(resourceUrl);
        signedUrlBuilder.append("?");

        // Iterating through keys in sorted order to build up the string to sign
        // and the signed url query string
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            signedUrlBuilder.append(URLEncoder.encode(entry.getKey(),Constants.URL_CHARACTER_ENCODING_TYPE));
            signedUrlBuilder.append("=");
            signedUrlBuilder.append(URLEncoder.encode(entry.getValue(),Constants.URL_CHARACTER_ENCODING_TYPE));
            signedUrlBuilder.append("&");

            strToSignBuilder.append(entry.getKey());
            strToSignBuilder.append("=");
            strToSignBuilder.append(entry.getValue());
            strToSignBuilder.append("&");
        }

        // Removing trailing "&"
        strToSignBuilder.deleteCharAt(strToSignBuilder.length() - 1);

        // Create the signature
        String signature = signWithKey(secret,strToSignBuilder.toString());
        if(signature == null){
            throw new InvalidKeyException();
        }
        signedUrlBuilder.append("signature=");
        signedUrlBuilder.append(URLEncoder.encode(signature,Constants.URL_CHARACTER_ENCODING_TYPE));

        return signedUrlBuilder.toString();
    }

    /**
     * This method generates signature to be appended to Request URL using 
     * developer secret and access key.
     * @param key secret
     * @param data data to be signed
     * @return signed data
     */
    static String signWithKey(String key, String data){
        try{
            Key secretKey = new SecretKeySpec(key.getBytes(), Constants.SHA256_HASH_ALGORITHM);
            Mac mac = Mac.getInstance(Constants.SHA256_HASH_ALGORITHM);
            mac.init(secretKey);
            byte[] hashValue = mac.doFinal(data.getBytes(Constants.URL_CHARACTER_ENCODING_TYPE));
            return Base64.encodeToString(hashValue, Base64.DEFAULT).trim();
        }catch (Exception e){
            //do nothing.handled in authenticatRequest()
        }
        return null;
    }
}
