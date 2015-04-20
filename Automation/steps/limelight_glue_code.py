#-------------------------------------------------------------------------------
# Name:        limelight
# Purpose:
#
# Author:      Rebaca
#
# Created:     16-03-2015
# Copyright:   (c) Rebaca 2015
# Licence:     <your licence>
#-------------------------------------------------------------------------------

from behave import *
from limelight_mod import Limelight
import time
from logger import info, error, warning, exception
from constant import LIME_LIGHT_OBJ, LONG_WAIT, MEDIUM_WAIT, SORT_WAIT

ret_data = {}

@given('the application has launched')
def step_impl(context):
    '''This will launch the application'''
    global LIME_LIGHT_OBJ
    if not LIME_LIGHT_OBJ or not LIME_LIGHT_OBJ.is_app_running():
        info("NO APP RUNNING, LAUNCHING THE APP.")
        LIME_LIGHT_OBJ = Limelight()
        LIME_LIGHT_OBJ.launch_app()
    else:
        info("APP IS RUNNING FROM PREVIOUS SCENARIO, SO REUSING IT.")

@when('I {opr} {val} as value for {target_ele} in {page_name} Page')
def step_impl(context, opr, val, target_ele, page_name):
    """
    @args :
      opr : set/select
      val : value that need to set
      target_ele : The element on which we r going to perform the operation
      page_name : The page where we need to go
    """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.select_tab(page_name)
    LIME_LIGHT_OBJ.set_select_value(page_name, target_ele, opr, val)
    time.sleep(10)


@when('I {opr} the {tab_name} tab')
def step_impl(context, opr, tab_name):
    """
    @args :
      opr : refresh/select/scroll-down
      tab_name : operation perform on which page
    """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.perform_oper_on_tab(tab_name, opr)

@when('I {opr} the "{media_type}" video from {media_source} with "{encoding_type}" encoding')
def step_impl(context, opr, media_type, media_source, encoding_type):
    """
    @args :
          opr : play/pause/resume/seek-xx:xx/forwarded/reversed/attempt-to-play
          media_type : local/remote/media-name
          media_source : file-name/url/media-id/media-tab
          encoding_type: encoding-name/automatic/no-select
    """
    global LIME_LIGHT_OBJ
    tmp_dic = LIME_LIGHT_OBJ.perform_video_operations(opr, media_type,
                                                      media_source,
                                                      encoding_type)
    ret_data.update(tmp_dic)


@when('I apply {action_itm} {perform} on the {target}')
def step_impl(context, action_itm, perform, target):
    """
    @args :
          action_itm : home-button/app-icon/screen
          perform    : press/orientation
          target     : application/device
    """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.perform_device_operations(action_itm.strip(),
                                             perform.strip(),
                                             target.strip())


@then('value of {target_ele} in {page_name} page should be {val}')
def step_impl(context, target_ele, page_name, val):
    """
    @args :
      target_ele : The element on which we r going to perform the operation
      page_name : The page where we need to go
      val : value that need to check
    """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.select_tab(page_name)
    LIME_LIGHT_OBJ.verify_value(page_name, target_ele, val)

@then('the {check_ele} should {op} following {table_header} -')
def step_impl(context, check_ele, op, table_header):
    """
    @args :
      check_ele : the element that should contains the data
      table_header : Header of data table
    """
    should_equal = False if 'not' in op else True
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.check_contains(check_ele, [str(row[table_header.lower()]) for row in context.table], should_equal)

@then('player should {opr} the playback from {source_type} to duration {duration} in {state} state')
def step_impl(context, opr, source_type, duration, state):
    """
    @args :
          opr         : operations - play/pause/resume/continue-playing/
                        remain-pause/seek/forwarded/reversed
          source_type : Source of play - file/url/mediaId/media-tab
          duration    : Duration of play (min:sec)
          state       : State of the player - play/pause
    """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.check_player(opr, source_type, duration, state, ret_data)

@then('exit from the application')
def step_impl(context):
    """ This will exit the application """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.exit_app()
    LIME_LIGHT_OBJ = None
