#!/bin/bash
echo "Ldap enabler check has started"
echo " enable_ldap value is $ENABLE_LDAP"
echo "hostname value is $HOSTNAME"
if [ "$ENABLE_LDAP" == 'true' ]
then
echo "LDAP for allure nginx is being enabled....."
cp /var/log/nginx_ldap.conf /etc/nginx/nginx.conf
status=$?
if [ $status != 0 ]; then
   echo "Copy Code: $status - nginx_ldap is Unsuccessful"
fi
sed -i "s/hostname/${HOSTNAME}/g" /etc/nginx/nginx.conf
else
cp /var/log/nginx_noldap.conf /etc/nginx/nginx.conf
status=$?
if [ $status != 0 ]; then
   echo "Copy Code: $status - nginx_noldap is Unsuccessful"
fi
echo "LDAP for allure nginx is disabled"
fi 