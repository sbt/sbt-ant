# ant4sbt - Calling Ant from within SBT

This SBT plug-in enables you to integrate **[Ant](http://ant.apache.org)** targets and Ant properties with your *[sbt](http://scala-sbt.org)* build.

It is available for SBT 0.13.

## Getting ant4sbt

ant4sbt is hosted at the Typesafe sbt Community Plugin Repository. This repository is automatically available from within SBT. This means that you just have to add ant4sbt as a plug-in to your project (see the next section).

If you want to go bleeding edge, you can also:

```
git clone http://github.com/sbt/ant4sbt.git
cd ant4sbt
sbt publish-local
```

## Adding ant4sbt as a plug-in to your project

Add the following to your project's `build.sbt` file:

```scala
import de.johoop.ant4sbt.Ant4Sbt._

antSettings
```

Also, you have to add the plugin dependency to your project's `./project/plugins.sbt` or the global `.sbt/0.13/plugins/build.sbt`:

```scala
addSbtPlugin("de.johoop" % "ant4sbt" % "1.1.2")
```

## Using ant4sbt

ant4sbt adds the following tasks by default: 

### `antRun`

This is an input task that takes ant targets as arguments. It works exactly like ant would from the command line...

Example: `antRun clean compile`

### `antProperty`

This is an input task that takes the name of an Ant property as argument and will return the value of this property.

Example: `show antProperty os.name`

## Controlling the Ant Build Server

There are also a few tasks to manually start, stop and restart the embedded "Ant Build Server": They are called `antStartServer`, `antStopServer` and ` antRestartServer`.

Whenever you change your Ant build file, you will have to restart the Ant Build Server or reload your SBT project configuration.

The Ant Build Server will automatically shutdown when you leave SBT interactive mode (or when your SBT command task ends).

## Importing Ant targets and properties as SBT tasks

Instead of using the input tasks above, you can also import Ant targets and Ant properties into your SBT build, so that they are directly available as SBT tasks. This is especially useful if you want to depend on these tasks, or if you want these tasks to depend on other SBT tasks.

### `addAntTasks()`

Add `addAntTasks("targetA", "targetB", "targetC")` to your `build.sbt` to import the ant targets `targetA`, `targetB` and `targetC` into your build.

These targets are then known to SBT as `antRunTargetA`, `antRunTargetB` and `antRunTargetC`, respectively.

In `build.sbt`, these tasks are known as `antTaskKey("targetA")` etc.

For example, you could now add stuff like the following to your settings: 

```scala
TaskKey[Unit]("depends-on-ant-compile") <<= antTaskKey("compile") map { meep => () }
```

### `addAntProperties()`

Add `addAntProperties(propA, propB)` to your `build.sbt` to import the Ant properties `propA` and `propB` into your build.

These properties are then known to SBT as `antPropertyPropA` and `antPropertyPropB`, respectively.

In `build.sbt`, these tasks are known as `antPropertyKey("propA")` etc.

See also above. They work like the imported Ant targets.

## Configuration Settings

The following settings are also available in order to configure the plug-in:

### `antBuildFile`

* *Description:* Location of the Ant build file (usually named `build.xml`)
* *Accepts:* `File`
* *Default:* `baseDirectory / "build.xml"`

### `antBaseDir`

* *Description:* Base directory for the Ant build.
* *Accepts:* `File`
* *Default:* `baseDirectory`

### `antOptions`

* *Description:* Additional JVM options for Ant (`ANT_OPTS`).
* *Accepts:* `Seq[String]`
* *Default:* `$ANT_OPTS`, split up by spaces

### `antServerPort`

* *Description:* Port the Ant Build Server should listen at..
* *Accepts:* `Int`
* *Default:* `21345`

## License

This program and the accompanying materials are made available under the terms of the **Eclipse Public License v1.0** which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html