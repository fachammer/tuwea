package dev.achammer.tutor

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import kotlin.random.Random

data class CheckmarksEntry(val firstName: String, val lastName: String, val idNumber: String, val checkmarks: Set<String>)


data class ParseConfiguration(
    val csvDelimiter: Char,
    val csvLineOffset: Int,
    val preCheckmarksFieldOffset: Int,
    val firstNameKey: String,
    val lastNameKey: String,
    val idNumberKey: String,
    val exerciseEndSignifierKey: String,
    val checkmarkSignifiers: Set<String>
)

fun main(args: Array<String>) {
    val fileName = args.firstOrNull()
    if (fileName == null) {
        println("Please provide a csv file to parse")
        return
    }

    if (!File(fileName).exists()) {
        println("Please provide a valid file path. $fileName does not point to a valid file")
    }

    val file = File(fileName)
    val configuration = ParseConfiguration(
        csvDelimiter = ';',
        csvLineOffset = 6,
        preCheckmarksFieldOffset = 3,
        firstNameKey = "Vorname",
        lastNameKey = "Nachname",
        idNumberKey = "ID-Nummer",
        exerciseEndSignifierKey = "Kreuzerl",
        checkmarkSignifiers = setOf("X", "(X)")
    )

    processFile(file, configuration)
}

private fun processFile(file: File, parseConfiguration: ParseConfiguration) {
    val (exercises, entries) = parseCsvFile(file, parseConfiguration)
    val chosenAssignments = assignExercises(exercises, entries)
    println(chosenAssignments.joinToString("\n") {
        "${it.first}: ${it.second.firstName} ${it.second.lastName}"
    })
}

private fun assignExercises(
    exercises: List<String>,
    entries: List<CheckmarksEntry>
): List<Pair<String, CheckmarksEntry>> {
    return exercises
        .sortedBy { exercise -> entries.count { it.checkmarks.contains(exercise) } }
        .fold(emptyList()) { chosenAssignments, exercise ->
            val potentialEntries = entries
                .filter { it.checkmarks.contains(exercise) }
                .filterNot { entry -> chosenAssignments.firstOrNull { it.second == entry } != null }
            chosenAssignments + Pair(exercise, potentialEntries[Random.nextInt(potentialEntries.size)])
        }
}

private fun parseCsvFile(
    file: File,
    parseConfiguration: ParseConfiguration
): Pair<List<String>, List<CheckmarksEntry>> {
    parseConfiguration.apply {
        val csvLines = file.readLines().drop(csvLineOffset)
        val headerRow = csvLines.first()
        val reader = csvReader { delimiter = csvDelimiter }
        val exercises = reader
            .readAll(headerRow)
            .first()
            .drop(preCheckmarksFieldOffset)
            .takeWhile { s -> s != exerciseEndSignifierKey }
            .map { sanitizeExerciseName(it) }

        val entries = reader
            .readAllWithHeader(csvLines.joinToString("\n"))
            .mapIndexed { index, entry ->
                if (!entry.keys.containsAll(setOf(firstNameKey, lastNameKey, idNumberKey))) {
                    throw Exception("row $index is not formatted correctly: ${csvLines[index]}")
                }

                val checkmarks = entry.keys
                    .filterNot { it in setOf(firstNameKey, lastNameKey, idNumberKey) }
                    .filter { entry[it] in checkmarkSignifiers }
                    .map { sanitizeExerciseName(it) }
                    .toSet()
                CheckmarksEntry(
                    firstName = sanitizeFirstName(entry[firstNameKey]!!),
                    lastName = entry[lastNameKey]!!,
                    idNumber = entry[idNumberKey]!!,
                    checkmarks = checkmarks
                )
            }
        return Pair(exercises, entries)
    }
}

private fun sanitizeExerciseName(exerciseName: String): String = firstWordOf(exerciseName)
private fun sanitizeFirstName(firstName: String): String = firstWordOf(firstName)
private fun firstWordOf(string: String) = string.trim().split(" ").first()