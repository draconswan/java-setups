# Java Configuration Examples/templates
Sample repo of various java configurations for new/existing applications

# To Run
You must have a docker container running with both a redis cache and a local mongodb
* Setting up Redis: 
  * docker pull redis/redis-stack-server:latest
  * docker run -d --name redis-stack-server -p 6379:6379 redis/redis-stack-server:latest
* Setting up Mongo: 
  * docker pull mongodb/mongodb-community-server:latest
  * docker run --name mongodb -p 27017:27017 -d mongodb/mongodb-community-server:latest

# TODO
Setup a GCP environment to allow connections for the pubsub configuration
