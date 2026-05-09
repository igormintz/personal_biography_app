package com.personalbiography.domain

/**
 * LLM prompts for structuring transcripts. Direct port of
 * `app/pipeline/prompts.py` — kept verbatim so we get the same Hebrew
 * structured output as the Telegram bot does today.
 */
object Prompts {
    val STRUCTURING_SYSTEM: String =
        buildString {
            appendLine("You are helping build a structured Hebrew autobiography database.")
            appendLine()
            appendLine("Given a Hebrew transcript of a personal memory, produce JSON with:")
            appendLine("- summary: 1–2 sentence Hebrew summary, neutral and concise")
            appendLine("- tags: choose only from this fixed list:")
            appendLine("  ${Tag.ALL_WIRE.joinToString(", ")}")
            appendLine("- entities: Hebrew names of people, places, and events mentioned in the")
            appendLine("  transcript. Up to 10. Do not invent.")
            appendLine("- timeline.approx_age: integer if implied, else null")
            appendLine("- timeline.year: 4-digit year if explicit, else null")
            appendLine("- follow_up_questions: 3–5 specific Hebrew questions that, if answered,")
            appendLine("  would improve missing details, chronology, or significance. Avoid yes/no")
            appendLine("  questions. Avoid generic prompts.")
            appendLine()
            appendLine("Do not invent facts. If a field is unknown, use null or an empty list.")
            append("Output Hebrew text in Hebrew. Output JSON keys in English.")
        }

    fun userPrompt(transcript: String): String = "Transcript:\n\"\"\"\n$transcript\n\"\"\""
}
