FROM armdocker.rnd.ericsson.se/proj_taf_te/allure_base_images/debian:9

MAINTAINER "PDLTAFRAME@pdl.internal.ericsson.com"

WORKDIR /data
#
# Define some variables.
#
ENV NGINX_VERSION release-1.12.2

#
# Install needed packages, compile and install.
# Remove unused packages and cleanup some directories.
#
COPY ldap_enabler.sh /var/log/
COPY nginx_ldap.conf /var/log/
COPY nginx_noldap.conf /var/log/
RUN echo $HOSTNAME > /var/log/hostname
RUN chmod +x /var/log/ldap_enabler.sh
RUN \
    apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y \
        ca-certificates \
        git \
        gcc \
        make \
        libpcre3-dev \
        zlib1g-dev \
        libldap2-dev \
        libssl-dev \
        wget && \
    mkdir /var/log/nginx && \
    mkdir /etc/nginx && \
    cd /tmp && \
    git clone https://github.com/kvspb/nginx-auth-ldap.git && \
    git clone https://github.com/nginx/nginx.git && \
    cd /tmp/nginx && \
    git checkout tags/${NGINX_VERSION} && \
    ./auto/configure \
        --add-module=/tmp/nginx-auth-ldap \
        --with-http_ssl_module \
        --with-http_gzip_static_module \
        --with-pcre \
        --with-debug \
        --conf-path=/etc/nginx/nginx.conf \
        --sbin-path=/usr/sbin/nginx \
        --pid-path=/var/log/nginx/nginx.pid \
        --error-log-path=/var/log/nginx/error.log \
        --http-log-path=/var/log/nginx/access.log && \
    make install && \
    apt-get purge -y \
        git \
        gcc \
        make \
        libpcre3-dev \
        zlib1g-dev \
        libldap2-dev \
        libssl-dev \
        wget && \
    apt-get autoremove -y && \
    apt-get -y clean && \
    rm -rf /var/lib/apt/lists/* && \
    rm -rf /usr/src/* && \
    rm -rf /tmp/* && \
    rm -rf /usr/share/doc/* && \
    rm -rf /usr/share/man/* && \
    rm -rf /usr/share/locale/*

RUN mkdir -p /var/www


# Expose nginx volumes
VOLUME /var/log/nginx
VOLUME /var/www

# Access to assets
#RUN chown -R root:root /var/www \
#    && chmod -R 777 /var/www/ \
#    && mkdir /var/client \
#    && mv /var/www/* /var/client/ \
#    && mv /var/client/* /var/www/ \
#    && rm -rf /var/www/client

#
# link access and error logs to docker log collector.
#
RUN ln -sf /dev/stdout /var/log/nginx/access.log && \
    ln -sf /dev/stderr /var/log/nginx/error.log

# ADD frontend configuration
ADD nginx.conf /etc/nginx/sites-enabled/nginx.conf
ADD allure-ssl.key /etc/ssl/private/allure-ssl.key
ADD allure-ssl.pem /etc/ssl/certs/allure-ssl.pem

# Install supervisor
RUN apt-get -qq update && apt-get -qq install -y --force-yes supervisor && apt-get -qq clean
ADD supervisord/* /etc/supervisor/conf.d/

# Install logrotate
RUN apt-get install logrotate -y --force-yes

ADD logrotate.conf /etc/logrotate.d/nginx
RUN chmod 644 /etc/logrotate.d/nginx

# Create cron job
ADD cron.sh /etc/cron.hourly/nginx
RUN chmod 755 /etc/cron.hourly/nginx

# CMD [ "/var/log/ldap_enabler.sh" ]

CMD ["sh","-c","/var/log/ldap_enabler.sh && /usr/bin/supervisord -n -c /etc/supervisor/supervisord.conf && /usr/sbin/nginx -g daemon off;" ]


#
# Start nginx.
#CMD ["nginx", "-g", "daemon off;"]

