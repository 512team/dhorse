#!/bin/sh

#  本软件遵守Apache开源许可协议2.0，
#  详情见：http://www.apache.org/licenses/LICENSE-2.0
#
# ---------------------------------------------------------------------------
# Stop script for the DHorse Server
# ---------------------------------------------------------------------------

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin"/server.sh stop