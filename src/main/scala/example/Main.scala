package example

import sangria.ast.Document
import sangria.parser.QueryParser
import sangria.renderer.QueryRenderer
import sangria.macros._
import sangria.schema._
import scala.util.Success
import scala.util.Failure
import sangria.execution.deferred.{Fetcher, HasId}
import scala.concurrent.Future
object Episode extends Enumeration {
  val NEWHOPE, EMPIRE, JEDI = Value
}
trait Character {
  def id: String
  def name: Option[String]
  def friends: List[String]
  def appearsIn: List[Episode.Value]
}

case class Human(
    id: String,
    name: Option[String],
    friends: List[String],
    appearsIn: List[Episode.Value],
    homePlanet: Option[String]
) extends Character

case class Droid(
    id: String,
    name: Option[String],
    friends: List[String],
    appearsIn: List[Episode.Value],
    primaryFunction: Option[String]
) extends Character

class CharacterRepo {
  import CharacterRepo._

  def getHero(episode: Option[Episode.Value]) =
    episode flatMap (_ => getHuman("1000")) getOrElse droids.last

  def getHuman(id: String): Option[Human] = humans.find(c => c.id == id)

  def getDroid(id: String): Option[Droid] = droids.find(c => c.id == id)

  def getHumans(limit: Int, offset: Int): List[Human] =
    humans.drop(offset).take(limit)

