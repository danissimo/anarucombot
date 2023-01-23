#!/usr/bin/env sh
for i in $(git tag -l); do
  git co $i > /dev/null 2>&1
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
git switch - > /dev/null 2>&1
