import sangria.ast.Document
import sangria.parser.QueryParser
import sangria.renderer.QueryRenderer
import scala.util.Success
import scala.util.Failure
import sangria.macros._
import sangria.schema._

case class Author (
  name: String
)

case class Book (
  title: String
)

object Main {
  val Author = ObjectType(
    "Author", 
    "An author of a book",
    fields[Unit, Author](
      Field("name", StringType, resolve = _.value.name)
    )
  )
  val Book = ObjectType(
    "Book", 
    "A book",
    fields[Unit, Book](
      Field("title", StringType, resolve = _.value.title)
    )
  )

  def main(args: Array[String]): Unit = {
  }
}
