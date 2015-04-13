#-------------------------------------------------------------------------------
# Name:        Appium Lib
# Purpose:
#
# Author:      gouri
#
# Created:     09-02-2015
# Copyright:   (c) gouri 2015
# Licence:     <your licence>
#-------------------------------------------------------------------------------
'''
This module is use to communicating with the limelight app using the Appium
application. The Appium application install on a remote MAC machine.
We will run the script in our local machine using this library, this library
is basically communicating with that remote machine, where Appium is running
and android device has plugged in or the emulator is running.

To start emulator : emulator -avd <emulator name>
e.g. emulator -avd MyAVD
'''
import sys
import os
import datetime
import random
import pickle
#import selenium
from appium import webdriver
from time import sleep
import ConfigParser

from logger import info, error, warning, exception

def input_handlar(fn):
    def wrapper(*args, **kwargs):
        tmp_args = []
        for ei in args:
            if type(ei) == unicode:
                try:
                    tmp_args.append(eval(ei))
                except:
                    tmp_args.append(str(ei))
            else:
                tmp_args.append(ei)

        tmp_kwargs = {}
        for k, v in kwargs.iteritems():
            if type(v) == unicode:
                try:
                    tmp_kwargs[k] = eval(v)
                except:
                    tmp_kwargs[k] = str(v)
            else:
                tmp_kwargs[k] = v
        return fn(*tmp_args, **tmp_kwargs)
    return wrapper

