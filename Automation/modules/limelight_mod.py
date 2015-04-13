#-------------------------------------------------------------------------------
# Name:        module1
# Purpose:
#
# Author:      gouri
#
# Created:     18-03-2015
# Copyright:   (c) gouri 2015
# Licence:     <your licence>
#-------------------------------------------------------------------------------

import re
import time
from appium_driver import *
from logger import info, error, warning, exception, success

from constant import LONG_WAIT, MEDIUM_WAIT, SORT_WAIT, \
                     APPIUM_SERVER, TEST_TARGET_CFG, MAPPER, \
                     LEFT_MOST_TAB, TAB_SCROLL, FETCHING_MEDIA_MSG, \
                     WIDEVINE_OFFLINE_DOWNLOAD_MSG

import exception_mod


class Limelight(object):
    def __init__(self):
        self.__obj = None

    @exception_mod.handle_exception
    def launch_app(self):
        info( "Launch the app.")
        if self.__obj:
            self.exit_app()
        self.__obj = Driver(APPIUM_SERVER,
                                      TEST_TARGET_CFG['os'],
                                      TEST_TARGET_CFG['os-version'],
                                      TEST_TARGET_CFG['device-name'])
        try:
            self.__obj.set_up()
        except Exception as ex :
            resn = ex.__dict__.get('reason')
            resn = resn if resn else str(ex)
            raise Exception(resn)
        #self.__obj.wait_for(LONG_WAIT)
        info("App Launched.")

    def exit_app(self):
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
        info("Check if the app is running.")
        stat = False
        if self.__obj == None:
            info("App is not running.")
        else:
            stat = True
            info("App is running.")
        return stat

    @exception_mod.handle_exception
    def select_tab(self, tab_name):
        info("SELECT TAB => %s" % (tab_name))

        if tab_name.lower().strip() == "current":
            pass
        else:
            # First move to the left most tab then start search
            self.__obj.scrol_right_to_left(retry=1)
            # Now go to targetted tab
            self.click_on(MAPPER[tab_name.lower()], generic_param=(tab_name, ))

        info("SELECTED TAB => %s" % (tab_name))
        try:
            self.__obj.scrol_bottom_to_top(retry=1)
        except Exception as exc:
            warning("While reset the scrol to top :%s"%str(exc))

    def click_on(self, element_name, generic_param=()):
        """ """
        info("Clicking on : %s :: %s"%(element_name, generic_param))
        self.__obj.click_on(element_name, generic_param=generic_param)

    def should_visible(self, element_name, generic_param=()):
        """ """
        info("Clicking Visibility :: %s :: %s"%(element_name, generic_param))
        self.__obj.should_visible(element_name, generic_param=generic_param)

    def set_value(self, element_name, value, generic_param=()):
        """ """
        info("Setting Value :: %s :: %s ::: value=%s"%(element_name,
                                                       generic_param, value))
        self.__obj.set_value(element_name, value, generic_param=generic_param)

    def value_should_be(self, element_name, value, generic_param=()):
        """ """
        info("Checking exact value :: %s :: %s :::: value=%s"%(element_name,
                                                         generic_param, value))
        self.__obj.value_should_be(element_name, value, generic_param=generic_param)

    def value_should_contains(self, element_name, value, generic_param=()):
        """ """
        info("Check ele value should have :: %s :: %s ::::value=%s"%(element_name,
                                                         generic_param, value))
        self.__obj.value_should_contains(element_name, value, generic_param=generic_param)

    def scrol_top_to_bottom(self, **kwargs):
        self.__obj.scrol_top_to_bottom(**kwargs)

    def get_value(self, element_name, generic_param=()):
        return self.__obj.get_value(element_name, generic_param=generic_param)

    def tap_on(self, point_x, point_y):
        return self.__obj.tap_on(point_x, point_y)

    def back(self):
        self.__obj.driver.back()

    def is_item_visible(self, element_name, generic_param=()):
        info("Is item :: %s::%s visible"%(element_name, generic_param))
        visible = False
        try:
            self.__obj.should_visible(element_name, generic_param=generic_param)
            visible = True
        except Exception as ex :
            info(str(ex))
            pass
        return visible

    def get_element_details(self, element_name, generic_param=()):
        ele_obj = self.__obj.get_element( element_name,
                                          generic_param=generic_param)
        return { 'width': int(ele_obj.size['width']),
                 'height': int(ele_obj.size['height']),
                 'x-cordinate' : int(ele_obj.location['x']),
                 'y-cordinate' : int(ele_obj.location['y'])
               }

    @exception_mod.handle_exception
    def refresh_tab(self, tab_name):
        info("REFRESH TAB => %s" % (tab_name))
        self.__obj.refresh_by_pull_down()

    @exception_mod.handle_exception
    def scroll_down(self, tab_name):
        pass

    @exception_mod.handle_exception
    def set_select_value(self, tab_name, tel, op, vl):
        info("SET/SELECT VALUE => %s value %s for %s ON %s" %(op, vl, tel, tab_name))

        # For settings page
        if "setting" in tab_name.lower():
            self.click_on("setting-elements-level", generic_param=(tel,))
            info("Clicked => %s" %tel)
            if op.lower().strip() == "set":
                self.should_visible("setting-popup-title")
                self.set_value("setting-popup-textbox", vl)
                self.click_on('setting-popup-ok-btn')
            elif op.lower().strip() == "select":
                self.should_visible("setting-popup-title")
                self.click_on("setting-log-level-dropdown", generic_param=(vl,))
            else:
                raise exception_mod.InvalidKeyWordUsed(operation = op)

        # For channel groups page
        elif "channel groups" in tab_name.lower() or \
             "all channels" in tab_name.lower() or \
             "channels" in tab_name.lower() or \
             "all media" in tab_name.lower() or \
             "media" in tab_name.lower() :
            if op.lower().strip() == "select":
                self.click_on("data-list-item", generic_param=(vl,))
            else:
                raise exception_mod.InvalidKeyWordUsed(operation = op)

        # For player page
        elif "player" in tab_name.lower():
            if op.lower().strip() == "set":
                self.click_on(tel)
                self.set_value(tel, vl)
            else:
                raise exception_mod.InvalidKeyWordUsed(operation = op)
        else:
            raise exception_mod.GlueCodeNotImplemented

        info("DONE => %s value %s for %s ON %s" %(op, vl, tel, tab_name))

    @exception_mod.handle_exception
    def verify_value(self, tab_name, tel, vl):
        info("CHECK VALUE => %s = %s" %(tel, vl))
        if "setting" in tab_name.lower():
            self.value_should_be("setting-elements-level-op", vl, generic_param=(tel,))
        info("DONE => %s = %s" %(tel, vl))

    @exception_mod.handle_exception
    def perform_oper_on_tab(self, tab_name, oper):
        """ """
        self.select_tab(tab_name)

        if oper.strip().lower() == "select" :
            pass
        elif oper.strip().lower() == "refresh" :
            self.refresh_tab(tab_name)
        elif oper.strip().lower() == "scroll-down" :
            self.scroll_down()
        else:
            raise exception_mod.GlueCodeNotImplemented

    def compare_two_list(self, list1, list2):
        return reduce(lambda v1, v2: v1 and v2, map(lambda ei: ei in list1, list2))

    @exception_mod.handle_exception
    def check_contains(self, check_ele, varify_data, should_equal):
        """ """
        # If we checking the data of a tab
        if check_ele.lower().strip().endswith(" tab"):
            tab_name = re.search("(\S+)\s+TAB", check_ele, flags=re.IGNORECASE).group(1)

            self.select_tab(tab_name)

            data_got = self.scrol_top_to_bottom(ret_all_data=True)

            stat = self.compare_two_list(data_got, varify_data)
            if should_equal and not stat:
                raise exception_mod.NotEqualException(varify_data, data_got)
            elif not should_equal and stat:
                raise exception_mod.EqualException(data_got)
            elif should_equal and stat:
                success(exception_mod.equal_success_msg%data_got)
            elif not should_equal and not stat:
                success(exception_mod.not_equal_success_msg%(varify_data,data_got))
            else:
                raise exception_mod.GlueCodeNotImplemented

        # If we checking the element of player
        if check_ele.lower().strip() == "player":
            info("checking the player controls: %s"%', '.join(varify_data))
            for ech_ele in varify_data:
                info("chk :: %s"%ech_ele)
                for et in range(2):
                    is_present = self.is_item_visible(ech_ele)
                    if not is_present: self.click_on("player")
                    else: break

                if is_present:
                    msg = "ele: %s is present"%ech_ele
                else:
                    msg = "ele: %s is not present"%ech_ele

                if is_present == should_equal:
                    info(msg)
                else:
                    raise Exception(msg)

    def go_to_tab_and_select_media(self, tab_name, media_name):
        self.select_tab(tab_name)
        self.set_select_value(tab_name, "", "select", media_name)

    def select_encoding(self, encoding_name, play_type):
        """
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
                self.click_on("popup-button", generic_param=("Cancel",))
                # Select the delivery check box
                self.click_on("check-box", generic_param=("Delivery",))
                # Click on the play button
                self.click_on("play-button")
            else:
                # Click on the encoding element
                self.click_on("dropdown", generic_param=(encoding_name,))



    def wait_for_media_fetch_from_server(self):
        """
        Wait for the media fetched from the limelight server,
        Here we are checking if the progrss bar has disabled or not
        """

        for i in xrange(8):
            if self.is_item_visible("alert-msg") and \
             self.get_value("alert-msg") == FETCHING_MEDIA_MSG :
                info("%s :: is still visible"%FETCHING_MEDIA_MSG)
                time.sleep(LONG_WAIT)
            else:
                info("%s :: is not visible"%FETCHING_MEDIA_MSG)
                break
        else:
            raise Exception("%s ::: msg still visible"%FETCHING_MEDIA_MSG)

    def wait_for_widevine_offline_download(self, encoding_name, play_type):
        """
        Wait for the widevine offline data gets downloaded
        Here we are checking if the progrss bar has disabled or not
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
            for i in xrange(8):
                if self.is_item_visible("alert-msg") and \
                 self.get_value("alert-msg") == WIDEVINE_OFFLINE_DOWNLOAD_MSG :
                    info("%s :: is visible"%WIDEVINE_OFFLINE_DOWNLOAD_MSG)
                    if strict_check : strict_check = False
                    time.sleep(LONG_WAIT)
                else:
                    info("%s :: not visible"%WIDEVINE_OFFLINE_DOWNLOAD_MSG)
                    if strict_check:
                        raise Exception(exception_modod.widevine_error)

                    break
            else:
                raise Exception("%s ::: msg still visible"%WIDEVINE_OFFLINE_DOWNLOAD_MSG)


    @exception_mod.handle_exception
    def perform_video_operations(self, opr, media_type, media_source, encoding_type):
        """
        opr : play/pause/resume/seek-xx:xx/forwarded/reversed/attempt-to-play
        media_type : local/remote/media-name
                   local -> Play from the memory card of the device
                   remote -> Play the remote url/media id
                   media-name -> if we want to play it from all media/media tab
        media_source : "menuelink/file-name"/url/media-id/media-tab
        encoding_type : encoding-name/automatic/no-select
        """
        info("Performing the video operations with parameters - ")
        pmsg = "opration=%s, media_type=%s, media_source=%s, encoding_type=%s"
        info(pmsg%(opr, media_type, media_source, encoding_type))

        if opr.strip().lower() == "play":

            # If play has paased then encoding_type should not "no-select"
            if encoding_type.lower().strip() == "no-select":
                msg = "for opration is %s, encoding selection should not %s"%(opr, encoding_type)
                raise exception_mod.InvalidCombination(**{'MSG':msg})

            # Local encoding type is only when we play a local file from sd card
            if encoding_type.lower().strip() == "local" and \
               media_type.strip().lower() != "local":
                msg = "local encoding is only when you play a local file"
                raise exception_mod.InvalidCombination(**{'MSG':msg})

            # For playing a local file then encoding should only be 'automatic'/'local'
            if media_type.strip().lower() == "local" and \
               encoding_type.lower().strip() not in ['automatic', 'local']:
                msg = "for playing a local file encoding type should be automatic/local"
                raise exception_mod.InvalidCombination(**{'MSG':msg})


            if media_type.strip().lower() == "local":
                menue_link, file_name = media_source.split("/")
                # Go to player tab
                self.select_tab("PLAYER")
                # Click on browse button
                self.click_on("browse-button")
                # Check on toggle menue button
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
                    # back so that the menue disappear
                    self.back()
                # Click on the file
                self.click_on("file-link-in-menu", generic_param=(file_name,))
                # Click The file name is came in search text box
                #self.value_should_contains("media-id-text-box", file_name) # failure


            elif media_type.strip().lower() == "remote":
                self.select_tab("PLAYER")
                info("Set url/media-id=%s"%(media_source))
                # Set url/ media id
                self.set_select_value("player", "media-id-text-box", "set", media_source)
                # Click on the play button
                self.click_on("play-button")

            else:
                info("Go to tab=%s and select media=%s"%(media_source, media_type))
                # Go to a tab and select the media
                self.go_to_tab_and_select_media(media_source, media_type)

            # Select the encoding from drop down
            self.select_encoding(encoding_type, media_type)

            # Check if the fetching media from server msg disappear
            self.wait_for_media_fetch_from_server()

            # Check if the widevine download msg is displayer
            self.wait_for_widevine_offline_download(encoding_type, media_type)



        elif opr.strip().lower() == "pause":
            for et in range(3):
                # Check if the pause button is visible
                if not self.is_item_visible("player-pause-button"):
                    # Tap on the video player, if the pause button is not visible
                    self.click_on("player")
                try:
                    # Click on the pause button
                    self.click_on("player-pause-button")
                    break
                except Exception as ex:
                    warning(str(ex))
                    warning("pause button get invisible so quick")
                    continue

        elif opr.strip().lower() == "resume":
            for et in range(3):
                # Check if the play button is visible
                if not self.is_item_visible("player-play-button"):
                    # Tap on the video player, if the play button is not visible
                    self.click_on("player")
                try:
                    # Click on the play button
                    self.click_on("player-play-button")
                    break
                except Exception as ex:
                    warning(str(ex))
                    warning("play button get invisible so quick")
                    continue

        elif opr.strip().lower() == "forwarded":
            for et in range(3):
                # Check if the forward button is visible
                if not self.is_item_visible("player-forward-button"):
                    # Tap on the video player, if the forward button is not visible
                    self.click_on("player")
                try:
                    # Click on the forward button
                    self.click_on("player-forward-button")
                    """
                    ele_obj = self.__obj.get_element("player-forward-button")
                    for jj in range(8):
                        ele_obj.click()
                    """
                    break
                except Exception as ex:
                    warning(str(ex))
                    warning("forward button get invisible so quick")
                    continue

        elif opr.strip().lower() == "reversed":
            for et in range(3):
                # Check if the rewind button is visible
                if not self.is_item_visible("player-rewind-button"):
                    # Tap on the video player, if the rewind button is not visible
                    self.click_on("player")
                try:
                    # Click on the rewind button
                    self.click_on("player-rewind-button")
                    """
                    ele_obj = self.__obj.get_element("player-rewind-button")
                    for jj in range(8):
                        ele_obj.click()
                    """
                    break
                except Exception as ex:
                    warning(str(ex))
                    warning("rewind button get invisible so quick")
                    continue

        elif re.search("^seek-\d+:\d+$", opr.strip().lower()):
            seek_to_dur = opr.split("-")[1].split(':')
            seek_to_sec = int(seek_to_dur[0]) * 60 + int(seek_to_dur[1])

            for et in range(3):
                # Check if the seek bar is visible
                if not self.is_item_visible("player-seekbar"):
                    # Tap on the video player, if the seek bar is not visible
                    self.click_on("player")
                try:

                    tot_vdo_dur = self.get_value("player-video-duration").split(':')
                    tot_vdo_sec = int(tot_vdo_dur[0]) * 60 + int(tot_vdo_dur[1])

                    ele_details = self.get_element_details("player-seekbar")
                    x_cal = (float(ele_details['width']) / tot_vdo_sec) * seek_to_sec
                    go_to_x = ele_details['x-cordinate'] + x_cal #+ 6
                    go_to_x = int(round(go_to_x))
                    go_to_y = ele_details['y-cordinate']
                    # Seek the value
                    self.tap_on(go_to_x, go_to_y)
                    '''
                    print "*"*100
                    print "ele_details:::", ele_details
                    print "tot_vdo_sec:::", tot_vdo_sec
                    print "seek_to_sec:::", seek_to_sec
                    print "go_to_x:::", go_to_x
                    print "go_to_y:::", go_to_y
                    print "*"*100
                    '''
                    time.sleep(10)
                    break
                except Exception as ex:
                    warning(str(ex))
                    warning("seekbar get invisible so quick")
                    continue

        elif opr.strip().lower() == "attempt-to-play":
            # If attempt-to-play has paased then encoding_type should be "no-select"
            if encoding_type.lower().strip() != "no-select":
                raise exception_mod.InvalidKeyWordUsed(**{'encoding selection':encoding_type,
                                                        'operation':opr})

            if media_type.strip().lower() == "local":
                raise exception_mod.GlueCodeNotImplemented

            elif media_type.strip().lower() == "remote":
                # Set url/ media id
                self.set_select_value("player", "media-id-text-box", "set", media_source)
                # Click on the play button
                self.click_on("play-button")

            else:
                # Go to a tab and select the media
                self.go_to_tab_and_select_media(media_source, media_type)

            # Select the encoding from drop down
            self.select_encoding(encoding_type, media_type)

        else:
            raise exception_mod.InvalidKeyWordUsed(operation=opr)