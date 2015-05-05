# pylint: disable=W0703, W0612, C0301, C0103
"""
#-------------------------------------------------------------------------------
# Name      :  appium_driver.py
# Purpose   :  This module is use to communicating with the limelight app using
#              the Appium application. The Appium application install on a
#              remote MAC machine. We will run the script in our local machine
#              using this library, this library is basically communicating with
#              that remote machine, where Appium is running and android device
#              has plugged in or the emulator is running.
# Author    :  Rebaca
# Created   :  09-02-2015
# Copyright :  (c) Rebaca 2015
#-------------------------------------------------------------------------------
"""
import os
from appium import webdriver
from time import sleep
import ConfigParser
from modules.logger import info, warning
from modules.OpenCvLib import OpenCvLibrary

class Driver(object):
    """
    This class has been used to communicating with the device using Appium
    """
    def __init__(self, remote_host, platform, version, device_name):
        """  Initialize the driver object

        @args:
            remote_host : remote host ip / dns
            platform    : android / iOS
            version     : platform version
            device_name : name of the device
        """
        self.remote_host_url = 'http://%s/wd/hub' % remote_host
        self.driver = None
        self.config_data = None
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
        """ Set the desire capabilities according to the targeted platform
        @args:
            platform    : android / iOS
            version     : platform version
            device_name : name of the device
        """
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
        Connect with the Appium and set implicitly wait to find an element.
        This will also launch the application in the device.
        """
        info("CONNECTING APPIUM ON %s"%self.remote_host_url)
        self.driver = webdriver.Remote(self.remote_host_url, self.desired_caps)
        self.driver.implicitly_wait(4)#10
        info("CONNECTED APPIUM ON %s"%self.remote_host_url)


    def tear_down(self):
        """ Tear down the driver """
        self.driver.quit()

    def refresh_driver(self):
        """ Refresh the driver """
        self.driver.refresh()

    def take_screenshot(self, file_name=""):
        """ Take the screen shot from the device
        @arg:
            file_name : provide name of the screen shot file to be saved
        """
        if not file_name:
            screenshot_fnam = "screenshot%d.png" % self.screen_shot_file_num
        else: screenshot_fnam = file_name
        self.screen_shot_file_num += 1
        return self.driver.save_screenshot(screenshot_fnam)

    def take_screenshot_of_element(self, element_name, generic_param=(),
                                   file_name=""):
        """
        Take screen shot of the whole screen and crop out the element pic
        """
        ele_obj = self.get_element(element_name, generic_param=generic_param)
        ele_height = int(ele_obj.size['height'])
        ele_width = int(ele_obj.size['width'])
        ele_x_val = int(ele_obj.location['x'])
        ele_y_val = int(ele_obj.location['y'])

        ele_x1_val = ele_x_val + ele_width
        ele_y1_val = ele_y_val + ele_height

        self.take_screenshot(file_name)

        orientation_map = { 'LANDSCAPE': ( ele_x_val, ele_x1_val,
                                           ele_y_val, ele_y1_val),
                            'PORTRAIT': ( ele_y_val, ele_y1_val,
                                          ele_x_val, ele_x1_val )
                          }
        current_orientation = self.get_current_orientation()
        OpenCvLibrary.crop_file(file_name,
                                *orientation_map[str(current_orientation)])
        return file_name

    def value_should_contains(self, element, expected_value, generic_param=()):
        """
        Check if the element value contains a particular value
        @args :
            element        : name of element (whose entry is in element-path.cfg)
                             or the element object
            expected_value : expected value that the element value should contains
            generic_param  : dynamic xpath/id variable
        """
        ele_obj = self.get_element(element, generic_param=generic_param)
        if str(ele_obj.text).strip() not in str(expected_value).strip():
            msg = "element: %s::%s, actual value received: %s, value doesn't contains: %s"
            raise Exception(msg % (element, generic_param,
                                   ele_obj.text, expected_value))

    def value_should_be(self, element, expected_value, generic_param=()):
        """
        Check element has the same value that we provided
        @args :
            element        : name of element (whose entry is in element-path.cfg)
                             or the element object
            expected_value : expected value
            generic_param  : dynamic xpath/id variable
        """
        ele_obj = self.get_element(element, generic_param=generic_param)
        if str(ele_obj.text).strip() != str(expected_value).strip():
            raise Exception("Expected value: %s, Got value: %s"%(expected_value,
                                                                 ele_obj.text))

    def get_element_by_xpath(self, ele_xpath):
        """ Get an element by its xpath
        @arg:
            ele_xpath   : xpath of the element
        """
        obj =  self.driver.find_elements_by_xpath(ele_xpath)
        if not obj :
            raise Exception("Element not found for xpath: %s" % ele_xpath)
        return obj if isinstance(obj, list) else [obj]

    def get_element_by_id(self, ele_id):
        """ Get an element by its id
        @arg:
            ele_id   : id of the element
        """
        obj = self.driver.find_elements_by_id(ele_id)
        if not obj :
            raise Exception("Element not found for id: %s" % ele_id)
        return obj if isinstance(obj, list) else [obj]

    def get_element_by_link_text(self, text):
        """ Get an element by link text
        @arg:
            text   : text of the element
        """
        obj = self.driver.find_elements_by_link_text(text)
        if not obj :
            raise Exception("Element not found for text: %s" % text)
        return obj if isinstance(obj, list) else [obj]

    def find_element(self, ele_data, generic_param=()):
        """
        Find a element by xpath/id/link-text according to the data that
        provided in configuration file
        @args:
            ele_data   : data object containing element selector
            generic_param   : dynamic xpath/id variable
        """
        if 'xpath' in ele_data:
            ele_obj_list = self.get_element_by_xpath(ele_data['xpath'] % generic_param)
        elif 'id' in ele_data:
            ele_obj_list = self.get_element_by_id(ele_data['id'] % generic_param)
        elif 'text' in ele_data:
            ele_obj_list = self.get_element_by_link_text(ele_data['text'] % generic_param)
        else:
            raise Exception("Provide element xpath or id in config file")
        return ele_obj_list

    def get_elements(self, element, generic_param=()):
        """ Get the elements with same element details
        @args:
            element :  element to be selected
            generic_param   : dynamic xpath/id variable
        """
        element = str(element).strip()
        if element in self.config_data:
            ele_data = self.config_data[element]
            ele_obj_list = []
            try:
                ele_obj_list = self.find_element( ele_data,
                                                  generic_param=generic_param )
            except Exception:
                pass
            return ele_obj_list
        else:
            raise Exception("Element %s not found in config file"%element)

    def get_element(self, ele, generic_param=()):
        """ Get the element using the element details
        @args:
            ele :   element
            generic_param   : dynamic xpath/id variable
        """
        ele = str(ele).strip()
        if ele in self.config_data:
            ele_data = self.config_data[ele]
            scroll_h = int(ele_data['scroll-h'])
            scroll_v = int(ele_data['scroll-v'])
            search_flag = int(ele_data.get('search-flag','0'))
            ele_obj_list = None

            # Searching in current screen
            if search_flag:
                try:
                    ele_obj_list = self.find_element(ele_data,
                                                   generic_param=generic_param)
                except Exception:
                    pass

            # Performing Horizontal scroll
            if scroll_h != 0 and not ele_obj_list:
                left_x, right_x = 5, 670
                scrol_typ = 'H'
                if scroll_h < 0:
                    from_point = [left_x,
                                  int(ele_data['tap-point'].split(',')[:2][1])]
                    to_point = [right_x, from_point[1]]
                else:
                    from_point = [right_x,
                                  int(ele_data['tap-point'].split(',')[:2][1])]
                    to_point = [left_x, from_point[1]]

                for e_itr in xrange(abs(scroll_h)):
                    self.scroll(scrol_typ, direction=None,
                                frm_pt=from_point, to_pt=to_point)
                    if search_flag:
                        try:
                            ele_obj_list = self.find_element(ele_data,
                                                    generic_param=generic_param)
                            break
                        except Exception:
                            pass

            # Performing Vertical scroll
            if scroll_v != 0 and not ele_obj_list:
                top_y, bottom_y = 242, 1098
                scrol_typ = 'V'
                if scroll_v < 0:
                    from_point = [int(ele_data['tap-point'].split(',')[:2][0]),
                                  top_y]
                    to_point = [from_point[0], bottom_y]
                else:
                    from_point = [int(ele_data['tap-point'].split(',')[:2][0]),
                                  bottom_y]
                    to_point = [from_point[0], top_y]

                for e_itr in xrange(abs(scroll_v)):
                    self.scroll(scrol_typ, direction=None,
                                frm_pt=from_point, to_pt=to_point)
                    self.wait_for(1)
                    if search_flag:
                        try:
                            ele_obj_list = self.find_element(ele_data,
                                                    generic_param=generic_param)
                            break
                        except Exception:
                            pass

            if not search_flag and not ele_obj_list:
                try:
                    ele_obj_list = self.find_element(ele_data,
                                                    generic_param=generic_param)
                except Exception:
                    pass

            if ele_obj_list:
                return ele_obj_list[int(ele_data['position']) - 1]
            else:
                raise Exception("Element - %s not found."%ele)
        else:
            raise Exception("Element %s not found in config file"%ele)

    def get_element_my_algo(self, ele, generic_param=()):
        """
        Deprecated function
        @args:
            ele :   element
            generic_param   : dynamic xpath/id variable
        """
        if str(ele) in self.config_data:
            ele_data = self.config_data[str(ele)]
            frm_pt, to_pt = [], []

            if 'scroll' not in ele_data:
                # Normal case
                ele_obj_list = self.find_element(ele_data,
                                                 generic_param=generic_param)

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
                        ele_obj_list = self.find_element(ele_data,
                                                   generic_param=generic_param)
                        # Got the element so don't need to do further scroll
                        break
                    except Exception as ex :
                        if ech_scroll == tot_scroll - 1:
                            # If is is last turn then raise the exception;
                            raise
                        if str(ex).lower() == "element not found.":
                            # Perform the scroll
                            self.scroll(scrol_typ, direction=None,
                                        frm_pt=frm_pt[:2], to_pt=to_pt[:2])

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

                # When the window move from up to down the vertical
                # coordinate will be same
                # when we move down to up then the vertical
                # coordinate will exchange
                # the from will be to and the to will be from
                for ech_h_slide in xrange(h_scroll_num):
                    # Horizontal slide
                    for ech_v_slide in xrange(v_scroll_num):
                        # Vertical scroll
                        try:
                            ele_obj_list = self.find_element(ele_data,
                                                   generic_param=generic_param)
                            break
                        except Exception:
                            if ech_v_slide == v_scroll_num - 1:
                                # Don't perform last scroll
                                # because the it won't enter the loop
                                pass
                            else:
                                self.scroll('V', direction=None,
                                            frm_pt=v_frm_pt[:2],
                                            to_pt=v_to_pt[:2])
                    else:
                        # If vertically down then it should go up next time
                        v_frm_pt, v_to_pt = v_to_pt, v_frm_pt
                        # If it is last turn then raise exception
                        if ech_h_slide == h_scroll_num - 1:
                            raise Exception("Element not found.")
                        # slide horizontal window as we don't get the element
                        self.scroll('H', direction=None,
                                    frm_pt=h_frm_pt[:2], to_pt=h_to_pt[:2])
                        continue
                    # If I came till here which means we got the element
                    # break out as we got the element
                    break

            ele = ele_obj_list[int(ele_data['position']) - 1]
            return ele
        else:
            raise Exception("Element %s not found in config file" % ele)

    def click_on(self, ele, generic_param=()):
        """
        If you provide element name then:
           It will get the details entry for this element from element-path.cfg
           and then get that element and click on that
        If you provide an element object then it will click on that element.
        If click successful then it will return the element object.
        @args :
            ele           : name of the element (whose entry is in element-path.cfg)
                            or the element object
            generic_param : dynamic xpath/id variable
        """
        ele_obj = self.get_element(ele, generic_param=generic_param)
        ele_obj.click()
        return ele_obj

    @staticmethod
    def clear_value(ele_obj):
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

    def set_value(self, ele, value, generic_param=()):
        """
        Set a value to a GUI element, raise exception if the element
        does not present.
        @args :
            ele   : name of the element (whose entry is in element-path.cfg)
                  or the element object
            value : value that need to set
            generic_param : dynamic xpath/id variable
        """
        ele_obj = self.get_element(ele, generic_param=generic_param)
        self.clear_value(ele_obj)
        ele_obj.set_text(str(value))

    def get_value(self, ele, generic_param=()):
        """
        Get the value of an element
        @args :
            ele : name of the element (whose entry is in element-path.cfg)
                  or the element object
            generic_param  : dynamic xpath/id variable
        """
        ele_obj = self.get_element(ele, generic_param=generic_param)
        return str(ele_obj.text).strip()

    def get_values(self, ele, generic_param=()):
        """
        Get the value of all matched elements
        @args :
            ele : name of the element (whose entry is in element-path.cfg)
                  or the element object
            generic_param  : dynamic xpath/id variable
        """
        ele_obj_list = self.get_elements(ele, generic_param=generic_param)
        ele_obj_list_text = [str(ei.text).strip() for ei in ele_obj_list]
        return ele_obj_list_text

    def touch_on(self, x_coordinate, y_coordinate, tap_dur=0.5):
        """
        Touch a particular coordinate on GUI of the application
        @args :
            x_coordinate : x coordinate of tap point
            y_coordinate : y coordinate of tap point
            tap_dur      : tapping duration
        """
        self.driver.execute_script("mobile: tap", {"tapCount": 1,
                                                   "touchCount": 1,
                                                   "duration": tap_dur,
                                                   "x": int(x_coordinate),
                                                   "y": int(y_coordinate)})

    def should_visible(self, ele, generic_param=()):
        """
        Check if an element is visible, raise exception if it is not.
        @args :
            ele : name of the element (whose entry is in element-path.cfg)
                  or the element object
            generic_param  : dynamic xpath/id variable
        """
        ele_obj = self.get_element(ele, generic_param=generic_param)
        if not ele_obj.is_displayed():
            raise Exception("Element %s is not visible"%ele)

    def should_not_visible(self, ele, generic_param=()):
        """
        Check if an element is not visible, raise exception if it is.
        @args :
            ele : name of the element (whose entry is in element-path.cfg)
                  or the element object
            generic_param  : dynamic xpath/id variable
        """
        try:
            ele_obj = self.get_element(ele, generic_param=generic_param)
            if ele_obj.is_displayed():
                raise Exception("Element %s is visible"%ele)
        except Exception as ex:
            info(str(ex))

    @staticmethod
    def wait_for(secs):
        """ Wait for some seconds """
        sleep(secs)

    def native_key_press(self, key):
        """
        Simulate the native key press:
        @arg :
            key : the key that you want to simulate
        """
        key_code = {
                 'VOLUME_DOWN': 25,
                 'VOLUME_MUTE ': 164,
                 'VOLUME_UP' : 24,
                 'HOME' : 3
        }
        self.driver.press_keycode( key_code[key.upper().strip()] )

    def re_launch_limelight_app(self, app_display_name):
        """
        Re launch the application from the device menu
        @arg :
            app_display_name : Application name that is displaying in menu
        """
        self.click_on("phone-menu")
        from_point, to_point = None, None
        app_icn = "android.widget.TextView"
        set_coordinate = True

        for ech_try in range(3):

            for ech_element in self.driver.find_elements_by_class_name(app_icn):
                x_coordinate = int(ech_element.location['x'])
                y_coordinate = int(ech_element.location['y'])
                if set_coordinate:
                    if not from_point or from_point[1] == y_coordinate:
                        from_point = [x_coordinate, y_coordinate]
                    elif from_point[1] < y_coordinate:
                        from_point[1] = y_coordinate
                        to_point = [from_point[0]/2, y_coordinate]
                        set_coordinate = False

                if str(ech_element.text).strip() == app_display_name:
                    ech_element.click()
                    return True
            self.scroll("H", direction=None, frm_pt=from_point, to_pt=to_point)
        # If control reach here then then app-icon not found
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
        point to another point

        @args:
            scrol_typ : Type of scroll, expected values are :
                        H - for horizontal
                        V - for vertical
            direction : Which direction we should scroll, expected values are :
                        L2R - Left to right
                        R2L - Right to left
                        U2D - Up to down
                        D2U - Down to Up
            frm_pt : Coordinate of scroll from point
            to_pt : Coordinate of scroll to point
        """
        def x_y_correction(point):
            """ x, y coordinate correction """
            _x, _y = point
            _x = 10 if _x <= 0 or not isinstance(_x, int) else _x
            _y = 10 if _y <= 0 or not isinstance(_y, int) else _y
            return _x, _y

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

    @staticmethod
    def contains(sub_list, big_list):
        """
        Check if a big list contains the sub list and
        return True/False accordingly
        @args :
            sub_list : The list that we want to search
            big_list : The big list where we want to search
        """
        for i in xrange(len(big_list) - len(sub_list) + 1):
            for j in xrange(len(sub_list)):
                if big_list[i + j] != sub_list[j]:
                    break
                else:
                    return i, i + len(sub_list)
        return False

    def scrol_bottom_to_top(self, retry=2, indx=None, ret_all_data=False):
        """
        Scrolling till the end of top, new element comes from top in GUI
        @args :
            retry         : number of retries
            indx          : from which part we need to drag down, useful
                            for refresh a tab
            ret_all_data  : if u want to fetch all data during the scroll,
                            so that u will have a list of values that came while scroll
        """
        ele = "//android.widget.ListView[1]//android.widget.TextView[1]"

        ele_obj = self.get_element_by_xpath(ele)
        list_text = list_text_old = [ech_ele.text for ech_ele in ele_obj]

        to_pt = ele_obj[-1].location
        to_pt = (int(to_pt['x']) + 20, int(to_pt['y']))
        indx = - len(ele_obj)/2 if indx == None else indx
        from_pt = ele_obj[indx].location
        from_pt = (to_pt[0], int(from_pt['y']))

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
            list_text_new = [ech_ele.text for ech_ele in ele_obj]
            if list_text_new[:3] == list_text_old[:3]:
                tmp_retry -= 1
            else:
                if ret_all_data:
                    output = self.contains(list_text_old[:3], list_text_new)
                    if output == False:
                        info(str(list_text_old[:3]) + " not in " + \
                             str(list_text_new))
                        list_text[0:0] = list_text_new
                    else:
                        list_text[0:0] = list_text_new[:output[0]]
                list_text_old = list_text_new
                tmp_retry = retry

        if ret_all_data:
            return list_text

    def scrol_top_to_bottom( self, retry=2, indx=None,
                             ret_all_data=False, scroll_ele_typ=""):
        """
        Scroll from top to bottom,
        @args :
            retry : Number of scroll retries after reaching to bottom
            indx  : If you pass a low value it will try to scroll more, which result
                    the big scrolling.
            ret_all_data : True if you want to get all data that found during the
                           scroll, else False
            scroll_ele_typ : if you are scrolling through normal tab with data
                             or you r scrolling through pop-up
        """
        if scroll_ele_typ.lower() == "popup":
            ele = "//android.widget.CheckedTextView"
        else:
            # Default
            ele = "//android.widget.ListView[1]//android.widget.TextView[1]"

        ele_obj = self.get_element_by_xpath(ele)
        list_text = list_text_old = [ech_ele.text for ech_ele in ele_obj]

        indx = len(ele_obj)/2 if indx == None else indx
        from_pt = ele_obj[indx].location
        from_pt = (int(from_pt['x']) + 20, int(from_pt['y']))
        to_pt = ele_obj[0].location
        to_pt = (from_pt[0], int(to_pt['y']))

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
            list_text_new = [ech_ele.text for ech_ele in ele_obj]
            if list_text_new[-3:] == list_text_old[-3:]:
                tmp_retry -= 1
            else:
                if ret_all_data:
                    output = self.contains(list_text_old[-3:], list_text_new)
                    if output == False:
                        info(str(list_text_old[-3:]) + " not in " + \
                             str(list_text_new))
                        list_text.extend(list_text_new)
                    else:
                        list_text.extend(list_text_new[output[1]:])

                list_text_old = list_text_new
                tmp_retry = retry

        if ret_all_data:
            return list_text

    def scrol_right_to_left(self, retry=2, indx=None, ret_all_data=False):
        """
        Scroll from right to left,
        @args:
            retry : Number of scroll retries after reaching to left
            indx  : If you pass a low value it will try to scroll more, which result
                    the big scrolling.
            ret_all_data : True if you want to get all data that found during the
                           scroll, else False
        """
        ele = "//android.widget.HorizontalScrollView[1]/" + \
              "android.widget.LinearLayout[1]//android.widget.TextView[1]"
        for echtry in range(4):
            try:
                ele_obj = self.get_element_by_xpath(ele)
                break
            except Exception as excp:
                warning(str(excp))
                self.wait_for(1)

        list_text = list_text_old = [ech_ele.text for ech_ele in ele_obj]
        indx = len(ele_obj)/2 if indx == None else indx
        from_pt = ele_obj[0].location
        from_pt = (from_pt['x'], from_pt['y'])
        to_pt = ele_obj[indx].location
        to_pt = (to_pt['x'], to_pt['y'])

        tmp_retry = retry
        while tmp_retry > 0:
            self.scroll('H', frm_pt=from_pt, to_pt=to_pt)
            ele_obj = self.get_element_by_xpath(ele)
            list_text_new = [ech_ele.text for ech_ele in ele_obj]
            if list_text_new[:2] == list_text_old[:2]:
                tmp_retry -= 1
            else:
                if ret_all_data:
                    output = self.contains(list_text_old[:2], list_text_new)
                    if output == False:
                        info(str(list_text_old[:2]) + " not in " + \
                             str(list_text_new))
                        list_text[0:0] = list_text_new
                    else:
                        list_text[0:0] = list_text_new[:output[0]]

                list_text_old = list_text_new
                tmp_retry = retry

        if ret_all_data:
            return list_text

    def scrol_left_to_right(self, retry=2, indx=None, ret_all_data=False):
        """
        Scroll from left to right,
        @args :
            retry : Number of scroll retries after reaching to right
            indx  : If you pass a low value it will try to scroll more, which result
                    the big scrolling.
            ret_all_data : True if you want to get all data that found during the
                           scroll, else False
        """
        ele = "//android.widget.HorizontalScrollView[1]/" + \
              "android.widget.LinearLayout[1]//android.widget.TextView[1]"

        for echtry in range(4):
            try:
                ele_obj = self.get_element_by_xpath(ele)
                break
            except Exception as exc:
                info(str(exc))
                self.wait_for(1)

        list_text = list_text_old = [ech_ele.text for ech_ele in ele_obj]
        indx = -len(ele_obj)/2 if indx == None else indx
        from_pt = ele_obj[-1].location
        from_pt = (from_pt['x'], from_pt['y'])
        to_pt = ele_obj[indx].location
        to_pt = (to_pt['x'], to_pt['y'])

        tmp_retry = retry
        while tmp_retry > 0:
            self.scroll('H', frm_pt=from_pt, to_pt=to_pt)
            ele_obj = self.get_element_by_xpath(ele)
            list_text_new = [ech_ele.text for ech_ele in ele_obj]
            if list_text_new[-2:] == list_text_old[-2:]:
                tmp_retry -= 1
            else:
                if ret_all_data:
                    output = self.contains(list_text_old[-2:], list_text_new)
                    if output == False:
                        info(str(list_text_old[-2:]) + " not in " + \
                             str(list_text_new))
                        list_text.extend(list_text_new)
                    else:
                        list_text.extend(list_text_new[output[1]:])
                list_text_old = list_text_new
                tmp_retry = retry

        if ret_all_data:
            return list_text

    def refresh_by_pull_down(self):
        """ Refresh your tab by pull it down """
        try:
            self.scrol_bottom_to_top(retry=1)
        except Exception as ex:
            info(str(ex))
        obj = self.get_element("tab-content")
        obj_data = {'width' :  int(obj.size['width']),
                    'height':  int(obj.size['height']),
                    'x' :      int(obj.location['x']),
                    'y' :      int(obj.location['y'])}

        start_pt = (obj_data['x'] + (obj_data['width']/2) , obj_data['y'])
        end_pt = (start_pt[0], start_pt[1] + int(obj_data['height'] * 0.75))
        self.driver.swipe(start_pt[0], start_pt[1], end_pt[0], end_pt[1])

    def refresh_by_pull_up(self):
        """ Refresh your tab by pull it up """
        self.scrol_top_to_bottom (retry=1)
        self.scrol_top_to_bottom(retry=1, indx=0)
        '''
        obj = self.get_element("tab-data-list")
        obj_data = {'width' :  int(obj.size['width']),
                    'height':  int(obj.size['height']),
                    'x' :      int(obj.location['x']),
                    'y' :      int(obj.location['y'])}

        end_pt = (obj_data['x'] + (obj_data['width']/2) , obj_data['y'])
        start_pt = (end_pt[0], end_pt[1] + obj_data['height'])
        self.driver.swipe(start_pt[0], start_pt[1], end_pt[0], end_pt[1])
        '''

    def tap_on(self, point_x, point_y):
        """
        To tap on a particular coordinate
        @args:
            point_x : x coordinate
            point_y : y coordinate
        """
        self.driver.swipe(point_x, point_y, point_x, point_y)

