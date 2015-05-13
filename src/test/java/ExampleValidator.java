import org.apache.xerces.parsers.SAXParser;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(Parallelized.class)
public class ExampleValidator {

    private static List<String> ENABLED_SAX_FEATURES = Arrays.asList(
            "http://xml.org/sax/features/namespaces",
            "http://xml.org/sax/features/namespace-prefixes",
            "http://xml.org/sax/features/validation",
            "http://apache.org/xml/features/validation/schema",
            "http://apache.org/xml/features/validation/schema-full-checking"
    );

    private static File EXAMPLES_DIRECTORY = new File("examples");

    private SAXParser parser = new SAXParser();

    private static ErrorHandler SAX_ERROR_HANDLER = new ErrorHandler() {
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
    };

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

    private static void copySchemaFiles(File destinationDirectory) throws
            IOException {
        for (String schemaFileName : SCHEMA_FILE_NAMES) {
            File source = new File(SCHEMA_DIRECTORY, schemaFileName);
            File destination = new File(destinationDirectory, schemaFileName);
            Files.copy(source.toPath(), destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        copySchemaFiles(EXAMPLES_DIRECTORY);
    }

    @Before
    public void setUp() throws Exception {
        parser.setErrorHandler(SAX_ERROR_HANDLER);

        for (String feature : ENABLED_SAX_FEATURES) {
            parser.setFeature(feature, true);
        }

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

        parser.parse(fileName);
    }
}
