# data-validation-cli

A example CLI for validating flat files with syrup.

## Building

Builds with [Leiningen](https://leiningen.org/).

Run tests: `lein test`

Build standalone executable JAR: `lein uberjar`

## Running

Download the [JAR](bin/data-validation.jar) and run it with `java -jar`.

To validate a file, run the following command in cmd, Powershell, or some kind of terminal:

<pre>
java -jar data-validation.jar /path/to/file.ext
</pre>
