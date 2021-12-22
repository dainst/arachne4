FROM node:lts as builder

COPY . /frontend 
WORKDIR /frontend

RUN npm i
RUN npm run build

FROM nginx

COPY --from=builder /frontend/public /usr/share/nginx/html
COPY config/nginx.conf /etc/nginx/nginx.conf
