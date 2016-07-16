FROM debian:jessie

ENV VERSION=9.4
ENV PGDATA=/var/lib/postgresql/$VERSION/main

RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive \
    apt-get -y install \
        postgresql \
    && rm -rf /var/lib/apt/lists/*

COPY conf/pg_hba.conf /etc/postgresql/$VERSION/main/

RUN mkdir -p /var/run/postgresql/$VERSION-main.pg_stat_tmp
RUN chown -R postgres:postgres /var/run/postgresql/$VERSION-main.pg_stat_tmp
RUN chown postgres:postgres /etc/postgresql/$VERSION/main/pg_hba.conf

USER postgres

RUN /etc/init.d/postgresql start \
    && psql --command "CREATE USER docker WITH SUPERUSER PASSWORD 'docker';" \
    && createdb -O docker docker

EXPOSE 5432

CMD /usr/lib/postgresql/${VERSION}/bin/postgres -c config_file=/etc/postgresql/$VERSION/main/postgresql.conf