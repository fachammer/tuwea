package dev.achammer.tuwea.browser

import dev.achammer.tuwea.core.findExerciseAssignment
import dev.achammer.tuwea.core.parseCsvContent
import dev.achammer.tuwea.core.tuwelParseConfiguration
import kotlinx.browser.document
import kotlinx.dom.clear
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.js.*
import org.w3c.dom.get
import org.w3c.files.FileReader

fun main() {
    document.getElementById("app")
        ?.also { it.innerHTML = "" }
        ?.append {
            h1 { +"tuwea" }
            val assignmentTable = table {
                thead {
                    tr {
                        th {
                            +"exercise"
                        }
                        th {
                            +"name"
                        }
                    }
                }
                tbody {
                }
            }
            val dataTable = table {}
            input(type = InputType.file) {
                onChangeFunction = { event: dynamic ->
                    val file = event.target?.files[0]
                    val reader = FileReader()
                    reader.onload = { it: dynamic ->
                        val content: String = it.target.result
                        val parseResult = parseCsvContent(tuwelParseConfiguration, content)
                        val exerciseAssignment = findExerciseAssignment(parseResult)
                        dataTable.clear()
                        dataTable.createTHead().append {
                            tr {
                                th { +"name" }
                                parseResult.exercises.forEach {
                                    th { +it }
                                }
                            }
                        }
                        dataTable.createTBody().append {
                            parseResult.studentCheckmarksEntries.forEach {
                                tr {
                                    td { +"${it.firstName} ${it.lastName}" }
                                    parseResult.exercises.forEach { exercise ->
                                        td {
                                            if (it.checkmarks.contains(exercise)) {
                                                +"X"
                                            } else {
                                                +""
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        assignmentTable.tBodies[0]?.clear()
                        exerciseAssignment.forEach {
                            assignmentTable.tBodies[0]?.append {
                                tr {
                                    td {
                                        +it.first
                                    }
                                    td {
                                        +"${it.second.firstName} ${it.second.lastName}"
                                    }
                                }
                            }
                        }
                    }
                    reader.readAsText(file)
                }
            }

        }
}