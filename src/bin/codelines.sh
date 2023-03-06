#!/usr/bin/env bash

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
cd "$home" || exit $?

br="$(git branch --show-current)"
for i in $(git tag --list | sort --version-sort); do
  git checkout $i > /dev/null 2>&1
  total=$(find . \( \
      !  -path './.idea*'     \
      !  -path './.git*'      \
      !  -path './.mvn*'      \
      !  -path './mvnw'       \
      !  -path './target*'    \
      -o -path './.gitignore' \
    \) \
    -type f -exec wc -l {} + | grep total)
  echo $i : $total
done
git checkout $br > /dev/null 2>&1
