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
cd "$home"                   \
&& echo "Pulling repo"       \
&& git pull --rebase         \
&& echo "Deleting image"     \
&& src/bin/image-delete.sh   \
&& echo "Building new image" \
&& src/bin/image-build.sh    \
&& echo "Pushing image"      \
&& src/bin/image-push.sh     \
&& src/docker/host/run-bot.sh latest
