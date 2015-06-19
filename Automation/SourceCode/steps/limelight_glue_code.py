# pylint: disable=E0602,E0102,W0602,W0613,W0611,F0401,C0103,W0603,C0301,W0612
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

def parse_optional1(text):
    return text.strip()
parse_optional1.pattern = r'\s*(with\s\S+\smode)?\s*'
register_type(optional1=parse_optional1)

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
    if not LIME_LIGHT_OBJ or not LIME_LIGHT_OBJ.is_app_running:
        info("NO APP RUNNING, LAUNCHING THE APP.")
        LIME_LIGHT_OBJ = Limelight()
        LIME_LIGHT_OBJ.launch_app()
        LIME_LIGHT_OBJ.switch_internet_connection('on')
    elif LIME_LIGHT_OBJ.need_to_relaunch_app:
        info("relaunching the app from menu")
        LIME_LIGHT_OBJ.relaunch_app_frm_menu()
    else:
        info("APP IS RUNNING FROM PREVIOUS SCENARIO, SO RE-USING IT.")

    LIME_LIGHT_OBJ.set_orientation()
    LIME_LIGHT_OBJ.uncheck_delivery()


@given('the application has launched with following ' + \
       'configuration in {tab_name} tab -')
@test_step
def step_impl(context, tab_name):
    """This will launch the application"""
    global LIME_LIGHT_OBJ
    if not LIME_LIGHT_OBJ or not LIME_LIGHT_OBJ.is_app_running:
        info("NO APP RUNNING, LAUNCHING THE APP.")
        LIME_LIGHT_OBJ = Limelight()
        LIME_LIGHT_OBJ.launch_app()
        LIME_LIGHT_OBJ.switch_internet_connection('on')
        LIME_LIGHT_OBJ.set_orientation()
        LIME_LIGHT_OBJ.select_tab(tab_name)
        for row in context.table:
            LIME_LIGHT_OBJ.set_select_value( tab_name, str(row['name']),
                                             'set', str(row['value']))
        for ech_tab in ['CHANNEL GROUPS', 'ALL CHANNELS', 'ALL MEDIA']:
            LIME_LIGHT_OBJ.perform_oper_on_tab(ech_tab, "refresh")
    elif LIME_LIGHT_OBJ.need_to_relaunch_app:
        info("relaunching the app from menu")
        LIME_LIGHT_OBJ.relaunch_app_frm_menu()
        LIME_LIGHT_OBJ.switch_internet_connection('on')
        LIME_LIGHT_OBJ.set_orientation()
    else:
        info("APP IS RUNNING FROM PREVIOUS SCENARIO, SO RE-USING IT.")
        LIME_LIGHT_OBJ.switch_internet_connection('on')
        LIME_LIGHT_OBJ.set_orientation()
    LIME_LIGHT_OBJ.uncheck_delivery()


