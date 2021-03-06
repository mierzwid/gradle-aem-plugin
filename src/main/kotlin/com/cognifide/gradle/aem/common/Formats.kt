package com.cognifide.gradle.aem.common

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import java.io.File
import java.io.InputStream
import java.math.BigInteger
import java.nio.file.Paths
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DurationFormatUtils
import org.apache.commons.validator.routines.UrlValidator
import org.apache.jackrabbit.util.ISO8601
import org.gradle.api.Project

@Suppress("MagicNumber")
object Formats {

    val URL_VALIDATOR = UrlValidator(arrayOf("http", "https"), UrlValidator.ALLOW_LOCAL_URLS)

    val JSON_MAPPER = {
        val printer = DefaultPrettyPrinter()
        printer.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)

        ObjectMapper().writer(printer)
    }()

    fun asJson(input: InputStream) = JsonPath.parse(input)

    fun asJson(value: String) = JsonPath.parse(value)

    fun toJson(value: Any): String {
        return JSON_MAPPER.writeValueAsString(value) ?: ""
    }

    fun toJson(value: Map<String, Any?>): String {
        return JSON_MAPPER.writeValueAsString(value) ?: "{}"
    }

    fun <T> fromJson(json: String, clazz: Class<T>): T {
        return ObjectMapper().readValue(json, clazz)
    }

    fun toList(value: String?, delimiter: String = ","): List<String> {
        if (value.isNullOrBlank()) {
            return emptyList()
        }

        val between = StringUtils.substringBetween(value, "[", "]") ?: value

        return between.split(delimiter)
    }

    fun toBase64(value: String): String {
        return Base64.getEncoder().encodeToString(value.toByteArray())
    }

    fun size(file: File): String {
        return bytesToHuman(when {
            file.exists() -> FileUtils.sizeOf(file)
            else -> 0L
        })
    }

    fun bytesToHuman(bytes: Long): String {
        return when {
            bytes < 1024 -> bytes.toString() + " B"
            bytes < 1024 * 1024 -> (bytes / 1024).toString() + " KB"
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    fun percent(current: Int, total: Int): String {
        return percent(current.toLong(), total.toLong())
    }

    fun percent(current: Long, total: Long): String {
        val value: Double = when (total) {
            0L -> 0.0
            else -> current.toDouble() / total.toDouble()
        }

        return "${"%.2f".format(value * 100.0)}%"
    }

    fun date(date: Date = Date()): String {
        return ISO8601.format(Calendar.getInstance().apply { time = date })
    }

    fun dateFileName(date: Date = Date()): String {
        return SimpleDateFormat("yyyyMMddHHmmss").format(date)
    }

    fun duration(millis: Long): String {
        return DurationFormatUtils.formatDurationHMS(millis)
    }

    fun rootProjectPath(file: File, project: Project): String {
        return rootProjectPath(file.absolutePath, project)
    }

    fun rootProjectPath(path: String, project: Project): String {
        return projectPath(path, project.rootProject)
    }

    fun projectPath(file: File, project: Project): String {
        return projectPath(file.absolutePath, project)
    }

    fun projectPath(path: String, project: Project): String {
        return relativePath(path, project.projectDir.absolutePath)
    }

    fun relativePath(path: String, basePath: String): String {
        val source = Paths.get(path)
        val base = Paths.get(basePath)

        return base.relativize(source).toString()
    }

    fun normalizeSeparators(name: String, separator: String): String {
        return name.replace(":", separator)
                .replace("-", separator)
                .replace(".", separator)
                .removePrefix(separator)
                .removeSuffix(separator)
    }

    fun calculateChecksum(text: String): String {
        val messageDigest = MessageDigest.getInstance("MD5")
        val data = text.toByteArray()
        messageDigest.update(data, 0, data.size)
        val result = BigInteger(1, messageDigest.digest())
        return String.format("%1$032x", result)
    }
}

val Collection<File>.fileNames
    get() = if (isNotEmpty()) joinToString(", ") { it.name } else "none"