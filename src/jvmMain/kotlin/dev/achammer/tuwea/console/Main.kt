package dev.achammer.tuwea.console

import dev.achammer.tuwea.core.ParseConfiguration
import dev.achammer.tuwea.core.ParseResult
import dev.achammer.tuwea.core.findExerciseAssignment
import dev.achammer.tuwea.core.parseCsvLines
import java.io.File


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
    val parseResult = parseCsvFile(parseConfiguration, file)
    val exerciseStudentAssignment = findExerciseAssignment(parseResult)
    println(exerciseStudentAssignment.joinToString("\n") {
        "${it.first}: ${it.second.firstName} ${it.second.lastName}"
    })
}

private fun parseCsvFile(
    parseConfiguration: ParseConfiguration,
    file: File
): ParseResult = parseCsvLines(parseConfiguration, file.readLines())
