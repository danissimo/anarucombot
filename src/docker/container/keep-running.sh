BASENAME="$(basename $0)"
TAG="$1"
if [ -z "$TAG" ]; then
  echo "$BASENAME: no tag"
  echo "Usage: $BASENAME TAG"
  exit 127
fi
JAR="anarucombot-$TAG.jar"
if [ ! -r "$JAR" ]; then
  echo "$BASENAME: jar not found: $JAR"
  exit 126
fi
echo "$BASENAME: Running $JAR forever..."
while true; do
  java -jar anarucombot-$1.jar 2>&1 | tee -a console.log
done
