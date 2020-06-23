package v2.format.clausewitz

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import v2.format.antlr.ClausewitzLexer
import v2.format.antlr.ClausewitzParser
import v2.format.config.Config
import v2.format.config.FormatOptions
import v2.format.getResourceFile
import v2.format.resetConfig
import java.io.File

internal class ClausewitzVisitorTest {
    @Test
    fun `Test visit program`() {
        testDirectory("/countries")
        testDirectory("/decision")
        testDirectory("/default")
        testDirectory("/diplomacy")
        testDirectory("/event")
        testDirectory("/news")
        testDirectory("/oob")
        testDirectory("/poptype")
    }

    @Test
    fun `Test visit assignExpr`() {
        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(tabWidth = 2),
            """level1 = {
level2 = {
level3 = {
level4 = {
}
}
}
}""",
            """level1 = {
  level2 = {
    level3 = {
      level4 = {}
    }
  }
}"""
        )

        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(tabWidth = 2),
            """level1 = {
level2 = {
level3 = {
level4 = {
}


level4 = {
}
}
}
}""",
            """level1 = {
  level2 = {
    level3 = {
      level4 = {}


      level4 = {}
    }
  }
}"""
        )

        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(tabWidth = 2),
            """something =

{
name = some_name
type = some_type


}""",
            """something = {
  name = some_name
  type = some_type
}"""
        )

        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(tabWidth = 2),
            """something =
# some comment here
{
name = some_name
type = some_type


}""",
            """something = {
  name = some_name
  type = some_type
}"""
        )

        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(tabWidth = 2),
            """assignment = {
  nested = {
    x = y
    x2 = y2
  }
}""",
            """assignment = {
  nested = {
    x = y
    x2 = y2
  }
}"""
        )

        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(tabWidth = 2, assignmentSpacing = false),
            """assignment={
  nested={
    x=y
    x2=y2
  }
}""",
            """assignment={
  nested={
    x=y
    x2=y2
  }
}"""
        )

        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(tabWidth = 2),
            """random_owned = {
    limit = { owner = { ai = no } }
    owner = { add_country_modifier = { name = test_modifier duration = -1 } }
}""",
            """random_owned = {
  limit = {
    owner = { ai = no }
  }
  owner = {
    add_country_modifier = {
      name = test_modifier
      duration = -1
    }
  }
}"""
        )
    }

    @Test
    fun `Test visit assignExpr with single line blocks`() {
        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(),
            "x = {\n    x2 = y2\n}",
            "x = { x2 = y2 }"
        )

        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(),
            "x = { x2 = { x3 = y3\n}\n}",
            "x = {\n    x2 = { x3 = y3 }\n}"
        )
    }

    @Test
    fun `Test visit braceExpr with no bracket spacing`() {
        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(bracketSpacing = false),
            "{ x = y }",
            "{x = y}"
        )

        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(bracketSpacing = false),
            "{ 12345 }",
            "{12345}"
        )

        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(bracketSpacing = false, tabWidth = 1),
            "{ x = y x2 = y2}",
            "{\n x = y\n x2 = y2\n}"
        )
    }

    @Test
    fun `Test visit braceExpr with nested expressions`() {
        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(tabWidth = 2, bracketSpacing = false),
            "{{{}}{}}",
            "{\n  {{}}\n  {}\n}"
        )

        val manyNestedInput = """{
  {
    {
    }
    {
      {
      }
      {
      
      }
      {
      
      
      }
      {
      
      
      
      }
    }
  }
  {
  }
}"""
        val manyNestedValue = """{
  {
    {}
    {
      {}
      {}
      {}
      {}
    }
  }
  {}
}"""
        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(tabWidth = 2, bracketSpacing = false),
            manyNestedInput,
            manyNestedValue
        )

        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(tabWidth = 2),
            manyNestedInput,
            manyNestedValue
        )

        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(bracketSpacing = false),
            "{{{{{}}}}}",
            "{{{{{}}}}}"
        )

        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(tabWidth = 100),
            "{{{{{}}}}}",
            "{ { { { {} } } } }"
        )

        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(tabWidth = 1, singleLineBlock = false),
            "{{{{{}}}}}",
            "{\n {\n  {\n   {\n    {\n    }\n   }\n  }\n }\n}"
        )
    }

    @Test
    fun `Test visit braceExpr with no assignment spacing`() {
        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(assignmentSpacing = false),
            "{ x = y }",
            "{ x=y }"
        )

        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(tabWidth = 2, assignmentSpacing = false),
            "{ x = y  x2=y2 x3   = y3}",
            "{\n  x=y\n  x2=y2\n  x3=y3\n}"
        )
    }

    @Test
    fun `Test visit braceExpr with single line expressions`() {
        testInput(ClausewitzParser::braceExpr, FormatOptions(), "{ x = y }", "{ x = y }")

        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(assignmentSpacing = false, bracketSpacing = false),
            "{ x = y }",
            "{x=y}"
        )
    }

    @Test
    fun `Test visit braceExpr with no single line blocks`() {
        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(tabWidth = 2, singleLineBlock = false),
            "{ 10 }",
            "{\n  10\n}"
        )

        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(tabWidth = 1, singleLineBlock = false),
            "{ 10 }",
            "{\n 10\n}"
        )
    }

    @Test
    fun `Test visit braceExpr with bracketWraparound`() {
        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(tabWidth = 2, bracketWraparound = 5),
            "{ 0 1 2 3 4 }",
            "{\n  0 1 2 3 4\n}"
        )

        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(tabWidth = 2, bracketWraparound = 3),
            "{ 0 1 2 3 4 }",
            "{\n  0 1 2\n  3 4\n}"
        )

        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(tabWidth = 2, bracketWraparound = 1),
            "{ 0 1 2 3 4 }",
            "{\n  0\n  1\n  2\n  3\n  4\n}"
        )
    }

    @Test
    fun `Test cosmetic spacing`() {
        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(),
            """i = {
    
        x = y
                
        
        x2 = y2
        
        
        
        x3 = y3
        }""",
            """i = {
    x = y


    x2 = y2



    x3 = y3
}"""
        )

        testInput(
            ClausewitzParser::program,
            FormatOptions(),
            """

x = y
            
x2 = y2
    
            
x3 = y3                 
""",
            """x = y

x2 = y2


x3 = y3
"""
        )
    }

    @Test
    fun `Test single value braceExpr with comments`() {
        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(tabWidth = 1),
            "x = {\n 1 # comment\n}",
            "x = {\n 1 # comment\n}"
        )

        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(tabWidth = 1),
            "x = {\n 1 \n\n\n # comment\n}",
            "x = {\n 1 # comment\n}"
        )

        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(tabWidth = 1),
            "x = {\n y = 1 # comment\n\n\n\n\n\n\n}",
            "x = {\n y = 1 # comment\n}"
        )

        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(tabWidth = 1),
            "x = {\n y = 1 \n # comment y\n\n\n\n\n\n}",
            "x = {\n y = 1\n # comment y\n}"
        )
    }

    @Test
    fun `Test bracketWraparound with comments`() {
        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(tabWidth = 1, bracketWraparound = 5), """x = {
 # comment
 1 2 3 # comment
 4 5 6 7 8 9 10 11 12 13 14 15 # comment
 # comment
}""",
            """x = {
 # comment
 1 2 3 # comment
 4 5 6 7 8
 9 10 11 12 13
 14 15 # comment
 # comment
}"""
        )

        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(tabWidth = 1, bracketWraparound = 5), """x = {
 1 2 3 # comment
 4 5 6 7 8 9 10 11 12 13 14 15 # comment 2
                
}""",
            """x = {
 1 2 3 # comment
 4 5 6 7 8
 9 10 11 12 13
 14 15 # comment 2
}"""
        )

        testInput(
            ClausewitzParser::assignExpr,
            FormatOptions(tabWidth = 1, bracketWraparound = 5), """x = { y = {
 1 2 3 # comment
 4 5 6 7 8 9 10 11 12 13 14 15 # comment 2
                
}}""",
            """x = {
 y = {
  1 2 3 # comment
  4 5 6 7 8
  9 10 11 12 13
  14 15 # comment 2
 }
}"""
        )
    }

    @Test
    fun `Test simple top-level comments`() {
        testInput(ClausewitzParser::program, FormatOptions(), "#comment\n{}", "#comment\n{}\n")

        testInput(ClausewitzParser::program, FormatOptions(), "{#}\n}", "{ #}\n}\n")

        testInput(
            ClausewitzParser::program,
            FormatOptions(), "#    comment\nx = y", "#    comment\nx = y\n"
        )

        testInput(ClausewitzParser::program, FormatOptions(), "x = y # comment", "x = y # comment\n")
    }

    @Test
    fun `Test comments with extraneous newlines`() {
        testInput(ClausewitzParser::program, FormatOptions(), "\n\n\n\n\n\n\n\n", "\n")

        testInput(ClausewitzParser::program, FormatOptions(), "x = y\n\n\n\n\n\n", "x = y\n")

        testInput(ClausewitzParser::program, FormatOptions(), "\n\n\n\n\n\nx = y", "x = y\n")

        testInput(ClausewitzParser::program, FormatOptions(), "{}\n\n\n\n\n\n", "{}\n")

        testInput(ClausewitzParser::program, FormatOptions(), "\n\n\n\n\n\n{}", "{}\n")

        testInput(
            ClausewitzParser::program,
            FormatOptions(), "x = {}\n\n\n\n\n\n", "x = {}\n"
        )

        testInput(
            ClausewitzParser::program,
            FormatOptions(), "\n\n\n\n\n\nx = {}", "x = {}\n"
        )
    }

    @Test
    fun `Test multiple comments`() {
        testInput(
            ClausewitzParser::program,
            FormatOptions(),
            """# comment 1
#    comment 2

#  comment 3
x = y # comment 4
# comment 5""",
            """# comment 1
#    comment 2

#  comment 3
x = y # comment 4
# comment 5
"""
        )

        testInput(
            ClausewitzParser::program,
            FormatOptions(),
            """
                x = y
                #comment
                x2 = y2
                                # comment1
#  comment 2
    x3 = y3#comment 3
                # comment 4
""",
            """x = y
#comment
x2 = y2
# comment1
#  comment 2
x3 = y3 #comment 3
# comment 4
"""
        )

        testInput(
            ClausewitzParser::program,
            FormatOptions(),
            """
                x = y
                
                
                #comment
                x2 = y2
                
                                # comment1
#  comment 2
    x3 = y3#comment 3
    
                # comment 4
""",
            """x = y


#comment
x2 = y2

# comment1
#  comment 2
x3 = y3 #comment 3

# comment 4
"""
        )
    }

    @Test
    fun `Test empty braces`() {
        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(singleLineBlock = false),
            "{}",
            "{\n}"
        )

        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(singleLineBlock = false),
            "{#comment\n}",
            "{ #comment\n}"
        )

        testInput(ClausewitzParser::braceExpr, FormatOptions(), "{}", "{}")

        testInput(ClausewitzParser::braceExpr, FormatOptions(), "{\n\n\n}", "{}")

        testInput(
            ClausewitzParser::braceExpr, FormatOptions(), "{# comment\n\n}", "{ # comment\n\n}"
        )

        testInput(
            ClausewitzParser::braceExpr,
            FormatOptions(), "{\n# comment\n\n}", "{\n    # comment\n\n}"
        )
    }

    @Test
    fun `Test comment indentation`() {
        testInput(
            ClausewitzParser::program,
            FormatOptions(),
            """level1 = {
#comment1
level2 = {
#comment2
level3 = { #comment3
#comment4
}
#comment5
}
}""",
            """level1 = {
    #comment1
    level2 = {
        #comment2
        level3 = { #comment3
            #comment4
        }
        #comment5
    }
}
"""
        )

        testInput(
            ClausewitzParser::program,
            FormatOptions(),
            """type = {
                x = y
                #comment
                x2 = y2
                                # comment1
#  comment 2
    x3 = y3#comment 3
                # comment 4
}""",
            """type = {
    x = y
    #comment
    x2 = y2
    # comment1
    #  comment 2
    x3 = y3 #comment 3
    # comment 4
}
"""
        )

        testInput(
            ClausewitzParser::program,
            FormatOptions(),
            """type = {
    # {{{{{}}}}}
    # { 1 2 3 4 5 6 }
    # ### }}}}
}""",
            """type = {
    # {{{{{}}}}}
    # { 1 2 3 4 5 6 }
    # ### }}}}
}
"""
        )
    }

    private inline fun testInput(
        block: (ClausewitzParser) -> ParserRuleContext,
        options: FormatOptions,
        input: String,
        expected: String
    ) {
        val result = formatInput(block, options, input)
        assertEquals(expected, result)

        val reformat = formatInput(block, options, result)
        assertEquals(result, reformat)
    }

    private inline fun formatInput(
        block: (ClausewitzParser) -> ParserRuleContext,
        options: FormatOptions,
        input: String
    ): String {
        val lexer = ClausewitzLexer(CharStreams.fromString(input))

        val tokenStream = CommonTokenStream(lexer)
        val parser = ClausewitzParser(tokenStream)
        val visitor = ClausewitzVisitor(tokenStream, options)

        val ctx = block(parser)
        val actual = visitor.visit(ctx)

        return actual.toString()
    }

    private fun testDirectory(directory: String) {
        val file = getResourceFile(directory)
        val original = file.resolve("original")

        Config.loadConfig(file.resolve("format.config.json"))

        for (test in file.listFiles()!!) {
            if (test.name !in listOf("original", "format.config.json")) {
                testFile(
                    Config[test.name],
                    original,
                    test
                )
            }
        }

        resetConfig()
    }

    private fun testFile(options: FormatOptions, original: File, test: File) {
        testInput(ClausewitzParser::program, options, original.readText(), test.readText())
    }
}
