#!/bin/bash


# Ferma il container Docker
docker-compose stop

running_containers=$(docker-compose ps -q)

# Controlla se il container Docker si è fermato correttamente
if [ "$running_containers" ]; then
    echo "Killing containers"
    for container in $running_containers
    do
        docker kill $container
    done
fi

# Rimuove il container Docker
docker-compose down

# Riavvia il container Docker
docker-compose up -d