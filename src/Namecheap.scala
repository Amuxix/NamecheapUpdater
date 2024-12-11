import cats.effect.IO
import org.http4s.EntityDecoder
import org.http4s.Uri
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.client.Client
import org.http4s.scalaxml.xmlDecoder
import org.http4s.syntax.literals.uri

import scala.xml.NodeSeq

case class Host(recordType: String, hostName: String, address: String)

class Namecheap(client: Client[IO], user: String, apiKey: String, domain: String):
  private val uri = uri"https://api.namecheap.com/xml.response"

  private val queryParams =
    val Array(sld, tld) = domain.split("\\.")
    Map(
      "ApiUser"  -> user,
      "UserName" -> user,
      "ClientIp" -> "127.0.0.1",
      "ApiKey"   -> apiKey,
      "SLD"      -> sld,
      "TLD"      -> tld,
    )

  private def commandParams(command: String) = queryParams + ("Command" -> s"namecheap.domains.dns.$command")

  def responseDecoder[T](f: NodeSeq => T): EntityDecoder[IO, Either[String, T]] = xmlDecoder[IO].map { elem =>
    val response = elem \\ "ApiResponse"
    if response \@ "Status" != "OK" then Left((response \\ "Errors" \\ "Error").text) else Right(f(response))
  }

  given hostDecoder: EntityDecoder[IO, Either[String, List[Host]]] = responseDecoder { response =>
    (response \\ "CommandResponse" \\ "DomainDNSGetHostsResult" \\ "host")
      .map(host => Host(host \@ "Type", host \@ "Name", host \@ "Address"))
      .toList
  }

  given booleanDecoder: EntityDecoder[IO, Either[String, Boolean]] = responseDecoder { response =>
    (response \\ "CommandResponse" \\ "DomainDNSSetHostsResult" \@ "IsSuccess").toBoolean
  }

  extension (client: Client[IO])
    def expectResponse[T](command: String, params: Map[String, String] = Map.empty)(using
      EntityDecoder[IO, Either[String, T]],
    ): IO[T] =
      client
        .expect[Either[String, T]](uri.withQueryParams(commandParams(command) ++ params))
        .flatMap(_.fold(error => IO.raiseError(new Exception(error)), IO.pure))

  def getHosts: IO[List[Host]] =
    client.expectResponse[List[Host]]("getHosts")

  def setHosts(hosts: List[Host]): IO[Boolean] =
    val hostParams = hosts.foldLeft((1, Map.empty[String, String])) { case ((id, map), host) =>
      val hostMap = Map(
        s"RecordType$id" -> host.recordType,
        s"HostName$id"   -> host.hostName,
        s"Address$id"    -> host.address,
      )
      (id + 1, map ++ hostMap)
    }(1)
    client.expectResponse[Boolean]("setHosts", hostParams)
