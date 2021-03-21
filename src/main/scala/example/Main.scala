import sangria.ast.Document
import sangria.parser.QueryParser
import sangria.renderer.QueryRenderer

import scala.util.Success
import scala.util.Failure

object Main {
  val query =
    """
      query FetchLukeAndLeiaAliased(
            $someVar: Int = 1.23
            $anotherVar: Int = 123) @include(if: true) {
        luke: human(id: "1000")@include(if: true){
          friends(sort: NAME)
        }

        leia: human(id: "10103\n ö") {
          name
        }

        ... on User {
          birth{day}
        }

        ...Foo
      }

      fragment Foo on User @foo(bar: 1) {
        baz
      }
    """
  
    def main(args: Array[String]):Unit = {
      // Parse GraphQL query
      QueryParser.parse(query) match {
        case Success(document) =>
          // Pretty rendering of the GraphQL query as a `String`
          println(document.renderPretty)
          
        case Failure(error) =>
          println(s"Syntax error: ${error.getMessage}")
      }
    }
}
