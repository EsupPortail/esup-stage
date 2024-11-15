FROM node:20.17.0

RUN mkdir /home/node/.npm-global
ENV PATH=/home/node/.npm-global/bin:$PATH
ENV NPM_CONFIG_PREFIX=/home/node/.npm-global

ENV HOME=/home/node

ENV TZ Europe/Paris

WORKDIR $HOME/app

RUN npm install -g npm@10.8.2

RUN npm install -g @angular/cli@18.2.7 --save-dev && npm cache clean --force

EXPOSE 4200

ENTRYPOINT ["ng"]
