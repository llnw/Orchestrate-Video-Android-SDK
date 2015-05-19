"""
#-------------------------------------------------------------------------------
# Name      :  limelight_glude_code
# Purpose   :  Feature file grammar parser
#
# Author    :  Rebaca
#
# Created   :  16-03-2015
# Copyright :  (c) Rebaca 2015
#-------------------------------------------------------------------------------
"""
from behave import *
from limelight_mod import Limelight
import time
from logger import info, error, warning, exception, success, fail
from constant import LIME_LIGHT_OBJ, LONG_WAIT, MEDIUM_WAIT, SORT_WAIT

from behave.matchers import register_type
def parse_optional(text):
    return text.strip()
parse_optional.pattern = r'\s*-?\s*'
register_type(optional=parse_optional)

ret_data = {}

def test_step(function):
    """This is inner decorator function"""
    def wrapper(*args, **kwargs):
        """ The wrapper function """
        try:
            ret_control = function(*args, **kwargs)
            return ret_control
        except Exception as ex:
            raise
    return wrapper


@given('the application has launched')
@test_step
def step_impl(context):
    """This will launch the application"""
    global LIME_LIGHT_OBJ
    if not LIME_LIGHT_OBJ or not LIME_LIGHT_OBJ.is_app_running():
        info("NO APP RUNNING, LAUNCHING THE APP.")
        LIME_LIGHT_OBJ = Limelight()
        LIME_LIGHT_OBJ.launch_app()
        LIME_LIGHT_OBJ.uncheck_delivery()
    else:
        info("APP IS RUNNING FROM PREVIOUS SCENARIO, SO RE-USING IT.")

@given('the application has launched with following ' + \
       'configuration in {tab_name} tab -')
@test_step
def step_impl(context, tab_name):
    """This will launch the application"""
    global LIME_LIGHT_OBJ
    if not LIME_LIGHT_OBJ or not LIME_LIGHT_OBJ.is_app_running():
        info("NO APP RUNNING, LAUNCHING THE APP.")
        LIME_LIGHT_OBJ = Limelight()
        LIME_LIGHT_OBJ.launch_app()
        LIME_LIGHT_OBJ.select_tab(tab_name)
        for row in context.table:
            LIME_LIGHT_OBJ.set_select_value( tab_name, str(row['name']),
                                             'set', str(row['value']))
        for ech_tab in ['CHANNEL GROUPS', 'ALL CHANNELS', 'ALL MEDIA']:
            LIME_LIGHT_OBJ.perform_oper_on_tab(ech_tab, "refresh")

        LIME_LIGHT_OBJ.uncheck_delivery()
    else:
        info("APP IS RUNNING FROM PREVIOUS SCENARIO, SO RE-USING IT.")


@when('I {opr} {val} as value for {target_ele} in {tab_name} tab')
@test_step
def step_impl(context, opr, val, target_ele, tab_name):
    """
    @args :
        opr : operations - set / select
        val : value that need to set
        target_ele : The element on which we are going to perform the operation
        tab_name : The tab where we need to go
    """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.select_tab(tab_name)
    LIME_LIGHT_OBJ.set_select_value(tab_name, target_ele, opr, val)

@when('I {opr} the {tab_name} tab')
@test_step
def step_impl(context, opr, tab_name):
    """
    @args :
        opr : operations - refresh / select / scroll-down
        tab_name : perform operation on which tab
    """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.perform_oper_on_tab(tab_name, opr)

@when('I {opr} the "{media_type}" video from {media_source} with "{encoding_type}" encoding')
@test_step
def step_impl(context, opr, media_type, media_source, encoding_type):
    """
    @args :
        opr : operations - play / pause / resume / seek-xx:xx / forwarded
                       / reversed / attempt-to-play
        media_type : local / remote / media-name
        media_source : file-name / url / media-id / media-tab
        encoding_type: encoding-name / automatic / no-select
    """
    global LIME_LIGHT_OBJ
    tmp_dic = LIME_LIGHT_OBJ.perform_video_operations(opr, media_type,
                                                      media_source,
                                                      encoding_type)
    ret_data.update(tmp_dic)

