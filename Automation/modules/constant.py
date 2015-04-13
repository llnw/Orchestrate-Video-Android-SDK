import os
import ConfigParser
config_data = ConfigParser.ConfigParser()
config_data.read(os.path.join(os.path.abspath(os.path.dirname(__file__)),'..',
                                 'config', 'system.cfg'))

ser_ip = str(config_data.get('Appium', 'server-ip')).strip()
ser_port = str(config_data.get('Appium', 'server-port')).strip()
APPIUM_SERVER = "%s:%s"%(ser_ip, ser_port)

TEST_TARGET_CFG = {
 'os': str(config_data.get('Device-Under-Test', 'os')).strip(),
 'os-version': str(config_data.get('Device-Under-Test', 'version')).strip(),
 'device-name': str(config_data.get('Device-Under-Test', 'device-name')).strip()
}

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





