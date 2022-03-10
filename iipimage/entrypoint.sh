#!/bin/bash

export VERBOSITY=2
export MAX_IMAGE_CACHE_SIZE=0
FASTCGI_USER=www-data
FASTCGI_GROUP=www-data
IIPSRV=/usr/lib/iipimage-server/iipsrv.fcgi

echo "Starting IIPImage"

# spawn iip instance without watermarking
SOCKET="$SOCKET_PATH/iip-fastcgi.socket"
spawn-fcgi -u $FASTCGI_USER -g $FASTCGI_GROUP -f $IIPSRV -a $(hostname -I) -p 9000

# spawn iip instances with watermarks
export WATERMARK_PROBABILITY="1"
export WATERMARK_OPACITY="0.1"

export WATERMARK="/usr/lib/cgi-bin/watermarks/watermark_arachne.tif"
spawn-fcgi -u $FASTCGI_USER -g $FASTCGI_GROUP -f $IIPSRV -a $(hostname -I) -p 9001

export WATERMARK="/usr/lib/cgi-bin/watermarks/watermark_berlin.tif"
spawn-fcgi -u $FASTCGI_USER -g $FASTCGI_GROUP -f $IIPSRV -a $(hostname -I) -p 9002

export WATERMARK="/usr/lib/cgi-bin/watermarks/watermark_dai.tif"
spawn-fcgi -u $FASTCGI_USER -g $FASTCGI_GROUP -f $IIPSRV -a $(hostname -I) -p 9003

export WATERMARK="/usr/lib/cgi-bin/watermarks/watermark_oppenheim.tif"
spawn-fcgi -u $FASTCGI_USER -g $FASTCGI_GROUP -f $IIPSRV -a $(hostname -I) -p 9004

wait
