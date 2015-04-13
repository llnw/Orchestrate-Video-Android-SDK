#-------------------------------------------------------------------------------
# Name:        module1
# Purpose:
#
# Author:      gouri
#
# Created:     06-02-2015
# Copyright:   (c) gouri 2015
# Licence:     <your licence>
#-------------------------------------------------------------------------------

import sys
import datetime
import time
import os
import webbrowser

from lib.bin.behave_cmd import behave_main0 as runCli
from modules.report_generator import *

rootDirPath = os.path.abspath(os.path.dirname(__file__))
#sys.path.append(os.path.join(rootDirPath,'lib'))
sys.path[1:1] = [os.path.join(rootDirPath,'lib'), os.path.join(rootDirPath,'modules')]

def main():
    # ['main.py', 'feature-files\\tut.feature', '--format', 'json.pretty', '--out', 'steps_json-pretty_fail.json']
    json_out_file = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S_json_out.json")
    json_out_file = os.path.join("output", json_out_file)
    html_report_file = datetime.datetime.now().strftime("report-%Y_%m_%d_%H_%M_%S.html")
    html_report_file = os.path.join("output", html_report_file)

    sys.argv.extend(['--format', 'json.pretty', '--out', json_out_file])
    runCli()
    time.sleep(1)
    report_generator(json_out_file, html_report_file)
    #os.system(html_report_file)
    print "REPORT PATH::", os.path.join(rootDirPath,html_report_file)
    webbrowser.open("file:///"+os.path.join(rootDirPath,html_report_file))
if __name__ == '__main__':
    main()
