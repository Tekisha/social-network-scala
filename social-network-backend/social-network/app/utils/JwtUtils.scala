package utils

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import play.api.libs.json.{Json, OFormat}
import java.time.Instant

object JwtUtils {
  private val secretKey = "your_secret_key"
  private val algorithm = JwtAlgorithm.HS256

  case class JwtClaimContent(userId: Int, username: String, expiration: Long)

  implicit val claimFormat: OFormat[JwtClaimContent] = Json.format[JwtClaimContent]

  def createToken(id: Int,username: String, expirationPeriodInDays: Int): String = {
    val expiration = Instant.now.plusSeconds(expirationPeriodInDays * 24 * 3600).toEpochMilli
    val claimContent = JwtClaimContent(id, username, expiration)
    val claimJson = Json.toJson(claimContent).toString()

    Jwt.encode(claimJson, secretKey, algorithm)
  }

  def validateToken(token: String): Option[JwtClaimContent] = {
    Jwt.decode(token, secretKey, Seq(algorithm)).toOption.flatMap { decoded =>
      Json.parse(decoded.content).asOpt[JwtClaimContent].filter(_.expiration > Instant.now.toEpochMilli)
    }
  }

  def getUserIdFromToken(token: String): Option[Int] = {
    JwtUtils.validateToken(token).map(_.userId)
  }
}
