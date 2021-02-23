package example
import sangria.macros._

object Main {

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

  }
}
