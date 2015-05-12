"""
#-------------------------------------------------------------------------------
# Name       :  limelight_mod
# Purpose    :  From glue code these functions will get called
# Author     :  Rebaca
# Created    :  18-03-2015
# Copyright  :  (c) Rebaca 2015
#-------------------------------------------------------------------------------
"""
import re
import os
import datetime
from modules.appium_driver import Driver
from modules.logger import info, error, warning, exception, success, fail
from modules.constant import *
from modules import exception_mod
from modules.OpenCvLib import OpenCvLibrary


TAB_POINT = None
NEXT_TAB_POINT = None


class Limelight(object):
    """
    Limelight interface layer from here the main driver function will
    be called
    """
    def __init__(self):
        self.__obj = None

    @exception_mod.handle_exception
    def launch_app(self):
        """
        Use to launch the application and create object of the main driver
        file
        """
        info( "Launch the app.")
        if self.__obj:
            self.exit_app()
        self.__obj = Driver( APPIUM_SERVER, TEST_TARGET_CFG['os'],
                             TEST_TARGET_CFG['os-version'],
                             TEST_TARGET_CFG['device-name'])
        try:
            self.__obj.set_up()
        except Exception as ex :
            reason = ex.__dict__.get('reason')
            reason = reason if reason else str(ex)
            error("App launch failed : %s" % str(reason))
            raise Exception(reason)
        success("App Launched.")

    def exit_app(self):
        """
        Use to tear down the application.
        """
        info("Close the app.")
        try:
            self.__obj.tear_down()
            self.__obj.wait_for(MEDIUM_WAIT)
            self.__obj = None
            info("App Closed.")
        except Exception as ex :
            error(str(ex))
        finally:
            self.__obj = None

    def is_app_running(self):
        """
        Return True is the app is running else return False
        """
        return self.__obj != None

    @exception_mod.handle_exception
    def select_tab(self, tab_name):
        """
        Select the tab whose name has been provided
        @arg:
            tab_name : display name of the tab
        """
        info("SELECT TAB => %s" % (tab_name))

        if tab_name.lower().strip() == "current":
            pass
        else:
            # First move to the left most tab then start search
            self.__obj.scrol_right_to_left(retry=1)
            # Now go to targeted tab
            self.click_on(MAPPER[tab_name.lower()], generic_param=(tab_name, ))

        info("SELECTED TAB => %s" % (tab_name))
        try:
            self.__obj.scrol_bottom_to_top(retry=1)
        except Exception as exc:
            warning("While reset the scroll to top :%s"%str(exc))

    def click_on(self, element, generic_param=()):
        """
        If you provide element name then:
           It will get the details entry for this element from element-path.cfg
           and then get that element and click on that
        If you provide an element object then it will click on that element.
        If click successful then it will return the element object.
        @args :
            element : name of the element (whose entry is in element-path.cfg)
                      or the element object
            generic_param : dynamic xpath/id variable
        """
        if type(element) == str:
            info("Clicking on : %s :: %s"%(element, generic_param))
            return self.__obj.click_on(element, generic_param=generic_param)
        else:
            element.click()

    def should_visible(self, element_name, generic_param=()):
        """
        Check if an element is visible, raise exception if it is not
        @args :
            element_name  : name of the element
                            (whose entry is in element-path.cfg)
                            or the element object
            generic_param : dynamic xpath/id variable
        """
        info("Clicking Visibility :: %s :: %s" % (element_name, generic_param))
        self.__obj.should_visible(element_name, generic_param=generic_param)

    def set_value(self, element_name, value, generic_param=()):
        """
        Set a value to a GUI element, raise exception if the element
        does not present.
        @args :
            element_name  : name of the element
                            (whose entry is in element-path.cfg)
                            or the element object
            value : value to set for the GUI element
            generic_param : dynamic xpath/id variable
        """
        info("Setting Value :: %s :: %s ::: value=%s" % (element_name,
                                                         generic_param, value))
        self.__obj.set_value(element_name, value, generic_param=generic_param)

    def value_should_be(self, element_name, value, generic_param=()):
        """
        Check element has the same value that we provided
        @args :
            element_name   : name of the element
                             (whose entry is in element-path.cfg)
                             or the element object
            value          : expected value
            generic_param  : dynamic xpath/id variable
        """
        info("Checking exact value :: %s :: %s :::: value=%s" % \
                             (element_name, generic_param, value))
        self.__obj.value_should_be(element_name, value,
                                   generic_param=generic_param)

    def value_should_contains(self, element_name, value, generic_param=()):
        """
        Check if the element value contains a particular value
        @args :
            element_name   : name of the element
                             (whose entry is in element-path.cfg)
                             or the element object
            value          : expected value, that the element value should
                             contains
            generic_param  : dynamic xpath/id variable
        """
        info("Check element value should have :: %s :: %s ::::value=%s" % \
                                  (element_name, generic_param, value))
        self.__obj.value_should_contains(element_name, value,
                                         generic_param=generic_param)

    def scrol_top_to_bottom(self, **kwargs):
        """ Scroll from top to bottom """
        return self.__obj.scrol_top_to_bottom(**kwargs)

    def get_value(self, element_name, generic_param=()):
        """
        Get the value of an element
        @args :
            element_name   : name of element
                             (whose entry is in element-path.cfg)
                             or the element object
            generic_param  : dynamic xpath/id variable
        """
        return self.__obj.get_value(element_name, generic_param=generic_param)

    def tap_on(self, point_x, point_y):
        """ Tap on a point whose co-ordinate has been provided """
        return self.__obj.tap_on(point_x, point_y)

    def back(self):
        """ execute the back button command """
        self.__obj.driver.back()

    def is_item_visible(self, element_name, generic_param=()):
        """
        Return True if item is visible else False
        @args :
            element_name  : name of element (whose entry is in element-path.cfg)
                            or the element object
            generic_param : dynamic xpath/id variable
        """
        info("Is item :: %s::%s visible" % (element_name, generic_param))
        visible = False
        try:
            self.__obj.should_visible(element_name, generic_param=generic_param)
            visible = True
        except Exception as ex :
            info(str(ex))

        return visible

    def get_element_details(self, element_name, generic_param=()):
        """
        Get the element details like width, height, coordinates etc.
        @args :
            element_name  : name of element (whose entry is in element-path.cfg)
                            or the element object
            generic_param : dynamic xpath/id variable
        """
        ele_obj = self.__obj.get_element( element_name,
                                          generic_param=generic_param)
        return { 'width': int(ele_obj.size['width']),
                 'height': int(ele_obj.size['height']),
                 'x-coordinate' : int(ele_obj.location['x']),
                 'y-coordinate' : int(ele_obj.location['y']),
                 'obj' : ele_obj
               }

    def take_screenshot(self):
        """ Take screen shot from the device and save it in hard disk """
        f_nam = datetime.datetime.now().strftime("SS-%Y_%m_%d_%H_%M_%S.png")
        f_nam = os.path.join(SCREEN_SHOT_DIR, f_nam)
        self.__obj.take_screenshot(f_nam)
        info("screen shot taken : %s"%f_nam)
        return f_nam

    def take_screenshot_of_element(self, element_name, generic_param=()):
        """ Take screen shot from the device and save it in hard disk """
        f_nam = datetime.datetime.now().strftime("SS-%Y_%m_%d_%H_%M_%S.png")
        f_nam = os.path.join(SCREEN_SHOT_DIR, f_nam)
        self.__obj.take_screenshot_of_element( element_name,
                                               generic_param=generic_param,
                                               file_name=f_nam)
        info("screen shot taken : %s"%f_nam)
        return f_nam

    def perform_home_btn_press(self):
        """ Perform home key press """
        global TAB_POINT
        global NEXT_TAB_POINT
        if TAB_POINT == None or NEXT_TAB_POINT == None:
            tmp = self.get_element_details( MAPPER['media'],
                                            generic_param=("MEDIA", ))
            TAB_POINT = (tmp['x-coordinate'], tmp['y-coordinate'])
            tmp = self.get_element_details( MAPPER['player'],
                                            generic_param=("PLAYER", ))
            NEXT_TAB_POINT = (tmp['x-coordinate'], tmp['y-coordinate'])
        self.__obj.native_key_press('HOME')

    def perform_mute_btn_press(self):
        """ Perform mute button press """
        self.__obj.native_key_press('VOLUME_MUTE')

    def perform_volume_up_btn_press(self):
        """ Perform volume up button press """
        self.__obj.native_key_press('VOLUME_UP')

    def perform_volume_down_btn_press(self):
        """ Perform volume down button press """
        self.__obj.native_key_press('VOLUME_DOWN')

    def relaunch_app_frm_menu(self):
        """ Click on menu and then click on the application to launch it """
        if self.__obj.re_launch_limelight_app(TEST_TARGET_CFG['app-name']):
            self.wait_for_app_to_launch()
            success("App is re-launched")
        else:
            raise Exception("App not launched")

    def rotate_device(self):
        """
        Perform the rotation of the device.
        If currently it is in horizontal orientation then it will rotate it to
        vertical, and if it is in vertical orientation then it will rotate to
        horizontal
        """
        info("rotating the device")
        change_from, change_to, out_put = self.__obj.rorate_device()
        info(str(out_put))
        success("orientation changed from %s to %s"%(change_from, change_to))

    def wait_for_app_to_launch(self):
        """
        Wait for the application to launch. sometime app takes time to launch
        """
        for ech_try in range(6):
            if not self.is_item_visible("app-title") :
                info('Wait for app to launch')
                self.__obj.tap_on(*TAB_POINT)
                self.__obj.wait_for(2)
                self.__obj.tap_on(*NEXT_TAB_POINT)
                self.__obj.wait_for(2)
            else:
                info('App is launched')
                break
        else:
            #raise Exception("app not launched")
            info("App not launched")

    @exception_mod.handle_exception
    def refresh_tab(self, tab_name):
        """ Refresh a tab """
        info("REFRESH TAB => %s" % (tab_name))
        self.__obj.refresh_by_pull_down()

    @exception_mod.handle_exception
    def scroll_down(self):
        """ Scroll down """
        self.__obj.refresh_by_pull_up()

    @exception_mod.handle_exception
    def set_select_value(self, tab_name, tel, operation, value):
        """
        Set/Select a particular element/value from a tab
        @args:
            tab_name      : name of the tab
            tel           : target element
            operation     : operation , set / select
            value            : set/select a value
        """
        info("SET/SELECT VALUE => %s value %s for %s ON %s" % \
                     (operation, value, tel, tab_name))

        # For settings page
        if "setting" in tab_name.lower():
            self.click_on("setting-elements-level", generic_param=(tel , ))
            info("Clicked => %s" %tel)
            if operation.lower().strip() == "set":
                self.should_visible("setting-popup-title")
                self.set_value("setting-popup-textbox", value)
                self.click_on('setting-popup-ok-btn')
            elif operation.lower().strip() == "select":
                self.should_visible("setting-popup-title")
                self.click_on("dropdown",
                              generic_param=(value , ))
            else:
                raise exception_mod.InvalidKeyWordUsed(operation=operation)

        # For channel groups page
        elif "channel groups" in tab_name.lower() or \
             "all channels" in tab_name.lower() or \
             "channels" in tab_name.lower() or \
             "all media" in tab_name.lower() or \
             "media" in tab_name.lower() :
            if operation.lower().strip() == "select":
                self.click_on("data-list-item", generic_param=(value,))
            else:
                raise exception_mod.InvalidKeyWordUsed(operation = operation)

        # For player page
        elif "player" in tab_name.lower():
            if operation.lower().strip() == "set":
                self.click_on(tel)
                self.set_value(tel, value)
            else:
                raise exception_mod.InvalidKeyWordUsed(operation=operation)
        else:
            raise exception_mod.GlueCodeNotImplemented

        info("DONE => %s value %s for %s ON %s" % \
            (operation, value, tel, tab_name))

    @exception_mod.handle_exception
    def verify_value(self, tab_name, tel, value):
        """
        Verify a value for an element of a tab
        @args:
            tab_name : name of the tab that need to select
            tel      : target element
            value       : value that need to verify
        """
        info("CHECK VALUE => %s = %s" %(tel, value))
        if "setting" in tab_name.lower():
            self.value_should_be("setting-elements-level-op", value,
                                 generic_param=(tel,))
        info("DONE => %s = %s" %(tel, value))

    @exception_mod.handle_exception
    def perform_oper_on_tab(self, tab_name, oper):
        """ perform a operation on a tab
        @args:
            tab_name    :   name of the tab
            oper        :   operations - select / refresh / scroll-down
        """
        self.select_tab(tab_name)
        if oper.strip().lower() == "select" :
            pass
        elif oper.strip().lower() == "refresh" :
            self.refresh_tab(tab_name)
        elif oper.strip().lower() == "scroll-down" :
            self.scroll_down()
        else:
            raise exception_mod.GlueCodeNotImplemented

    @staticmethod
    def compare_two_list(list1, list2):
        """ List comparison helper function"""
        return reduce( lambda v1, v2: v1 and v2,
                       map(lambda ei: ei in list1, list2))

    @exception_mod.handle_exception
    def check_icon(self, check_ele, verify_data, should_equal):
        """
        """
        if check_ele.lower().strip().endswith(" tab"):
            tab_name = re.search( "(\S+)\s+TAB", check_ele,
                                  flags=re.IGNORECASE).group(1)
            self.select_tab(tab_name)

            for element_text, icon_name in verify_data:
                info("checking : %s :: %s" % (element_text, icon_name))
                last_excp = ""
                for ech_try in range(3):
                    try:
                        scrn_sht_file = self.take_screenshot_of_element(
                                                     "icon-of-item",
                                                     (element_text, ))

                        stat = OpenCvLibrary.search_picture_in_picture(
                                      scrn_sht_file,
                                      os.path.join(ICON_DIR, icon_name))
                        if stat and should_equal:
                            success("icon matched: %s - %s" % \
                                 (element_text, icon_name))
                        elif not stat and not should_equal:
                            success("icon not matched: %s - %s" % \
                                 (element_text, icon_name))
                        elif should_equal:
                            raise Exception("icon not matched: %s - %s" % \
                                 (element_text, icon_name))
                        elif not should_equal:
                            raise Exception("icon matched: %s - %s" % \
                                 (element_text, icon_name))
                        try:
                            os.remove(scrn_sht_file)
                        except Exception as os_ex:
                            info("Not able to delete file :: %s" % str(os_ex))
                        break
                    except Exception as ex:
                        last_excp = str(ex)
                        info(last_excp + "--- Retrying" )
                        self.__obj.wait_for(LONG_WAIT)
                else:
                    raise Exception(last_excp)


    @exception_mod.handle_exception
    def check_errors(self, check_ele, verify_data, should_equal):
        """
        Match the error message from device with the error message from
        feature file
        """
        # If we checking the data of a tab
        if check_ele.lower().strip().endswith(" tab"):
            tab_name = re.search( "(\S+)\s+TAB", check_ele,
                                  flags=re.IGNORECASE).group(1)

            self.select_tab(tab_name)
            error_msg = ""
            for ech_try in range(1,5):
                try:
                    error_msg = self.get_value("error-message-in-tab")
                    break
                except Exception as ex:
                    info(str(ex))
                    info("retrying %s wait for 2 secs" % ech_try)
                    self.__obj.wait_for(2)

            if error_msg.lower().strip() == verify_data[0].lower().strip() \
              and should_equal:
                success(exception_mod.equal_success_msg % error_msg)

            elif error_msg.lower().strip() != verify_data[0].lower().strip() \
              and not should_equal:
                success(exception_mod.not_equal_success_msg % ( verify_data,
                                                              error_msg))
            elif should_equal :
                raise exception_mod.NotEqualException(verify_data, error_msg)

            else:
                raise exception_mod.EqualException(error_msg)



    @exception_mod.handle_exception
    def check_contains(self, check_ele, verify_data, should_equal):
        """ match the data from device with the data from feature file"""
        # If we checking the data of a tab
        if check_ele.lower().strip().endswith(" tab"):
            tab_name = re.search( "(\S+)\s+TAB", check_ele,
                                  flags=re.IGNORECASE).group(1)

            self.select_tab(tab_name)

            data_got = self.scrol_top_to_bottom(ret_all_data=True)
            data_got = [] if data_got == None else data_got
            stat = self.compare_two_list(data_got, verify_data)
            if should_equal and not stat:
                raise exception_mod.NotEqualException(verify_data, data_got)
            elif not should_equal and stat:
                raise exception_mod.EqualException(data_got)
            elif should_equal and stat:
                success(exception_mod.equal_success_msg%data_got)
            elif not should_equal and not stat:
                success(exception_mod.not_equal_success_msg%( verify_data,
                                                              data_got))
            else:
                raise exception_mod.GlueCodeNotImplemented

        # If we checking the element of player
        if check_ele.lower().strip() == "player":
            info("checking the player controls: %s"%', '.join(verify_data))
            for ech_ele in verify_data:
                info("chk :: %s"%ech_ele)
                for ech_try in range(2):
                    is_present = self.is_item_visible(ech_ele)
                    if not is_present:
                        self.click_on("player")
                    else: break

                if is_present:
                    msg = "element: %s is present" % ech_ele
                else:
                    msg = "element: %s is not present" % ech_ele

                if is_present == should_equal:
                    info(msg)
                else:
                    raise Exception(msg)

        # If the checking the element of a popup
        if check_ele.lower().strip() == "popup":
            data_got = self.scrol_top_to_bottom( ret_all_data=True,
                                                 scroll_ele_typ="popup")
            data_got = [] if data_got == None else data_got
            stat = self.compare_two_list(data_got, verify_data)
            if should_equal and not stat:
                raise exception_mod.NotEqualException(verify_data, data_got)
            elif not should_equal and stat:
                raise exception_mod.EqualException(data_got)
            elif should_equal and stat:
                success(exception_mod.equal_success_msg%data_got)
            elif not should_equal and not stat:
                success(exception_mod.not_equal_success_msg%(verify_data,
                                                             data_got))
            else:
                raise exception_mod.GlueCodeNotImplemented

    def go_to_tab_and_select_media(self, tab_name, media_name):
        """
        Select a tab then select the data of that tab
        @args:
            tab_name : name of the tab
            media_name : name of the media
        """
        self.select_tab(tab_name)
        self.set_select_value(tab_name, "", "select", media_name)

    def select_encoding(self, encoding_name, play_type):
        """
        Selects the encoding type
        @args:
            encoding_name : encoding-name/automatic/no-select
            play_type : local/remote
        """
        if play_type.strip().lower() == "local":
            if encoding_name.strip().lower() == "local":
                pass
            elif encoding_name.strip().lower() == "automatic":
                info("Select automatic encoding.")
                # Select the delivery check box
                self.click_on("check-box", generic_param=("Delivery",))
                # Click on the play button
                self.click_on("play-button")
        else:
            # Check if the choose encoding pop up is shown
            self.value_should_be("popup-title", "Choose Encoding")

            if encoding_name.strip().lower() == "no-select":
                # No need to select anything
                info("Not selecting any encoding.")
                return
            elif encoding_name.strip().lower() == "automatic":
                info("Select automatic encoding.")
                # Click on cancel button
                self.click_on("popup-button", generic_param=("Cancel", ))
                # Select the delivery check box
                self.click_on("check-box", generic_param=("Delivery", ))
                # Click on the play button
                self.click_on("play-button")
            else:
                # Click on the encoding element
                self.click_on("dropdown", generic_param=(encoding_name, ))

    def wait_for_any_alert_popup_get_close(self):
        """
        Wait for the alert popup get close
        """

        for i in xrange(30):
            try:
                if self.is_item_visible("alert-msg") :
                    info("%s :: alert popup still visible" % \
                         self.get_value("alert-msg"))
                    self.__obj.wait_for(MEDIUM_WAIT)
                else:
                    info("alert popup is not visible")
                    break
            except Exception:
                break
        else:
            raise Exception("alert popup still visible")

    def wait_for_media_fetch_from_server(self):
        """
        Wait for the media fetched from the limelight server,
        Here we are checking if the progress bar has disabled or not
        """

        for i in xrange(30):
            try:
                if self.is_item_visible("alert-msg") and \
                 self.get_value("alert-msg") == FETCHING_MEDIA_MSG :
                    info("%s :: is still visible"%FETCHING_MEDIA_MSG)
                    self.__obj.wait_for(SORT_WAIT)
                else:
                    info("%s :: is not visible"%FETCHING_MEDIA_MSG)
                    break
            except Exception:
                break
        else:
            raise Exception("%s ::: msg still visible"%FETCHING_MEDIA_MSG)

    def wait_for_offline_download(self, encoding_name, play_type):
        """
        Wait for the off-line data gets downloaded
        Here we are checking if the progress bar has disabled or not
        @args:
            encoding_name : name of the selected encoding
            play_type     : local/remote
        """

        if play_type.strip().lower() == "local":
            return

        if encoding_name.strip().lower() == "automatic":
            strict_check = False
        elif "widevineoffline" in encoding_name.strip().lower() :
            strict_check = True
        else:
            strict_check = None

        # For other cases we don't need to check this
        if strict_check != None :
            for i in xrange(30):
                try:
                    if self.is_item_visible("alert-msg") and \
                       self.get_value("alert-msg") == \
                       WIDEVINE_OFFLINE_DOWNLOAD_MSG :
                        info("%s is visible" % WIDEVINE_OFFLINE_DOWNLOAD_MSG)
                        if strict_check :
                            strict_check = False
                        self.__obj.wait_for(MEDIUM_WAIT)
                    else:
                        info("%s not visible" % WIDEVINE_OFFLINE_DOWNLOAD_MSG)
                        # currently commented out this raise
                        #if strict_check:
                        #    raise Exception(exception_mod.widevine_error)
                        break
                except Exception as excp :
                    info(str(excp))
                    break
            else:
                raise Exception("%s ::: msg still visible" % \
                                WIDEVINE_OFFLINE_DOWNLOAD_MSG)

    @exception_mod.handle_exception
    def perform_video_operations(self, opr, media_type, media_source,
                                 encoding_type):
        """
        @args:
            opr : play/pause/resume/seek-xx:xx/forwarded/
                  reversed/attempt-to-play
            media_type : local/remote/media-name
                         local -> Play from the memory card of the device
                         remote -> Play the remote url/media id
                         media-name -> if we want to play it from all
                                       media/media tab
            media_source : "menulink/file-name"/url/media-id/media-tab
            encoding_type : encoding-name/automatic/no-select
        """
        ret_data = {'bfr_elapsed_time': None,
                    'aftr_elapsed_time': None,
                    'encoding_type': encoding_type}

        info("Performing the video operations with parameters - ")
        pmsg = "operation=%s, media_type=%s, media_source=%s, encoding_type=%s"
        info(pmsg % (opr, media_type, media_source, encoding_type))

        if opr.strip().lower() == "play":

            # If play has passed then encoding_type should not "no-select"
            if encoding_type.lower().strip() == "no-select":
                msg = "for operation is %s, encoding should not %s" % \
                       (opr, encoding_type)
                raise exception_mod.InvalidCombination(**{'MSG':msg})

            # Local encoding type is for play a local file from SD card
            if encoding_type.lower().strip() == "local" and \
               media_type.strip().lower() != "local":
                msg = "local encoding is only when you play a local file"
                raise exception_mod.InvalidCombination(**{'MSG':msg})

            # For playing a local file then encoding should only be
            # 'automatic'/'local'
            if media_type.strip().lower() == "local" and \
               encoding_type.lower().strip() not in ['automatic', 'local']:
                msg = "To play a local file encoding should be automatic/local"
                raise exception_mod.InvalidCombination(**{'MSG':msg})

            if media_type.strip().lower() == "local":
                menue_link, file_name = media_source.split("/")
                # Go to player tab
                self.select_tab("PLAYER")
                # Click on browse button
                self.click_on("browse-button")
                # Check on toggle menu button
                self.click_on("toggle-menu-button")
                # Click on Downloads link
                self.click_on("toggle-menu-left-side-links",
                              generic_param=(menue_link,))
                # Check alert title bar has text Downloads
                self.value_should_be("local-file-browse-title", menue_link)
                # Open the view menu on the right side
                self.click_on("local-video-view-menu-open-btn")

                if self.is_item_visible("local-video-view-menu-option-link",
                                        generic_param=("List view",)):
                    info("List view option visible so clicking on it")
                    self.click_on("local-video-view-menu-option-link",
                                  generic_param=("List view",))
                else:
                    # back so that the menu disappear
                    self.back()
                # Click on the file
                self.click_on("file-link-in-menu", generic_param=(file_name,))
                # Click The file name is came in search text box
                #self.value_should_contains("media-id-text-box", file_name)
                # failure

            elif media_type.strip().lower() == "remote":
                self.select_tab("PLAYER")
                info("Set url/media-id=%s"%(media_source))
                # Set url/ media id
                self.set_select_value("player", "media-id-text-box",
                                      "set", media_source)
                # Click on the play button
                self.click_on("play-button")

            else:
                info("Go to tab=%s and select media=%s" % (media_source,
                                                           media_type))
                # Go to a tab and select the media
                self.go_to_tab_and_select_media(media_source, media_type)

            # wait for any alert popup like settings is fetched from server
            self.wait_for_any_alert_popup_get_close()

            # Select the encoding from drop down
            self.select_encoding(encoding_type, media_type)

            # Check if the fetching media from server msg disappear
            self.wait_for_media_fetch_from_server()

            # Check if the widevine download msg is displayer
            self.wait_for_offline_download(encoding_type, media_type)

            # wait for any alert popup like widevine rights
            self.wait_for_any_alert_popup_get_close()

            for ech_try in range(4):
                if self.is_item_visible('player-elapsed-time'):
                    try:
                        ret_data['aftr_elapsed_time'] = \
                          self.get_value('player-elapsed-time')
                        break
                    except Exception as ex:
                        info(str(ex))
                try:
                    self.click_on("player")
                except Exception as ex:
                    info(str(ex))
                    self.__obj.wait_for(2)
                info("player elapsed time not found, retrying again")

        elif opr.strip().lower() == "pause":
            for ech_try in range(3):
                # Check if the pause button is visible
                if not self.is_item_visible("player-pause-button"):
                    # Tap on the video player, if pause button isn't visible
                    self.click_on("player")
                try:
                    ret_data['bfr_elapsed_time'] = \
                       self.get_value('player-elapsed-time')
                    # Click on the pause button
                    self.click_on("player-pause-button")
                    ret_data['aftr_elapsed_time'] = \
                       self.get_value('player-elapsed-time')
                    break
                except Exception as ex:
                    warning(str(ex))
                    warning("pause button get invisible so quick")
                    continue

        elif opr.strip().lower() == "resume":
            for ech_try in range(3):
                # Check if the play button is visible
                if not self.is_item_visible("player-play-button"):
                    # Tap on video player, if play button is not visible
                    self.click_on("player")
                try:
                    ret_data['bfr_elapsed_time'] = \
                       self.get_value('player-elapsed-time')
                    # Click on the play button
                    self.click_on("player-play-button")
                    ret_data['aftr_elapsed_time'] = \
                       self.get_value('player-elapsed-time')
                    break
                except Exception as ex:
                    warning(str(ex))
                    warning("play button get invisible so quick")
                    continue

        elif opr.strip().lower() == "forwarded":
            for ech_try in range(3):
                # Check if the forward button is visible
                if not self.is_item_visible("player-forward-button"):
                    # Tap on video player, if forward button is not visible
                    self.click_on("player")
                try:
                    ret_data['bfr_elapsed_time'] = \
                      self.get_value('player-elapsed-time')
                    # Click on the forward button
                    self.click_on("player-forward-button")
                    ret_data['aftr_elapsed_time'] = \
                      self.get_value('player-elapsed-time')
                    break
                except Exception as ex:
                    warning(str(ex))
                    warning("forward button get invisible so quick")
                    continue

        elif opr.strip().lower() == "reversed":
            for ech_try in range(3):
                # Check if the rewind button is visible
                if not self.is_item_visible("player-rewind-button"):
                    # Tap on video player, if rewind button is not visible
                    self.click_on("player")
                try:
                    ret_data['bfr_elapsed_time'] = \
                      self.get_value('player-elapsed-time')
                    # Click on the rewind button
                    self.click_on("player-rewind-button")
                    ret_data['aftr_elapsed_time'] = \
                      self.get_value('player-elapsed-time')
                    break
                except Exception as ex:
                    warning(str(ex))
                    warning("rewind button get invisible so quick")
                    continue

        elif re.search("^seek-\d+:\d+$", opr.strip().lower()):
            seek_to_dur = opr.split("-")[1].split(':')
            seek_to_sec = int(seek_to_dur[0]) * 60 + int(seek_to_dur[1])

            for ech_try in range(3):
                # Check if the seek bar is visible
                if not self.is_item_visible("player-seekbar"):
                    # Tap on video player, if seek bar is not visible
                    self.click_on("player")
                try:

                    tot_vdo_dur = self.get_value("player-video-duration")\
                                  .split(':')
                    tot_vdo_sec = int(tot_vdo_dur[0]) * 60 + int(tot_vdo_dur[1])

                    ele_details = self.get_element_details("player-seekbar")
                    x_cal = (float(ele_details['width']) / tot_vdo_sec) \
                            * seek_to_sec
                    go_to_x = ele_details['x-coordinate'] + x_cal
                    go_to_x = int(round(go_to_x))
                    go_to_y = ele_details['y-coordinate']
                    ret_data['bfr_elapsed_time'] = \
                       self.get_value('player-elapsed-time')
                    # Seek the value
                    self.tap_on(go_to_x, go_to_y)
                    ret_data['aftr_elapsed_time'] = \
                     self.get_value('player-elapsed-time')
                    break
                except Exception as ex:
                    warning(str(ex))
                    warning("seek bar get invisible so quick")
                    continue

        elif opr.strip().lower() == "attempt-to-play":
            # If attempt-to-play has passed then
            # encoding_type should be "no-select"
            if encoding_type.lower().strip() != "no-select":
                raise exception_mod.InvalidKeyWordUsed(**{
                                            'encoding selection':encoding_type,
                                            'operation':opr})

            if media_type.strip().lower() == "local":
                raise exception_mod.GlueCodeNotImplemented

            elif media_type.strip().lower() == "remote":
                # Set url/ media id
                self.set_select_value("player", "media-id-text-box",
                                      "set", media_source)
                # Click on the play button
                self.click_on("play-button")

            else:
                # Go to a tab and select the media
                self.go_to_tab_and_select_media(media_source, media_type)

            # Select the encoding from drop down
            self.select_encoding(encoding_type, media_type)

        else:
            raise exception_mod.InvalidKeyWordUsed(operation=opr)

        return ret_data

    def verify_elapsed_time(self, duration, shud_diff=False):
        """
        Get elapsed time & match with passed duration
        @args:
            duration : the duration that need to verify
            shud_diff : True if it should difference else False
        """
        num_of_try = 0
        while num_of_try < 5:
            try:
                if not self.is_item_visible("player-elapsed-time"):
                    self.click_on('player')
                elapse_time = self.get_value("player-elapsed-time")
                break
            except Exception as ex:
                last_err = str(ex)
                info("error while getting elapsed time :: %s" % last_err)
                num_of_try += 1
                self.__obj.wait_for(SORT_WAIT)
        else:
            raise Exception(last_err)

        elapse_time_sec = elapse_time.split(':')
        elapse_time_sec = int(elapse_time_sec[0]) * 60 + int(elapse_time_sec[1])

        duration_sec = duration.split(':')
        duration_sec = int(duration_sec[0]) * 60 + int(duration_sec[1])
        if shud_diff:
            if 0 <= (elapse_time_sec - duration_sec) < 3:
                raise Exception("elapse-time: prev=%s, now=%s" % (duration,
                                                                  elapse_time))
            else:
                info("elapse-time: prev=%s, now=%s" % (duration, elapse_time))

        else:
            if -5 <= (elapse_time_sec - duration_sec) < 10:
                info("got elapse time=%s, passed %s" % (elapse_time,
                                                        duration))
            else:
                raise Exception("got elapse time=%s; expected=%s" % (elapse_time,
                                                                     duration))

        return elapse_time

    def check_player_btn_icon(self, btn):
        """Take screen shot with player control bar & check given button icon"""
        img_btn = os.path.join(SCREEN_SHOT_DIR, "player_btn", "%s.png"%btn)
        info("chk img_btn %s present"%img_btn)
        screen_shot = self.tk_screen_shot_of_player(wth_plyr_cntrl=True)
        try:
            if OpenCvLibrary.search_picture_in_picture(screen_shot, img_btn):
                success("%s icon present in screen"%btn)
        except Exception as ex:
            error(str(ex))
            raise Exception("%s btn icon chk failed in screen." % btn)

    def tk_screen_shot_of_player(self, wth_plyr_cntrl, only_player=False):
        """
        Take the screen shot with or without player control
        @args:
            wth_plyr_cntrl : True  - Take screenshot with player control bar
                             False - Take screenshot without player control bar
            only_player    : True  - Take screenshot of player only
                             False - Take screenshot of full screen
        """
        if only_player:
            msg = "take screen shot of player"
        else:
            msg = "take screen shot of whole screen"
        info(msg)
        for ty in range(3):
            try:
                player_ele_data = self.get_element_details('player')
                break
            except Exception as ex :
                warning(str(ex))
                self.__obj.wait_for(2)
        else:
            raise Exception("not able to access player")

        if wth_plyr_cntrl:
            if not self.is_item_visible("player-controller-container"):
                self.click_on(player_ele_data['obj'])
        else:
            if self.is_item_visible("player-controller-container"):
                self.click_on(player_ele_data['obj'])
        if not self.is_item_visible("player-controller-container") and \
           wth_plyr_cntrl:
            self.click_on(player_ele_data['obj'])
        screen_shot = self.take_screenshot()
        if only_player:
            y1 = player_ele_data['y-coordinate']
            x1 = player_ele_data['x-coordinate']
            y2 = y1 + player_ele_data['height']
            x2 = x1 + player_ele_data['width']

            orientation_map = {'LANDSCAPE': (x1, x2, y1, y2),
                               'PORTRAIT': (y1, y2, x1, x2)}
            current_orientation = self.__obj.get_current_orientation()
            OpenCvLibrary.crop_file(screen_shot,
                                    *orientation_map[str(current_orientation)])
        return screen_shot

    def check_player_is_playing(self, data, skip_step=[]):
        """
        @args :
              data    : data from previous grammar
              skip_step : the steps u want to skip
        Steps:
        1. Check pause button icon
        2. Take screen shot of the player without player control bar
        3. Wait for some time to take the next screen shot
        4. Take screen shot of whole screen without player control bar
        5. Verify the screen shots are different
        6. Check elapsed time has changed
        """
        bfr_elapsed_time = data['bfr_elapsed_time']
        aftr_elapsed_time = data['aftr_elapsed_time']
        if not aftr_elapsed_time:
            if not self.is_item_visible("player-elapsed-time"):
                self.click_on('player')
            aftr_elapsed_time = self.get_value("player-elapsed-time")

        if '1' not in skip_step:
            # Check pause button icon
            self.check_player_btn_icon('pause')

        if '2' not in skip_step:
            # Take screen shot of the player without player control bar
            sc_sht = self.tk_screen_shot_of_player(wth_plyr_cntrl=False,
                                                   only_player=True)

        if '3' not in skip_step:
            # Wait for some time to take the next screen shot
            self.__obj.wait_for(3)

        if '4' not in skip_step:
            # Take screen shot of whole screen without player control bar
            sc_sht1 = self.tk_screen_shot_of_player(wth_plyr_cntrl=False)

        if '5' not in skip_step:
            # Verify the screen shots are different
            if OpenCvLibrary.search_picture_in_picture(sc_sht1, sc_sht):
                msg = "screen-shots are same:: %s :: %s"
                raise Exception(msg %(sc_sht, sc_sht1))
            else:
                msg = "screen-shots are diff:: %s :: %s"
                success(msg %(sc_sht, sc_sht1))

        if '6' not in skip_step:
            # Check elapsed time has changed
            self.verify_elapsed_time(aftr_elapsed_time, shud_diff=True)

    def check_player_is_paused(self, data, skip_step=[]):
        """
        @args :
              data    : data from previous grammar
              skip_step : the steps u want to skip
        Steps:
        1. Verify elapsed time, time should not change
        2. Check play button icon
        3. Take screen shot of the player without player control bar
        4. Wait for some time to take the next screen shot
        5. Take screen shot of whole screen without player control bar
        6. Verify the screen shots are same
        7. Check elapsed time has not changed

        """
        bfr_elapsed_time = data['bfr_elapsed_time']
        aftr_elapsed_time = data['aftr_elapsed_time']

        if not bfr_elapsed_time:
            if not self.is_item_visible("player-elapsed-time"):
                self.click_on('player')
            bfr_elapsed_time = self.get_value("player-elapsed-time")
            self.__obj.wait_for(1)
            aftr_elapsed_time = None

        if not aftr_elapsed_time:
            if not self.is_item_visible("player-elapsed-time"):
                self.click_on('player')
            aftr_elapsed_time = self.get_value("player-elapsed-time")


        # Verify elapsed time, time should not change
        if '1' not in skip_step:
            info('verify elapsed time, time should not change')
            bfr_elapsed_time_sec = bfr_elapsed_time.split(':')
            bfr_elapsed_time_sec = int(bfr_elapsed_time_sec[0]) * 60 + \
                                   int(bfr_elapsed_time_sec[1])
            aftr_elapsed_time_sec = aftr_elapsed_time.split(':')
            aftr_elapsed_time_sec = int(aftr_elapsed_time_sec[0]) * 60 + \
                                    int(aftr_elapsed_time_sec[1])
            if (aftr_elapsed_time_sec - bfr_elapsed_time_sec) > 3:
                msg = "on click pause btn elapse time not stop." + \
                      " prev: %s, after: %s"
                raise Exception(msg%(bfr_elapsed_time, aftr_elapsed_time))
            else:
                msg = "on click pause btn elapse time stop." + \
                      " prev: %s, after: %s"
                success(msg % (bfr_elapsed_time, aftr_elapsed_time))

        # Check play button icon
        if '2' not in skip_step:
            self.check_player_btn_icon('play')

        # Take screen shot without player control bar
        if '3' not in skip_step:
            sc_sht = self.tk_screen_shot_of_player(wth_plyr_cntrl=False,
                                                   only_player=True)

        # Wait for some time to take the next screen shot
        if '4' not in skip_step:
            self.__obj.wait_for(3)

        # Take screen shot of whole screen without player control bar
        if '5' not in skip_step:
            sc_sht1 = self.tk_screen_shot_of_player(wth_plyr_cntrl=False)

        # Verify the screen shots are same
        if '6' not in skip_step:
            info("verify screen shots are same")
            if OpenCvLibrary.search_picture_in_picture(sc_sht1, sc_sht):
                success("screen shots are same: %s :: %s" % (sc_sht, sc_sht1))
            else:
                raise Exception("screen shots are not same: %s :: %s" % (sc_sht,
                                                                       sc_sht1))

        # Check elapsed time has not changed
        if '7' not in skip_step:
            self.verify_elapsed_time(aftr_elapsed_time, shud_diff=False)

    def check_player_is_resumed(self, data, skip_step=[]):
        """
        @args :
              data : data from previous grammar
        Steps:
        1. Check pause button icon
        2. Take screen shot of the player without player control bar
        3. Wait for some time to take the next screen shot
        4. Take screen shot of whole screen without player control bar
        5. Verify the screen shots are different
        6. Check elapsed time has changed
        """
        #bfr_elapsed_time = data['bfr_elapsed_time']
        #aftr_elapsed_time = data['aftr_elapsed_time']
        self.check_player_is_playing(data, skip_step=skip_step)

    def check_player_is_forwarded(self, data, state, skip_step=[]):
        """
        @args :
              data    : data from previous grammar
              state   : play/pause
        Steps:
        1. Verify elapsed time, time is move forward with a certain range
        2. Check pause/play button icon according to player state
        3. Take screen shot of the player without player control bar
        4. Wait for some time to take the next screen shot
        5. Take screen shot of whole screen without player control bar
        6. verify the screen shot are different/same according to player state
        7. Check elapsed time has changed/remain same according to play/pause
        """
        bfr_elapsed_time = data['bfr_elapsed_time']
        aftr_elapsed_time = data['aftr_elapsed_time']

        # Verify elapsed time, time is move forward with a certain range
        info('verify elapsed time, time is move forward with a certain range')
        bfr_elapsed_time_sec = bfr_elapsed_time.split(':')
        bfr_elapsed_time_sec = int(bfr_elapsed_time_sec[0]) * 60 + \
                               int(bfr_elapsed_time_sec[1])
        aftr_elapsed_time_sec = aftr_elapsed_time.split(':')
        aftr_elapsed_time_sec = int(aftr_elapsed_time_sec[0]) * 60 + \
                                int(aftr_elapsed_time_sec[1])
        if FORWARD_SEC <= (aftr_elapsed_time_sec - bfr_elapsed_time_sec) \
           < FORWARD_SEC + 7:
            msg = "on click forward btn elapse time change as expect." + \
                  " prev: %s, after: %s"
            success(msg % (bfr_elapsed_time, aftr_elapsed_time))
        else:
            msg = "on click forward btn elapse time don't change as expect." + \
                  " prev: %s, after: %s"
            raise Exception(msg % (bfr_elapsed_time, aftr_elapsed_time))

        # perform the steps according to state : play/pause
        if state == 'play':
            self.check_player_is_playing( data, skip_step=skip_step)
        else:
            skip_step.append('1')
            self.check_player_is_paused( data, skip_step=skip_step)

    def check_player_is_reversed(self, data, state, skip_step=[]):
        """
        @args :
              data    : data from previous grammar
              state   : play/pause
        Steps:
        1. Verify elapsed time, time is move backwards with a certain range
        2. Check pause/play button icon according to player state
        3. Take screen shot of the player without player control bar
        4. Wait for some time to take the next screen shot
        5. Take screen shot of whole screen without player control bar
        6. verify the screen shot are different/same according to player state
        7. Check elapsed time has changed/remain same according to play/pause
        """
        bfr_elapsed_time = data['bfr_elapsed_time']
        aftr_elapsed_time = data['aftr_elapsed_time']

        # Verify elapsed time, time is move backward with a certain range
        info('verify elapsed time, time is move backward with a certain range')
        bfr_elapsed_time_sec = bfr_elapsed_time.split(':')
        bfr_elapsed_time_sec = int(bfr_elapsed_time_sec[0]) * 60 + \
                               int(bfr_elapsed_time_sec[1])
        aftr_elapsed_time_sec = aftr_elapsed_time.split(':')
        aftr_elapsed_time_sec = int(aftr_elapsed_time_sec[0]) * 60 + \
                                int(aftr_elapsed_time_sec[1])
        ## Reverse cause move back
        if -REVERSE_SEC <= (aftr_elapsed_time_sec - bfr_elapsed_time_sec) < \
           -REVERSE_SEC + 5:
            msg = "on click forward btn elapse time change as expect." + \
                  " prev: %s, after: %s"
            success(msg % (bfr_elapsed_time, aftr_elapsed_time))
        else:
            msg = "on click forward btn elapse time don't change as expect." + \
                  " prev: %s, after: %s"
            raise Exception(msg % (bfr_elapsed_time, aftr_elapsed_time))

        # perform the steps according to state : play/pause
        if state == 'play':
            self.check_player_is_playing(data, skip_step=skip_step)
        else:
            skip_step.append('1')
            self.check_player_is_paused(data, skip_step=skip_step)

    def check_player_is_seeking(self, duration, data, state, skip_step=[]):
        """
        @args :
              duration: duration that passed from front end
              data    : data from previous grammar
              state   : play/pause
        Steps:
        1. Verify elapsed time, aftr_elapsed_time ~= duration
        2. Check pause/play button icon according to player state
        3. Take screen shot of the player without player control bar
        4. Wait for some time to take the next screen shot
        5. Take screen shot of whole screen without player control bar
        6. verify the screen shot are different/same according to player state
        7. Check elapsed time has changed/remain same according to play/pause
        """
        aftr_elapsed_time = data['aftr_elapsed_time']

        # Verify elapsed time, aftr_elapsed_time ~= duration
        info('verify elapsed time, aftr_elapsed_time ~= duration')
        duration_sec = duration.split(':')
        duration_sec = int(duration_sec[0]) * 60 + int(duration_sec[1])
        aftr_elapsed_time_sec = aftr_elapsed_time.split(':')
        aftr_elapsed_time_sec = int(aftr_elapsed_time_sec[0]) * 60 + \
                                int(aftr_elapsed_time_sec[1])
        ## Reverse cause move back
        if abs(aftr_elapsed_time_sec - duration_sec) < 6:
            msg = "verify elapsed time is seek to time." + \
                  " elapsed time: %s, duration: %s"
            success(msg % (aftr_elapsed_time, duration))
        else:
            msg = "verify failed elapsed time is seek time." + \
                  " elapsed time: %s, duration: %s"
            raise Exception(msg%(aftr_elapsed_time, duration))

        # perform the steps according to state : play/pause
        if state == 'play':
            self.check_player_is_playing(data, skip_step=skip_step)
        else:
            skip_step.append('1')
            self.check_player_is_paused(data, skip_step=skip_step)

    @exception_mod.handle_exception
    def check_player(self, operation, vdo_src_typ, duration,
                     player_state, ret_data):
        """
        @args :
              operation   : operations - play/pause/resume/continue-playing/
                            remain-pause/seek/forwarded/reversed/not play
              vdo_src_typ : Source of play - file/url/mediaId/media-tab
              duration    : Duration of play (min:sec)
              player_state: State of the player - play/pause
              ret_data    : Previous grammar data
        """

        if operation.lower().strip() == "play" and \
           player_state.lower().strip() == "play":
            ## '''Checking play'''
            # For protected content skip image comparison
            if ("widevine" in ret_data['encoding_type'].lower()) or \
               ( "automatic" in ret_data['encoding_type'].lower() and \
                 vdo_src_typ.lower().strip() != "file"):
                skip_step = ['2', '4', '5']
            else:
                skip_step = []
            self.check_player_is_playing(ret_data, skip_step=skip_step)

        elif operation.lower().strip() == "pause" and \
           player_state.lower().strip() == "pause":
            ## '''Checking pause'''
            # For protected content skip image comparison
            if ("widevine" in ret_data['encoding_type'].lower()) or \
               ( "automatic" in ret_data['encoding_type'].lower() and \
                 vdo_src_typ.lower().strip() != "file"):
                skip_step = ['3', '5', '6']
            else:
                skip_step = []
            self.check_player_is_paused(ret_data, skip_step=skip_step)

        elif operation.lower().strip() == "resume" and \
           player_state.lower().strip() == "play":
            ## '''Checking resume'''
            # For protected content skip image comparison
            if ("widevine" in ret_data['encoding_type'].lower()) or \
               ( "automatic" in ret_data['encoding_type'].lower() and \
                 vdo_src_typ.lower().strip() != "file"):
                skip_step = ['2', '4', '5']
            else:
                skip_step = []
            self.check_player_is_resumed(ret_data, skip_step=skip_step)

        elif operation.lower().strip() == "forwarded":
            ## '''Checking forwarded with play/pause state'''
            # For protected content skip image comparison
            if ("widevine" in ret_data['encoding_type'].lower()) or \
               ( "automatic" in ret_data['encoding_type'].lower() and \
                 vdo_src_typ.lower().strip() != "file"):
                if player_state.lower().strip() == "play":
                    skip_step = ['2', '4', '5']
                else:
                    skip_step = ['3', '5', '6']
            else:
                skip_step = []

            self.check_player_is_forwarded(ret_data,
                                           player_state.lower().strip(),
                                           skip_step=skip_step)

        elif operation.lower().strip() == "reversed":
            ## '''Checking reversed with play/pause state'''
            # For protected content skip image comparison
            if ("widevine" in ret_data['encoding_type'].lower()) or \
               ( "automatic" in ret_data['encoding_type'].lower() and \
                 vdo_src_typ.lower().strip() != "file"):
                if player_state.lower().strip() == "play":
                    skip_step = ['2', '4', '5']
                else:
                    skip_step = ['3', '5', '6']
            else:
                skip_step = []

            self.check_player_is_reversed( ret_data,
                                           player_state.lower().strip(),
                                           skip_step=skip_step)

        elif operation.lower().strip() == "seek":
            ## '''Checking seek'''
            # For protected content skip image comparison
            if ("widevine" in ret_data['encoding_type'].lower()) or \
               ( "automatic" in ret_data['encoding_type'].lower() and \
                 vdo_src_typ.lower().strip() != "file"):
                if player_state.lower().strip() == "play":
                    skip_step = ['2', '4', '5']
                else:
                    skip_step = ['3', '5', '6']
            else:
                skip_step = []
            self.check_player_is_seeking(duration, ret_data,
                                         player_state.lower().strip(),
                                         skip_step=skip_step)

        elif operation.lower().strip() == "continue-playing":
            ## '''Checking play'''
            # For protected content skip image comparison
            if ("widevine" in ret_data['encoding_type'].lower()) or \
               ( "automatic" in ret_data['encoding_type'].lower() and \
                 vdo_src_typ.lower().strip() != "file"):
                skip_step = ['2', '4', '5']
            else:
                skip_step = []
            self.check_player_is_playing(ret_data, skip_step=skip_step)

        elif operation.lower().strip() == "remain-pause":
            ## Checking pause
            # For protected content skip image comparison
            if ("widevine" in ret_data['encoding_type'].lower()) or \
               ( "automatic" in ret_data['encoding_type'].lower() and \
                 vdo_src_typ.lower().strip() != "file"):
                skip_step = ['3', '5', '6']
            else:
                skip_step = []
            self.check_player_is_paused(ret_data, skip_step=skip_step)

        elif operation.lower().strip() == "not play" and \
           player_state.lower().strip() == "play":
            for ech_try in range(5):
                if self.is_item_visible("player"):
                    raise Exception("Player is loaded")
                else:
                    self.__obj.wait_for(SORT_WAIT)
            success("Player does not load")

    @exception_mod.handle_exception
    def perform_device_operations(self, action_itm, perform, target):
        """
        @args :
              action_itm : home-button/app-icon/screen
              perform    : press/orientation
              target     : application/device
        """
        if action_itm == 'home-button' and perform == "press" and \
           target == "application":
            self.perform_home_btn_press()
        elif action_itm == 'app-icon' and perform == "press" and \
           target == "device":
            self.relaunch_app_frm_menu()
        elif action_itm == 'screen' and perform == "orientation" and \
           target == "device":
            self.rotate_device()
        else:
            exception_mod.InvalidCombination( action_itm=action_itm,
                                              perform=perform,
                                              target=target)
    def add_to_playlist(self, target, media_names=None, tab_name=None):
        """
        @args :
        target      : following / all
        media_names  : [] -> a list of media name
        tab_name    : tab name of the app

        Steps:
        1. Go to that tab

        If need to add selective media then:
         2. Click on the the add to playlist button for each media that has been
         passed from feature file.

        If need to add all media:
         3. click on the "Add All To Playlist" button
        """
        # Step 1
        self.select_tab(tab_name)

        # Step 2
        if target == "all" and not media_names:
            self.click_on("add-all-to-playlist-btn")
        elif target == "following" and media_names:
            for ech_media_name in media_names:
                self.click_on("add-to-playlist-btn", (ech_media_name, ))
        else:
            raise exception_mod.InvalidCombination(target=target,
                                                   media_names=media_names)


    def remove_from_playlist(self, target, media_names=None, tab_name=None):
        """
        @args :
        target      : following / all
        media_names : [] -> a list of media name
        tab_name    : tab name of the app

        Steps:
        1. Go to that tab

        If need to remove selective media then:
         2. Click on the the remove button for each media that has been
         passed from feature file.

        If need to add all media:
         3. click on the remove button for all media
        """
        # Step 1
        self.select_tab(tab_name)
        self.__obj.scrol_bottom_to_top(retry=8)
        # Step 2
        if target == "all" and not media_names:
            self.__obj.scrol_bottom_to_top(retry=8)

            while True:
                try:
                    data = self.__obj.scrol_top_to_bottom(ret_all_data=True)
                except Exception as ex :
                    info(str(ex))
                    break
                if not data: break
                info("resetting the scroll")
                self.__obj.scrol_bottom_to_top(retry=8)
                info("resetting the scroll done")
                remove_item = []
                for ech_media_name in data:
                    if ech_media_name in remove_item:
                        continue
                    remove_item.append(ech_media_name)
                    self.click_on("remove-from-playlist-btn", (ech_media_name, ))
                    try:
                        self.__obj.scrol_bottom_to_top(retry=8)
                    except:
                        pass
                info("Items removed: %s" % remove_item)

        elif target == "following" and media_names:
            for ech_media_name in media_names:
                self.click_on("remove-from-playlist-btn", (ech_media_name, ))
                try:
                    self.__obj.scrol_bottom_to_top()
                except:
                    pass
        else:
            raise exception_mod.InvalidCombination(target=target,
                                                   media_names=media_names)

    @exception_mod.handle_exception
    def add_delete_from_playlist(self, operation, target, tab_name, media_name):
        """
        @args :
        operation : add / remove
        target    : following / all
        tab_name  : tab name of the app
        media_name  : [] -> a list of media name
        """
        if operation == "add" and target == "all" :
            # perform steps to add all media in playlist
            self.add_to_playlist(target, tab_name=tab_name)
        elif operation == "add" and target == "following" :
            # perform steps to add following media from feature file in playlist
            if not media_name:
                raise exception_mod.InvalidCombination(target=target,
                                                       media_name=media_name)
            self.add_to_playlist(target, media_name, tab_name)
        elif operation == "remove" and target == "all" :
            # perform steps to remove all media in playlist
            self.remove_from_playlist(target, tab_name=tab_name)
        elif operation == "remove" and target == "following" :
            # perform steps to remove provided media from feature file to
            # playlist
            if not media_name:
                raise exception_mod.InvalidCombination(target=target,
                                                       media_name=media_name)
            self.remove_from_playlist(target, media_name, tab_name)
        else:
            raise exception_mod.InvalidCombination( operation=operation,
                                                    target=target,
                                                    tab_name=tab_name,
                                                    media_name=media_name)

    @exception_mod.handle_exception
    def verify_add_delete_from_playlist(self, operation, target,
                                        source_tab_name, playlist_tab_name,
                                        media_name):
        """
        @args :
        target    : following / all
        source_tab_name : The tabe name feom where we are adding the media
                          This is very important while verifying the add all
                          scenario
        operation : added / removed
        playlist_tab_name  : tab name of the app that contains the playlist
        media_name  : [] -> a list of media name
        """
        if operation == "added" and target == "all" :
            # Steps :
            # 1. Go to tab source_tab_name
            self.select_tab(source_tab_name)
            # 2. Get all media name those are present in that tab
            self.__obj.scrol_bottom_to_top(retry=8)
            source_media_list = self.__obj.scrol_top_to_bottom(retry=4,
                                                           ret_all_data=True)
            self.__obj.scrol_bottom_to_top(retry=8) # reset the screen
            # 3. Go to tab playlist_tab_name
            self.select_tab(playlist_tab_name)
            # 4. Get all media that present in playlist
            self.__obj.scrol_bottom_to_top(retry=8)
            playlist_data = self.__obj.scrol_top_to_bottom(retry=4,
                                                           ret_all_data=True)
            self.__obj.scrol_bottom_to_top(retry=8) # reset the screen

            # 5. Verify that all media from source tab is present in playlist
            for ech_media_name in source_media_list:
                if ech_media_name in playlist_data:
                    info("media %s -- present" % ech_media_name)
                else:
                    raise Exception("media %s -- not present" % ech_media_name)
            success("All media from %s tab is present in playlist" % \
                    source_tab_name)

        elif operation == "added" and target == "following" :
            if not media_name:
                raise exception_mod.InvalidCombination(target=target,
                                                       media_name=media_name)
            # Steps :
            # 1. Go to tab playlist_tab_name
            self.select_tab(playlist_tab_name)

            # 2. Get all media that present in playlist
            self.__obj.scrol_bottom_to_top(retry=8)
            playlist_data = self.__obj.scrol_top_to_bottom(retry=4,
                                                           ret_all_data=True)
            self.__obj.scrol_bottom_to_top(retry=8) # reset the screen

            # 3. Verify that all media from feature file is present in playlist
            info(playlist_data)
            for ech_media_name in media_name:
                if ech_media_name in playlist_data:
                    info("media %s -- present" % ech_media_name)
                else:
                    raise Exception("media %s -- not present" % ech_media_name)

            success("All media from feature file is present in playlist")

        elif operation == "removed" and target == "all" :
            # Steps :
            # 1. Go to tab playlist_tab_name
            self.select_tab(playlist_tab_name)
            # 2. Get all media that present in playlist
            playlist_data = []
            if self.is_item_visible("playlist-container"):
                self.__obj.scrol_bottom_to_top(retry=8)
                playlist_data = self.__obj.scrol_top_to_bottom(retry=4,
                                                              ret_all_data=True)
                self.__obj.scrol_bottom_to_top(retry=8) # reset the screen

            # 3. Verify that all media has removed
            if playlist_data:
                raise Exception("all media has not removed from play list")
            else:
                success("all media has removed from play list")

        elif operation == "removed" and target == "following" :
            # Steps :
            # 1. Go to tab playlist_tab_name
            self.select_tab(playlist_tab_name)
            # 2. Get all media that present in playlist
            playlist_data = []
            if self.is_item_visible("playlist-container"):
                self.__obj.scrol_bottom_to_top(retry=8)
                playlist_data = self.__obj.scrol_top_to_bottom(retry=4,
                                                           ret_all_data=True)
                self.__obj.scrol_bottom_to_top(retry=8) # reset the screen
            # 3. Verify that all media from feature file doesn't present
            #    in playlist
            for ech_media_name in media_name:
                if ech_media_name not in playlist_data:
                    info("media %s :: not present" % ech_media_name)
                else:
                    raise Exception("media %s :: present" % ech_media_name)

            success("All media from feature file doesn't present in playlist")

        else:
            raise exception_mod.InvalidCombination( target=target,
                                            source_tab_name=source_tab_name,
                                            operation=operation,
                                            playlist_tab_name=playlist_tab_name,
                                            media_name=media_name)