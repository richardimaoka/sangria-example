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

class AuthorRepo {
  private val authors = List(Author("Sydney"),Author("Ernst"))
  def author(name: String) : Option[Author] = authors.find(_.name == name)
  def products: List[Author] = authors
}

object Main {
  val AuthorType = ObjectType(
    "Author", 
    "An author of a book",
    fields[Unit, Author](
      Field("name", StringType, resolve = _.value.name)
    )
  )
  val BookType = ObjectType(
    "Book", 
    "A book",
    fields[Unit, Book](
      Field("title", StringType, resolve = _.value.title)
    )
  )

  val QueryType = ObjectType("Query", fields[AuthorRepo, Unit](
    Field("authors", ListType(AuthorType),
    description = Some("Returns a list of all available authors."),
    resolve = _.ctx.products)))

  def main(args: Array[String]): Unit = {
    val schema = Schema(QueryType)
  }
}
