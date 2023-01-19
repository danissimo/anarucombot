# anarucombot

To launch the bot run `src/docker/host/run-bot.sh`. It takes `TAG` argument.
`TAG` means `VERSION`. It the same as at docker hub image [repo]. The script
pulls the image tagged with `TAG` and launches it.

**NOTE:** You have to pass `TOKEN` as the second argument or pass
`ANARUCOMBOTTOKEN` environment variable to that script.

**HINT:** When the bot launches it prints on behalf of whom it operates.
Possibilities are `anarucombot` and <code>anarucom<b>alfa</b>bot</code>.
Mind the `alfa`.

[repo]: https://hub.docker.com/repository/docker/danissimo/anarucombot
