"""
#-------------------------------------------------------------------------------
# Name       :   main.py
# Purpose    :	 automation initiating module
#
# Author     :   Rebaca
#
# Created    :   06-02-2015
# Copyright  :   (c) Rebaca 2015
#-------------------------------------------------------------------------------
"""
import sys
import datetime
import time
import os
import webbrowser

from lib.bin.behave_cmd import behave_main0 as runCli
from modules.report_generator import report_generator

ROOT_DIR_PATH = os.path.abspath(os.path.dirname(__file__))
sys.path[1:1] = [os.path.join(ROOT_DIR_PATH,'lib'),
                 os.path.join(ROOT_DIR_PATH,'modules')]

def main():
    """
    This function is used to initiate the automation, and on test
    completion generates the test report.

    Command format to run automation
    # ['main.py', 'feature-files\\tut.feature', '--format', 'json.pretty',
    # '--out', 'steps_json-pretty_fail.json']
    """
    json_out_file = datetime.datetime.now().\
                    strftime("%Y_%m_%d_%H_%M_%S_json_out.json")
    json_out_file = os.path.join("output", json_out_file)
    html_report_file = datetime.datetime.now().\
                    strftime("report-%Y_%m_%d_%H_%M_%S.html")
    html_report_file = os.path.join("output", html_report_file)

    sys.argv.extend(['--format', 'json.pretty', '--out', json_out_file])
    runCli()
    time.sleep(1)
    report_generator(json_out_file, html_report_file)
    print "REPORT PATH::", os.path.join(ROOT_DIR_PATH, html_report_file)
    webbrowser.open("file:///"+os.path.join(ROOT_DIR_PATH, html_report_file))
if __name__ == '__main__':
    main()
