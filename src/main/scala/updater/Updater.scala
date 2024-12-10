//> using scala 3.5.0
//> using dep org.typelevel::cats-effect:3.5.4
//> using dep io.circe::circe-core:0.14.9
//> using dep io.circe::circe-parser:0.14.9
//> using dep com.github.cb372::cats-retry:3.1.3
//> using dep org.typelevel::log4cats-slf4j:2.7.0
//> using dep ch.qos.logback:logback-classic:1.5.7
//> using dep com.github.pureconfig::pureconfig-core:0.17.7
//> using dep com.github.pureconfig::pureconfig-cats-effect:0.17.7
//> using dep org.http4s::http4s-dsl:1.0.0-M38
//> using dep org.http4s::http4s-ember-client:1.0.0-M38
//> using dep org.http4s::http4s-ember-server:1.0.0-M38
//> using dep org.http4s::http4s-circe:1.0.0-M38
//> using dep org.http4s::http4s-scala-xml:1.0.0-M38.1
//> using dep org.scala-lang.modules::scala-xml:2.3.0
//> using file Namecheap.scala

package updater

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.instances.option.*
import cats.syntax.traverse.*
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.LoggerName
import org.typelevel.log4cats.slf4j.Slf4jFactory

sealed trait CLI

object Updater extends IOApp:

  val namecheap = for
    given Slf4jFactory[IO] <- Resource.eval(IO(Slf4jFactory.create[IO]))
    given Logger[IO]       <- Resource.eval(Slf4jFactory[IO].fromName(summon[LoggerName].value))
    client                 <- EmberClientBuilder.default[IO].build
    namecheap               = Namecheap(client, "Aifosi", "001353b73c7a4c248872945ce212d217", "aifosi.top")
  yield namecheap

  def help(arg: Option[String]) = IO.println {
    arg.fold("")(arg => s"Invalid argument $arg, ") + "please use either:" +
      "\n  addHost record host address (example: addHost A * 10.0.0.1)" +
      "\n  removeHost record host (example: removeHost A *)"
  }.as(ExitCode.Error)

  override def run(args: List[String]): IO[ExitCode] =
    args.headOption.traverse {
      case "addHost" if args.tail.size == 3 =>
        namecheap.use { namecheap =>
          for
            hosts   <- namecheap.getHosts
            newHosts = (hosts :+ Host(args(1), args(2), args(3))).distinct
            _       <- IO.whenA(newHosts.size > hosts.size)(namecheap.setHosts(newHosts).void)
          yield ExitCode.Success
        }

      case "removeHost" if args.tail.size == 2 =>
        namecheap.use { namecheap =>
          for
            hosts   <- namecheap.getHosts
            newHosts = hosts.filterNot(host => host.recordType == args(1) && host.hostName == args(2))
            _       <- IO.whenA(newHosts.size > hosts.size)(namecheap.setHosts(newHosts).void)
          yield ExitCode.Success
        }

      case arg => help(Some(arg))
    }
      .flatMap(_.fold(help(None))(IO.pure))
      .attempt
      .flatMap(_.fold(error => IO.println(error).as(ExitCode.Error), IO.pure))

