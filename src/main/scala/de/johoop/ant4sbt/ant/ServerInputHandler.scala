package de.johoop.ant4sbt.ant

import java.io.BufferedReader
import org.apache.tools.ant.input._

class ServerInputHandler(reader: BufferedReader) extends InputHandler {
  override def handleInput(input: InputRequest) = input setInput reader.readLine
}
