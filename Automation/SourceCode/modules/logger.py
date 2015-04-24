"""
#-------------------------------------------------------------------------------
# Name       :  logger
# Purpose    :  currently it is giving the console log
#
# Author     :  Rebaca
#
# Created    :  26-03-2015
# Copyright  :  (c) Rebaca 2015
#-------------------------------------------------------------------------------
"""
from datetime import datetime

def info(msg):
    """Print the normal info messages"""
    log('INFO', msg)

def error(msg):
    """Print the error messages"""
    log('ERROR', msg)

def warning(msg):
    """Print the warning messages"""
    log('WARNING', msg)

def exception(msg):
    """Print the exception messages"""
    log('EXCEPTION', msg)

def success(msg):
    """Print the success messages"""
    log('SUCCESS', msg)

def fail(msg):
    """Print the failure messages"""
    log('FAIL', msg)

def log(prefix, msg):
    """ Print the all message with datetime stamp"""
    print datetime.now().strftime("%Y-%m-%d %H:%M:%S [%%s] %%s")%(prefix, msg)