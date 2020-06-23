# V2 Format
A program formatter for Victoria 2

## Usage
Download `v2format.jar` from the latest release in the release tab.

A default configuration is provided as well. 
It's recommended that formatting is only done in repositories in the chance that a reversion is desired.

```
$ java -jar v2format.jar

$ java -jar v2format.jar --help

$ java -jar v2format.jar --config path/to/config

$ java -jar v2format.jar --folder path/to/mod

$ java -jar v2format.jar --file path/to/file

$ java -jar v2format.jar --file path/to/file --config path/to/config

$ java -jar v2format.jar --folder path/to/mod --file path/to/file

$ java -jar v2format.jar --folder path/to/mod --config path/to/config

$ java -jar v2format.jar --folder path/to/mod --file path/to/file --config path/to/config
```

### `--folder`

The `--folder` argument is by default the directory from which `v2format.jar` was executed, 
also known as the current directory.
The program will then search for a single `.mod` file and open up the mod based off the file results.
If no `.mod` file is found or multiple are found, the program exits. 
In those cases, `--folder` should be used.

### `--file`

The `--file` argument is an optional argument and has no default value.
If the argument is provided, the program will still search for a `.mod` file and configuration in the same directory.
The `--folder` argument still applies to defining the mod and configuration.
If no `.mod` file is found, the program exits.
If the file passed in as an argument cannot be formatted by way of its extension or exclusion configuration, the program will exit.

Note that it is possible to use the `--folder` argument to indicate the mod to format while passing in an argument
to `--file` that is in a different directory. 
It is important to know that excluding files is based off the relative path from the specified mod directory and not some pattern.

### `--config`

The `--config` argument is by default the file `v2format.config.json`.
If the file doesn't exist, the default configuration will be used for all files.
If the argument is incorrect, the program will exit.
It is recommended to create a `v2format.config.json` file since some files will need different configuration. 

### Configuration

Without any external configuration, the style used is:

```json
{
  "paths": {
    "/": {
      "tabWidth": 4,
      "bracketSpacing": true,
      "assignmentSpacing": true,
      "singleLineBlock": true,
      "bracketWraparound": 10
    }
  },
  "excludeFiles": []
}
```

### Paths

#### Terminology

An `expression` is either an assignment or a braced expression.

An `assignment` looks can look like either `key = value` or `key = { ...values }`.

A `braced expression` is the latter form of an assignment.
It can also be the `{ ... values }` part of `key = { { ...values } }`,
however this use case is noticeably only used in `map/positions.txt` and can be ignored.

A string is a quoted section of text that looks like `"text"`.

An identifier is a unquoted contiguous piece of text that can look like `identifier`.
It can contain digits in it, but cannot be completely composed of digits, otherwise it would be a number.

#### Keys

There are five rules that can be configured for each path.

##### tabWidth

The rule `tabWidth` defines the number of spaces per level of indentation.

Default value: `4`

Example:
```
block = {
inner = {
x = {}
}
}
```

evaluates into

```
block = {
    inner = {
        x = {}
    }
}
```

##### bracketSpacing

The rule `bracketSpacing` if true, places a space between brackets if they are a single line expression.
If false, no spaces will be put in.

Default value: `true`

Example:

**true:**
```
block = { key = value }
```

**false:**
```
block = {key = value}
```

##### assignmentSpacing

The rule `assignmentSpacing` if true, places a single space between all equal signs `=`.
If false, no spaces will be put in.

Default value: `true`

Example:

**true:**
```
block = {
    inner = { key = value }
}
```

**false:**
```
block={
    inner={ key=value }
}
```

##### singleLineBlock

The rule `singleLineBlock` if true, allows for braced expressions with one inner element to be placed
on one line. This only applies if the inner element is not another braced expression.
If false, all braced expressions will be put on multiple lines.

Default value: `true`

Example:

**true:**
```
block = {
    inner = { key = value }

    inner = {}
}
```

**false:**
```
block = {
    inner = { 
        key = value
    }

    inner = {
    }
}
```

