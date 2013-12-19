package com.example

import unfiltered.netty._
import unfiltered.util.Port

/** embedded server */
object Server {
	val logger = org.clapper.avsl.Logger(Server.getClass)


	class Instance(port: Int, path: String) {
		val theJar = new java.io.File(path)
		val svr = unfiltered.netty.Http(port)
			.handler(JarServer(theJar))

		def run(afterRun: Http => Unit) = svr.run(afterRun)
	}

	def main(args: Array[String]) {
		val instance = args match {
			case Array("-p", PortOpt(port), path) =>
				new Instance(port.toInt, path)
			case Array(path, "-p", PortOpt(port)) =>
				new Instance(port.toInt, path)
			case Array(path) =>
				new Instance(Port.any, path)
			case _ =>
				logger.error("Command : serve [-p port] path")
				new Instance(Port.any, "/424242.42")
		}
		instance.run(_ => println("Server running"))
		dispatch.Http.shutdown()
	}

	val PortOpt = """(\d{4})""".r
}
