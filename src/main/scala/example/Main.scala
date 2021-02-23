package example
import sangria.macros._
import scala.concurrent.Future
import io.circe.Json
import sangria.execution._
import sangria.marshalling.circe._
import sangria.schema._
import sangria.macros.derive._

trait Identifiable {
  def id: String
}

case class Picture(width: Int, height: Int, url: Option[String])

case class Product(id: String, name: String, description: String)
    extends Identifiable {
  def picture(size: Int): Picture =
    Picture(width = size, height = size, url = Some(s"//cdh.com$size/$id.jpg"))
}

class ProductRepo {
  private val Products = List(
    Product("1", "Cheesecake", "Tasty"),
    Product("2", "Health Potion", "+50 HP")
  )

  def product(id: String): Option[Product] =
    Products find (_.id == id)

  def products: List[Product] = Products
}

object Main {

  val Id = Argument("id", StringType)
  implicit val PictureType =
    deriveObjectType[Unit, Picture](
      ObjectTypeDescription("The product picture"),
      DocumentField("url", "Picture CDN URL")
    )
  val IdentifiableType = InterfaceType(
    "Identifiable",
    "Entity that can be identified",
    fields[Unit, Identifiable](Field("id", StringType, resolve = _.value.id))
  )
  val ProductType =
    deriveObjectType[Unit, Product](
      Interfaces(IdentifiableType),
      IncludeMethods("picture")
    )

  val QueryType = ObjectType(
    "Query",
    fields[ProductRepo, Unit](
      Field(
        "product",
        OptionType(ProductType),
        description = Some("Returns a product with specific `id`."),
        arguments = Id :: Nil,
        resolve = c ⇒ c.ctx.product(c arg Id)
      ),
      Field(
        "products",
        ListType(ProductType),
        description = Some("Returns a list of all available products."),
        resolve = _.ctx.products
      )
    )
  )

  def main(args: Array[String]): Unit = {
    val query = graphql"""
    query MyProduct {
      product(id: "2") {
        name
        description

        picture(size: 500) {
          width, height, url
        }
      }

      products {
        name
      }
    }
  """
    val schema = Schema(QueryType)

    implicit val ec: scala.concurrent.ExecutionContext =
      scala.concurrent.ExecutionContext.global
    val result: Future[Json] =
      Executor.execute(schema, query, new ProductRepo)

  }
}
