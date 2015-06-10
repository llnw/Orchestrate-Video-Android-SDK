# pylint: disable=W0703,W0612,R0915,R0914
"""
#-------------------------------------------------------------------------------
# Name       :  report_generator
# Purpose    :  generate html report
#
# Author     :  Rebaca
#
# Created    :  06-02-2015
# Copyright  :  (c) Rebaca 2015
#-------------------------------------------------------------------------------
"""
import json
#from modules.logger import info
import time

def format_argument(argument):
    """ To format the argument that send from feature file """
    return_format = ""
    for each in argument:
        if isinstance(each, dict):
            for _ky, _vl in each.iteritems():
                return_format += str(_ky) + " = " + str(_vl) + "<br />"
        elif isinstance(each, list):
            return_format += "<br />".join(each)
    return return_format

def humanize_time(secs):
    """ Convert seconds to human readable time format """
    mins, secs = divmod(secs, 60)
    hours, mins = divmod(mins, 60)
    return '%02d:%02d:%02d' % (hours, mins, round(secs))

def report_generator(file_name, op_html):
    """
    This function generates the html report and save it
    to the file op_html
    """
    for ech_try in range(60):
        try:
            with open(file_name) as json_data:
                data = json.load(json_data)
                data = [data] if not isinstance(data, list) else data
            break
        except Exception as exc :
            print "got err while reading json file:", str(exc)
            time.sleep(1)

    html = """
    <html>
    <head>
    <title>Automation Report</title>
    <style>
    .feature-table{
       margin-bottom: 10px;
    }
    table {
       empty-cells: show;
       font-size: 8pt;
       word-wrap: break-word;
       border: 0px solid slategray /*1px solid #DDDDDD*/;
       background-color: #F0F0F0;
       font-family: arial;
       color: #222222;
       width: 100%;
    }
    tr {
       line-height: 20px;
    }
    th {
       background-color: #dcdcf0;
       padding: 0.2em 0.3em;
    }
    th, td {
       border: 1px solid slategray;
       padding: 4px;
       /*font-family: Helvetica,sans-serif;*/
       min-width: 140px;
       vertical-align: top;
    }
    .passed {
       background-color: #5f9d18/*#99ff66*/;
       color:white;
       font-weight:bold;
    }
    .failed {
       background-color: #a43838/*#FF3333*/;
       color:white;
       font-weight:bold;
    }
    h4 {
       padding: 0px;
       margin: 0px;
       font-size: 10pt;
    }
    .key {
       font-size: 10pt !important;
       font-weight: bold;
       vertical-align: text-top;
       width: 100px;

    }
    .line-brk{
       border:0px;
       height:9px
    }
    .togBtn{
        background-color: #dcdcf0;
        border: 1px solid black;
        border-radius: 9px;
        cursor: pointer;
        display: inline-block;
        font-size: 7.5pt;
        padding: 2px;
        text-align: center;
        margin-left : 10px;

    }
    .full-err-msg{
        background-color: #ffffff;
        border: 1px dashed #ccd5de;
        color: black;
        font-weight: lighter;
        max-height: 200px;
        overflow-y: auto;
        padding: 5px;
        display: none;
    }
    .err-msg-link{
        cursor: pointer;
    }
    </style>
    <script type="text/javascript"
    src="http://ajax.googleapis.com/ajax/libs/jquery/1.4/jquery.min.js">
    </script>
    <script type="text/javascript">

    $( document ).ready(function() {
        $(".stepsTable").toggle();
        $('.togBtn').click(function(){
            $(this).parent().parent().next().children()
            .next().children(".stepsTable").toggle();
            if ($(this).text() === 'Show Steps') {
                $(this).text('Hide Steps');
            } else {
                $(this).text('Show Steps');
            }
        });

    $('.err-msg-link').click(function(){
      var msg = "<pre>"+$(this).next(".full-err-msg").html()+"</pre>";
      var w = window.open('', '', 'width=600,height=400,resizeable,scrollbars');
      w.document.write(msg);
      w.document.close(); // needed for chrome and safari
    });
   });

  </script>
  </head>
  <body>
    """
    for ech_dict in data:
        feature_duration = 0
        tmp_html = """<table class="tab-%s feature-table" cellspacing="1">""" \
                   % ech_dict['status']
        tmp_html += """<tr><td class="key">Feature : </td>
                       <td><h4>%s</h4></td></tr>""" % ech_dict.get('name', '')
        for ech_itm in ["tag", "location"]:
            tmp_html += """
              <tr><td class="key">%s : </td>
                  <td>%s</td></tr>
            """ % (ech_itm.title(), ech_dict.get(ech_itm, ''))
        # Adding the scenario
        tmp_scenario_table = """<table cellspacing="1">"""
        for ech_scenario_data in ech_dict.get('elements', []):
            scenario_duration = 0
            tmp_scenario_table += """<tr><td class="key">Scenario : </td>
               <td>%s</td></tr>""" % ech_scenario_data.get('name', '')
            tmp_scenario_table += """<tr><td class="key">Tags : </td>
               <td>%s</td></tr>""" % ','.join(ech_scenario_data.get('tags', []))


            # Adding Steps
            tmp_step_table = """<table class="stepsTable" cellspacing="1">"""
            tmp_step_table += """<tr>
                           <th>Name</th><th>Keyword</th><th>Location</th>
                           <th>Arguments</th><th>Duration<br />(in seconds)</th>
                           <th>Status</th><th>Error Message</th>
                           </tr>"""
            scenario_status = 'passed'
            for ech_step in ech_scenario_data.get("steps", []):
                step_status = ech_step.get('result', {})\
                                      .get('status', 'Not Executed')

                if step_status.lower().strip() == "failed":
                    scenario_status = "failed"

                step_name = ech_step['name']
                step_keyward = ech_step['keyword']
                step_location = ech_step['location']
                step_argument = format_argument(ech_step.get('match', {})\
                                                        .get('arguments', []))
                step_dur = ech_step.get('result', {})\
                                   .get('duration', '00.00')
                scenario_duration += float(step_dur)
                msgs = ech_step.get('result', {})\
                               .get('error_message', [])
                step_err_msg = ""
                if msgs:
                    step_err_msg = '<br />'.join(msgs)
                    step_err_msg = """
                       <a class='err-msg-link'>%s</a>
                       <div class='full-err-msg'>%s</div>
                    """ % (msgs[-1], step_err_msg)

                try:
                    tmp_step_table += """
                       <tr class="%s">
                         <td>%s</td><td>%s</td><td>%s</td>
                         <td>%s</td><td>%0.3f</td><td>%s</td>
                         <td>%s</td></tr>
                    """ % ( step_status, step_name,
                          step_keyward, step_location, step_argument,
                          float(step_dur), step_status.upper(), step_err_msg)
                except Exception as ex:
                    print ">>>>>>>>>", ex

            feature_duration += sum(int(x) * 60 ** i for i, x in \
              enumerate(reversed(humanize_time(scenario_duration).split(":"))))
            tmp_step_table += """</table>"""

            tmp_scenario_table += """
               <tr>
                  <td class="key">Duration (hh:mm:ss) : </td>
                  <td>%s</td>
               </tr>
            """ % (humanize_time(scenario_duration))
            tmp_scenario_table += """<tr><td class="key">Location : </td>
               <td>%s</td></tr>""" % ech_scenario_data.get('location', '')
            tmp_scenario_table += """
                <tr class="%s">
                   <td class="key">Status : </td>
                   <td>
                       %s
                       <button class="togBtn">Show Steps</button>
                   </td>
                </tr>
            """ % (scenario_status, scenario_status.upper())

            tmp_scenario_table += """<tr>
                  <td class="key">Steps :</td>
                  <td>%s</td></tr>""" % tmp_step_table
            tmp_scenario_table += """<tr>
                 <td colspan="2" class="line-brk"></td>
            </tr>"""
        tmp_scenario_table += """</table>"""
        tmp_html += """
           <tr><td class="key">Duration (hh:mm:ss) : </td>
             <td>%s</td>
           </tr>
        """ % (humanize_time(feature_duration))
        tmp_html += """
           <tr class="%s">
             <td class="key">Overall Status : </td>
             <td>%s</td>
           </tr>
        """ % (ech_dict.get('status', ''), ech_dict.get('status', '').upper())
        tmp_html += """<tr>
            <td class="key">Scenario : </td><td>%s</td>
         </tr>
         """ % tmp_scenario_table
        tmp_html += """</table>"""
        html += tmp_html
    html += """</body></html>"""
    with open(op_html, "w") as f_out:
        try:
            f_out.write(html)
        except Exception:
            f_out.write(html.encode('utf-8').strip())

if __name__ == '__main__':
    FILE_NAME = "output\\2015_04_08_13_11_34_json_out.json"
    OUTPUT_HTML_FILE_NAME = "output.html"
    report_generator(FILE_NAME, OUTPUT_HTML_FILE_NAME)
