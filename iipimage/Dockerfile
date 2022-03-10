FROM ubuntu:latest

RUN apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get -yq install spawn-fcgi iipimage-server

COPY ./entrypoint.sh /
COPY ./watermarks /watermarks

ENTRYPOINT ["/entrypoint.sh"]
