# Scalable Distributed System

## Introduction:

A distributed, concurrent system that utilizes messaging broker (RabbitMQ) to process text stream. 

The HTTP web server was hosted on AWS EC2 using Apache Tomcat Servers. 

The multi-threaded Java client was also hosted in EC2s for testing based on metrics such as latencies, throughputs, and P99 response time.
Some optimization strategies used to improve overall system performance are AWS Load Balancing and RabbitMQ based Messaging Architecture

Used Redis as the backend database

## Tech Stack:
Java, AWS, RabbitMQ, Redis, JMeter
