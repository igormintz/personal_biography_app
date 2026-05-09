package com.personalbiography.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Fixed tag vocabulary. Must match `TAG_VOCABULARY` in
 * `app/store/models.py` and the enum baked into the OpenAI JSON schema.
 */
@Serializable
enum class Tag(val wire: String) {
    @SerialName("childhood")
    CHILDHOOD("childhood"),

    @SerialName("family")
    FAMILY("family"),

    @SerialName("school")
    SCHOOL("school"),

    @SerialName("army")
    ARMY("army"),

    @SerialName("career")
    CAREER("career"),

    @SerialName("relationships")
    RELATIONSHIPS("relationships"),

    @SerialName("health")
    HEALTH("health"),

    @SerialName("travel")
    TRAVEL("travel"),

    @SerialName("milestones")
    MILESTONES("milestones"),

    @SerialName("daily_life")
    DAILY_LIFE("daily_life"),
    ;

    companion object {
        fun fromWireOrNull(value: String): Tag? = entries.firstOrNull { it.wire == value }

        val ALL_WIRE: List<String> = entries.map { it.wire }
    }
}
