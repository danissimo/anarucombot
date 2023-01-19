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
  exit -1
fi
docker run                        \
  --env ANARUCOMBOTTOKEN="$TOKEN" \
  --detach                        \
  --hostname anarucombot          \
  --name     anarucombot          \
  --restart  always               \
  danissimo/anarucombot:"$TAG"
