package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static ch.admin.bit.jeap.initializer.TestUtils.addTestFileToFolder;
import static java.nio.file.Files.createDirectories;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupIdRenamerContributorTest {

    @TempDir
    private Path tempDir;

    private GroupIdRenamerContributor contributor = new GroupIdRenamerContributor();

    @Test
    void groupIdsAreRenamed() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        addTestFileToFolder("pom.xml", tempDir);
        createDirectories(Path.of(tempDir + "/bit-jme-app-service"));
        addTestFileToFolder("bit-jme-app-service/pom.xml", tempDir);


        ProjectTemplate template = new ProjectTemplate();
        template.setGroupId("ch.admin.bit.jme");
        ProjectRequest request = new ProjectRequest();
        request.setGroupId("com.foo");

        contributor.contribute(tempDir, request, template);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();

        Document document = builder.parse(new File(tempDir + "/pom.xml"));
        assertEquals("com.foo", xpath.evaluate("/project/groupId", document, XPathConstants.STRING));
        assertEquals("ch.admin.bit.jeap", xpath.evaluate("/project/parent/groupId", document, XPathConstants.STRING));

        document = builder.parse(new File(tempDir + "/bit-jme-app-service/pom.xml"));
        assertEquals("com.foo", xpath.evaluate("/project/parent/groupId", document, XPathConstants.STRING));
    }

}