@when('I apply {action_itm} {perform} on the {target}')
@test_step
def step_impl(context, action_itm, perform, target):
    """
    @args :
        action_itm : home-button / app-icon / screen
        perform    : press / orientation
        target     : application / device
    """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.perform_device_operations(action_itm.strip(),
                                             perform.strip(),
                                             target.strip())

@when('I {operation} {target} media in play list from {tab_name} tab{is_table:optional}')
@test_step
def step_impl(context, operation, target, tab_name, is_table):
    """
    @args :
        operation : add / remove
        target    : following / all
        tab_name  : tab name of the app
        is_table  : "-"
    """
    global LIME_LIGHT_OBJ
    media_name = []
    if is_table and is_table.strip() == "-":
        media_name = [str(row["media name"]) for row in context.table]
    LIME_LIGHT_OBJ.add_delete_from_playlist( operation.lower().strip(),
                                             target.lower().strip(),
                                             tab_name, media_name)


@then('{target} media from {source_tab_name} tab gets {operation} in ' + \
      'play list of {playlist_tab_name} tab{is_table:optional}')
@test_step
def step_impl( context, target, source_tab_name, operation,
               playlist_tab_name, is_table):
    """
    @args :
        target    : following / all
        source_tab_name : The tabe name feom where we are adding the media
                          This is very important while verifying the add all
                          scenario
        operation : added / removed
        playlist_tab_name  : tab name of the app that contains the playlist
        is_table  : "-"
    """
    global LIME_LIGHT_OBJ
    media_name = []
    if is_table and is_table.strip() == "-":
        media_name = [str(row["media name"]) for row in context.table]
    LIME_LIGHT_OBJ.verify_add_delete_from_playlist( operation.lower().strip(),
                                                  target.lower().strip(),
                                                  source_tab_name,
                                                  playlist_tab_name,
                                                  media_name)

@then('value of {target_ele} in {page_name} tab should be {val}')
@test_step
def step_impl(context, target_ele, page_name, val):
    """
    @args :
        target_ele : The element on which we are going to perform the operation
        page_name : The tab where we need to go
        val : value that need to check
    """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.select_tab(page_name)
    LIME_LIGHT_OBJ.verify_value(page_name, target_ele, val)

@then('the {check_ele} should {op} following {table_header} -')
@test_step
def step_impl(context, check_ele, op, table_header):
    """
    @args :
        check_ele : the element that should contain the data
        op : have / not have
        table_header : Header of data table / error message / icon set

    """
    should_equal = False if 'not' in op else True
    global LIME_LIGHT_OBJ
    if table_header.lower().strip() == "error message":
        LIME_LIGHT_OBJ.check_errors(check_ele,
            [str(row[table_header.lower()]) for row in context.table],
            should_equal)
    elif table_header.lower().strip() == "icon set":
        LIME_LIGHT_OBJ.check_icon( check_ele,
                      [(row['name'], row['icon']) for row in context.table],
                      should_equal )
    else:
        LIME_LIGHT_OBJ.check_contains(check_ele,
             [str(row[table_header.lower()]) for row in context.table],
             should_equal)

@then('player should {opr} the playback from {source_type} to duration {duration} in {state} state')
@test_step
def step_impl(context, opr, source_type, duration, state):
    """
    @args :
        opr         : operations - play / pause / resume / continue-playing /
                    remain-pause / seek / forwarded / reversed / not play
        source_type : Source of play - file / url / mediaId / media-tab
        duration    : Duration of play (min:sec)
        state       : State of the player - play / pause
    """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.check_player(opr, source_type, duration, state, ret_data)

@then('the player should send {notification_type} notification')
@test_step
def step_impl(context, notification_type):
    """
    This will check the expected notification
    @args :
        notification_type : StartSession/Play/Pause/Seek/MediaComplete/expected
        (expected : it will search the number of play pause seek etc operation
        in this scenario and try to match with those)
        :: currently implementation has been done only for "expected" keyword
    """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.check_notification(notification_type)

@then('exit from the application')
@test_step
def step_impl(context):
    """ This will exit the application """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.exit_app()
    LIME_LIGHT_OBJ = None
