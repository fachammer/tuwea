package dev.achammer.tuwea.browser

import dev.achammer.tuwea.core.*
import kotlinx.browser.document
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.files.Blob
import org.w3c.files.FileReader
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.useState
import styled.css
import styled.styledTd
import styled.styledTr

val app = functionalComponent<RProps> {
    val (parseResult, setParseResult) = useState<ParseResult?>(null)
    val (presentSet, setPresentSet) = useState<Set<StudentCheckmarksEntry>>(emptySet())
    val (exerciseStudentAssignment, setExerciseStudentAssignment) = useState<ExerciseStudentAssignment?>(null)
    val isStudentPresent = { student: StudentCheckmarksEntry -> presentSet.contains(student) }
    h1 {
        +"tuwea"
    }

    if (parseResult != null) {
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
                    styledTr {
                        css {
                            if (exerciseStudentAssignment != null &&
                                exerciseStudentAssignment.any { it.second == studentCheckmarksEntry }
                            ) {
                                backgroundColor = Color.lightGray
                            }
                        }
                        td { +"${studentCheckmarksEntry.firstName} ${studentCheckmarksEntry.lastName}" }
                        td {
                            input(type = InputType.checkBox) {
                                attrs {
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
                            styledTd {
                                css {
                                    if (exerciseStudentAssignment != null
                                        && exerciseStudentAssignment.any { it.first == exercise && it.second == studentCheckmarksEntry }
                                    ) {
                                        backgroundColor = Color.red
                                    }
                                }

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

        button {
            attrs {
                onClickFunction = {
                    setExerciseStudentAssignment(findExerciseAssignment(parseResult) { isStudentPresent(it) })
                }
            }
            +"assign exercises"
        }

        br {}

        if (exerciseStudentAssignment != null) {
            textArea {
                attrs {
                    rows = parseResult.exercises.size.toString()
                    disabled = true
                    value = exerciseStudentAssignment.joinToString("\n") {
                        "${it.first}: ${it.second.firstName} ${it.second.lastName}"
                    }
                }
            }
        }

        br {}
    }

    input(type = InputType.file) {
        attrs {
            onChangeFunction = { event: dynamic ->
                val file = event.target?.files[0] as Blob
                val reader = FileReader()
                reader.onload = { it: dynamic ->
                    val content = it.target.result as String
                    setParseResult(parseCsvContent(tuwelParseConfiguration, content))
                    setExerciseStudentAssignment(null)
                    setPresentSet(emptySet())
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