if __name__ == "__main__":
    from modules.constant import *
    obj = Driver( APPIUM_SERVER, TEST_TARGET_CFG['os'],
                 TEST_TARGET_CFG['os-version'],
                 TEST_TARGET_CFG['device-name'])
    obj.set_up()
    ele = obj.get_element("tab-item-left-half", ('CHANNEL GROUPS'))
    ele.click()
    obj.wait_for(20)
    obj.take_screenshot_of_element( "icon-of-item",
                                    ("Rebaca Channel Group", ),
                                    file_name="Rebaca-Channel-Group.png"
                                   )
    obj.take_screenshot_of_element( "icon-of-item",
                                    ("Limelight", ),
                                    file_name="Limelight.png"
                                   )


    ele = obj.get_element("tab-item-left-half", ('ALL CHANNELS'))
    ele.click()
    obj.wait_for(20)
    obj.take_screenshot_of_element( "icon-of-item",
                                    ("Some Sample Videos1", ),
                                    file_name="Some-Sample-Videos1.png"
                                   )
    obj.take_screenshot_of_element( "icon-of-item",
                                    ("Maru Madnes", ),
                                    file_name="Maru-Madnes.png"
                                   )

    ele = obj.get_element("tab-item-left-half", ('ALL MEDIA'))
    ele.click()
    obj.wait_for(20)
    obj.take_screenshot_of_element( "icon-of-item",
                                    ("Late For Work", ),
                                    file_name="Late-For-Work.png"
                                   )
    obj.take_screenshot_of_element( "icon-of-item",
                                    ("Code Rush", ),
                                    file_name="Code-Rush.png"
                                   )