#-------------------------------------------------------------------------------
# Name:        appium_driver.py
# Purpose:     This module is use to communicating with the limelight app using
#              the Appium application. The Appium application install on a
#              remote MAC machine. We will run the script in our local machine
#              using this library, this library is basically communicating with
#              that remote machine, where Appium is running and android device
#              has plugged in or the emulator is running.
# Author:      Rebaca
# Created:     09-02-2015
# Copyright:   (c) Rebaca 2015
#-------------------------------------------------------------------------------

import sys
import os
import datetime
import random
import pickle
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
        """  Initialize the driver object """
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
        """ Load the element configuration data from element-path.cfg file """
        self.config_data = ConfigParser.ConfigParser()
        self.config_data.read(os.path.join(os.path.abspath(os.path.dirname(__file__)),
                              '..', 'config', 'element-path.cfg'))
        self.config_data = {ek: dict(ev) for ek, ev in dict(self.config_data._sections).iteritems()}

    def test_on(self, platform, version, device_name):
        """ Set the desire capabilities according to the targeted platform """
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
        """
        Connect with the appium and set implicitly wait to find an element.
        This will also launch the application in the device.
        """
        info("CONNECTING APPIUM ON %s"%self.remote_host_url)
        self.driver = webdriver.Remote(self.remote_host_url, self.desired_caps)
        self.driver.implicitly_wait(1)#10
        info("CONNECTED APPIUM ON %s"%self.remote_host_url)

    def exit_testcase(self):
        """ Depricated function """
        fp = open(self.dump_file_name, 'wb')
        pickle.dump(self.driver, fp)
        fp.close()
        return self.dump_file_name

    def start_testcase(self):
        """ Depricated function """
        fp = open(self.dump_file_name, 'rb')
        self.driver = pickle.load(fp)
        fp.close()

    def tear_down(self):
        """ Tear down the driver """
        self.driver.quit()

    def refresh_driver(self):
        """ Refresh the driver """
        self.driver.refresh()

    def take_screenshot(self, fileName=""):
        """ Take the screenshot from the device """
        screenShotFileName = "Screenshot-%d.png"%self.screen_shot_file_num if not fileName else fileName
        self.screen_shot_file_num += 1
        return self.driver.save_screenshot(screenShotFileName)

    def value_should_contains(self, ele, expected_value, generic_param=()):
        """
        Check if the element value contains a particular value
        @arg :
        ele            : name of the element (whoes entry is in element-path.cfg)
                         or the element object
        expected_value : expected value that the element value should contains
        generic_param  : dynamic xpath/id variable
        """
        ele_obj = self.get_element(ele, generic_param=generic_param)
        if str(ele_obj.text).strip() not in str(expected_value).strip():
            msg = "ele: %s::%s, actual val recv: %s, val doesn't contains: %s"
            raise Exception(msg % (ele, generic_param,
                                   ele_obj.text, expected_value))

    def value_should_be(self, ele, expected_value, generic_param=()):
        """
        Check element has the same value that we provided
        @arg :
        ele            : name of the element (whoes entry is in element-path.cfg)
                         or the element object
        expected_value : expected value
        generic_param  : dynamic xpath/id variable
        """
        ele_obj = self.get_element(ele, generic_param=generic_param)
        if str(ele_obj.text).strip() != str(expected_value).strip():
            raise Exception("Expected value: %s, Got value: %s"%(expected_value,
                                                                 ele_obj.text))

    def get_element_by_xpath(self, ele_xpath):
        """ Get an element by its xpath """
        obj =  self.driver.find_elements_by_xpath(ele_xpath)
        if not obj : raise Exception("Element not found for xpath: %s"%ele_xpath)
        return obj if isinstance(obj, list) else [obj]

    def get_element_by_id(self, ele_id):
        """ Get an element by its id """
        obj = self.driver.find_elements_by_id(ele_id)
        if not obj : raise Exception("Element not found for id: %s"%ele_id)
        return obj if isinstance(obj, list) else [obj]

    def get_element_by_link_text(self, text):
        """ Get an element by link text """
        obj = self.driver.find_elements_by_link_text(text) # find_element_by_link_text
        if not obj : raise Exception("Element not found for text: %s"%text)
        return obj if isinstance(obj, list) else [obj]

    def find_element(self, ele_data, generic_param=()):
        """
        Find a element by xpath/id/link-text according to the data that
        provided in configuration file
        """
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
        """ Get the elements with same element details """
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
        """ Get the element using the element details """
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
        """ depreciated function """
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
                        break # If got the element then don't need to do further scroll
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

                # When the window move from up to down the vertical coordinate will be same
                # when we move down to up then the vertical coordinate will exchange
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
                                # Don't perform the last scroll because the it won't enter the loop
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
        """
        If you provide element name then:
           It will get the details entry for this element from element-path.cfg
           and then get that element and click on that
        If you provide an element object then it will click on that element.
        If click successful then it will return the element object.
        @arg :
        ele           : name of the element (whose entry is in element-path.cfg)
                        or the element object
        generic_param : dynamic xpath/id variable
        """
        ele_obj = self.get_element(ele, generic_param=generic_param)
        ele_obj.click()
        return ele_obj

    def clear_value(self, ele_obj):
        """
        Clear out the element text
        @arg :
        ele_obj : element object that text need to clear out
        """
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
        """
        Set a value to a GUI element, raise exception if the element
        does not present.
        @arg :
        ele           : name of the element (whose entry is in element-path.cfg)
                        or the element object
        generic_param : dynamic xpath/id variable
        """
        ele_obj = self.get_element(ele, generic_param=generic_param)
        self.clear_value(ele_obj)
        ele_obj.set_text(str(val))


    def get_value(self, ele, generic_param=()):
        """
        Get the value of an element
        @arg :
        ele            : name of the element (whose entry is in element-path.cfg)
                         or the element object
        generic_param  : dynamic xpath/id variable
        """
        ele_obj = self.get_element(ele, generic_param=generic_param)
        return str(ele_obj.text).strip()

    def get_values(self, ele, generic_param=()):
        """
        Get the value of all matched elements
        @arg :
        ele            : name of the element (whose entry is in element-path.cfg)
                         or the element object
        generic_param  : dynamic xpath/id variable
        """
        ele_obj_list = self.get_elements(ele, generic_param=generic_param)
        ele_obj_list_text = [str(ei.text).strip() for ei in ele_obj_list]
        return ele_obj_list

    def touch_on(self, xCordinate, yCordinate, tapDur=0.5):
        """
        Touch a particular co-ordinate on GUI of the application
        @arg :
        xCordinate : x coordinate of tap point
        yCordinate : x coordinate of tap point
        tapDur     : duration of tapping
        """
        self.driver.execute_script("mobile: tap", {"tapCount": 1,
                                                   "touchCount": 1,
                                                   "duration": tapDur,
                                                   "x": int(xCordinate),
                                                   "y": int(yCordinate)})

    def should_visible(self, ele, generic_param=()):
        """
        Check if an element is visible, raise exception if it is not.
        @arg :
        ele            : name of the element (whose entry is in element-path.cfg)
                         or the element object
        generic_param  : dynamic xpath/id variable
        """
        ele_obj = self.get_element(ele, generic_param=generic_param)
        if not ele_obj.is_displayed():
            raise Exception("Element %s is not visiable"%ele)

    def should_not_visible(self, ele, generic_param=()):
        """
        Check if an element is not visible, raise exception if it is.
        @arg :
        ele            : name of the element (whose entry is in element-path.cfg)
                         or the element object
        generic_param  : dynamic xpath/id variable
        """
        try:
            ele_obj = self.get_element(ele, generic_param=generic_param)
            if ele_obj.is_displayed():
                raise Exception("Element %s is visiable"%ele)
        except Exception as ex:
            info(str(ex))

    def wait_for(self, secs):
        """ Wait for some seconds """
        sleep(secs)

    def native_key_press(self, key):
        """
        Simulate the native key press:
        @args:
        key : the key that you want to simulate
        """
        KEY_CODE = {
                 'VOLUME_DOWN': 25,
                 'VOLUME_MUTE ': 164,
                 'VOLUME_UP' : 24,
                 'HOME' : 3
        }
        self.driver.press_keycode( KEY_CODE[key.upper().strip()] )

    def re_launch_limelight_app(self, app_display_name):
        """
        Re launch the application from the device menu
        @args:
        app_display_name : Application name that is displaying in menu
        """
        self.click_on("phone-menu")
        from_point, to_point = None, None
        app_icn = "android.widget.TextView"
        set_coordinate = True

        for ech_try in range(3):

            for ech_element in self.driver.find_elements_by_class_name(app_icn):
                x = int(ech_element.location['x'])
                y = int(ech_element.location['y'])
                if set_coordinate:
                    if not from_point or from_point[1] == y:
                        from_point = [x, y]
                    elif from_point[1] < y:
                        from_point[1] = y
                        to_point = [from_point[0]/2, y]
                        set_coordinate = False

                if str(ech_element.text).strip() == app_display_name:
                    ech_element.click()
                    return True
            self.scroll("H", direction=None, frm_pt=from_point, to_pt=to_point)
        # If controll reach here then then app-icon not found
        return False

    def get_current_orientation(self):
        """ Get the current orientation of the device """
        ret_data = self.driver.execute('getScreenOrientation')
        return str(ret_data.get('value'))

    def rorate_device(self):
        """
        Rotate the device:
        If the device is in horizontal then make it vertical and vice versa
        Output of setScreenOrientation:
        {u'status': 0, u'sessionId': u'48e6ef5f-8113-4418-88f2-537538a46169',
         u'value': u'LANDSCAPE'}
        {u'status': 0, u'sessionId': u'53a017b6-3839-433e-a851-4ba1861b398e',
         u'value': u'PORTRAIT'}
        """
        orientation_map = {'LANDSCAPE':'PORTRAIT', 'PORTRAIT':'LANDSCAPE'}
        change_from = self.get_current_orientation()
        change_to = orientation_map[change_from]
        out_put = self.driver.execute( 'setScreenOrientation',
                                       {'orientation': change_to})
        return change_from, change_to, out_put

    def scroll(self, scrol_typ, direction=None, frm_pt=(), to_pt=()):
        """Scroll horizontally or vertically in a particular direction from a
        from point to a to point

        @args:
         - scrol_typ - Type of scroll, expected values are :
                     H - for horizontal
                     V - for vertical
         - direction - Which direction we should scroll, expected values are :
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
        """Showing the current activity"""
        info("show_current_activity :: " + str(self.driver.current_activity))

    def contains(self, sub_list, big_list):
        """
        Check if a big list contains the small list and
        return True/False accordingly
        @args:
        sub_list : The list that we want to search
        big_list : The big list where we want to search
        """
        for i in xrange(len(big_list)-len(sub_list)+1):
            for j in xrange(len(sub_list)):
                if big_list[i+j] != sub_list[j]: break
                else: return i, i+len(sub_list)
        return False

    def scrol_bottom_to_top(self, retry=2, indx=None, ret_all_data=False):
        """
        Scrolling till the end of top, new element comes from top in gui
        @args:
        retry         : number of retries
        indx          : from which part we need to drag down, useful
                      for refresh a tab
        ret_all_data  : if u want to fetch all data during the scroll,
           so that u will have a list of values that came while scroll

        """
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
        for echtry in range(4):
            try:
                ele_obj = self.get_element_by_xpath(ele)
                break
            except Exception as ee:
                warning(str(ee))
                self.wait_for(1)
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

        for echtry in range(4):
            try:
                ele_obj = self.get_element_by_xpath(ele)
                break
            except Exception as ee:
                warning(str(ee))
                self.wait_for(1)

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
