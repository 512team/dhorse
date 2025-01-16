#!/bin/bash

#  本软件遵守Apache开源许可协议2.0，
#  详见：http://www.apache.org/licenses/LICENSE-2.0
#
# ---------------------------------------------------------------------------
# Start script for the DHorse Server
# ---------------------------------------------------------------------------

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin"/server.sh start