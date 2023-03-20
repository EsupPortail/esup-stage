FROM node:16.15.0

RUN mkdir /home/node/.npm-global
ENV PATH=/home/node/.npm-global/bin:$PATH
ENV NPM_CONFIG_PREFIX=/home/node/.npm-global

ENV HOME=/home/node

ENV TZ Europe/Paris

WORKDIR $HOME/app

RUN npm install -g npm@8.5.5

RUN npm install -g @angular/cli@14.2.11 --save-dev && npm cache clean --force

EXPOSE 4200

ENTRYPOINT ["ng"]
