# pylint: disable=C0103,W0613,F0401
'''
This module is using by behave to call these perticular functions in different
stages of run
'''
from behave import *
from modules.logger import info, success, fail
from modules.constant import SCENARIO_DATA

import telnetlib
import datetime
import time
from threading import *

# Made this True if you want to accumulate the device log
ACCUMULATE_LOG = False

EXIT_CAPTURE_LOG = False
EXITED_THREAD = False

def execute_telnet():
    global EXIT_CAPTURE_LOG
    global EXITED_THREAD
    HOST = "192.168.50.177"
    USER = r"gouri"
    PASSWORD = r"Rebaca2015"
    OUT_FILE = "/Users/gouri/Desktop/Output/OUTPUT-%s.log" % \
               datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
    CLR_COMMAND = "adb logcat -c -v time\n"
    #COMMAND = "adb logcat -v time > %s\n" % OUT_FILE
    COMMAND = 'adb logcat -v time | grep -v "D/dalvikvm(" > %s\n' % OUT_FILE
    EXITED_THREAD = False

    # Connecting the telnet
    tn = telnetlib.Telnet(HOST)
    tn.read_until("login: ")
    tn.write(USER + "\n")
    tn.read_until("Password:")
    tn.write(PASSWORD + "\n")

    # Executing the commands
    tn.write(CLR_COMMAND)
    tn.write(COMMAND)
    while not EXIT_CAPTURE_LOG:
        pass
    tn.write('\x03\n')

    # Exiting the telnet
    tn.write("exit\n")
    info("OUTPUT DEVICE LOG FILE SAVED AT -> %s" % OUT_FILE)
    EXIT_CAPTURE_LOG = False
    EXITED_THREAD = True
    #info(tn.read_all())


def before_feature(context, feature):
    """ This gets called before executing the feature """
    pass

def after_feature(context, feature):
    """ This gets called after executing the feature """
    pass

def before_scenario(context, scenario):
    """ This gets called before executing the scenario """
    SCENARIO_DATA.clear()

    if ACCUMULATE_LOG:
        t = Thread(target=execute_telnet)
        t.daemon = True
        t.start()

    info("EXECUTING-SCENARIO : %s"% scenario.name)
    info("FILE : %s"% scenario.filename)

def after_scenario(context, scenario):
    """ This gets called after executing the scenario """

    if 'failed' == scenario.status.lower().strip():
        call_fun = fail
    elif 'passed' == scenario.status.lower().strip():
        call_fun = success
    else:
        call_fun = info

    if ACCUMULATE_LOG:
        global EXIT_CAPTURE_LOG
        global EXITED_THREAD
        # To exit the log capture
        EXIT_CAPTURE_LOG = True
        while not EXITED_THREAD:
            pass
        info(">>>>>>>> Thread Exited")

    call_fun("EXECUTED-SCENARIO : %s"% scenario.name)
    info("SCENARIO STATUS : %s"% scenario.status)
    info("DURATION TAKEN : %s\n"% scenario.duration)

def before_step(context, step):
    """ This gets called before executing the step """
    info("EXECUTING-STEP : %s"% step.name)

def after_step(context, step):
    """ This gets called after executing the step """
    if 'failed' == step.status.lower().strip():
        call_fun = fail
    elif 'passed' == step.status.lower().strip():
        call_fun = success
    else:
        call_fun = info
    call_fun("EXECUTED-STEP : %s"% step.name)
    info("STEP STATUS : %s"% step.status)
    info("STEP DURATION : %s"% step.duration)