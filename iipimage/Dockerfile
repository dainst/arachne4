FROM ubuntu:latest

RUN apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get -yq install iipimage-server

COPY ./entrypoint.sh /

ENTRYPOINT ["/entrypoint.sh"]
