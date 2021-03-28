import sangria.ast.Document
import sangria.parser.QueryParser
import sangria.renderer.QueryRenderer
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import sangria.macros._
import scala.concurrent.Await
import sangria.schema._
import sangria.execution._
import sangria.marshalling.circe._
import io.circe.Json
import scala.concurrent.duration.Duration

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
    println(schema.renderPretty) // Though the above Schema took only the QueryType argument, it renders `type Author {name: String!}` as well

    val query = 
      graphql"""
        query MyAuthor {
          authors {
            name
          }
        } 
      """
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global 
    val result: Future[Json] = Executor.execute(schema, query, new AuthorRepo)
    println(Await.result(result, Duration(10, "seconds")))
  }
}
