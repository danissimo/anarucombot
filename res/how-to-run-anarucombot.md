# How to run the anarucombot

```sh
$ docker pull danissimo/anarucombot:1.0.0
$ docker run \
  --env ANARUCOMBOTTOKEN=<token> \
  --name     anarucombot         \
  --hostname anarucombot         \
  --attach stdout                \
  --attach stderr                \
  danissimo/anarucombot:1.0.0    \
  > anarucombot.log 2>&1 &
```
