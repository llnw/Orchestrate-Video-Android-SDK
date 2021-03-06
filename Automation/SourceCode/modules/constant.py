"""
keeping the constant values here
"""
import os
import ConfigParser

# Configuration constant
CONFIG_DATA = ConfigParser.ConfigParser()
CONFIG_DATA.read(os.path.join(os.path.abspath(os.path.dirname(__file__)),
                                 '..', 'config', 'system.cfg'))

# Appium constant
APPIUM_SERVER_IP = str(CONFIG_DATA.get('Appium', 'server-ip')).strip()
APPIUM_SERVER_PORT = str(CONFIG_DATA.get('Appium', 'server-port')).strip()
APPIUM_SERVER = "%s:%s" % (APPIUM_SERVER_IP, APPIUM_SERVER_PORT)

# Target device constant
TEST_TARGET_CFG = {
 'os': str(CONFIG_DATA.get('Device-Under-Test', 'os')).strip(),
 'os-version': str(CONFIG_DATA.get('Device-Under-Test',
                                   'version')).strip(),
 'device-name': str(CONFIG_DATA.get('Device-Under-Test',
                                    'device-name')).strip(),
 'app-name': str(CONFIG_DATA.get('App', 'title')).strip(),
}

# Limelight project constant
LIME_LIGHT_OBJ = None
LONG_WAIT = 6
MEDIUM_WAIT = 3
SORT_WAIT = 1

MAPPER = {'settings': "tab-item-left-half",
          'channel groups': "tab-item-left-half",
          'all channels': "tab-item-left-half",

          'channels': "tab-item-right-half",
          'all media': "tab-item-right-half",
          'media': "tab-item-right-half",
          'player': "tab-item-right-half"}

LEFT_MOST_TAB = 'SETTINGS'
TAB_SCROLL = {'settings-tab':3}

FETCHING_MEDIA_MSG = "Fetching Media From Server"
WIDEVINE_OFFLINE_DOWNLOAD_MSG = "Downloading. Please Wait..."

SCREEN_SHOT_DIR = os.path.join(os.path.abspath(os.path.dirname(__file__)),
                               '..', 'screenShots')

FORWARD_SEC = 15
REVERSE_SEC = 5

ICON_DIR = os.path.join(SCREEN_SHOT_DIR, "player_btn")

SCENARIO_DATA = {}
SCENARIO_EVENT_KY = "SCENARIO_WISE_PLAYER_EVENT"

BASE_URL = "https://staging-api.lvp.llnw.net/rest"
ACCESS_KEY = "6QDyPljwRS8L2w7Q7AnRo3sYIoQ="
SECRET_KEY = "+d62cBI73hxWcRPpput4RR7a8v8="
ORG_ID = "a851c50193064ed6be08c7e75f8f4910"