##### bracketWraparound

The rule `bracketWraparound` defines the maximum number of constant values that will be placed on a single line.
If there exists an overflow, the values will continue into the next line.
A value of `1` is recommended for files that contain braced expressions such as those in the `/news` folder.

Default value: `10`

Example:
```
block = { 1 2 3 4 5 }
block = { 1 2 3 4 5 6 7 8 9 10 }
block = { 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 }
```

evaluates into

```
block = {
    1 2 3 4 5
}
block = {
    1 2 3 4 5 6 7 8 9 10
}
block = {
    1 2 3 4 5 6 7 8 9 10
    11 12 13 14 15
}
```

---

The key for each path is the relative path description from the base mod directory to the target path. 

This means that given the mod `Mod` and the path `/map`, the configuration will apply to all files
in directory `Mod/map`.

---

Paths follow a best-fit approach, which allows for rules to be layered on a directory basis:

```json
{
  "paths": {
    "/": {
      "tabWidth": 4,
      "assignmentSpacing": false
    },
    "/map": {
      "tabWidth": 2
    }
  }
}
```

This means that all files that are in `/map` will be indented with two spaces rather than four.
However, the rule `assignmentSpacing` will still apply.
To override it, a negating entry must be put into `/map`.

---

If there are two identical keys defined for one path, the last defined entry is what is chosen.
However, the ordering of paths won't otherwise matter.

```json
{
  "paths": {
    "/map": {
      "tabWidth": 2
    },
    "/": {
      "tabWidth": 4,
      "assignmentSpacing": false
    }
  }
}
```

This will evaluate identically to the above section.

---

Paths can omit the leading slash `/` at the start of every path such that `/map` and `map` as keys are identical.
Furthermore, the usage of Windows backslashes `\\` is supported, but not recommended.
Placing extra slashes such as `map///terrain` is undefined behavior and should be avoided.

---

### Exclude Files

Files can be purposefully excluded which will lead to them not being formatted.
This is best fit for files that can be modified by external programs or by choice.
A recommended exclusion is `map/positions.txt` since it tends to be modified programmatically.

The entry `excludeFiles` in the json file can be omitted if there is nothing to exclude.

### Formatting Semantics

* If a file cannot be successfully parsed due to syntax errors, the error will be reported and the file skipped.
* Most contextual spacing between expressions is preserved.
What is not preserved is the spacing between the opening brace of an expression and the first value, 
and the last value and closing brace.

  Example:
  ```
  block = {

      x = y


      inner = {}

  }
  ```
  
  evaluates into
  
  ```
  block = {
      x = y 

      inner = {}
  }
  ```
  
* Most comments are preserved.
Comments that will be lost are those that make an assignment expression difficult to use.

  Example:
  ```
  block # comment
  = # comment
  { # comment
  }
  ```
  
  evaluates into
  
  ```
  block = { # comment
  }
  ```

  If any of these comments exist before formatting and are highly desired, 
  move them somewhere else otherwise they will be deleted.
  
* Comments will be repositioned if necessary.
If a comment is on the same line as an expression, it will stay on that line, but have one space separating it.
If a comment is not on the same line as an expression, it will match the block's level of indentation.

  Example:
  ```
  block = { # comment
  # comment
    #comment
      #comment
        #comment
          # comment
  }
  ```
  
  evaluates into
  
  ```
  block = { # comment
      # comment
      #comment
      #comment
      #comment
      # comment
  }
  ```
  
  The exact text of a comment will not change,
  but blocks that may be more aesthetic without comment indentation will be still be formatted.

* Expressions such as `color = { 10 20 30 }` will currently format into:

  ```
  color = {
      10 20 30
  }
  ```
  
  If that is undesired behavior, submit an issue to the repository.

## Building
The project uses gradle as the build system.

### Windows
`.\gradlew build` to build.

`.\gradlew jar` to build a jar in `build\libs\v2format.jar`.

### macOS/Unix
`./gradlew build` to build.

`./gradlew jar` to build a jar in `build/libs/v2format.jar`.