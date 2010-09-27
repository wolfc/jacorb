#! /bin/sh

#
# $Id: ntfy-wrapper.tpl,v 1.1 2006/12/15 14:27:24 alphonse.bendt Exp $
#
# shell script suitable for starting and stopping
# the JacORB Notification Service controlled by the Java Service Wrapper
#
# Make sure that PIDDIR points to the correct location,
# if you have changed the default location set in the
# wrapper configuration file.
#

#-----------------------------------------------------------------------------

# Application
APP_NAME="JacORB-Notify"
APP_LONG_NAME="JacORB Notification Service"

WRAPPER_CONF="NotifyService-Wrapper.conf"

# Wrapper

# specify a custom location (full qualified path) for the wrapper
# command here. Not necessary if 'wrapper' is available via
# PATH. Works also if WRAPPER_CMD is a symlink which points to the
# actual cmd.
#WRAPPER_CMD="/usr/local/wrapper/bin/wrapper"

# specify the location of the wrapper home here. This is not
# necessary if WRAPPER_CMD is located inside WRAPPER_HOME/bin.
#WRAPPER_HOME="/usr/local/wrapper"

# Priority at which to run the wrapper.  See "man nice" for valid priorities.
#  nice is only used if a priority is specified.
PRIORITY=

# Location of the pid file.
PIDDIR="."

# source the common wrapper script

.  @JACORB_HOME@/bin/jacorb-wrapper-common
