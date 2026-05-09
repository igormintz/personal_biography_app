package com.personalbiography.domain

/**
 * Slash command parser. Mirrors the command set registered in
 * `app/bot/handlers.py::register` so the chat UX is identical to the bot.
 *
 * Anything that doesn't start with `/` is a [Command.FreeText] — gets fed to
 * the structuring pipeline as-is.
 */
sealed interface Command {
    data class FreeText(val body: String) : Command

    data object Last : Command

    data class Show(val shortId: String) : Command

    data class Questions(val shortId: String) : Command

    data class Edit(val shortId: String) : Command

    data class Tags(val shortId: String) : Command

    data class Restructure(val shortId: String) : Command

    data class Search(val text: String) : Command

    data object Usage : Command

    data object Help : Command

    data object Start : Command

    data class Invalid(val raw: String, val reason: String) : Command
}

fun parseCommand(raw: String): Command {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return Command.Invalid(raw, "empty")
    if (!trimmed.startsWith("/")) return Command.FreeText(trimmed)

    val firstSpace = trimmed.indexOfFirst { it.isWhitespace() }
    val name = (if (firstSpace == -1) trimmed.substring(1) else trimmed.substring(1, firstSpace)).lowercase()
    val rest = if (firstSpace == -1) "" else trimmed.substring(firstSpace).trim()

    fun requireId(builder: (String) -> Command): Command =
        if (rest.isEmpty()) Command.Invalid(raw, "missing id") else builder(rest.uppercase())

    return when (name) {
        "last" -> Command.Last
        "show" -> requireId { Command.Show(it) }
        "questions" -> requireId { Command.Questions(it) }
        "edit" -> requireId { Command.Edit(it) }
        "tags" -> requireId { Command.Tags(it) }
        "restructure" -> requireId { Command.Restructure(it) }
        "search" ->
            if (rest.isEmpty()) Command.Invalid(raw, "missing text") else Command.Search(rest)
        "usage" -> Command.Usage
        "help" -> Command.Help
        "start" -> Command.Start
        else -> Command.Invalid(raw, "unknown command")
    }
}
