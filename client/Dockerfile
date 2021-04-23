FROM nginx:1.15-alpine
COPY resources/public /usr/share/nginx/html
# COPY nginx/default.template /etc/nginx/conf.d/default.conf # <- Eventueel, indien andere routering dan via de index.html
# Run startup script on startup
COPY nginx/startup.sh .
CMD [ "./startup.sh"]
