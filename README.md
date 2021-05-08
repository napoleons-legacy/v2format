# V2 Format

A program formatter for Victoria 2.

## Usage

Download `v2format.jar` from the latest release in the releases section.

A default configuration is provided as well. It's recommended that formatting is only done in repositories to control
reversions.

```
$ java -jar v2format.jar

$ java -jar v2format.jar --help

$ java -jar v2format.jar --mod path/to/mod

$ java -jar v2format.jar --mod path/to/mod --file path/to/mod/file

$ java -jar v2format.jar --config path/to/config

$ java -jar v2format.jar --mod path/to/mod --config path/to/config

$ java -jar v2format.jar --mod path/to/mod --file path/to/mod/file --config path/to/config
```

### `--mod`

The `--mod` argument specifies the directory to format, bypassing `.mod` file reading. The program searches for a
single `.mod` file in the current directory, acquiring the mod directory from it.

If there is no `.mod` file or multiple exist, the program exits due to not knowing which mod to format. In those
cases, `--mod` should be used to specify which mod should be formatted.

### `--file`

The `--file` argument is an optional argument and has no default value. The `--mod` argument must be provided in order
to use configurations. It allows either files or directories to be formatted specifically without the rest of the mod
being formatted with it.

If the file passed in as an argument cannot be formatted by way of its extension, exclusion configuration, or read/write
property, the program will exit.

Note: it is possible to use the `--mod` argument to indicate the mod to format while passing in an argument to `--file`
that is in a different directory. Excluding files is relative to the mod directory path, and not absolute.

### `--config`

The `--config` argument is by default the file `v2format.config.json`. If the file doesn't exist, the default
configuration will be used for all files. If the argument is incorrect, the program will exit. It is recommended to
create a `v2format.config.json` file since some files will need different configuration.

### Configuration

Without any external configuration, the style used is:

```json
{
  "paths": {
    "/": {
      "useTab": false,
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

The ordering of each rule does not matter.

### Paths

#### Terminology

An `expression` is either an assignment, or a braced expression.

An `assignment` looks can look like either `key = value` or `key = { ...values }`.

A `braced expression` is the latter form of an assignment. It can also be the `{ ... values }` part
of `key = { { ...values } }`, however this use case only has usage in `map/positions.txt` and can be ignored.

A string is a quoted section of text that looks like `"text"`.

An identifier is a unquoted contiguous piece of text that can look like `identifier`. It can contain digits in it, but
cannot be solely composed of digits, otherwise it would be a number.

#### Keys

There are six rules that can be configured for each path.

##### useTab

The rule `useTab` if true, utilizes the tab character for indentation. If false, spaces will be used in its place with
the number of spaces comprising one tab defined by the `tabWidth` rule.

Default value: `false`

Example:

**true:**

```
block = {
....inner = {
........x = {}
....}
}
```

Each `.` is one space.

**false:**

```
block = {
————inner = {
————————x = {}
————}
}
```

Each grouping of four `—` is one tab.

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

The rule `bracketSpacing` if true, places a space between brackets if they are a single line expression. If false, no
spaces will be put in.

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

The rule `assignmentSpacing` if true, places a single space between all equal signs `=`. If false, no spaces will be put
in.

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

The rule `singleLineBlock` if true, allows for braced expressions with one inner element to be placed on one line. This
only applies if the inner element is not another braced expression. If false, all braced expressions will be put on
multiple lines.

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

The rule `bracketWraparound` defines the maximum number of constant values that will be placed on a single line. If
there exists an overflow, the values will continue into the next line. A value of `1` is recommended for files that
contain braced expressions such as those in the `/news` folder.

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

This means that given the mod `Mod` and the path `/map`, the configuration will apply to all files in
directory `Mod/map`.

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

This means that all files that are in `/map` will be indented with two spaces rather than four. However, the
rule `assignmentSpacing` will still apply. To override it, a negating entry must be put into `/map`.

---

If there are two identical keys defined for one path, the latest defined entry is chosen. The ordering of paths won't
otherwise matter.

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
Furthermore, the usage of Windows backslashes `\\` is supported, but not recommended. Placing extra slashes such
as `map///\\terrain` will work as if the path was `map/terrain`.

---

### Exclude Files

Files can be purposefully excluded which will lead to them not being formatted. This is best fit for files that can be
modified by external programs or by choice. A recommended exclusion is `map/positions.txt` since it tends to be modified
programmatically.

The entry `excludeFiles` in the json file can be omitted if there is nothing to exclude.

### Formatting Semantics

* If a file cannot be successfully parsed due to syntax errors, the error will be reported and the file skipped.
* Most contextual spacing between expressions is preserved. What is not preserved is the spacing between the opening
  brace of an expression and the first value, and the last value and closing brace.

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

* Most comments are preserved. Comments that will be lost are those that make an assignment expression difficult to use.

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

  If any of these comments exist before formatting and are highly desired, move them somewhere else otherwise they will
  be deleted.

* Comments will be repositioned if necessary. If a comment is on the same line as an expression, it will stay on that
  line, but have one space separating it. If a comment is not on the same line as an expression, it will match the
  block's level of indentation.

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

  The exact text of a comment will not change, but blocks that may be more aesthetic without comment indentation will be
  still be formatted.

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