FROM node:alpine
ADD . /frontend
WORKDIR /frontend
RUN npm install -g @angular/cli
RUN npm install
CMD ["ng", "serve", "--host", "0.0.0.0"]
