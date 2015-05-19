# pylint: disable=C0103,W0613,F0401
'''
This module is using by behave to call these perticular functions in different
stages of run
'''
from behave import *
from modules.logger import info, success, fail
from modules.constant import SCENARIO_DATA


def before_feature(context, feature):
    """ This gets called before executing the feature """
    pass

def after_feature(context, feature):
    """ This gets called after executing the feature """
    pass

def before_scenario(context, scenario):
    """ This gets called before executing the scenario """
    SCENARIO_DATA.clear()
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