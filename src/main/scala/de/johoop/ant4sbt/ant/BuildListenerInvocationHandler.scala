package de.johoop.ant4sbt.ant

import java.lang.reflect.Method
import java.lang.reflect.InvocationHandler
import org.fest.reflect.core.Reflection._
import sbt.Logger
import org.apache.tools.ant.Project
import sbt.Level

class BuildListenerInvocationHandler(logger: Logger, classLoader: ClassLoader) extends InvocationHandler {
  override def invoke(proxy: AnyRef, method: Method, args: Array[AnyRef]) = {
    val event = new BuildEvent(args.head)
    method.getName match {
      case "buildStarted" => logger debug "Processing Ant Build..."
      case "buildFinished" => logger debug "Finished Ant Build."
      case "messageLogged" => log(event.priority, event.message)
      case "targetStarted" => logger info ("Processing Ant Target '%s'..." format event.target)
      case "targetFinished" => logger debug ("Finished Ant Target '%s'." format event.target)
      case "taskStarted" => logger debug ("Processing Ant Task '%s'..." format event.task)
      case "taskFinished" => logger debug ("Finished Ant Task '%s'." format event.task)
      case _ => throw new IllegalArgumentException("impossible method called")
    }

    null
  }

  private def log(antLevel: Int, message: String) = logger.log(level(antLevel), message)

  private def level(antLevel: Int) = antLevel match {
    case Project.MSG_ERR => Level.Error
    case Project.MSG_WARN => Level.Warn
    case Project.MSG_INFO => Level.Info
    case Project.MSG_DEBUG => Level.Debug
    case Project.MSG_VERBOSE => Level.Debug
    case _ => throw new IllegalArgumentException("invalid ant log level: %d" format antLevel)
  }

  class BuildEvent(buildEvent: AnyRef) {
    val targetClass = `type`("org.apache.tools.ant.Target").withClassLoader(classLoader).load
    val taskClass = `type`("org.apache.tools.ant.Task").withClassLoader(classLoader).load

    def priority = method("getPriority").withReturnType(Integer.TYPE).in(buildEvent).invoke()
    def message = method("getMessage").withReturnType(classOf[String]).in(buildEvent).invoke()
    def target = method("getTarget").withReturnType(targetClass).in(buildEvent).invoke().toString
    def task = method("getTask").withReturnType(taskClass).in(buildEvent).invoke().toString
  }
}

