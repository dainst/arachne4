#!/bin/bash

export VERBOSITY=2
export MAX_IMAGE_CACHE_SIZE=0

FASTCGI_USER=www-data
FASTCGI_GROUP=www-data
IIPSRV=/usr/lib/iipimage-server/iipsrv.fcgi

# tells the shell to exit if any of the foreground command fails
set -eu

echo "Starting IIPImage"

pids=()

# spawn iip instance without watermarking
spawn-fcgi -u $FASTCGI_USER -g $FASTCGI_GROUP -f $IIPSRV -a $(hostname -I) -p 9000 -n &
pids+=($!)

# spawn iip instances with watermarks
export WATERMARK_PROBABILITY="1"
export WATERMARK_OPACITY="0.1"

export WATERMARK="/watermarks/watermark_arachne.tif"
spawn-fcgi -u $FASTCGI_USER -g $FASTCGI_GROUP -f $IIPSRV -a $(hostname -I) -p 9001 -n &
pids+=($!)

export WATERMARK="/watermarks/watermark_berlin.tif"
spawn-fcgi -u $FASTCGI_USER -g $FASTCGI_GROUP -f $IIPSRV -a $(hostname -I) -p 9002 -n &
pids+=($!)

export WATERMARK="/watermarks/watermark_dai.tif"
spawn-fcgi -u $FASTCGI_USER -g $FASTCGI_GROUP -f $IIPSRV -a $(hostname -I) -p 9003 -n &
pids+=($!)

export WATERMARK="/watermarks/watermark_oppenheim.tif"
spawn-fcgi -u $FASTCGI_USER -g $FASTCGI_GROUP -f $IIPSRV -a $(hostname -I) -p 9004 -n &
pids+=($!)

echo $pids

# ait for each specific process to terminate.
for pid in "${pids[@]}"; do
  wait "$pid"
done
