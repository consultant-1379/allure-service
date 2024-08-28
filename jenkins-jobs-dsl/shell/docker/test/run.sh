#!/bin/bash
set -e

if [ $(docker-compose ps -q | wc -l) != 0 ]; then
    echo "# Stopping Allure services"
    docker-compose down
fi

echo "# Pulling latest Allure service image"
docker-compose pull backend

echo "# Pulling latest nginx service image"
docker-compose pull nginx

echo "# Pulling latest cAdvisor service image"
docker-compose pull cAdvisor

no_tag=$(docker images -qf "dangling=true");
if [ -n "$no_tag" ]; then
    echo "# Removing old Allure service images"
    echo "$no_tag"
    docker rmi ${no_tag}
fi

no_networks=$(docker network ls | grep -E "outside" | awk '/ / { print $1 }');
if [ -n "$no_networks" ]; then
    echo "# Removing Allure service networks"
    echo "$no_networks"
    docker network rm ${no_networks}
fi

echo "# Creating networks"
docker network create -d bridge outside

echo "# Starting up Allure services"
docker-compose up -d
