# anarucombot

To launch the bot run `src/docker/host/run-bot.sh`. It takes `TAG` argument.
`TAG` means `VERSION`. It the same as at docker hub image [repo]. The script
pulls the image tagged with `TAG` and launches it.

**NOTE:** You have to pass `TOKEN` as the second argument or pass
`ANARUCOMBOTTOKEN` environment variable to that script.

**HINT:** When the bot launches it prints on behalf of whom it operates.
Possibilities are `anarucombot` and <code>anarucom<b>alfa</b>bot</code>.
Mind the `alfa`.

---

Прикинь, теперь когда следующая команда поступает раньше, чем через 5 секунд, бот отвечает, прям риплаит, что он не железный, обратись через 5 секунд/4/3/пару/секунду. А через 4 секунды после того, как пориплаил, удаляет и команду, которая вызвала риплай, и сам риплай.

А ещё бот регает список команд для каждого чата свой. Для админов свой список, для неадминов свой

А ещё удаляет сообщение, что кто–то добавил в чат бота, одного из двух — основного и тестового. Или удалил из чата одного из этих двух ботов, тоже удаляет

Последнее, может, и зря... Посмотрим по практике использования

А ещё он пытается удалить сообщение с:
- неподдерживаемой командой;
- командой, отданной другому боту;
- командой, пришедшей из неподдердиваемого чата.


[repo]: https://hub.docker.com/repository/docker/danissimo/anarucombot