@when('I {opr} {val} as value for {target_ele} in {tab_name} tab')
@test_step
def step_impl(context, opr, val, target_ele, tab_name):
    """
    @args :
        opr : operations - set / select
        val : value that need to set
        target_ele : The element on which we are going to perform the operation
        tab_name : The tab where we need to go
    For searching :
        opr : search
        val : value to be search
        target_ele : search by
        tab_name : search in tab
    """
    global LIME_LIGHT_OBJ
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
        media_source : file-name-with-path / url / media-id / media-tab /
                      PLAY-LIST
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

        For full screen: I apply full-screen ON on the player
            action_itm : full-screen
            perform    : on/off
            target     : player
        For no-internet scenario: I apply internet off on the device
            action_itm : internet
            perform    : on/off
            target     : device
        For player next and previous button click: I apply player-next-button click on the player
            action_itm : player-next-button/player-previous-button
            perform    : click
            target     : player
    """
    global LIME_LIGHT_OBJ
    if target.strip().lower() == "player":
        # Perform the actions on player
        tmp_dic = LIME_LIGHT_OBJ.perform_player_operation(action_itm.strip().lower(),
                                                perform.strip().lower())
        ret_data.update(tmp_dic)
    else:
        LIME_LIGHT_OBJ.perform_device_operations(action_itm.strip(),
                                             perform.strip(),
                                             target.strip())

@when('I {operation} {target} {add_type} in play list from {tab_name} tab{is_table:optional}')
@test_step
def step_impl(context, operation, target, add_type, tab_name, is_table):
    """
    @args :
        operation : add / remove
        target    : following / all
        add_type  : media/channel
        tab_name  : tab name of the app
        is_table  : "-"
    """
    global LIME_LIGHT_OBJ
    if add_type.lower().strip() == 'media':
        media_name = []
        if is_table and is_table.strip() == "-":
            media_name = [str(row["media name"]) for row in context.table]
        LIME_LIGHT_OBJ.add_delete_from_playlist( operation.lower().strip(),
                                                 target.lower().strip(),
                                                 tab_name, media_name,
                                                 add_type.lower().strip())
    elif add_type.lower().strip() == 'channel':
        channel_name = []
        if is_table and is_table.strip() == "-":
            channel_name_list = [str(row["channel name"]) for row in context.table]
            if channel_name_list:
                channel_name = [channel_name_list[0]]
        LIME_LIGHT_OBJ.add_delete_from_playlist( operation.lower().strip(),
                                                 target.lower().strip(),
                                                 tab_name, channel_name,
                                                 add_type.lower().strip())
    else:
        raise Exception('playlist add type should either media or channel')


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

@then('the {check_ele} should {op} following {table_header} at {location} -')
@test_step
def step_impl(context, check_ele, op, table_header, location):
    """
    @args :
        check_ele : the element that should contain the data
        op : have / not have
        table_header : Header of data table
        location : top/bottom
    """
    should_equal = False if 'not' in op else True
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.check_contains(check_ele,
             [str(row[table_header.lower()]) for row in context.table],
             should_equal, table_header.lower().strip(), location)


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
             should_equal, table_header.lower().strip())

@then('player should {opr} the playback from {source_type} to duration {duration} in {state} state{full_screen_msg:optional1}')
@test_step
def step_impl(context, opr, source_type, duration, state, full_screen_msg):
    """
    @args :
        opr         : operations - play / pause / resume / continue-playing /
                    remain-pause / seek / forwarded / reversed / not play
        source_type : Source of play - file / url / mediaId / media-tab
        duration    : Duration of play (min:sec)
        state       : State of the player - play / pause
        full_screen_msg : with full-screen mode/with normal-screen mode
    """
    global LIME_LIGHT_OBJ
    chk_full_screen = False
    if full_screen_msg and 'full-screen' in full_screen_msg.strip().lower():
        chk_full_screen = True
    LIME_LIGHT_OBJ.check_player(opr, source_type, duration, state, ret_data,
                                chk_full_screen)

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

@then('close the application')
@test_step
def step_impl(context):
    """ This will close the application """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.close_app()

@when('I {oper} the "{media_name}" video from play list with auto playlist {stat}')
@test_step
def step_impl(context, oper, media_name, stat):
    """
    @args :
        opr : operations - play
        media_name : name of the media
        stat: on / off
    """
    global LIME_LIGHT_OBJ
    tmp_dic = LIME_LIGHT_OBJ.perform_playlist_video_oper(oper, media_name, stat)
    ret_data.update(tmp_dic)

@then('player should {oper} the {which_media} media from play-list at duration {duration} in {state} state')
@test_step
def step_impl(context, oper, which_media, duration, state):
    """
    This will check playlist play
    @args :
        oper : play /
        which_media : media-name /
    """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.check_playlist_oper(oper, which_media, duration, state, ret_data)


@when('I update "{new_val}" as value for "{param_name}" in "{media_name}" media')
@test_step
def step_impl(context, new_val, param_name, media_name):
    """
    @args :
        new_val : new value
        param_name : name of the parameter that need to update
        media_name : media name
    """
    global LIME_LIGHT_OBJ
    LIME_LIGHT_OBJ.update_media(media_name, param_name, new_val)