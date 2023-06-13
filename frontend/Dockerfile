FROM node:20.3-bullseye as builder

ARG BUILD_NO=DEV

COPY . /frontend 
WORKDIR /frontend

RUN npm i
RUN npm run build -- --env build=${BUILD_NO}

FROM nginx

COPY --from=builder /frontend/public /usr/share/nginx/html
COPY config/nginx.conf /etc/nginx/nginx.conf
