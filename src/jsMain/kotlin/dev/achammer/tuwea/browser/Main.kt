package dev.achammer.tuwea.browser

import dev.achammer.tuwea.core.*
import kotlinx.browser.document
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import org.w3c.files.Blob
import org.w3c.files.FileReader
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.useState

val app = functionalComponent<RProps> {
    val (parseResult, setParseResult) = useState<ParseResult?>(null)
    val (presentSet, setPresentSet) = useState<Set<StudentCheckmarksEntry>>(emptySet())
    val isStudentPresent = { student: StudentCheckmarksEntry -> presentSet.contains(student) }
    h1 {
        +"tuwea"
    }

    if (parseResult != null) {
        val exerciseStudentAssignment = findExerciseAssignment(parseResult) { isStudentPresent(it) }
        table {
            thead {
                tr {
                    th { +"exercise" }
                    th { +"student" }
                }
            }
            tbody {
                exerciseStudentAssignment.forEach { (exercise, student) ->
                    tr {
                        td { +exercise }
                        td { +"${student.firstName} ${student.lastName}" }
                    }
                }
            }
        }
        table {
            thead {
                tr {
                    th { +"name" }
                    th { +"present" }
                    parseResult.exercises.forEach { exercise ->
                        th { +exercise }
                    }
                }
            }
            tbody {
                parseResult.studentCheckmarksEntries.forEach { studentCheckmarksEntry ->
                    tr {
                        td { +"${studentCheckmarksEntry.firstName} ${studentCheckmarksEntry.lastName}" }
                        td {
                            input(type = InputType.checkBox) {
                                attrs {
                                    id = studentCheckmarksEntry.idNumber
                                    checked = isStudentPresent(studentCheckmarksEntry)
                                    onChangeFunction = { event: dynamic ->
                                        if (isStudentPresent(studentCheckmarksEntry)) {
                                            setPresentSet(presentSet - studentCheckmarksEntry)
                                        } else {
                                            setPresentSet(presentSet + studentCheckmarksEntry)
                                        }
                                    }
                                }
                            }
                        }
                        parseResult.exercises.forEach { exercise ->
                            td {
                                if (studentCheckmarksEntry.checkmarks.contains(exercise)) {
                                    +"X"
                                } else {
                                    +""
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    input(type = InputType.file) {
        attrs {
            onChangeFunction = { event: dynamic ->
                val file = event.target?.files[0] as Blob
                val reader = FileReader()
                reader.onload = { it: dynamic ->
                    val content = it.target.result as String
                    setParseResult(parseCsvContent(tuwelParseConfiguration, content))
                }
                reader.readAsText(file)
            }
        }
    }
}

fun main() {
    render(document.getElementById("app")) {
        child(app)
    }
}