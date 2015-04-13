#-------------------------------------------------------------------------------
# Name:        report_generator
# Purpose:
#
# Author:      gouri
#
# Created:     06-02-2015
# Copyright:   (c) gouri 2015
# Licence:     <your licence>
#-------------------------------------------------------------------------------

import json

def format_argument(argument):
    return_format = ""
    for each in argument:
        if isinstance(each, dict):
            for k, v in each.iteritems():
                return_format += str(k) + " = " + str(v) + "<br />"
        elif isinstance(each, list):
             return_format += "<br />".join(each)
    return return_format

def report_generator(file_name, op_html):
    with open(file_name) as json_data:
        data = json.load(json_data)
        data = [data] if not isinstance(data, list) else data
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
       min-width: 90px;
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
    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4/jquery.min.js"></script>
    <script type="text/javascript">

    $( document ).ready(function() {
        $(".stepsTable").toggle();
        $('.togBtn').click(function(){
            $(this).parent().parent().next().children().next().children(".stepsTable").toggle();
            if ($(this).text() === 'Show Steps') {
                $(this).text('Hide Steps');
            } else {
                $(this).text('Show Steps');
            }
        });
        /*
        $('.err-msg-link').click(function(){
            $(this).next(".full-err-msg").toggle();
        });
        */
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
        tmp_html = """<table class="tab-%s feature-table" cellspacing="1">""" %ech_dict['status']
        tmp_html += """<tr><td class="key">Feature : </td>
                       <td><h4>%s</h4></td></tr>"""%ech_dict.get('name', '')
        for ech_itm in ["tag", "location"]:
            tmp_html += """
              <tr><td class="key">%s</td>
                  <td>%s</td></tr>
            """%(ech_itm.title(), ech_dict.get(ech_itm, ''))
        tmp_html += """
           <tr class="%s">
             <td class="key">Overall Status : </td>
             <td>%s</td>
           </tr>
        """%(ech_dict.get('status', ''), ech_dict.get('status', '').upper())

        # Adding the scenario
        tmp_scenario_table = """<table cellspacing="1">"""
        for ech_scenario_data in ech_dict.get('elements', []):
            tmp_scenario_table += """<tr><td class="key">Scenario : </td>
               <td>%s</td></tr>""" %ech_scenario_data.get('name', '')
            tmp_scenario_table += """<tr><td class="key">Tags : </td>
               <td>%s</td></tr>""" %', '.join(ech_scenario_data.get('tags', []))
            tmp_scenario_table += """<tr><td class="key">Location : </td>
               <td>%s</td></tr>""" %ech_scenario_data.get('location', '')

            # Adding Steps
            tmp_step_table = """<table class="stepsTable" cellspacing="1">"""
            tmp_step_table += """<tr><th>Name</th><th>Keyword</th><th>Location</th>
                                <th>Arguments</th><th>Duration<br />(in seconds)</th>
                                <th>Status</th><th>Error Message</th></tr>"""
            scenario_status = 'passed'
            for ech_step in ech_scenario_data.get("steps", []):
                step_status = ech_step.get('result', {}).get('status', 'Not Executed')

                if step_status.lower().strip() == "failed":
                    scenario_status = "failed"

                step_name = ech_step['name']
                step_keyward = ech_step['keyword']
                step_location = ech_step['location']
                step_argument = format_argument(ech_step.get('match',{}).get('arguments', []))
                step_dur = ech_step.get('result', {}).get('duration', '00.00')
                #step_dur = ech_step.get('result', {}).get('duration', 'Not Executed')
                msgs = ech_step.get('result', {}).get('error_message', [])
                step_err_msg = ""
                if msgs:
                    step_err_msg = '<br />'.join(msgs)
                    step_err_msg = """
                       <a class='err-msg-link'>%s</a>
                       <div class='full-err-msg'>%s</div>
                    """%(msgs[-1], step_err_msg)

                try:
                    tmp_step_table += """
                       <tr class="%s">
                         <td>%s</td><td>%s</td><td>%s</td>
                         <td>%s</td><td>%0.3f</td><td>%s</td>
                         <td>%s</td></tr>
                    """%( step_status, step_name,
                          step_keyward, step_location, step_argument,
                          float(step_dur), step_status.upper(), step_err_msg)
                except Exception as ex:
                    print ">>>>>>>>>", ex

            tmp_step_table += """</table>"""

            tmp_scenario_table += """
                <tr class="%s">
                   <td class="key">Status : </td>
                   <td>
                       %s
                       <button class="togBtn">Show Steps</button>
                   </td>
                </tr>
            """%(scenario_status,scenario_status.upper())

            tmp_scenario_table += """<tr><td class="key">Steps : </td><td>%s</td></tr>"""%tmp_step_table
            tmp_scenario_table += """<tr><td colspan="2" class="line-brk"></td></tr>"""
        tmp_scenario_table += """</table>"""
        tmp_html += """<tr><td class="key">Scenario : </td><td>%s</td></tr>"""%tmp_scenario_table
        tmp_html += """</table>"""
        html += tmp_html
    html += """</body></html>"""
    with open(op_html, "w") as f_out:
        try:
            f_out.write(html)
        except:
            f_out.write(html.encode('utf-8').strip())

if __name__ == '__main__':
    file_name = "output\\2015_02_09_18_56_35_json_out.json"
    op_html = "output.html"
    report_generator(file_name, op_html)
