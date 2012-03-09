package de.johoop.ant4sbt.ant

import sbt.Logger
import sbt.Level
import org.apache.tools.ant._

class AntBuildListener(logger: Logger) extends BuildListener {
    override def buildStarted(event: BuildEvent) = logger debug ("Processing Ant Build...")
    override def buildFinished(event: BuildEvent) = logger debug ("Finished Ant Build.")
    override def taskStarted(event: BuildEvent) = logger debug ("Processing Ant Task '%s'..." format event.getTask)
    override def taskFinished(event: BuildEvent) = logger debug ("Finished Ant Task '%s'." format event.getTask)
    override def targetFinished(event: BuildEvent) = logger debug ("Finished Ant Target '%s'." format event.getTarget)
    override def targetStarted(event: BuildEvent) = logger info ("Processing Ant Target '%s'..." format event.getTarget)

    override def messageLogged(event: BuildEvent) = log(event.getPriority, event.getMessage)

    private def log(antLevel: Int, message: String) = logger.log(level(antLevel), message)

    private def level(antLevel: Int) = antLevel match {
      case Project.MSG_ERR => Level.Error
      case Project.MSG_WARN => Level.Warn
      case Project.MSG_INFO => Level.Info
      case Project.MSG_DEBUG => Level.Debug
      case Project.MSG_VERBOSE => Level.Debug
      case _ => throw new IllegalArgumentException("invalid ant log level: %d" format antLevel)
    }
}
