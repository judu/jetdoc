package jetdoc

import unfiltered.netty._
import unfiltered.util.Port

/** embedded server */
object Server {
	val logger = org.clapper.avsl.Logger(Server.getClass)


	class Instance(port: Int, path: String) {
		val theJar = path match {
			case Artifact(orga,artifact,version) => RetrieveJavadoc(orga,artifact,version)
			case Url(u) => RetrieveJarfile(u)
			case p => Some(new java.io.File(p))
		}
		val svr = theJar.map(tj => unfiltered.netty.Http(port)
			.handler(JarServer(tj))
		)
		if(svr.isEmpty) {
			logger.error("Can't locate the jar")
			System.exit(1)
		}
		logger.info(s"Server set up to listen on ${port}")
		def run(afterRun: Http => Unit) = svr.get.run(afterRun)
	}

	def main(args: Array[String]) {
		val instance = args match {
			case Array("-p", PortOpt(port), path) =>
				Some(new Instance(port.toInt, path))
			case Array(path, "-p", PortOpt(port)) =>
				Some(new Instance(port.toInt, path))
			case Array(path) =>
				Some(new Instance(Port.any, path))
			case _ =>
				displayHelpMessageAndQuit
				None
		}
		instance.get.run(_ => println("Server running"))
		dispatch.Http.shutdown
	}

	def displayHelpMessageAndQuit: Unit = {
		println("Usage: serve [-p port] path\n")
		println("\tport is a number")
		println("\tpath is either:")
		println("\t\t- a path to a local file")
		println("\t\t- a http URL to a remote file")
		println("\t\t- an artifact shaped as {organization}:{artifact}:{version}")
		System.exit(1)
	}

	val PortOpt = """(\d{4})""".r
	val Artifact = """(.+):(.+):(.+)""".r
	val Url = """(https?://.+)""".r
}
