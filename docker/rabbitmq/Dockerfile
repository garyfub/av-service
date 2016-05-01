FROM debian:jessie

RUN apt-get update \
    && apt-get -y install \
        rabbitmq-server \
    && rm -rf /var/lib/apt/lists/*

COPY conf/rabbitmq-env.conf /etc/rabbitmq/
COPY bin/prepareRMQ.sh /root
RUN rabbitmq-server -detached \
    && sleep 10 \
    && rabbitmq-plugins enable rabbitmq_management \
    && /root/prepareRMQ.sh \
    && rabbitmqctl stop

EXPOSE 5672
EXPOSE 15672

CMD ["rabbitmq-server"]