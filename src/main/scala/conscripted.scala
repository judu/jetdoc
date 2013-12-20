package jetdoc

class JetdocScript extends xsbti.AppMain {
	case class Exit(val code: Int) extends xsbti.Exit

	def run(config: xsbti.AppConfiguration) = {
		val inst = Server.instance(config.arguments)
		if(inst.isEmpty) {
			Server.displayHelpMessage
			Exit(1)
		} else {
			inst.foreach(inst => inst.run({
					_ => Exit(0)
				}))
			Exit(0)
		}
	}
}
