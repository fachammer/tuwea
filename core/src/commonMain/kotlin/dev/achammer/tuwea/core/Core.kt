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

typealias EntityId = Int

sealed class Attribute
data class StudentFirstName(val value: String) : Attribute()
data class StudentLastName(val value: String) : Attribute()
data class StudentIdNumber(val value: String) : Attribute()
data class ExerciseName(val value: String) : Attribute()
data class StudentIsPresent(val value: Boolean) : Attribute()
data class CheckmarkStudent(val value: EntityId) : Attribute()
data class CheckmarkExercise(val value: EntityId) : Attribute()
data class CheckmarkSolved(val value: Boolean) : Attribute()

enum class Operation {
    Assertion,
    Retraction
}

data class Fact(val entityId: EntityId, val attribute: Attribute, val transactionId: EntityId, val operation: Operation)

inline class Database(val facts: Set<Fact>) {
    fun nextTransactionId(): Int = facts.maxOf { it.transactionId } + 1
}

data class Student(val idNumber: String, val firstName: String, val lastName: String)
data class Exercise(val name: String)
data class Checkmark(val student: Student, val exercise: Exercise, val solved: Boolean)

fun Set<Fact>.latest() =
    this.groupBy { Pair(it.entityId, it.attribute::class) }
        .flatMap {
            val entityAttributeFacts = it.value
            val transactionId = entityAttributeFacts.maxOf { fact -> fact.transactionId }
            val latestFact = entityAttributeFacts.last { fact -> fact.transactionId == transactionId }
            if (latestFact.operation == Operation.Assertion)
                setOf(latestFact)
            else
                emptySet()
        }
        .toSet()

class Selector<T> {
    var value: T? = null
    fun select(value: T) {
        this.value = value
    }
}

fun <T> Set<Fact>.entityAttribute(entityId: EntityId, select: Selector<T>.(Attribute) -> Unit): T? where T : Attribute {
    return this.latest().groupBy { it.entityId }[entityId]?.flatMap {
        val selector = Selector<T>()
        select(selector, it.attribute)
        when (val value = selector.value) {
            null -> emptyList()
            else -> listOf(value)
        }
    }?.firstOrNull()
}

fun Set<Fact>.asEntities(): Map<EntityId, Set<Fact>> = this.groupBy { it.entityId }.mapValues { it.value.toSet() }

fun Set<Fact>.student(entityId: EntityId): Student? = this.asEntities()[entityId]?.students()?.firstOrNull()
fun Set<Fact>.exercise(entityId: EntityId): Exercise? = this.asEntities()[entityId]?.exercises()?.firstOrNull()

fun Set<Fact>.students(): Set<Student> =
    this.latest().asEntities().flatMap {
        val idNumberAttribute =
            it.value.entityAttribute<StudentIdNumber>(it.key) { a -> if (a is StudentIdNumber) select(a) }
        val firstNameAttribute =
            it.value.entityAttribute<StudentFirstName>(it.key) { a -> if (a is StudentFirstName) select(a) }
        val lastNameAttribute =
            it.value.entityAttribute<StudentLastName>(it.key) { a -> if (a is StudentLastName) select(a) }

        if (idNumberAttribute != null && firstNameAttribute != null && lastNameAttribute != null) {
            listOf(Student(idNumberAttribute.value, firstNameAttribute.value, lastNameAttribute.value))
        } else {
            emptyList()
        }
    }.toSet()

fun Set<Fact>.exercises(): Set<Exercise> =
    this.latest().asEntities().flatMap {
        val nameAttribute = it.value.entityAttribute<ExerciseName>(it.key) { a -> if (a is ExerciseName) select(a) }

        if (nameAttribute != null) {
            listOf(Exercise(nameAttribute.value))
        } else {
            emptyList()
        }
    }.toSet()


