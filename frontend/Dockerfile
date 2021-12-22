FROM node:lts as builder

COPY . /ui 
WORKDIR /ui

RUN npm i
RUN npm run build

FROM nginx

COPY --from=builder /ui/public /usr/share/nginx/html
COPY config/nginx.conf /etc/nginx/nginx.conf
