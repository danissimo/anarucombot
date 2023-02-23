# anarucombot

To launch the bot run `src/docker/host/run-bot.sh`. It takes `TAG` argument.
`TAG` means `VERSION`. It's the same as at the docker hub image [repo]. The
script pulls the image tagged with `TAG` and launches it.

**NOTE:** You have to pass `TOKEN` as the second argument or export
`ANARUCOMBOTTOKEN` environment variable to that script.

**HINT:** When the bot launches it prints on behalf of whom it operates.
Possibilities are `anarucombot` and <code>anarucom<b>alfa</b>bot</code>.
Mind the `alfa`.

---

When the bot receives a next command earlier than after 5 seconds it
replies—literally replies—he's not steel and suggest to come back in
5 seconds, 4, 3, a couple of seconds, in a second—literally changes
the text. And 4 seconds after the reply it removes the command caused
the reply and the reply itself.

The bot serves different chats. For each chat it maintains a dedicated set
of commands. Sets of commands for admins differ from sets of commands for
non–admins.

There're two bots—production and staging (remember the `alfa` in bot's name).
When someone adds into or removes either of the two bots from a group a message
appears stating the bot was added or removed. The bot removes those messages.
I'm not sure the latter was a good idea. We'll see.

Also the bot removes messages if:
 - it is a non–supported command;
 - it is a command given to any other bot;
 - it is a command given to a bot in a non–supported group.


[repo]: https://hub.docker.com/repository/docker/danissimo/anarucombot
