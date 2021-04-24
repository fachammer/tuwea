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
    val (database, setDatabase) = useState(Database(emptySet()))
    val (presentSet, setPresentSet) = useState<Set<Student>>(emptySet())
    val (exerciseStudentAssignment, setExerciseStudentAssignment) = useState<List<Pair<Exercise, Student>>?>(null)
    val isStudentPresent = { student: Student -> presentSet.contains(student) }

    val exercises = database.exercises

    h1 {
        +"tuwea"
    }

    table {
        thead {
            tr {
                th { +"name" }
                th { +"present" }
                exercises.forEach { e ->
                    th { +e.name }
                }
            }
        }
        tbody {
            database.facts.checkmarkMap().map { entry ->
                val student = entry.key
                styledTr {
                    css {
                        if (exerciseStudentAssignment != null &&
                            exerciseStudentAssignment.any { it.second == student }
                        ) {
                            backgroundColor = Color.lightGray
                        }
                    }
                    td { +"${student.firstName} ${student.lastName}" }
                    td {
                        input(type = InputType.checkBox) {
                            attrs {
                                checked = isStudentPresent(student)
                                onChangeFunction = {
                                    if (isStudentPresent(student)) {
                                        setPresentSet(presentSet - student)
                                    } else {
                                        setPresentSet(presentSet + student)
                                    }
                                }
                            }
                        }
                    }
                    exercises.forEach { exercise ->
                        styledTd {
                            css {
                                if (exerciseStudentAssignment != null
                                    && exerciseStudentAssignment.any { it.first == exercise && it.second == student }
                                ) {
                                    backgroundColor = Color.red
                                }
                            }

                            if (entry.value[exercise]!!.solved) {
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
                setExerciseStudentAssignment(findRandomExerciseAssignment(database) { isStudentPresent(it) })
            }
        }
        +"assign exercises"
    }

    br {}

    if (exerciseStudentAssignment != null) {
        textArea {
            attrs {
                rows = database.exercises.size.toString()
                disabled = true
                value = exerciseStudentAssignment.joinToString("\n") {
                    "${it.first}: ${it.second.firstName} ${it.second.lastName}"
                }
            }
        }
    }

    br {}

    input(type = InputType.file) {
        attrs {
            onChangeFunction = { event: dynamic ->
                val file = event.target?.files[0] as Blob
                val reader = FileReader()
                reader.onload = { it: dynamic ->
                    val content = it.target.result as String
                    val parseResult = parseCsvContent(tuwelParseConfiguration, content)
                    setDatabase(importParseResult(database, parseResult))
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