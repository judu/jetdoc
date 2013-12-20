package jetdoc

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._

object typeRegexps {
		val HtmlReg = "(^.*\\.html$)".r
		val CssReg = "(^.*\\.css$)".r
		val GifReg = "(^.*\\.gif$)".r
		val JpgReg = "(^.*\\.jpe?g$)".r
		val PngReg = "(^.*\\.png$)".r
}

class JarServer(jar: java.io.File) extends async.Plan
	with ServerErrorResponse {
	import java.util.jar._
	import java.util.zip._
	import scala.reflect.ClassTag
	import typeRegexps._
	def f[T](v: T)(implicit ev: ClassTag[T]) = ev.toString

	def filetype(p: String): BaseContentType = p match {
		case HtmlReg(_) => HtmlContent
		case CssReg(_) => CssContent
		case GifReg(_)  => ContentType("image/gif")
		case JpgReg(_)  => ContentType("image/jpg")
		case PngReg(_)  => ContentType("image/png")
		case _ => PlainTextContent
	}
	val SlashReg = "(^.*/$)".r

	val jarFile = new ZipFile(jar)

	val logger = org.clapper.avsl.Logger(getClass)

	logger.info("Using file " + jar.getName)
	def intent = async.Intent {
		case r @ Path(p) => r match {
			case GET(_) =>
				val path = p match {
					case "" => "/index.html"
					case SlashReg(pp) => pp + "index.html"
					case pp => pp
				}
				Option(jarFile.getEntry(path.substring(1,path.length))).flatMap(ent => {
					if(ent.isDirectory)
						None
					else
						Option(jarFile.getInputStream(ent)).map(is => {
						  	Iterator continually is.read takeWhile (-1 !=) map (_.toByte) toArray
						})
				}).map(arr =>
					r respond Ok ~> filetype(path) ~> ResponseBytes(arr)
				).getOrElse(r respond NotFound ~> ResponseString("Not found"))
			case _ => r respond MethodNotAllowed
		}
	}
}

object JarServer {
	def apply(jar: java.io.File): JarServer = new JarServer(jar)
}

