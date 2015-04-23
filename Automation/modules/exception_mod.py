# pylint: disable=C0103,W0231
"""
#-------------------------------------------------------------------------------
# Name       :  exception_mod
# Purpose    :  exception handling
#
# Author     :  Rebaca
#
# Created    :  26-03-2015
# Copyright  :  (c) Rebaca 2015
#-------------------------------------------------------------------------------
"""
import traceback
import sys
from logger import error

# User defined messages
glue_not_implmnt_static_msg = "Glue code not implemented."
glue_not_implmnt_static_dynamic_msg = "Glue code not implemented for : %s"
invalid_keyword_used_msg = "Invalid Keyword found : %s"
not_equal_excp_msg = "Expected Value: %s, Got Value: %s"
equal_excp_msg = "Got values as : %s"
invalid_combination_msg = "%s"

equal_success_msg = "Got values as : %s"
not_equal_success_msg = "Not Expected Value : %s, Got Value: %s"

widevine_error = "The downloading pop-up is not visible for " + \
                 "offline widevine encoding"

def handle_exception(function):
    """This decorator is to handle the exceptions and print it in log"""
    def wrapper(*args, **kwargs):
        """ The wrapper function """
        try:
            return function(*args, **kwargs)
        except Exception as ex:
            exc_type, exc_value, exc_traceback = sys.exc_info()
            error("The traceback is :\n"+"" \
             .join(traceback.format_exception(exc_type, exc_value,
                                              exc_traceback)))
            error ("Exception Occurred: %s" %str(ex))
            args[0].exit_app()
            raise
    return wrapper

class GlueCodeNotImplemented(Exception):
    """Custom Exception Class"""
    def __init__(self,  msg=""):
        Exception.__init__(self)
        self.msg = msg

    def __str__(self):
        if self.msg :
            return self.msg
        else:
            return glue_not_implmnt_static_msg

class InvalidKeyWordUsed(Exception):
    """Custom Exception Class"""
    def __init__(self,  **param_and_val):
        Exception.__init__(self)
        self.param_and_val = param_and_val

    def __str__(self):
        msg = ', '.join("%s=%s" % (key, val) for (key, val) in \
                        self.param_and_val.iteritems())
        return invalid_keyword_used_msg % msg

class NotEqualException(Exception):
    """Custom Exception Class"""
    def __init__(self, expected, actual):
        self.expected = expected
        self.actual = actual

    def __str__(self):
        return not_equal_excp_msg % (self.expected, self.actual)

class EqualException(Exception):
    """Custom Exception Class"""
    def __init__(self, actual):
        self.actual = actual

    def __str__(self):
        return equal_excp_msg % (self.actual)

class InvalidCombination(Exception):
    """Custom Exception Class"""
    def __init__(self,  **param_and_val):
        Exception.__init__(self)
        self.param_and_val = param_and_val

    def __str__(self):
        msg = ', '.join("%s=%s" % (key, val) for (key, val) in \
                        self.param_and_val.iteritems())
        return invalid_combination_msg % msg