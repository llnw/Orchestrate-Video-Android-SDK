#-------------------------------------------------------------------------------
# Name:        logger
# Purpose:     currently it is giving the console log
#
# Author:      gouri
#
# Created:     26-03-2015
# Copyright:   (c) gouri 2015
# Licence:     <your licence>
#-------------------------------------------------------------------------------

from datetime import datetime

def info(msg):
    log('INFO', msg)

def error(msg):
    log('ERROR', msg)

def warning(msg):
    log('WARNING', msg)

def exception(msg):
    log('EXCEPTION', msg)

def success(msg):
    log('SUCCESS', msg)

def log(prefix, msg):
    print datetime.now().strftime("%Y-%m-%d %H:%M:%S [%%s] %%s")%(prefix, msg)