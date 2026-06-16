package com.example.zivito

import java.nio.file.{Files, Path, Paths}

import zio.*
import zio.http.*

/** Local-disk image storage. Images are POSTed as the raw request body (the `Content-Type` selects the extension) and served back as static files under
  * `/uploads/{file}`. Object storage is out of scope for the demo; this is the production-shaped-but-simple choice.
  */
object Uploads {

  val dir: Path = Paths.get(sys.env.getOrElse("UPLOAD_DIR", "./uploads"))

  val maxBytes: Long = 5L * 1024 * 1024

  /** Content types we accept, mapped to the on-disk file extension. */
  val extensionByContentType: Map[String, String] =
    Map("image/png" -> "png", "image/jpeg" -> "jpg", "image/webp" -> "webp", "image/gif" -> "gif")

  private val contentTypeByExtension: Map[String, String] =
    extensionByContentType.map((ct, ext) => ext -> ct)

  def extensionFor(contentType: String): IO[AppError, String] =
    extensionByContentType.get(contentType.toLowerCase.takeWhile(_ != ';').trim) match {
      case Some(ext) => ZIO.succeed(ext)
      case None      => ZIO.fail(AppError.Validation("image", "must be a PNG, JPEG, WebP or GIF"))
    }

  def save(fileName: String, bytes: Chunk[Byte]): Task[Unit] =
    ZIO.attemptBlocking {
      Files.createDirectories(dir)
      Files.write(dir.resolve(fileName), bytes.toArray)
    }.unit

  /** Serves stored images. Guards against path traversal by rejecting any name with a separator or `..`.
    */
  val routes: Routes[Any, Throwable] =
    Routes(
      Method.GET / "uploads" / string("file") -> handler { (file: String, _: Request) =>
        if (file.contains("/") || file.contains("\\") || file.contains(".."))
          ZIO.succeed(Response.status(Status.BadRequest))
        else {
          val path = dir.resolve(file)
          ZIO.attemptBlocking(Files.exists(path)).flatMap {
            case false => ZIO.succeed(Response.status(Status.NotFound))
            case true =>
              ZIO.attemptBlocking(Files.readAllBytes(path)).map { bytes =>
                val ext         = file.reverse.takeWhile(_ != '.').reverse.toLowerCase
                val contentType = contentTypeByExtension.getOrElse(ext, "application/octet-stream")
                Response(body = Body.fromArray(bytes)).addHeader(Header.ContentType.name, contentType)
              }
          }
        }
      }
    )
}
