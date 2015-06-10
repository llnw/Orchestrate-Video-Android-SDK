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

FILE_NAME_EXTN = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
CMD_OUT_FILE_NAME = os.path.join("consoleLog",
                                 "outputFile_%s.log" % FILE_NAME_EXTN)
JSON_OUT_FILE_NAME= "%s_json_out.json" % FILE_NAME_EXTN
HTML_REPORT_FILE_NAME = "report-%s.html" % FILE_NAME_EXTN

# comment below line if you want to see the output in terminal
sys.stdout = open(CMD_OUT_FILE_NAME, 'w')



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
    json_out_file = os.path.join("output", JSON_OUT_FILE_NAME)
    html_report_file = os.path.join("output", HTML_REPORT_FILE_NAME)
    sys.argv.extend(['--format', 'json.pretty', '--out', json_out_file])
    runCli()
    report_generator(json_out_file, html_report_file)
    print "REPORT PATH::", os.path.join(ROOT_DIR_PATH, html_report_file)
    webbrowser.open("file:///"+os.path.join(ROOT_DIR_PATH, html_report_file))

if __name__ == '__main__':
    main()
