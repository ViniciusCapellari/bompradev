#!/bin/sh
set -eu

# Carrega apenas valores iniciais; alterações posteriores no Consul são preservadas.
configurar_se_ausente() {
    chave="$1"
    valor="$2"
    if consul kv get "$chave" >/dev/null 2>&1; then
        echo "Configuração preservada: $chave"
    else
        consul kv put -cas -modify-index=0 "$chave" "$valor"
    fi
}

configurar_se_ausente "config/api-gateway/server.port" "8080"

configurar_se_ausente "config/auth-service/server.port" "8081"
configurar_se_ausente "config/auth-service/spring.datasource.url" "jdbc:postgresql://auth-db:5432/bompradev_auth"
configurar_se_ausente "config/auth-service/spring.datasource.username" "${POSTGRES_USER:-bompradev}"

configurar_se_ausente "config/catalog-service/server.port" "8082"
configurar_se_ausente "config/catalog-service/spring.datasource.url" "jdbc:postgresql://catalog-db:5432/bompradev_catalog"
configurar_se_ausente "config/catalog-service/spring.datasource.username" "${POSTGRES_USER:-bompradev}"

configurar_se_ausente "config/scheduling-service/server.port" "8083"
configurar_se_ausente "config/scheduling-service/spring.datasource.url" "jdbc:postgresql://scheduling-db:5432/bompradev_scheduling"
configurar_se_ausente "config/scheduling-service/spring.datasource.username" "${POSTGRES_USER:-bompradev}"
