#!/usr/bin/env bash
# rc: Run Container

function locate-dir() {
  local path="${BASH_SOURCE[0]}"
  while [ -L "${path}" ]; do
    local dir="$(cd -P -- "$(dirname -- "${path}")" >/dev/null 2>&1 && pwd)"
    path="$(readlink "${path}")"
    [[ "${path}" != /* ]] && path="${dir}/${path}"
  done
  path="$(readlink -f -- "${path}")"
  dir="$(cd -P -- "$(dirname -- "${path}")" >/dev/null 2>&1 && pwd)"
  echo "${dir}"
}

# home of the working directory of the repository
home="$(locate-dir)/../.."
cd "$home" && src/docker/host/run-bot.sh latest
