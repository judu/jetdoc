package jetdoc

import java.io.File

object RetrieveJavadoc {
	import org.apache.ivy.core.settings._
	import org.apache.ivy.core.resolve.DownloadOptions
	import org.apache.ivy.plugins.resolver._
	import org.apache.ivy.Ivy
	import org.apache.ivy.core.module.descriptor._
	import org.apache.ivy.core.resolve.ResolveOptions
	import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter
	import org.apache.ivy.core.module.id.ModuleRevisionId

	val logger = org.clapper.avsl.Logger(RetrieveJavadoc.getClass)

	val res = new URLResolver

	def apply(ref: String): Option[File] = {
		if(ref.contains(":"))
			ref.split(":") match {
				case Array(org, artifact, version) => apply(org,artifact,version)
				case _ => None //FIXME: we could use latest version in the future
			}
		else
			None
	}

	def apply(orga: String, artifact: String, version: String): Option[File] = {
		logger.info(s"Trying to get ${orga}/${artifact}/${version}")
		val settings = new IvySettings
		val resolver = new URLResolver
		resolver.setM2compatible(true)
		resolver.setName("central")
		resolver.addArtifactPattern(
			"http://repo1.maven.org/maven2/[organisation]/[module]/[revision]/[module](-[revision])(-[type]).[ext]"
		)
		val typesafe = new URLResolver
		typesafe.setName("typesafe")
		typesafe.setM2compatible(false)
		typesafe.addArtifactPattern(
			"http://repo.typesafe.com/typesafe/ivy-releases/" +
			"[organization]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)[revision]/[type]s/[artifact](-[classifier]).[ext]"
		)
		settings.addResolver(resolver)
		//settings.addResolver(typesafe)
		settings.setDefaultResolver(resolver.getName);
		val ivy = Ivy.newInstance(settings)
		val mri = ModuleRevisionId.newInstance(orga, artifact, version)
		val art: Artifact = new DefaultArtifact(mri, new java.util.Date, artifact, "javadoc", "jar")
		val ropts = new ResolveOptions
		ropts.setValidate(false)
		ropts.setTransitive(false)
		val ao = Option(ivy.getResolveEngine.locate(art))
		ao.map(aao =>
			ivy.getResolveEngine.download(aao, new DownloadOptions)
		).flatMap(adr => Option(adr.getLocalFile))
	}
}

object RetrieveJarfile {
	val logger = org.clapper.avsl.Logger(RetrieveJarfile.getClass)

	def apply(path: String): Option[File] = {
		import org.apache.ivy.core.settings._
		import java.net.URL
		import java.nio.file._
		import org.apache.http._,client._,methods._,impl.client._
		val settings = new IvySettings
		settings.loadDefault
		val cacheDir = settings.getDefaultCache
		val miscDir = new File(cacheDir.getAbsolutePath + "/misc")
		if (!miscDir.exists)
			miscDir.mkdir
		val url = new URL(path)
		val cacheFile = new File(miscDir.getAbsolutePath + "/" + new File(url.getPath).getName)
		if(!cacheFile.exists) {
			logger.info(s"Need to download from ${path}")
			val httpclient = HttpClients.createDefault
			val httpget = new HttpGet(url.toURI)
			val response = httpclient.execute(httpget)
			if (response.getStatusLine.getStatusCode >= 200
			  	&& response.getStatusLine.getStatusCode < 300) {
				val is = response.getEntity.getContent
				val arr = Iterator.continually(is.read).takeWhile(-1 !=).map(_.toByte).toArray
				if(arr.length == 0) {
					logger.error("Argh, download failed")
					None
				} else {
					val cachePath = Paths.get(cacheFile.toURI)
					Files.write(cachePath, arr, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
					Some(cacheFile)
				}
			} else {
			  	logger.error(s"Argh, download failed with status ${response.getStatusLine.getStatusCode}")
				None
			}
		} else {
			logger.error(s"Using cached file at ${cacheFile.getAbsolutePath}")
			Some(cacheFile)
		}
	}
}
