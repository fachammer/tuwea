package dev.achammer.tuwea.console

import dev.achammer.tuwea.core.findExerciseAssignment
import dev.achammer.tuwea.core.parseCsvContent
import dev.achammer.tuwea.core.tuwelParseConfiguration
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
    val parseResult = parseCsvContent(tuwelParseConfiguration, file.readText())
    val exerciseStudentAssignment = findExerciseAssignment(parseResult)
    println(exerciseStudentAssignment.joinToString("\n") {
        "${it.first}: ${it.second.firstName} ${it.second.lastName}"
    })
}

