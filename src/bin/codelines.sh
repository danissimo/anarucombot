#!/usr/bin/env bash

script="${BASH_SOURCE[0]}"
while [ -L "$script" ]; do
  ref="$(readlink "$script")"
  if [[ "$ref" == /* ]]; then
    script="$ref"
  else
    script="$(dirname "$script")/$ref"
  fi
done

# home of the working directory of the repository
home="$(dirname "$script")/../.."
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
