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

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/cloud-archive-context.xml")
public class ManageArchiveImplTest {

    @Autowired
    private ManageArchiveImpl manageArchive;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path tempDir;

    private FileSystem jarFs;

    private URI jarUri;

    @Before
    public void generateJarFileSystem() {
        jarUri = manageArchive.generateJarUri(Paths.get(temporaryFolder.getRoot().toURI()), "test.jar");
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try {
            jarFs = FileSystems.newFileSystem(jarUri, env);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setTempDir() {
        tempDir = Paths.get(temporaryFolder.getRoot().toURI());
    }


    @After
    public void closeJarFileSystem() {
        try {
            jarFs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void check_jar_uri_is_valid() {
        Assert.assertEquals("bad scheme for a jar uri", "jar", jarUri.getScheme());
    }

    @Test
    public void add_existing_file_in_archive() {
        manageArchive.createDirectoryInJarFile("WEB-INF", jarFs);
        manageArchive.addClasspathFileToJarFile(ManageArchiveImpl.TEMPLATE_WAR_DIR, "WEB-INF/web.xml", jarFs);
        Assert.assertTrue("WEB-INF/web.xml should exist in jar file system", Files.exists(jarFs.getPath("WEB-INF", "web.xml")));
    }

    @Test
    public void add_valid_source_file_in_archive() throws IOException {
        File file = temporaryFolder.newFile("empty.txt");
        Path path = Paths.get(file.toURI());
        manageArchive.addAbsoluteFileToJarFile(tempDir, path, jarFs);
        Assert.assertTrue("empty.txt should exist in jar file system", Files.exists(jarFs.getPath("empty.txt")));
    }

    @Test(expected = TechnicalException.class)
    public void add_invalid_source_file_in_archive() throws IOException {
        Path path = Paths.get("dummy");
        manageArchive.addAbsoluteFileToJarFile(tempDir, path, jarFs);
    }

    @Test
    public void add_valid_directory_in_archive() throws IOException {
        manageArchive.createDirectoryInJarFile("emptydir", jarFs);
        Assert.assertTrue("emptydir should exist in jar file system", Files.exists(jarFs.getPath("emptydir")));
    }

    @Test(expected = TechnicalException.class)
    public void add_invalid_directory_in_archive() throws IOException {
        manageArchive.createDirectoryInJarFile("dir1/dir2", jarFs);
    }

    @Test
    public void add_valid_directory_and_file_in_archive() throws IOException {
        temporaryFolder.newFolder("dir");
        File file = temporaryFolder.newFile("dir/file.txt");
        Path dirFile = Paths.get(file.toURI());
        manageArchive.createDirectoryInJarFile("dir", jarFs);
        manageArchive.addAbsoluteFileToJarFile(tempDir, dirFile, jarFs);
        Assert.assertTrue("dir/file.txt should exist in jar file system", Files.exists(jarFs.getPath("dir", "file.txt")));
    }

    @Test(expected = TechnicalException.class)
    public void add_invalid_directory_and_file_archive() throws IOException {
        temporaryFolder.newFolder("otherdir");
        File file = temporaryFolder.newFile("otherdir/file.txt");
        Path dirFile = Paths.get(file.toURI());
        // cannot add a file if directory does not exist
        manageArchive.addAbsoluteFileToJarFile(tempDir, dirFile, jarFs);
    }

    @Test(expected = TechnicalException.class)
    public void generate_minimal_ear_with_invalid_maven_reference() {
        MavenReference mavenReference = new MavenReference("com.orange.demo", "demo", "1.0.0-SNAPSHOT", "xxx");
        manageArchive.generateMinimalEar(mavenReference, "demo");
    }

    @Test
    public void check_freemarker_generation_for_application_xml() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("warFilename", "test.war");
        model.put("contextRoot", "test");
        InputStream is = manageArchive.generateFreemarkerContent("META-INF/application.xml.flt", model);
        try (Scanner scanner = new Scanner(is)) {
            String result = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            Assert.assertTrue("<web-uri> not found in application.xml", result.indexOf("<web-uri>test.war</web-uri>") > 0);
            Assert.assertTrue("<context-root> not found in application.xml", result.indexOf("<context-root>test</context-root>") > 0);
        }
    }

    @Test
    public void check_freemarker_generation_for_index_html() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("groupid", "mygroupid");
        model.put("artifactid", "myartifactid");
        model.put("version", "myversion");
        model.put("classifier", null);
        model.put("extension", "ear");
        model.put("buildDate", Calendar.getInstance().getTime());
        InputStream is = manageArchive.generateFreemarkerContent("index.html.flt", model);
        try (Scanner scanner = new Scanner(is)) {
            String result = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            Assert.assertTrue("<web-uri> not found in application.xml", result.indexOf("myartifactid") > 0);
        }

    }

    @Test
    public void generate_minimal_ear_and_check_application_dot_xml() {
        MavenReference mavenReference = new MavenReference("com.orange.demo", "demo", "1.0.0-SNAPSHOT", "ear");
        File earFile = manageArchive.generateMinimalEar(mavenReference, "demo");
        Assert.assertTrue("ear file not found", earFile.isFile());
        assertArchiveIsFineAndCleanup(earFile,"META-INF/application.xml");
    }


    @Test
    public void should_generate_minimal_war_and_check_web_dot_xml_when_jar_ref_is_provided() {
        MavenReference mavenReference = new MavenReference("com.orange.demo", "demo-jar", "1.0.0-SNAPSHOT", "jar");
        File warFile = manageArchive.generateMinimalWar(mavenReference, "");
        assertArchiveIsFineAndCleanup(warFile, "WEB-INF/web.xml");
    }


    @Test
    public void should_generate_minimal_war_and_check_web_dot_xml() {
        MavenReference mavenReference = new MavenReference("com.orange.demo", "demo", "1.0.0-SNAPSHOT", "war");
        File warFile = manageArchive.generateMinimalWar(mavenReference, "");
        assertArchiveIsFineAndCleanup(warFile, "WEB-INF/web.xml");
    }

    private void assertArchiveIsFineAndCleanup(File archiveFile, String filenameThatShouldBeIncludedInArchive) {
        Assert.assertTrue("war file not found", archiveFile.isFile());
        // search application.xml with old java.util.zip package
        boolean fileFound = false;
        try (ZipFile zipFile = new ZipFile(archiveFile)) { // zipFile is automatically closed
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                String entryName = entry.getName();
                if (entryName.equals(filenameThatShouldBeIncludedInArchive)) {
                    fileFound = true;
                    break;
                }
            }
        } catch (IOException e) {
            Assert.fail("IOException when analyzing archive file : " + e);
        } finally {
            try {
                // delete war and temporary directory
                Path warPath = Paths.get(archiveFile.toURI());
                Files.delete(warPath);
                Files.delete(warPath.getParent());
            } catch (IOException e) {
                Assert.fail("cannot delete archive file : " + e);
            }
        }

        if (!fileFound) {
            Assert.fail(filenameThatShouldBeIncludedInArchive +"not found in file: "+archiveFile.getName());
        }
    }


}
