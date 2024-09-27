#!/bin/bash

export VERBOSITY=2
export MAX_IMAGE_CACHE_SIZE=0

FASTCGI_USER=root
FASTCGI_GROUP=www-data
IIPSRV=/usr/lib/iipimage-server/iipsrv.fcgi

echo "Starting IIPImage"

# spawn iip instance without watermarking
spawn-fcgi -u $FASTCGI_USER -g $FASTCGI_GROUP -f $IIPSRV -a $(hostname -I) -p 9000 -n &

# spawn iip instances with watermarks
export WATERMARK_PROBABILITY="1"
export WATERMARK_OPACITY="0.1"

export WATERMARK="/watermarks/watermark_arachne.tif"
spawn-fcgi -u $FASTCGI_USER -g $FASTCGI_GROUP -f $IIPSRV -a $(hostname -I) -p 9001 -n &

export WATERMARK="/watermarks/watermark_berlin.tif"
spawn-fcgi -u $FASTCGI_USER -g $FASTCGI_GROUP -f $IIPSRV -a $(hostname -I) -p 9002 -n &

export WATERMARK="/watermarks/watermark_dai.tif"
spawn-fcgi -u $FASTCGI_USER -g $FASTCGI_GROUP -f $IIPSRV -a $(hostname -I) -p 9003 -n &

export WATERMARK="/watermarks/watermark_oppenheim.tif"
spawn-fcgi -u $FASTCGI_USER -g $FASTCGI_GROUP -f $IIPSRV -a $(hostname -I) -p 9004 -n &

# Wait for any process to exit
wait -n

# Exit with status of process that exited first
exit $?

