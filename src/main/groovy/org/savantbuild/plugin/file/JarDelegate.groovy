/*
 * Copyright (c) 2014, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.savantbuild.plugin.file

import org.savantbuild.domain.Project
import org.savantbuild.io.Directory
import org.savantbuild.io.FileTools
import org.savantbuild.parser.groovy.GroovyTools
import org.savantbuild.runtime.BuildFailureException
import org.savantbuild.util.jar.JarBuilder

/**
 * Delegate for the jar method's closure. This passes through everything to the JarBuilder.
 *
 * @author Brian Pontarelli
 */
class JarDelegate extends BaseFileDelegate {
  public static final String ERROR_MESSAGE = "The file plugin jar method must be called like this:\n\n" +
      "  file.jar(file: \"file.jar\") {\n" +
      "    fileSet(dir: \"some other dir\")\n" +
      "    directory(name: \"some dir\")\n" +
      "    manifest(file: \"some file\")\n" +
      "  }"

  public final JarBuilder builder

  JarDelegate(Project project, Map<String, Object> attributes) {
    super(project)

    if (!GroovyTools.attributesValid(attributes, ["file"], ["file"], [:])) {
      throw new BuildFailureException(ERROR_MESSAGE);
    }

    def file = FileTools.toPath(attributes["file"])
    this.builder = new JarBuilder(project.directory.resolve(file))
  }

  /**
   * Adds a directory to the JAR file:
   * <p>
   * <pre>
   *   directory(name: "someDir")
   * </pre>
   *
   * @param attributes The named attributes (name is required).
   */
  JarBuilder directory(Map<String, Object> attributes) {
    if (!GroovyTools.attributesValid(attributes, ["name"], ["name"], ["name": String.class])) {
      throw new BuildFailureException(ERROR_MESSAGE)
    }

    Directory directory = new Directory(attributes["name"], null, null, null)
    builder.directory(directory)
    return builder
  }

  /**
   * Adds a fileSet:
   *
   * <pre>
   *   fileSet(dir: "someDir")
   * </pre>
   *
   * @param attributes The named attributes (dir is required).
   * @return The JarBuilder.
   */
  JarBuilder fileSet(Map<String, Object> attributes) {
    if (!GroovyTools.attributesValid(attributes, ["dir", "includePatterns", "excludePatterns"], ["dir"], ["includePatterns": List.class, "excludePatterns": List.class])) {
      throw new BuildFailureException(ERROR_MESSAGE)
    }

    builder.fileSet(toFileSet(attributes))
    return builder
  }

  /**
   * Adds a MANIFEST.MF file to the jar. This can specify a file for the manifest or it can specify the manifest using a
   * Map of values. Here are some examples:
   * <p>
   * <pre>
   *   manifest(file: "src/main/META-INF/MANIFEST.MF")
   * </pre>
   * <p>
   * <pre>
   *   manifest(map: [
   *     "Implementation-Version": project.version
   *   ])
   * </pre>
   *
   * @param attributes The named attributes.
   * @return The JarBuilder
   */
  JarBuilder manifest(Map<String, Object> attributes) {
    if (!GroovyTools.attributesValid(attributes, ["file", "map"], [], ["map": Map.class])) {
      throw new BuildFailureException("Invalid manifest directive ${attributes.keySet()}. ${ERROR_MESSAGE}")
    }

    if (attributes.containsKey("file")) {
      println "File attribute"
      builder.manifest(project.directory.resolve(FileTools.toPath(attributes["file"])))
    } else if (attributes.containsKey("map")) {
      println "Map attribute"
      builder.manifest(attributes["map"])
    }
    return builder
  }

  /**
   * Adds an optionalFileSet:
   *
   * <pre>
   *   optionalFileSet(dir: "someDir")
   * </pre>
   *
   * @param attributes The named attributes (dir is required).
   * @return The JarBuilder.
   */
  JarBuilder optionalFileSet(Map<String, Object> attributes) {
    if (!GroovyTools.attributesValid(attributes, ["dir", "includePatterns", "excludePatterns"], ["dir"], ["includePatterns": List.class, "excludePatterns": List.class])) {
      throw new BuildFailureException(ERROR_MESSAGE)
    }

    builder.optionalFileSet(toFileSet(attributes))
    return builder
  }
}
