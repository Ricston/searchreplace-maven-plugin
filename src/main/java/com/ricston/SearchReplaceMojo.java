package com.ricston;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Search and replace text in given package
 * 
 */
@Mojo(name = "searchreplace", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class SearchReplaceMojo extends AbstractMojo {

	public static final String SAME_TARGET_PATH_AS_SOURCE = "";

	@Parameter(property = "sr.replaceString")
	private String replaceString;

	@Parameter(property = "sr.searchRegex")
	private String searchRegex;

	@Parameter(property = "sr.targetPackage")
	private String targetPackage;

	@Parameter(property = "sr.sourceDirectory", defaultValue = "${project.build.sourceDirectory}")
	protected File sourceDirectory;

	@Parameter(property = "sr.targetDirectoryPath")
	protected String targetDirectoryPath = SAME_TARGET_PATH_AS_SOURCE;

	public void execute() throws MojoExecutionException {
		final Log log = getLog();
		log.info("=======================");
		log.info(String.format("${sr.replaceString}: %s", replaceString));
		log.info(String.format("${sr.searchRegex}: %s", searchRegex));
		log.info(String.format("${sr.targetPackage}: %s", targetPackage));
		log.info(String.format("${sr.sourceDirectory}: %s", sourceDirectory));
		log.info(String.format("${sr.targetDirectoryPath}: %s", targetDirectoryPath));
		log.info("=======================");

		final String targetPackageWithOsSeparator = targetPackage.replaceAll("\\.", File.separator);
		final File directoryToProcess = new File(sourceDirectory.getAbsolutePath(), targetPackageWithOsSeparator);
		log.info(String.format("directory to process: %s", directoryToProcess));

		List<File> files = getFilesInFolder(directoryToProcess, new ArrayList<File>(100));
		log.info(String.format("files to process: %s", files));

		for (final File file : files) {
			try {
				String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
				content = content.replaceAll(searchRegex, replaceString);
				if (SAME_TARGET_PATH_AS_SOURCE.equals(targetDirectoryPath)) {
					Path targetPath = file.toPath();
					log.info(String.format("By default, writing [%s] to [%s]", file.getName(), targetPath));
					Files.write(targetPath, content.getBytes(StandardCharsets.UTF_8));
				} else {
					Path targetPath = new File(targetDirectoryPath, file.getName()).toPath();
					log.info(String.format("Writing [%s] to [%s]", file.getName(), targetPath));
					Files.write(targetPath, content.getBytes(StandardCharsets.UTF_8));
				}
			} catch (IOException e) {
				log.info(String.format("could not process %s", file.getName()));
				e.printStackTrace();
			}
		}
	}

	public List<File> getFilesInFolder(final File folder, List<File> files) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				getFilesInFolder(fileEntry, files);
			} else {
				files.add(fileEntry);
			}
		}

		return files;
	}
}
