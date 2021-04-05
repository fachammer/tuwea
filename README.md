# tuwea

*tuwea* stands for TU Wien exercise assistant and is a tool that assists tutors at TU Wien when teaching exercise groups

## features

given a csv file of a checkmarks exercise from TUWEL, tuwea computes a possible list of candidates to present the given
exercises

## installation

### from pre-built release

download the current release from github, extract it to a directory of your choice. to use tuwea easily from the command
line it is recommended to add the `bin` directory to your `PATH`

### from source

clone the repository and build a distribution

```
git clone https://github.com/fachammer/tuwea
cd tuwea
./gradlew installDist
```

then copy the `tuwea` directory inside `./build/install` to a directory of your choice. to use tuwea easily from the
command line it is recommended to add the `bin` directory to your `PATH`

## usage

given a csv file of the form (as found usually when exporting from TUWEL):

```
Kurs:;[course name]
Abgabebeginn:;[start date of submission]
Export;Alle
Aufgabe:;[exercise name]
Abgabeende:;[end date of submission]
Gruppen:;[group number]
Nachname;Vorname;ID-Nummer;[columns for all exercise names];Kreuzerl;Bewertung;Kommentar;Unterschrift
[last name of student 1];[first name of student 1];[id number of student 1];[X for all exercises that a student 1 marked, empty for all exercises that the student 2 didn't mark];[number of marked exercises];[evaluation of marked exercises];[comment];
[last name of student 2];[first name of student 2];[id number of student 2];[X for all exercises that a student 2 marked, empty for all exercises that the student 2 didn't mark];[number of marked exercises];[evaluation of marked exercises];[comment];
...
```

running the command

```
tuwea [path to TUWEL csv file]
```

outputs a list of possible candidates for exercises to the console:

```
[exercise 1 name]: [name of student assigned for exercise 1]
[exercise 2 name]: [name of student assigned for exercise 2]
...
```

for now the list of exercises is sorted by the number of students that marked the exercise as solved in ascending order