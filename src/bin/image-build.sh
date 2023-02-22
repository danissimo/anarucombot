#!/usr/bin/env bash
# di: Build Image

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
cd "$home"      \
&& docker build \
  --file target/Dockerfile           \
  --tag danissimo/anarucombot:latest \
  .