class Driver(object):

    def __init__(self, remote_host, platform, version, device_name):
        '''
        '''
        self.remote_host_url = 'http://%s/wd/hub'%remote_host
        if not sys.argv[-1].endswith("dump_file.pkl"):
            sys.argv.append(datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S_dump_file.pkl"))
        self.dump_file_name = sys.argv[-1]
        self.tap_on_cordinate = {'x':300 , 'y':500}
        self.screen_shot_file_num = 1
        self.desired_caps = {}
        self.load_config_data()
        self.test_on(platform, version, device_name)


    def load_config_data(self):
        self.config_data = ConfigParser.ConfigParser()
        self.config_data.read(os.path.join(os.path.abspath(os.path.dirname(__file__)), '..',
                                 'config', 'element-path.cfg'))
        self.config_data = {ek: dict(ev) for ek, ev in dict(self.config_data._sections).iteritems()}

    def test_on(self, platform, version, device_name):
        '''
        '''
        if platform.lower().strip() == "android":
            self.desired_caps['platformName'] = 'Android'
            self.desired_caps['platformVersion'] = str(version)
            self.desired_caps['deviceName'] = device_name
            info("TEST ON ANDROID ::: %s"%self.desired_caps)
        else:
            self.desired_caps['appium-version'] = '1.0'
            self.desired_caps['platformName'] = 'iOS'
            self.desired_caps['platformVersion'] = str(version)
            self.desired_caps['deviceName'] = device_name
            info("TEST ON IOS ::: %s" % self.desired_caps)


    def set_up(self):
        "Setup for the test"
        info("CONNECTING APPIUM ON %s"%self.remote_host_url)
        self.driver = webdriver.Remote(self.remote_host_url, self.desired_caps)
        self.driver.implicitly_wait(10)
        info("CONNECTED APPIUM ON %s"%self.remote_host_url)

    def exit_testcase(self):
        fp = open(self.dump_file_name, 'wb')
        pickle.dump(self.driver, fp)
        fp.close()
        return self.dump_file_name

    def start_testcase(self):
        fp = open(self.dump_file_name, 'rb')
        self.driver = pickle.load(fp)
        fp.close()

    def tear_down(self):
        "Tear down the test"
        self.driver.quit()

    def refresh_driver(self):
        self.driver.refresh()

    def take_screenshot(self, fileName=""):
        screenShotFileName = "Screenshot-%d.png"%self.screen_shot_file_num if not fileName else fileName
        self.screen_shot_file_num += 1
        return self.driver.save_screenshot(screenShotFileName)

    def value_should_contains(self, ele, expected_value, generic_param=()):
        ele_obj = self.get_element(ele, generic_param=generic_param)
        if str(ele_obj.text).strip() not in str(expected_value).strip():
            msg = "ele: %s::%s, actual val recv: %s, val doesn't contains: %s"
            raise Exception(msg % (ele, generic_param,
                                   ele_obj.text, expected_value))

    def value_should_be(self, ele, expected_value, generic_param=()):
        ele_obj = self.get_element(ele, generic_param=generic_param)
        if str(ele_obj.text).strip() != str(expected_value).strip():
            raise Exception("Expected value: %s, Got value: %s"%(expected_value,
                                                                 ele_obj.text))

    def get_element_by_xpath(self, ele_xpath):
        obj =  self.driver.find_elements_by_xpath(ele_xpath)
        if not obj : raise Exception("Element not found for xpath: %s"%ele_xpath)
        return obj if isinstance(obj, list) else [obj]

    def get_element_by_id(self, ele_id):
        obj = self.driver.find_elements_by_id(ele_id)
        if not obj : raise Exception("Element not found for id: %s"%ele_id)
        return obj if isinstance(obj, list) else [obj]

    def get_element_by_link_text(self, text):
        obj = self.driver.find_elements_by_link_text(text) # find_element_by_link_text
        if not obj : raise Exception("Element not found for text: %s"%text)
        return obj if isinstance(obj, list) else [obj]

    def find_element(self, ele_data, generic_param=()):
        if 'xpath' in ele_data:
            ele_obj_list = self.get_element_by_xpath(ele_data['xpath']%generic_param)
        elif 'id' in ele_data:
            ele_obj_list = self.get_element_by_id(ele_data['id']%generic_param)
        elif 'text' in ele_data:
            ele_obj_list = self.get_element_by_link_text(ele_data['text']%generic_param)
        else:
            raise Exception("Provide element xpath or id in config file")
        return ele_obj_list

    def get_elements(self, ele, generic_param=()):
        ele = str(ele).strip()
        if ele in self.config_data:
            ele_data = self.config_data[ele]
            ele_obj_list = []
            try:
                ele_obj_list = self.find_element(ele_data, generic_param=generic_param)
            except Exception as ex :
                pass
            return ele_obj_list
        else:
            raise Exception("Element %s not found in config file"%ele)

    def get_element(self, ele, generic_param=()):
        ele = str(ele).strip()
        ele_obj = None
        if ele in self.config_data:
            ele_data = self.config_data[ele]
            scroll_h = int(ele_data['scroll-h'])
            scroll_v = int(ele_data['scroll-v'])
            search_flag = int(ele_data.get('search-flag','0'))
            ele_obj_list = None

            # Searching in current screen
            if search_flag:
                try:
                    ele_obj_list = self.find_element(ele_data, generic_param=generic_param)
                except Exception as ex :
                    pass

            # Performing Horizontal scroll
            if scroll_h != 0 and not ele_obj_list:
                left_x, right_x = 5, 670
                scrol_typ = 'H'
                if scroll_h < 0:
                    from_point = [left_x, int(ele_data['tap-point'].split(',')[:2][1])]
                    to_point = [right_x, from_point[1]]
                else:
                    from_point = [right_x, int(ele_data['tap-point'].split(',')[:2][1])]
                    to_point = [left_x, from_point[1]]

                for e_itr in xrange(abs(scroll_h)):
                    self.scroll(scrol_typ, direction=None, frm_pt=from_point, to_pt=to_point)
                    if search_flag:
                        try:
                            ele_obj_list = self.find_element(ele_data, generic_param=generic_param)
                            break
                        except Exception as ex :
                            pass

            # Performing Vertical scroll
            if scroll_v != 0 and not ele_obj_list:
                top_y, bottom_y = 242, 1098
                scrol_typ = 'V'
                if scroll_v < 0:
                    from_point = [int(ele_data['tap-point'].split(',')[:2][0]), top_y]
                    to_point = [from_point[0], bottom_y]
                else:
                    from_point = [int(ele_data['tap-point'].split(',')[:2][0]), bottom_y]
                    to_point = [from_point[0], top_y]

                for e_itr in xrange(abs(scroll_v)):
                    self.scroll(scrol_typ, direction=None, frm_pt=from_point, to_pt=to_point)
                    if search_flag:
                        try:
                            ele_obj_list = self.find_element(ele_data, generic_param=generic_param)
                            break
                        except Exception as ex :
                            pass

            if not search_flag and not ele_obj_list:
                try:
                    ele_obj_list = self.find_element(ele_data, generic_param=generic_param)
                except Exception as ex :
                    pass

            if ele_obj_list:
                return ele_obj_list[int(ele_data['position']) - 1]
            else:
                raise Exception("Element - %s not found."%ele)
        else:
            raise Exception("Element %s not found in config file"%ele)

    def get_element_my_algo(self, ele, generic_param=()):
        if str(ele) in self.config_data:
            ele_data = self.config_data[str(ele)]
            frm_pt, to_pt, itr = [], [], 1
            last_scroll = 'H'

            if 'scroll' not in ele_data:
                # Normal case
                ele_obj_list = self.find_element(ele_data, generic_param=generic_param)

            elif ele_data['scroll'].strip().upper() in ['H', 'V']:
                # Either horizontal or vertical
                tot_scroll = int(ele_data['retry-scroll-num'])
                scrol_typ = ele_data['scroll']

                frm_pt = ele_data['from-point'].split(',')
                frm_pt = [int(i) for i in frm_pt if i]

                to_pt = ele_data['to-point'].split(',')
                to_pt = [int(i) for i in to_pt if i]

                for ech_scroll in xrange(tot_scroll):
                    try:
                        ele_obj_list = self.find_element(ele_data, generic_param=generic_param)
                        break # If got the element then don't need to do furthur scroll
                    except Exception as ex :
                        if ech_scroll == tot_scroll - 1:
                            # If is is last turn then raise the exception;
                            raise
                        if str(ex).lower() == "element not found.":
                            # Perform the scroll
                            self.scroll(scrol_typ, direction=None, frm_pt=frm_pt[:2], to_pt=to_pt[:2])

            else:
                # We need to search through the entire screen
                h_scroll_num = int(ele_data['horizontal-scroll-num'])
                v_scroll_num = int(ele_data['vertical-scroll-num'])

                h_frm_pt = ele_data['horizontal-from-point'].split(',')
                h_frm_pt = [int(i) for i in h_frm_pt if i]

                h_to_pt = ele_data['horizontal-to-point'].split(',')
                h_to_pt = [int(i) for i in h_to_pt if i]

                v_frm_pt = ele_data['vertical-from-point'].split(',')
                v_frm_pt = [int(i) for i in v_frm_pt if i]

                v_to_pt = ele_data['vertical-to-point'].split(',')
                v_to_pt = [int(i) for i in v_to_pt if i]

                # When the window move from up to down the vertical cordinate will be same
                # when we move down to up then the vertical cordinate will exchange
                # the from will be to and the to will be from
                for ech_h_slide in xrange(h_scroll_num):
                    # Horizontal slide
                    for ech_v_slide in xrange(v_scroll_num):
                        # Vertical scroll
                        try:
                            ele_obj_list = self.find_element(ele_data, generic_param=generic_param)
                            break
                        except Exception as ex :
                            if ech_v_slide == v_scroll_num - 1:
                                # Don't perform the last scroll becos the it won't enter the loop
                                pass
                            else:
                                self.scroll('V', direction=None, frm_pt=v_frm_pt[:2], to_pt=v_to_pt[:2])
                    else:
                        # If it is a vertically down then it should go up next time
                        v_frm_pt, v_to_pt = v_to_pt, v_frm_pt
                        # If it is last turn then raise exception
                        if ech_h_slide == h_scroll_num - 1:
                            raise Exception("Element not found.")
                        # slide the horizontal window as we don't get the element
                        self.scroll('H', direction=None, frm_pt=h_frm_pt[:2], to_pt=h_to_pt[:2])
                        continue
                    # If I came till here which means we got the element
                    # break out as we got the element
                    break

            ele = ele_obj_list[int(ele_data['position']) - 1]
            return ele
        else:
            raise Exception("Element %s not found in config file"%ele)


    def click_on(self, ele, generic_param=()):
        ele_obj = self.get_element(ele, generic_param=generic_param)
        ele_obj.click()

    def clear_value(self, ele_obj):
        for i in range(7):
            try:
                txt = str(ele_obj.text).strip()
                if txt:
                    ele_obj.click()
                    ele_obj.clear()
                else:
                    info("object got clear out")
                    break
            except Exception as ex :
                warning("Got exception while clear out value :: " + str(ex))

    def set_value(self, ele, val, generic_param=()):
        ele_obj = self.get_element(ele, generic_param=generic_param)
        self.clear_value(ele_obj)
        ele_obj.set_text(str(val))


    def get_value(self, ele, generic_param=()):
        ele_obj = self.get_element(ele, generic_param=generic_param)
        return str(ele_obj.text).strip()

    def get_values(self, ele, generic_param=()):
        ele_obj_list = self.get_elements(ele, generic_param=generic_param)
        ele_obj_list_text = [str(ei.text).strip() for ei in ele_obj_list]
        return ele_obj_list

    def touch_on(self, xCordinate, yCordinate, tapDur=0.5):
        self.driver.execute_script("mobile: tap", {"tapCount": 1,
                                                   "touchCount": 1,
                                                   "duration": tapDur,
                                                   "x": int(xCordinate),
                                                   "y": int(yCordinate)})

    def should_visible(self, ele, generic_param=()):
        ele_obj = self.get_element(ele, generic_param=generic_param)
        if not ele_obj.is_displayed():
            raise Exception("Element %s is not visiable"%ele)

    def should_not_visible(self, ele, generic_param=()):
        try:
            ele_obj = self.get_element(ele, generic_param=generic_param)
            if ele_obj.is_displayed():
                raise Exception("Element %s is visiable"%ele)
        except Exception as ex:
            info(str(ex))

    def wait_for(self, secs):
        sleep(secs)

    def home_button_press(self):
        KEY_CODE = {
                 'KEYCODE_VOLUME_DOWN': 25,
                 'KEYCODE_VOLUME_MUTE ': 164,
                 'KEYCODE_VOLUME_UP' : 24,
                 'KEYCODE_HOME' : 3
        }
        self.driver.press_keycode(KEY_CODE['KEYCODE_HOME'])

    def re_launch_limelight_app(self):
        self.click_on("phone-menu")
        self.wait_for(3)
        for ech_element in self.driver.find_elements_by_class_name("android.widget.TextView"):
            if str(ech_element.text).strip() == "TestLimelightVideoSDK":
                ech_element.click()
                break
        self.wait_for(3)

    def rorate_device(self, typ):
        # {u'status': 0, u'sessionId': u'48e6ef5f-8113-4418-88f2-537538a46169', u'value': u'LANDSCAPE'}
        # {u'status': 0, u'sessionId': u'53a017b6-3839-433e-a851-4ba1861b398e', u'value': u'PORTRAIT'}
        self.wait_for(5)
        info(str(self.driver.execute('getScreenOrientation')))
        self.wait_for(5)
        info(str(self.driver.execute('setScreenOrientation', {'orientation': 'LANDSCAPE'})))
        self.wait_for(15)
        info(":::::: Applied the LANDSCAPE ::::::")
        info(str(self.driver.execute('getScreenOrientation')))
        self.wait_for(5)
        info(str(self.driver.execute('setScreenOrientation', {'orientation': 'PORTRAIT'})))
        self.wait_for(15)
        '''
        {u'status': 0, u'sessionId': u'73dc3f1e-fb57-47d4-a6f4-b741e31091f4',
                    u'value': u'PORTRAIT'}
        {u'status': 0, u'sessionId': u'73dc3f1e-fb57-47d4-a6f4-b741e31091f4',
                    u'value': u'Rotation (LANDSCAPE) successful.'}
        :::::: Applied the LANDSCAPE ::::::
        {u'status': 0, u'sessionId': u'73dc3f1e-fb57-47d4-a6f4-b741e31091f4',
                    u'value': u'LANDSCAPE'}
        {u'status': 0, u'sessionId': u'73dc3f1e-fb57-47d4-a6f4-b741e31091f4',
                    u'value': u'Rotation (PORTRAIT) successful.'}
        '''

    def scroll(self, scrol_typ, direction=None, frm_pt=(), to_pt=()):
        """Scroll horizontally or vertically in a particular direction from a
        from point to a to point

        :Args:
         - scrol_typ - Type of scroll, expected values are :
                     H - for horizontal
                     V - for vertical
         - direction - Which direction we should scrol, expected values are :
                     L2R - Left to right
                     R2L - Right to left
                     U2D - Up to down
                     D2U - Down to Up
         - frm_pt - Co-ordinate of scroll from point
         - to_pt - Co-ordinate of scroll to point
        """
        def x_y_correction(pt):
            x, y = pt
            if x <= 0 or not isinstance(x, int): x = 10
            if y <= 0 or not isinstance(y, int): y = 10
            return x, y

        if scrol_typ.upper().strip() == "H":
            if frm_pt:
                start_x, start_y = x_y_correction(frm_pt)
            elif direction == "L2R":
                 start_x, start_y = 10, 177
            elif direction == "R2L":
                 start_x, start_y = 670, 177
            else:
                 start_x, start_y = 670, 177

            if to_pt:
                end_x, end_y = x_y_correction(to_pt)
            elif direction == "L2R":
                 end_x, end_y = start_x + 650, 177
            elif direction == "R2L":
                 end_x, end_y = start_x - 650, 177
            else:
                 end_x, end_y = 10, 177

        elif scrol_typ.upper().strip() == "V":
            if frm_pt:
                start_x, start_y = x_y_correction(frm_pt)
            elif direction == "U2D":
                 start_x, start_y = 650, 968
            elif direction == "D2U":
                 start_x, start_y = 650, 242
            else:
                 start_x, start_y = 650, 968

            if to_pt:
                end_x, end_y = x_y_correction(to_pt)
            elif direction == "U2D":
                 end_x, end_y = 650, start_y - 700
            elif direction == "D2U":
                 end_x, end_y = 650, start_y + 700
            else:
                 end_x, end_y = 650, 242
        else:
            start_x, start_y = 650, 968
            end_x, end_y = 650, 242


        self.driver.swipe(start_x, start_y, end_x, end_y)
        #self.wait_for(1)

    def show_current_activity(self):
        info("show_current_activity :: " + str(self.driver.current_activity))

    def contains(self, sub_list, big_list):
        for i in xrange(len(big_list)-len(sub_list)+1):
            for j in xrange(len(sub_list)):
                if big_list[i+j] != sub_list[j]: break
                else: return i, i+len(sub_list)
        return False


    ## Scrolling til the end of top, new element comes from top
    def scrol_bottom_to_top(self, retry=2, indx=None, ret_all_data=False):
        ele = "//android.widget.ListView[1]//android.widget.TextView[1]"

        ele_obj = self.get_element_by_xpath(ele)
        list_text = list_text_old = [ei.text for ei in ele_obj]

        to_pt = ele_obj[-1].location
        x, y = int(to_pt['x']) + 20, int(to_pt['y'])
        to_pt = (x, y)

        indx = - len(ele_obj)/2 if indx == None else indx
        from_pt = ele_obj[indx].location
        x, y = x, int(from_pt['y'])
        from_pt = (x, y)

        # When both point is same point then
        if from_pt[1] == to_pt[1]:
            if indx == 0:
                to_pt = (to_pt[0], to_pt[1]+200)
            else:
                to_pt = (to_pt[0], to_pt[1]+50)

        tmp_retry = retry
        while tmp_retry > 0:
            self.scroll('V', frm_pt=from_pt, to_pt=to_pt)
            ele_obj = self.get_element_by_xpath(ele)
            list_text_new = [ei.text for ei in ele_obj]
            if list_text_new[:3] == list_text_old[:3]:
                tmp_retry -= 1
            else:
                if ret_all_data:
                    op = self.contains(list_text_old[:3], list_text_new)
                    if op == False:
                        info(str(list_text_old[:3]) + " not in " + str(list_text_new))
                        list_text[0:0] = list_text_new
                    else:
                        list_text[0:0] = list_text_new[:op[0]]
                list_text_old = list_text_new
                tmp_retry = retry

        if ret_all_data:
            return list_text

    def scrol_top_to_bottom(self, retry=2, indx=None, ret_all_data=False):

        ele = "//android.widget.ListView[1]//android.widget.TextView[1]"

        ele_obj = self.get_element_by_xpath(ele)
        list_text = list_text_old = [ei.text for ei in ele_obj]

        indx = len(ele_obj)/2 if indx == None else indx
        from_pt = ele_obj[indx].location
        x, y = int(from_pt['x']) + 20, int(from_pt['y'])
        from_pt = (x, y)

        to_pt = ele_obj[0].location
        x, y = x, int(to_pt['y'])
        to_pt = (x, y)

        # When both point is same point then
        if from_pt[1] == to_pt[1]:
            if indx == 0:
                to_pt = (to_pt[0], to_pt[1]-200)
            else:
                to_pt = (to_pt[0], to_pt[1]-50)

        tmp_retry = retry
        while tmp_retry > 0:
            self.scroll('V', frm_pt=from_pt, to_pt=to_pt)
            ele_obj = self.get_element_by_xpath(ele)
            list_text_new = [ei.text for ei in ele_obj]
            if list_text_new[-3:] == list_text_old[-3:]:
                tmp_retry -= 1
            else:
                if ret_all_data:
                    op = self.contains(list_text_old[-3:], list_text_new)
                    if op == False:
                        info(str(list_text_old[-3:]) + " not in " + str(list_text_new))
                        list_text.extend(list_text_new)
                    else:
                        list_text.extend(list_text_new[op[1]:])

                list_text_old = list_text_new
                tmp_retry = retry

        if ret_all_data:
            return list_text

    def scrol_right_to_left(self, retry=2, indx=None, ret_all_data=False):
        ele = "//android.widget.HorizontalScrollView[1]/android.widget.LinearLayout[1]//android.widget.TextView[1]"

        ele_obj = self.get_element_by_xpath(ele)
        list_text = list_text_old = [ei.text for ei in ele_obj]

        indx = len(ele_obj)/2 if indx == None else indx
        from_pt = ele_obj[0].location
        from_pt = (from_pt['x'], from_pt['y'])
        to_pt = ele_obj[indx].location
        to_pt = (to_pt['x'], to_pt['y'])

        tmp_retry = retry
        while tmp_retry > 0:
            self.scroll('H', frm_pt=from_pt, to_pt=to_pt)
            ele_obj = self.get_element_by_xpath(ele)
            list_text_new = [ei.text for ei in ele_obj]
            if list_text_new[:2] == list_text_old[:2]:
                tmp_retry -= 1
            else:
                if ret_all_data:
                    op = self.contains(list_text_old[:2], list_text_new)
                    if op == False:
                        info(str(list_text_old[:2]) + " not in " + str(list_text_new))
                        list_text[0:0] = list_text_new
                    else:
                        list_text[0:0] = list_text_new[:op[0]]

                list_text_old = list_text_new
                tmp_retry = retry

        if ret_all_data:
            return list_text

    def scrol_left_to_right(self, retry=2, indx=None, ret_all_data=False):
        ele = "//android.widget.HorizontalScrollView[1]/android.widget.LinearLayout[1]//android.widget.TextView[1]"

        ele_obj = self.get_element_by_xpath(ele)
        list_text = list_text_old = [ei.text for ei in ele_obj]

        indx = -len(ele_obj)/2 if indx == None else indx
        from_pt = ele_obj[-1].location
        from_pt = (from_pt['x'], from_pt['y'])
        to_pt = ele_obj[indx].location
        to_pt = (to_pt['x'], to_pt['y'])

        tmp_retry = retry
        while tmp_retry > 0:
            self.scroll('H', frm_pt=from_pt, to_pt=to_pt)
            ele_obj = self.get_element_by_xpath(ele)
            list_text_new = [ei.text for ei in ele_obj]
            if list_text_new[-2:] == list_text_old[-2:]:
                tmp_retry -= 1
            else:
                if ret_all_data:

                    op = self.contains(list_text_old[-2:], list_text_new)
                    if op == False:
                        info(str(list_text_old[-2:]) + " not in " + str(list_text_new))
                        list_text.extend(list_text_new)
                    else:
                        list_text.extend(list_text_new[op[1]:])
                list_text_old = list_text_new
                tmp_retry = retry

        if ret_all_data:
            return list_text

    def refresh_by_pull_down(self):
        self.scrol_bottom_to_top(retry=1)
        self.scrol_bottom_to_top(retry=1, indx=0)

    def refresh_by_pull_up(self):
        self.scrol_top_to_bottom (retry=1)
        self.scrol_top_to_bottom(retry=1, indx=0)

    def tap_on(self, point_x, point_y):
        self.driver.swipe(point_x, point_y, point_x, point_y)


if __name__ == "__main__":
    obj =  Driver("192.168.50.177:4723", "android", "4.4", "MyAVD")
    obj.set_up()
    '''
    obj.home_button_press()
    obj.wait_for(10)
    obj.re_launch_limelight_app()

    obj.rorate_device('landscape')
    obj.wait_for(5)
    #obj.rorate_device('potrait')
    obj.wait_for(5)

    obj.scroll()
    obj.get_element_by_xpath("//android.view.View[1]/android.widget.FrameLayout[1]/android.widget.HorizontalScrollView[1]/android.widget.LinearLayout[1]/android.app.ActionBar.Tab[3]/android.widget.TextView[1]")[0].click()
    obj.wait_for(10)
    obj.scroll(h_scroll=False, v_scroll=True)
    '''
    ## Scrolling testing
    '''
    try:
        obj.click_on('All-Media-Tab')
        print "obj.click_on('All-Media-Tab')"
        obj.show_current_activity()

        obj.wait_for(10)
        obj.click_on('Code-Rush-video') # # Close-ups-video
        obj.wait_for(10)
        obj.click_on('Encoder-PopUp-Cancel-Button')
        obj.wait_for(10)
        obj.click_on('Delivery-Check-box')
        obj.wait_for(5)
        obj.click_on('Player-Play-Button')
        obj.wait_for(10)
    except Exception, e:
        raise
    finally:
        obj.tear_down()
    '''
    ## Scrolling to the end
    '''
    try:
        obj.click_on("tab-item-right-half", generic_param=("ALL MEDIA",))
        obj.wait_for(10)

        print "-"*100
        op = obj.scrol_top_to_bottom(ret_all_data=True)
        print "obj.scrol_top_to_bottom::::", op
        print "-- Reached button --"

        print "-"*100
        op = obj.scrol_bottom_to_top(ret_all_data=True)
        print "obj.scrol_bottom_to_top::::", op
        print "-- Reached top --"

        print "-"*100
        op = obj.scrol_left_to_right(ret_all_data=True)
        print "obj.scrol_left_to_right::::", op
        print "-- Reached right --"

        print "-"*100
        op = obj.scrol_right_to_left(ret_all_data=True)
        print "obj.scrol_right_to_left::::", op
        print "-- Reached left --"

    except Exception, e:
        raise
    finally:
        obj.tear_down()
    '''

    ## Refreshing the page by pull it down
    '''
    try:
        obj.click_on("tab-item-right-half", generic_param=("ALL MEDIA",))
        obj.wait_for(10)
        obj.refresh_by_pull_down()
        print "-- Refresh the video By pull down --"
        #obj.refresh_by_pull_up()
        #print "-- Refresh the video By pull it up --"

    except Exception, e:
        raise
    finally:
        obj.tear_down()
    '''
    # seekbar
    try:
        obj.click_on("tab-item-right-half", generic_param=("PLAYER", ))
        obj.click_on("browse-button")
        obj.click_on("toggle-menu-button")
        obj.click_on("toggle-menu-left-side-links",
                 generic_param=("Downloads",))
        obj.click_on("file-link-in-menu", generic_param=("tGsMo2013-1.mp4",))
        obj.click_on("player")
        #obj.set_value("player-seekbar", "2:00")
        ele_obj = obj.get_element("player-seekbar")
        print "ele_obj.size:::", ele_obj.size
        print "ele_obj.location:::", ele_obj.location
        print "ele_obj.set_value:::", ele_obj.set_value
        print "ele_obj.text:::", ele_obj.text
        print "ele_obj.submit:::", ele_obj.submit
        x, y = int(ele_obj.location['x']), int(ele_obj.location['y'])
        print "Starting tap on (x, y):", x, y
        obj.tap_on(x, y)

        x = x + int(ele_obj.size['width'])/2
        print "Again tap on (x, y):", x, y
        obj.tap_on(x, y)
        sleep(15)
    except Exception, e:
        raise
    finally:
        obj.tear_down()