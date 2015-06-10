import base64
import copy
import hashlib
import hmac
import math
from time import time

import sys

cur_version_major = sys.version_info.major

if cur_version_major == 2:
   #Imports for Python 2.5, 2.6, 2.7
   from urlparse import urlsplit
   from urllib import urlencode
else:
    #Imports for Python 3 and higher
    from urllib.parse import urlsplit
    from urllib.parse import urlencode


def authenticate_request(http_verb, resource_url, access_key, secret, params = {}):
    # instantiating/cloning params dictionary
    if params == None:
        params = {}
    else:
        params = copy.deepcopy(params)
    
    # Adding necessary items to the dictionary
    params["access_key"] = access_key
    if not "expires" in params:
        params["expires"] = int(math.ceil(time() + 300))


    # Generating signed url and the string to generate the signature from
    parsed_url = urlsplit(resource_url)

    str_to_sign = http_verb.lower() + "|" + parsed_url.hostname.lower() + "|" + parsed_url.path.lower() + "|"
    signed_url = resource_url + "?"

    # Iterating through keys in sorted order to build up the string to sign
    # and the signed url querystring
    items = sorted(params.items())

    for item in items:
        signed_url += urlencode({item[0] : item[1]}) + "&"
        str_to_sign += str(item[0]) + "=" + str(item[1]) + "&"

    # Removing trailing "&"
    str_to_sign = str_to_sign.strip("&")

    # Creating signature
    signature = base64.b64encode(hmac.new(secret.encode("ascii"), str_to_sign.encode("ascii"), hashlib.sha256).digest())
    signature = signature.decode("ascii")

    signed_url += urlencode({"signature" : signature})
    
    return signed_url
