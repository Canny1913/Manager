package com.aliucord.manager.network.utils

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Parses a Semantic version in the format of `v1.0.0` or `1.0.0`.
 * This always gets serialized and stringified without the `v` prefix.
 */
@Immutable
@Parcelize
@Serializable(SemVer.Serializer::class)
data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val timestamp: Long,
) : Comparable<SemVer>, Parcelable {
    override fun compareTo(other: SemVer): Int {
        var cmp = 0
        if (0 != major.compareTo(other.major).also { cmp = it })
            return cmp
        if (0 != minor.compareTo(other.minor).also { cmp = it })
            return cmp
        if (0 != patch.compareTo(other.patch).also { cmp = it })
            return cmp
        if (0 != timestamp.compareTo(other.timestamp).also { cmp = it })
            return cmp

        return 0
    }

    override fun equals(other: Any?): Boolean {
        val ver = other as? SemVer
            ?: return false

        return ver.major == major &&
            ver.minor == minor &&
            ver.patch == patch &&
            ver.timestamp == timestamp
    }

    override fun toString(): String {
        return if (timestamp != 0L) {
            "${timestamp}_$major.$minor.$patch"
        } else {
            "$major.$minor.$patch"
        }
    }

    fun toVersionString(): String {
        return "$major.$minor.$patch"
    }

    fun getTimestamp(): String {
        return convertLongToTime(timestamp)
    }

    override fun hashCode(): Int {
        var result = major
        result = 31 * result + minor
        result = 31 * result + patch
        return result
    }

    companion object {
        fun parse(version: String): SemVer = parseOrNull(version)
            ?: throw IllegalArgumentException("Invalid semver string $version")

        fun parseOrNull(version: String): SemVer? {
            val parts = version.removePrefix("v").split("_", ".")
            var i = 0

            if (parts.size !in 3..4) return null
            val timestamp = if (parts.size == 4) parts[i++].toLongOrNull() ?: return null else 0
            val major = parts[i++].toIntOrNull() ?: return null
            val minor = parts[i++].toIntOrNull() ?: return null
            val patch = parts[i++].toIntOrNull() ?: return null

            return SemVer(major, minor, patch, timestamp)
        }

        fun convertLongToTime(time: Long): String {
            val date = Date(time)
            val format = SimpleDateFormat.getDateTimeInstance()
            return format.format(date)
        }
    }

    object Serializer : KSerializer<SemVer> {
        override val descriptor = PrimitiveSerialDescriptor("SemVer", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder) =
            parse(decoder.decodeString())

        override fun serialize(encoder: Encoder, value: SemVer) {
            encoder.encodeString(value.toString())
        }
    }
}
