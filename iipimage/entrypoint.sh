#!/bin/bash

echo "Starting IIPImage"
spawn-fcgi -n -f /usr/lib/iipimage-server/iipsrv.fcgi -a $(hostname -I) -p 9000
