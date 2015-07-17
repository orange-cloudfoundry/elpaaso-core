/**
 * Copyright (C) 2015 Orange
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.francetelecom.clara.cloud.archive;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * Manage ear files or other archives using java 7 nio library
 * 
 * @author BEAL6226
 * 
 */
public class ManageArchiveImpl implements ManageArchive {

	private static Logger logger = LoggerFactory.getLogger(ManageArchiveImpl.class);

	/**
	 * Directory in classpath for source files to put in war
	 */
	public static final String TEMPLATE_WAR_DIR = "/paas-archive-templates-war/";

	/**
	 * Directory in classpath for source files to put in ear
	 */
	public static final String TEMPLATE_EAR_DIR = "/paas-archive-templates-ear/";

	/**
	 * freemarker configuration
	 */
	protected Configuration configuration;

	public ManageArchiveImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.francetelecom.clara.cloud.archive.ManageArchive#generateMinimalEar(com.francetelecom.clara.cloud.commons.MavenReference,
	 * java.lang.String)
	 */
	@Override
	public File generateMinimalEar(MavenReference mavenReference, String contextRoot) throws TechnicalException {

		// Prepare war and ear filename
		String earFilename = mavenReference.getArtifactName();
		if (!earFilename.endsWith(".ear")) {
			logger.error("provided Maven reference {} is not an ear", mavenReference);
			throw new TechnicalException("provided Maven reference is not an ear");
		}
		String warFilename = earFilename.replaceFirst(".ear$", ".war");

		// Prepare temp directory to store war and ear
		Path tempDir;
		try {
			tempDir = Files.createTempDirectory("ear");
		} catch (IOException e) {
			throw new TechnicalException("cannot create empty directory", e);
		}

		URI earUri = generateJarUri(tempDir, earFilename);
        logger.info("URI of ear to be generated : {}", earUri.toString());


        // Prepare map for Freemarker (the same map is used for all templates)
        Map<String, Object> freemarkerModel = createFreemarkerModel(mavenReference, contextRoot);

        Path warFile = generateWar(warFilename, tempDir, freemarkerModel);

		// build ear file with war file and files in TEMPLATE_EAR_DIR
		try (FileSystem earFileSystem = FileSystems.newFileSystem(earUri, getFileSystemEnv())) { // earFileSystem is automatically closed
			addAbsoluteFileToJarFile(warFile.getParent(), warFile, earFileSystem);
			// addSourceFilesToJarFile(TEMPLATE_EAR_DIR, freemarkerModel, earFileSystem);
			createDirectoryInJarFile("META-INF", earFileSystem);
			addClasspathTemplateToJarFile(TEMPLATE_EAR_DIR, "META-INF/application.xml.flt", freemarkerModel, earFileSystem);

		} catch (IOException e) {
			throw new TechnicalException("cannot create jar filesystem", e);
		}

		// delete war file as it is no more useful
		try {
			Files.delete(warFile);
		} catch (IOException e) {
			logger.debug("cannot delete war file {}", warFile, e);
		}
		return new File(tempDir.toString(), earFilename);
	}

    @Override
    public File generateMinimalWar(MavenReference mavenReferenceForWarGeneration, String contextRoot) throws TechnicalException {

        String warFilename = mavenReferenceForWarGeneration.getArtifactName();
        if (!warFilename.endsWith(".war")) {
            logger.warn("provided Maven reference {} is not an war", mavenReferenceForWarGeneration);
            warFilename=warFilename.substring(0,warFilename.length()-4) + ".war";
            logger.warn("renamed {} to {} for maven reference {}", mavenReferenceForWarGeneration.getArtifactName(),warFilename, mavenReferenceForWarGeneration);
        }

        // Prepare temp directory to store war and ear
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("war");
        } catch (IOException e) {
            throw new TechnicalException("cannot create empty directory", e);
        }

        URI warUri = generateJarUri(tempDir, warFilename);
        logger.info("URI of ear to be generated : {}", warUri.toString());


        // Prepare map for Freemarker (the same map is used for all templates)
        Map<String, Object> freemarkerModel = createFreemarkerModel(mavenReferenceForWarGeneration, contextRoot);

        Path warFile = generateWar(warFilename, tempDir, freemarkerModel);

        return new File(warFile.toString());
    }

    private Path generateWar(String warFilename, Path tempDir, Map<String, Object> freemarkerModel) {
        URI warUri = generateJarUri(tempDir, warFilename);
        // build war file with files in TEMPLATE_WAR_DIR
        try (FileSystem warFileSystem = FileSystems.newFileSystem(warUri, getFileSystemEnv())) { // warFileSystem is automatically closed
            // Not so easy to list all files in TEMPLATE_WAR_DIR, because on production environment we're inside a jar,
            // thus we cannot use NIO Files.walkFileTree() nor File.listFiles()
            // So for the moment we just add all files in war manually
            addClasspathTemplateToJarFile(TEMPLATE_WAR_DIR, "index.html.flt", freemarkerModel, warFileSystem);
            createDirectoryInJarFile("WEB-INF", warFileSystem);
            addClasspathFileToJarFile(TEMPLATE_WAR_DIR, "WEB-INF/web.xml", warFileSystem);
            createDirectoryInJarFile("styles", warFileSystem);
            addClasspathFileToJarFile(TEMPLATE_WAR_DIR, "styles/application.css", warFileSystem);
            addClasspathFileToJarFile(TEMPLATE_WAR_DIR, "styles/footer.css", warFileSystem);
            addClasspathFileToJarFile(TEMPLATE_WAR_DIR, "styles/gabarits.css", warFileSystem);
            addClasspathFileToJarFile(TEMPLATE_WAR_DIR, "styles/orange-main.css", warFileSystem);
            addClasspathFileToJarFile(TEMPLATE_WAR_DIR, "styles/signin.css", warFileSystem);
            createDirectoryInJarFile("images", warFileSystem);
            addClasspathFileToJarFile(TEMPLATE_WAR_DIR, "images/favicon.ico", warFileSystem);
            addClasspathFileToJarFile(TEMPLATE_WAR_DIR, "images/orange_logo.jpg", warFileSystem);
        } catch (IOException e) {
            throw new TechnicalException("cannot create war filesystem", e);
        }
        return Paths.get(tempDir.toString(), warFilename);
    }

    private Map<String, String> getFileSystemEnv() {
        // Prepare map for FileSystem creation
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        return env;
    }


    /**
	 * create the freemarker model with all ${} variables and values
	 * 
	 * @param mavenReference
	 *            the ear Maven reference, used to define war and ear file names
	 * @param contextRoot
	 *            the war context root
	 * @return the generated model
	 */
	protected Map<String, Object> createFreemarkerModel(MavenReference mavenReference, String contextRoot) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("warFilename", mavenReference.getArtifactName().replaceFirst(".ear$", ".war"));
		model.put("contextRoot", contextRoot);
		model.put("groupid", mavenReference.getGroupId());
		model.put("artifactid", mavenReference.getArtifactId());
		model.put("version", mavenReference.getVersion());
		model.put("classifier", mavenReference.getClassifier());
		model.put("extension", mavenReference.getExtension());
		model.put("buildDate", Calendar.getInstance().getTime());
		return model;
	}

	/**
	 * generate a jar URI by using the syntax defined in java.net.JarURLConnection for example
	 * jar:file:/tmp/ear11111111/myappli-1.0.0-SNAPSHOT.ear
	 */
	protected URI generateJarUri(Path directory, String jarFile) {
		StringBuilder uriString = new StringBuilder("jar:");
		uriString.append(directory.toUri().toString());
		uriString.append(jarFile);
		return URI.create(uriString.toString());
	}

	/**
	 * Add a file from classpath to a jar filesystem
	 * @param templateDir the directory containing file
	 * @param filename the relative path to the file to be added
	 * @param jarFileSystem the jar file system (war/ear)
	 */
	protected void addClasspathFileToJarFile(String templateDir, String filename, FileSystem jarFileSystem) {
		InputStream inputStream = getClass().getResourceAsStream(templateDir + filename);
		if (inputStream == null) {
			logger.debug("file : {}{} not found in classpath", templateDir, filename);
			throw new TechnicalException("file " + templateDir + filename + " not found in classpath");
		}
		addInputStreamToJarFile(inputStream, filename, jarFileSystem);
	}

	/**
	 * Add a generated freemarker template to a jar filesystem
	 * @param templateDir the directory containing template file
	 * @param templateFile the relative path to the template (*.flt) to be added
	 * @param freemarkerModel the freemarker model to be used
	 * @param jarFileSystem the jar file system (war/ear)
	 */
	protected void addClasspathTemplateToJarFile(String templateDir, String templateFile, Map<String, Object> freemarkerModel, FileSystem jarFileSystem) {
		String sourceFile;
		if (templateFile.endsWith(".flt")) {
			sourceFile = templateFile.replaceFirst(".flt$", "");
		} else {
			throw new TechnicalException("template file " + templateFile + " do not end with .flt");
		}
		
		logger.debug("generating {} content with freemarker", templateFile);
		InputStream inputStream = generateFreemarkerContent(templateFile, freemarkerModel);
		addInputStreamToJarFile(inputStream, sourceFile, jarFileSystem);

	}

	/**
	 * Generate a freemarker content based on a template and a model
	 * @param templateFile the template file
	 * @param freemarkerModel the model
	 * @return an InputStream
	 */
	protected InputStream generateFreemarkerContent(String templateFile, Map<String, Object> freemarkerModel) {
		try {
			String fileContent = FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate(templateFile), freemarkerModel);
			logger.debug("generated content:\n{}", fileContent);
			InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
			return inputStream;
		} catch (IOException | TemplateException e) {
			throw new TechnicalException("cannot generate template file " + templateFile, e);
		}
	}

	/**
	 * add some content to jar file system
	 * @param inputStream the content to be added
	 * @param filename the filename (relative path) within jar file system
	 * @param jarFileSystem the jar file system (war/ear)
	 */
	private void addInputStreamToJarFile(InputStream inputStream, String filename, FileSystem jarFileSystem) {
		Path internalFile = jarFileSystem.getPath(filename);
		logger.debug("Adding file {} to {}", internalFile, jarFileSystem);
		try {
			Files.copy(inputStream, internalFile);
		} catch (IOException e) {
			throw new TechnicalException("cannot copy file " + filename + " in jar file " + jarFileSystem, e);
		}
	}


	/**
	 * Copy a file in jarFileSystem. Path must be valid in filesystem
	 * 
	 * @param dir
	 *            the directory containing file
	 * @param sourceFile
	 *            the file to be copied in jarFileSystem
	 * @param jarFileSystem
	 *            the jar file system
	 */
	protected void addAbsoluteFileToJarFile(Path dir, Path sourceFile, FileSystem jarFileSystem) {
		if (!sourceFile.startsWith(dir)) {
			throw new TechnicalException("source file " + sourceFile + " is not in directory " + dir);
		}
		Path internalFile = jarFileSystem.getPath(dir.relativize(sourceFile).toString());
		logger.debug("Adding file {} to {}", internalFile, jarFileSystem);
		try {
			Files.copy(sourceFile, internalFile);
		} catch (IOException e) {
			throw new TechnicalException("cannot copy file " + sourceFile + " in jar file " + jarFileSystem, e);
		}
	}

	/**
	 * Create a directory in jarFileSystem. Path must be valid in fileSystem
	 * 
	 * @param sourceDir
	 *            the directory to be created in jarFileSystem
	 * @param jarFileSystem
	 *            the jar file system
	 */
	protected void createDirectoryInJarFile(String sourceDir, FileSystem jarFileSystem) {
		Path internalDir = jarFileSystem.getPath(sourceDir);
		try {
			Files.createDirectory(internalDir);
		} catch (IOException e) {
			throw new TechnicalException("cannot create directory " + internalDir + " in jar file " + jarFileSystem, e);
		}
	}

	/**
	 * IOC
	 * 
	 * @param configuration
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

}
