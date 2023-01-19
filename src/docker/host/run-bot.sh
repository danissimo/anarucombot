#!/bin/sh

TAG="$1"
TOKEN="$2"
[ -z "$TOKEN" ] && TOKEN="$ANARUCOMBOTTOKEN"

if [ -z "$TAG" ] || [ -z "$TOKEN" ]; then
  BASENAME="$(basename $0)"
  [ -z "$TAG"] && MSG="TAG missing" || MSG="TOKEN undefined"
  echo "$BASENAME: $MSG"
  echo "Usage: $BASENAME TAG [TOKEN]"
  echo "NOTE: if TOKEN not provided ANARUCOMBOTTOKEN"
  echo "      is used (which might be empty too)"
  exit 127
fi

IMAGE="danissimo/anarucombot:$TAG"
CONT=anarucombot

if [ -z "$(docker images -q "$IMAGE")" ] \
&& ! docker pull "$IMAGE"; then
  echo "Failed to pull image: $IMAGE"
  echo "Current container left intact: $CONT"
  echo "The new bot was NOT started"
  exit 126
fi

# kill currently running if any
docker rm -f $CONT > /dev/null 2>&1

# launch new
docker run                        \
  --env ANARUCOMBOTTOKEN="$TOKEN" \
  --detach                        \
  --hostname $CONT                \
  --name     $CONT                \
  --restart  always               \
  $IMAGE
RES=$?

[ $RES -eq 0 ] && echo Success || echo Failure
exit $RES
