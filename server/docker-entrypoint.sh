#!/bin/sh
set -eu

raw_url="${SPRING_DATASOURCE_URL:-${DATABASE_URL:-}}"

case "$raw_url" in
  postgres://*|postgresql://*)
    without_scheme="${raw_url#postgres://}"
    without_scheme="${without_scheme#postgresql://}"

    if [ "$without_scheme" != "${without_scheme#*@}" ]; then
      user_info="${without_scheme%%@*}"
      host_and_database="${without_scheme#*@}"

      if [ -z "${SPRING_DATASOURCE_USERNAME:-}" ] && [ -z "${DATABASE_USERNAME:-}" ]; then
        export SPRING_DATASOURCE_USERNAME="${user_info%%:*}"
      fi

      if [ -z "${SPRING_DATASOURCE_PASSWORD:-}" ] && [ -z "${DATABASE_PASSWORD:-}" ] && [ "$user_info" != "${user_info#*:}" ]; then
        export SPRING_DATASOURCE_PASSWORD="${user_info#*:}"
      fi
    else
      host_and_database="$without_scheme"
    fi

    export SPRING_DATASOURCE_URL="jdbc:postgresql://$host_and_database"
    ;;
esac

if [ -z "${SPRING_DATASOURCE_USERNAME:-}" ] && [ -n "${DATABASE_USERNAME:-}" ]; then
  export SPRING_DATASOURCE_USERNAME="$DATABASE_USERNAME"
fi

if [ -z "${SPRING_DATASOURCE_PASSWORD:-}" ] && [ -n "${DATABASE_PASSWORD:-}" ]; then
  export SPRING_DATASOURCE_PASSWORD="$DATABASE_PASSWORD"
fi

exec java -jar app.jar
