#! /bin/sh

#
# $Id: ns-wrapper.tpl,v 1.1 2006-12-15 14:27:24 alphonse.bendt Exp $
#
# sh script suitable for starting and stopping
# the JacORB Naming Service controlled by the Java Service Wrapper
#
# Make sure that PIDDIR points to the correct location,
# if you have changed the default location set in the
# wrapper configuration file.
#

#-----------------------------------------------------------------------------

# Application Settings
APP_NAME="JacORB-Naming"
APP_LONG_NAME="JacORB Naming Service"

# specify the wrapper executable
WRAPPER_CMD="wrapper"

# specify the wrapper configuration file
WRAPPER_CONF="NamingService-Wrapper.conf"

# Priority at which to run the wrapper.  See "man nice" for valid priorities.
#  nice is only used if a priority is specified.
PRIORITY=

# Location of the pid file.
PIDDIR="."

# source the common wrapper script.

. @JACORB_HOME@/bin/jacorb-wrapper-common
