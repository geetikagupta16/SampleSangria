package controllers

import javax.inject.Inject
import play.api.libs.json.JsValue
import play.api.mvc.{AbstractController, ControllerComponents}
import sangria.execution.Executor
import sangria.macros._
import sangria.macros.derive._
import sangria.schema.{Field, InterfaceType, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import sangria.marshalling.playJson._

trait Identifiable {
  def id: String
}

case class Picture(width: Int, height: Int, url: Option[String])

case class Product(id: String, name: String, description: String) extends Identifiable {
  /*def picture(size: Int): Picture =
    Picture(width = size, height = size, url = Some(s"//cdn.com/$size/$id.jpg"))*/
}

class ProductRepo {
  private val Products = List(
    Product("1", "Cheesecake", "Tasty"),
    Product("2", "Health Potion", "+50 HP"),
    Product("3", "Cheesecake", "Tasty"))

  def product(id: String): Option[Product] =
    Products find (_.id == id)

  def getProductByName(name: String) = {
    Products.filter(_.name == name)
  }

  def products: List[Product] = Products
}

class ProductController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {



  def getData = Action.async {

    val Id = Argument("id", StringType)
    val name = Argument("name", StringType)

    val IdentifiableType = InterfaceType(
      "Identifiable",
      "Entity that can be identified",
      fields[Unit, Identifiable](
        Field("id", StringType, resolve = _.value.id)))

    val ProductType =
      deriveObjectType[Unit, Product]()

    val QueryType: ObjectType[ProductRepo, Unit] = ObjectType("Query", fields[ProductRepo, Unit](
      Field("product", ListType(ProductType),
        description = Some("Returns a product with specific `id`."),
        arguments = name :: Nil,
        resolve = (c: Context[ProductRepo, Unit]) ⇒ c.ctx.getProductByName(c.args.arg(name))),

      Field("products", ListType(ProductType),
        description = Some("Returns a list of all available products."),
        resolve = _.ctx.products)))

    val schema: Schema[ProductRepo, Unit] = Schema(QueryType)

    val query =
      graphql"""query{ product(name : "Cheesecake") {id
               name}
                products {
                name
                }
                }"""

    val result: Future[JsValue] = Executor.execute(schema, query, new ProductRepo)

    result.map(res ⇒ println(res))
    result.map(x => Ok(x))
  }

}
