package dev.achammer.tuwea.core

import kotlin.random.Random

data class StudentCheckmarksEntry(
    val firstName: String,
    val lastName: String,
    val idNumber: String,
    val checkmarks: Set<String>
)

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

val tuwelParseConfiguration = ParseConfiguration(
    csvDelimiter = ';',
    csvLineOffset = 6,
    preCheckmarksFieldOffset = 3,
    firstNameKey = "Vorname",
    lastNameKey = "Nachname",
    idNumberKey = "ID-Nummer",
    exerciseEndSignifierKey = "Kreuzerl",
    checkmarkSignifiers = setOf("X", "(X)")
)

data class ParseResult(val exercises: List<String>, val studentCheckmarksEntries: List<StudentCheckmarksEntry>)
typealias ExerciseStudentAssignment = List<Pair<String, StudentCheckmarksEntry>>

fun findExerciseAssignment(parseResult: ParseResult): ExerciseStudentAssignment {
    val exercises = parseResult.exercises
    val entries = parseResult.studentCheckmarksEntries
    return exercises
        .sortedBy { exercise -> entries.count { it.checkmarks.contains(exercise) } }
        .fold(emptyList()) { chosenAssignments, exercise ->
            val potentialEntries = entries
                .filter { it.checkmarks.contains(exercise) }
                .filterNot { entry -> chosenAssignments.firstOrNull { it.second == entry } != null }
            chosenAssignments + Pair(exercise, potentialEntries[Random.nextInt(potentialEntries.size)])
        }
}

fun parseCsvContent(parseConfiguration: ParseConfiguration, csvContent: String): ParseResult {
    parseConfiguration.apply {
        val csvLines = csvContent.split("\n")
        val sanitizedCsvLines = csvLines.drop(csvLineOffset)
        val headerRow = sanitizedCsvLines.first()
        val fields = headerRow.split(csvDelimiter)
        val exercises = fields
            .drop(preCheckmarksFieldOffset)
            .takeWhile { s -> s != exerciseEndSignifierKey }
            .map { sanitizeExerciseName(it) }

        val entries = sanitizedCsvLines
            .drop(1)
            .map { line -> fields.zip(line.split(csvDelimiter)).toMap() }
            .map { entry -> parseCsvLine(entry) }
        return ParseResult(exercises, entries)
    }
}

private fun ParseConfiguration.parseCsvLine(entry: Map<String, String>): StudentCheckmarksEntry {
    val checkmarks = entry.keys
        .filterNot { it in setOf(firstNameKey, lastNameKey, idNumberKey) }
        .filter { entry[it] in checkmarkSignifiers }
        .map { sanitizeExerciseName(it) }
        .toSet()
    return StudentCheckmarksEntry(
        firstName = sanitizeFirstName(entry[firstNameKey]!!),
        lastName = entry[lastNameKey]!!,
        idNumber = entry[idNumberKey]!!,
        checkmarks = checkmarks
    )
}

private fun sanitizeExerciseName(exerciseName: String): String = firstWordOf(exerciseName)
private fun sanitizeFirstName(firstName: String): String = firstWordOf(firstName)
private fun firstWordOf(string: String) = string.trim().split(" ").first()