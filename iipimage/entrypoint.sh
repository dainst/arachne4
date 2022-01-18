#!/bin/bash

/usr/lib/iipimage-server/iipsrv.fcgi --bind $(hostname -I):9000 -p 9000