  def getDroids(limit: Int, offset: Int): List[Droid] =
    droids.drop(offset).take(limit)
}
object CharacterRepo {
  val humans = List(
    Human(
      id = "1000",
      name = Some("Luke Skywalker"),
      friends = List("1002", "1003", "2000", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = Some("Tatooine")
    ),
    Human(
      id = "1001",
      name = Some("Darth Vader"),
      friends = List("1004"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = Some("Tatooine")
    ),
    Human(
      id = "1002",
      name = Some("Han Solo"),
      friends = List("1000", "1003", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = None
    ),
    Human(
      id = "1003",
      name = Some("Leia Organa"),
      friends = List("1000", "1002", "2000", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = Some("Alderaan")
    ),
    Human(
      id = "1004",
      name = Some("Wilhuff Tarkin"),
      friends = List("1001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = None
    )
  )

  val droids = List(
    Droid(
      id = "2000",
      name = Some("C-3PO"),
      friends = List("1000", "1002", "1003", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      primaryFunction = Some("Protocol")
    ),
    Droid(
      id = "2001",
      name = Some("R2-D2"),
      friends = List("1000", "1002", "1003"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      primaryFunction = Some("Astromech")
    )
  )
}

object Main {
// Parse GraphQL query
  def main(args: Array[String]): Unit = {
    val characters = Fetcher.caching((ctx: CharacterRepo, ids: Seq[String]) =>
      Future.successful(
        ids.flatMap(id => ctx.getHuman(id) orElse ctx.getDroid(id))
      )
    )(HasId(_.id))

    val EpisodeEnum = EnumType(
      "Episode",
      Some("One of the films in the Star Wars Trilogy"),
      List(
        EnumValue(
          "NEWHOPE",
          value = Episode.NEWHOPE,
          description = Some("Released in 1977.")
        ),
        EnumValue(
          "EMPIRE",
          value = Episode.EMPIRE,
          description = Some("Released in 1980.")
        ),
        EnumValue(
          "JEDI",
          value = Episode.JEDI,
          description = Some("Released in 1983.")
        )
      )
    )

    val Character: InterfaceType[Unit, Character] =
      InterfaceType(
        "Character",
        "A character in the Star Wars Trilogy",
        () =>
          fields[Unit, Character](
            Field(
              "id",
              StringType,
              Some("The id of the character."),
              resolve = _.value.id
            ),
            Field(
              "name",
              OptionType(StringType),
              Some("The name of the character."),
              resolve = _.value.name
            ),
            // Field(
            //   "friends",
            //   OptionType(ListType(OptionType(Character))),
            //   Some(
            //     "The friends of the character, or an empty list if they have none."
            //   ),
            //   resolve = ctx => characters.deferSeqOpt(ctx.value.friends)
            // ),
            Field(
              "appearsIn",
              OptionType(ListType(OptionType(EpisodeEnum))),
              Some("Which movies they appear in."),
              resolve = _.value.appearsIn map (e => Some(e))
            )
          )
      )

    // val Human =
    //   ObjectType(
    //     "Human",
    //     "A humanoid creature in the Star Wars universe.",
    //     interfaces[Unit, Human](Character),
    //     fields[Unit, Human](
    //       Field(
    //         "id",
    //         StringType,
    //         Some("The id of the human."),
    //         resolve = _.value.id
    //       ),
    //       Field(
    //         "name",
    //         OptionType(StringType),
    //         Some("The name of the human."),
    //         resolve = _.value.name
    //       ),
    //       Field(
    //         "friends",
    //         OptionType(ListType(OptionType(Character))),
    //         Some(
    //           "The friends of the human, or an empty list if they have none."
    //         ),
    //         resolve = ctx => DeferFriends(ctx.value.friends)
    //       ),
    //       Field(
    //         "appearsIn",
    //         OptionType(ListType(OptionType(EpisodeEnum))),
    //         Some("Which movies they appear in."),
    //         resolve = _.value.appearsIn map (e => Some(e))
    //       ),
    //       Field(
    //         "homePlanet",
    //         OptionType(StringType),
    //         Some("The home planet of the human, or null if unknown."),
    //         resolve = _.value.homePlanet
    //       )
    //     )
    //   )

    // val Droid = ObjectType(
    //   "Droid",
    //   "A mechanical creature in the Star Wars universe.",
    //   interfaces[Unit, Droid](Character),
    //   fields[Unit, Droid](
    //     Field(
    //       "id",
    //       StringType,
    //       Some("The id of the droid."),
    //       tags = ProjectionName("_id") :: Nil,
    //       resolve = _.value.id
    //     ),
    //     Field(
    //       "name",
    //       OptionType(StringType),
    //       Some("The name of the droid."),
    //       resolve = ctx => Future.successful(ctx.value.name)
    //     ),
    //     Field(
    //       "friends",
    //       OptionType(ListType(OptionType(Character))),
    //       Some("The friends of the droid, or an empty list if they have none."),
    //       resolve = ctx => DeferFriends(ctx.value.friends)
    //     ),
    //     Field(
    //       "appearsIn",
    //       OptionType(ListType(OptionType(EpisodeEnum))),
    //       Some("Which movies they appear in."),
    //       resolve = _.value.appearsIn map (e => Some(e))
    //     ),
    //     Field(
    //       "primaryFunction",
    //       OptionType(StringType),
    //       Some("The primary function of the droid."),
    //       resolve = _.value.primaryFunction
    //     )
    //   )
    // )

    val ID = Argument("id", StringType, description = "id of the character")

    val EpisodeArg = Argument(
      "episode",
      OptionInputType(EpisodeEnum),
      description =
        "If omitted, returns the hero of the whole saga. If provided, returns the hero of that particular episode."
    )

    // val Query = ObjectType[CharacterRepo, Unit](
    //   "Query",
    //   fields[CharacterRepo, Unit](
    //     Field(
    //       "hero",
    //       Character,
    //       arguments = EpisodeArg :: Nil,
    //       resolve = ctx => ctx.ctx.getHero(ctx.argOpt(EpisodeArg))
    //     ),
    //     Field(
    //       "human",
    //       OptionType(Human),
    //       arguments = ID :: Nil,
    //       resolve = ctx => ctx.ctx.getHuman(ctx arg ID)
    //     ),
    //     Field(
    //       "droid",
    //       Droid,
    //       arguments = ID :: Nil,
    //       resolve = Projector((ctx, f) => ctx.ctx.getDroid(ctx arg ID).get)
    //     )
    //   )
    // )

    val definition = graphql"""
    type Picture {
      width: Int!
      height: Int!
      url: String
    }

    interface Identifiable {
      id: String!
    }

    type Product implements Identifiable {
      id: String!
      name: String!
      description: String
      picture(size: Int!): Picture
    }
    """
    // val StarWarsSchema = Schema(Query)
  }
}