fun Set<Fact>.checkmarks(): Set<Checkmark> =
    this.latest().asEntities().flatMap {
        val checkmarkStudentAttribute =
            it.value.entityAttribute<CheckmarkStudent>(it.key) { a -> if (a is CheckmarkStudent) select(a) }
        val checkmarkExerciseAttribute =
            it.value.entityAttribute<CheckmarkExercise>(it.key) { a -> if (a is CheckmarkExercise) select(a) }
        val checkmarkSolvedAttribute =
            it.value.entityAttribute<CheckmarkSolved>(it.key) { a -> if (a is CheckmarkSolved) select(a) }

        if (checkmarkStudentAttribute != null && checkmarkExerciseAttribute != null && checkmarkSolvedAttribute != null) {
            val student = this.student(checkmarkStudentAttribute.value)
            val exercise = this.exercise(checkmarkExerciseAttribute.value)
            if (student != null && exercise != null) {
                listOf(Checkmark(student, exercise, checkmarkSolvedAttribute.value))
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }.toSet()

fun Set<Fact>.checkmarkMap(): Map<Student, Map<Exercise, Checkmark>> =
    this.checkmarks().groupBy { it.student }.mapValues {
        it.value.groupBy { c -> c.exercise }.mapValues { cs -> cs.value.first() }
    }

val Database.latestFacts: Set<Fact>
    get() = this.facts.latest()

val Database.entities: Map<EntityId, Set<Fact>>
    get() = this.latestFacts.groupBy { it.entityId }.mapValues { it.value.toSet() }

val Database.exercises
    get() = this.latestFacts.exercises()

class EntityHelper(private val entityId: EntityId, private val transactionId: EntityId) {
    var facts: MutableSet<Fact> = mutableSetOf()
    fun assertAttribute(attribute: Attribute) {
        facts.add(Fact(entityId, attribute, transactionId, Operation.Assertion))
    }

    fun retractAttribute(attribute: Attribute) {
        facts.add(Fact(entityId, attribute, transactionId, Operation.Retraction))
    }
}

class TransactionHelper(private val transactionId: Int, private val nextEntityId: () -> EntityId) {
    var facts: MutableSet<Fact> = mutableSetOf()
    fun addNewEntity(body: EntityHelper.() -> Unit): Int {
        // TODO: allow temporary entity ids
        val entityId = nextEntityId()
        val helper = EntityHelper(entityId, transactionId)
        body(helper)
        facts.addAll(helper.facts)
        return entityId
    }
}

fun counter(initial: Int): () -> Int {
    var count = initial
    return {
        count += 1
        count
    }
}

typealias Transaction = Set<Fact>

fun transaction(database: Database, body: TransactionHelper.() -> Unit): Transaction {
    val helper = TransactionHelper(database.nextTransactionId()) { 0 }
    body(TransactionHelper(database.nextTransactionId(), counter(0)))
    return helper.facts
}

fun transact(database: Database, transaction: Transaction): Database = Database(database.facts + transaction)
fun transact(database: Database, body: TransactionHelper.() -> Unit): Database =
    transact(database, transaction(database, body))

data class ParseResult(val exercises: List<String>, val studentCheckmarksEntries: List<StudentCheckmarksEntry>)
typealias ExerciseStudentAssignment = List<Pair<String, StudentCheckmarksEntry>>

fun importParseResult(database: Database, parseResult: ParseResult): Database {
    Database(database.facts + Fact(1, StudentFirstName("xyz"), 0, Operation.Assertion))

    return transact(database) {
        val exerciseNameToId = parseResult.exercises.associateWith {
            addNewEntity {
                assertAttribute(ExerciseName(it))
            }
        }

        parseResult.studentCheckmarksEntries.forEach { entry ->
            val studentId = addNewEntity {
                assertAttribute(StudentIdNumber(entry.idNumber))
                assertAttribute(StudentFirstName(entry.firstName))
                assertAttribute(StudentLastName(entry.lastName))
            }

            exerciseNameToId.forEach {
                addNewEntity {
                    assertAttribute(CheckmarkExercise(it.value))
                    assertAttribute(CheckmarkStudent(studentId))
                    assertAttribute(CheckmarkSolved(entry.checkmarks.contains(it.key)))
                }
            }
        }
    }
}


// append-only
// end2end encrypted
// history view
// undo is an action (also append-only)

fun findRandomExerciseAssignment(
    database: Database,
    isStudentPresent: (Student) -> Boolean
): List<Pair<Exercise, Student>> {
    val checkmarkMap = database.facts.checkmarkMap()
    return database.exercises
        .sortedBy { exercise -> checkmarkMap.count { it.value[exercise]!!.solved } }
        .fold(emptyList()) { chosenAssignments, exercise ->
            val potentialEntries = checkmarkMap
                .filter { it.value[exercise]!!.solved }
                .filterNot { entry -> chosenAssignments.firstOrNull { it.second == entry.key } != null }
            if (potentialEntries.isEmpty()) {
                chosenAssignments
            } else {
                chosenAssignments + Pair(
                    exercise,
                    potentialEntries.keys.toTypedArray()[Random.nextInt(potentialEntries.size)]
                )
            }
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