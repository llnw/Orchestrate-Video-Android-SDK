import requests
import lvp_auth_util as limelight_auth
from constant import BASE_URL, ACCESS_KEY, SECRET_KEY, ORG_ID

class limelightCMS(object):
    def __init__(self, base_url = BASE_URL, org_id = ORG_ID,
                 secret_key = SECRET_KEY, access_key = ACCESS_KEY):
        """
        Authenticate and perform operations on limelight CMS.
        param base_url: <string> base url for calling APIs.
        param org_id: <string> organization id.
        param secret_key: <string> secret key.
        param access_key: <string> access key.
        """
        self.base_url = base_url
        self.org_id = org_id
        self.secret_key = secret_key
        self.access_key = access_key
 
        self.requestUrlPrefix = self.base_url+ "/organizations/" + \
                                self.org_id + "/"
        
    def authenticateUrl(self, action, url, params={}):
        """
        Authenticate the URL to  perform operations on limelight CMS.
        
        param action: <string> action to perform - POST, PUT, GET, DELETE, etc.
        param url: <string> action url.
        param params: <dict> additional parameters.
        """
        action = action.upper()
        signedRequest = limelight_auth.authenticate_request(action, url,
                                                            self.access_key,
                                                            self.secret_key,
                                                            params)
        return signedRequest
        
    def getPublishedMedia(self):    
        """
        Get a List of All 'published' Channels.
        """
        requestUrl = self.requestUrlPrefix + "media/search.json"
        requestData = {"and" : "state:published"}
        signedRequest = self.authenticateUrl("GET", requestUrl, requestData)
        channel_array = doRequest("GET", signedRequest)
        return channel_array['media_list']

    def getMediaID(self, media_title):
        """
        Get media id for the given media_title.
        param media_title: <string> Title of the media to get media_id.
        """
        requestUrl = self.requestUrlPrefix + "media/search.json"
        requestData = {"and" : "title:" + media_title}
        signedRequest = self.authenticateUrl("GET", requestUrl, requestData)
        channel_array = doRequest("GET", signedRequest)
        media_id = channel_array['media_list'][0]['media_id'] \
            if channel_array['media_list'] else ''
        return media_id

    def getMediaInfo(self, param_name, param_value):
        """
        Get media info for the given param and value
        param param_name: <string> Name of the param based on which to search the media.
        param param_value: <string> Value of the param based on which to search the media.
        """
        requestUrl = self.requestUrlPrefix + "media/search.json"
        requestData = {"and" : "%s:%s" % (param_name.lower(), param_value)}
        signedRequest = self.authenticateUrl("GET", requestUrl, requestData)
        channel_array = doRequest("GET", signedRequest)
        media_info = channel_array['media_list'][0] \
            if channel_array['media_list'] else {}
        return media_info
        
    def updateMedia(self, media_title, param_name, param_value):
        """
        Update media with provide param and value
        param media_title: <string> Title of the media to get media_id.
        param param_name: <string> Name of the param to be updated.
        param param_value: <string> Value of the param to be updated.
        """
        media_id = self.getMediaID(media_title)
        requestUrl = self.requestUrlPrefix + "media/" + media_id + "/properties"
        signedRequest = self.authenticateUrl("PUT", requestUrl)
        requestData = {param_name:param_value}
        media_details = doRequest("PUT", signedRequest, requestData)
        return media_details
    
    def setMediaState(self, media_title, state):
        """
        Set the state of the media, publish / unpublish
        param state: <string> Media state to be changed
        """
        media_id = self.getMediaID(media_title)
        requestUrl = self.requestUrlPrefix + "media/" + media_id + "/" + state + ".json"
        signedRequest = self.authenticateUrl("POST", requestUrl)
        channel_array = doRequest("POST", signedRequest)
        return channel_array  

        
def doRequest(action, url, requestData={}):
    """
    Open up a url an return the data
    
    param url: <string> Final url to open
    param action: <string> HTTP method
    param requestData: <dict> Additional params to be send along with URL.
    """
    
    def __doGetRequest(url):
        response = requests.get(url)
        return response
     
    def __doPutRequest(url, requestData = {}):
        response = requests.put(url,
                     data=requestData,
                     headers={'content-type':'application/x-www-form-urlencoded'}
                  )
        return response
        
    def __doPostRequest(url, requestData = {}):
        response = requests.post(url,
                     data=requestData,
                     headers={'content-type':'application/x-www-form-urlencoded'}
                  )
        return response
    
    if action == 'GET':
        resp = __doGetRequest(url)
    elif action == 'PUT':
        resp = __doPutRequest(url, requestData)
    elif action == 'POST':
        resp = __doPostRequest(url, requestData)
    
    return resp.json()

if __name__ == '__main__':

    CMS = limelightCMS()
    print CMS.updateMedia("Adobe", "description", "updated media")
    print CMS.updateMedia("Avengers Age of Ultron", "description", "updated media")
    #print CMS.getMediaInfo("title", "Adobe")
    #print "*"*100
    #print CMS.setMediaState("Adobe", "publish")     
    #print "*"*100
    #print CMS.getMediaInfo("title", "Adobe")
    