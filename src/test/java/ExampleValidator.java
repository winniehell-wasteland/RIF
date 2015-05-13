import org.apache.xerces.parsers.SAXParser;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class ExampleValidator {

    private static List<String> ENABLED_SAX_FEATURES = Arrays.asList(
            "http://xml.org/sax/features/namespaces",
            "http://xml.org/sax/features/namespace-prefixes",
            "http://xml.org/sax/features/validation",
            "http://apache.org/xml/features/validation/schema",
            "http://apache.org/xml/features/validation/schema-full-checking"
    );

    private static File EXAMPLES_DIRECTORY = new File("examples");

    private static SAXParser PARSER = new SAXParser();

    private static File SCHEMA_DIRECTORY = new File("schema");

    private static List<String> SCHEMA_FILE_NAMES = Arrays.asList(
            "rif.xsd",
            "rif-xhtml.xsd"
    );

    @Parameterized.Parameter(0)
    public File exampleFile;

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> generateData() throws IOException {
        File[] files = EXAMPLES_DIRECTORY.listFiles();

        Assert.assertNotNull(files);

        Arrays.sort(files);

        ArrayList<Object[]> parameterValues = new ArrayList<>();
        for (File exampleFile : files) {
            if (exampleFile.getName().endsWith("xml")) {
                parameterValues.add(new Object[]{exampleFile});
            }
        }

        return parameterValues;
    }

    @BeforeClass
    public static void setUp() throws Exception {
        for (String feature : ENABLED_SAX_FEATURES) {
            PARSER.setFeature(feature, true);
        }

        PARSER.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException e) throws SAXException {
                Assert.fail(e.toString());
            }

            @Override
            public void error(SAXParseException e) throws SAXException {
                Assert.fail(e.toString());
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                Assert.fail(e.toString());
            }
        });

        copySchemaFiles(EXAMPLES_DIRECTORY);
    }

    private static void copySchemaFiles(File destinationDirectory) throws
            IOException {
        for (String schemaFileName : SCHEMA_FILE_NAMES) {
            File source = new File(SCHEMA_DIRECTORY, schemaFileName);
            File destination = new File(destinationDirectory, schemaFileName);
            Files.copy(source.toPath(), destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Before
    public void setUpDirectory() throws IOException {
        if (this.exampleFile.isDirectory()) {
            copySchemaFiles(this.exampleFile);
        }
    }

    @Test
    public void validate() throws Exception {
        String fileName;

        if (this.exampleFile.isDirectory()) {
            fileName = new File(this.exampleFile, "rif_ef.xml").toString();
        } else {
            fileName = this.exampleFile.toString();
        }

        PARSER.parse(fileName);
    }

    @After
    public void tearDownDirectory() {
        if (this.exampleFile.isDirectory()) {
            deleteSchemaFiles(this.exampleFile);
        }
    }

    @AfterClass
    public static void tearDown() throws IOException {
        deleteSchemaFiles(EXAMPLES_DIRECTORY);
    }

    private static void deleteSchemaFiles(File directory) {
        for (String schemaFileName : SCHEMA_FILE_NAMES) {
            File schemaFile = new File(directory, schemaFileName);
            Assert.assertTrue(schemaFile.delete());
        }
    }
}
