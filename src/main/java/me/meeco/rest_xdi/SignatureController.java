package me.meeco.rest_xdi;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.exceptions.Xdi2ParseException;
import xdi2.core.features.signatures.KeyPairSignature;
import xdi2.core.features.signatures.Signature;
import xdi2.core.features.signatures.Signatures;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.io.XDIReader;
import xdi2.core.io.XDIReaderRegistry;
import xdi2.core.io.XDIWriter;
import xdi2.core.io.XDIWriterRegistry;
import xdi2.core.xri3.XDI3Segment;

@RestController
@RequestMapping(value = "/signature")
public class SignatureController {

    private static Logger log = LoggerFactory.getLogger(SignatureController.class);

    // TODO - these shouldn't be hard coded
    public static final String DIGEST_ALGORITHM = "sha";
    public static final int DIGEST_LENGTH = 256;
    public static final String KEY_ALGORITHM = "rsa";
    public static final int KEY_LENGTH = 2048;
    public static final boolean SINGLETON = true;

    // Output parameters
    public static final boolean WRITE_IMPLIED = false;
    public static final boolean WRITE_ORDERED = true;
    public static final boolean WRITE_INNER = true;
    public static final boolean WRITE_PRETTY = false;
    public static final String OUTPUT_FORMAT = "XDI DISPLAY"; // or "XDI/JSON"

    // Example keys from http://xdi2.projectdanube.org/XDISigner
    public static final String SIGNING_KEY = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCqrkJswChCygqqOpjeEtYZ7GEEXwcVCAFu1D30qfk3eo7yVWRtCzYMMQGxbvkZ05aTqBtkqpOOE0O0u7uib36pw3TRl8vQPfBjXYvI/sy4TQv8HuAQopsjrawOlU04vdt+bfh6BR/7f1bJcUv3+Okh9fEoAQzzNWqcM8X+cvLGRt1Vv5u4DOFEa5lsK5/E50DKQBU+QW9P/+RZaw0bdC/H1xf9EcaGhsmpwKIW+TLBKLRwDeFhUXt7ILzrZawmytDTZ/Nfb/t0R5gKncyhxeeamFe2gebDOjLh7bhLdbSbBqe4dQX2NUe9QG4dQf4jy22V8Y2zjlJEyfn0pELurAn9AgMBAAECggEBAIfmVHFu1x/G65L7MTixWtQtSFpIp8TxKOLsD6C9rfekmCkQIPRKFvDCHI0AxUrxFFXhZl5TC0X2xNQlHpOJnxrgzCUObnQSvVMA6wpRBwRAJKjMlK/qKQjRgcviySfC0//o5A2UAxEnJR0kHs8E2+v0fd3SaFNGVuqktqORNwjzmgKBOqWk2uhbThWBqJEYdeqljFOGobiDugHBWfGEHNQIG7YEqKzMz/5Q6NVkPNQtDgBa4+21tyLEJ49wO8FG/Sxjq6y+5mY13SQ0fM617q8x1IbW2kzGu3P63CXwUw1izGEvnLjRU7wIjS2eLPGWMsUCGNGV/C72cU8ID0JVZKkCgYEA4Rdm57JOoDaJ2WfDnth2ED57ou7awY8h4rryiviqGo3RTAVcF5FJBY4k4ZpyG230PyrHEhhB6lxCkvEc+jo+yYqgK1KmppfZAQ2jcl8/PTWkVwirF8YQM3Kb9DayxDbM83kEzGmXrKEcMuMhJ/3BWVPwQOT8IZ4KBBkow6kjnX8CgYEAwh4q4FuUUNNfefn8xRtqgzwHhm3Nnj6+4/hjq2ZuhvbnvpLpQDv8qSi47TULG6PjVUl4wB0G8q1UOliEgwQhhN5lhpBZqFqo4Alrz7iUALkTvESPt2Sxd+aZngHhmJjJu5tTd4I+i92R+HT0CHR1p3Lfm2u0CrvfIZTUpVbqjoMCgYEAsodpKyQVkKUxOKpAUeDF46RrU5O3FgZ8jeRRM0B/SohpFK67mEW3cRyIzBc/odnX+7HmKsfqoAOFGh77KMzBuACngTUQ0NlnWJqEpNY+xkGhkxZg/X4uo1+nqk8oAtCkRggacjbeAiHWx9W2Go39qOgWiqIUCGXc89swpd+lS+kCgYB4t7IKXGlb6ldRz7j2CxquCkLTwq1AX9zugKXbDZRmsl1kEpCjtapmuEBoo7gItF7Hxy0kq+iKOmhK8IlXwNXnfza7/EEFhXvH95PoVe0UlgRD7I9DiYcj/XBC5wCYmUu7M9kwVPr4mA4S6QhpyaLxQ2rziIMqubMFezzSpb6waQKBgQC/AmYnjmeToGgGigYfX6g4L5qjs4+7fPSxDpWHWDTMqP8bDlxW2QFY/OkesbU177OaoayC3TIT+GOx/h15lbbCgJcGo1o7iWK5W4YXvbE4DpHfX/BQvMsTCEbuMVHcrOMvRlDiA52P3LHiq4N/Ky2lBqw2pg5kANDvZonmbxElmQ==";
    public static final String VERIFYING_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqq5CbMAoQsoKqjqY3hLWGexhBF8HFQgBbtQ99Kn5N3qO8lVkbQs2DDEBsW75GdOWk6gbZKqTjhNDtLu7om9+qcN00ZfL0D3wY12LyP7MuE0L/B7gEKKbI62sDpVNOL3bfm34egUf+39WyXFL9/jpIfXxKAEM8zVqnDPF/nLyxkbdVb+buAzhRGuZbCufxOdAykAVPkFvT//kWWsNG3Qvx9cX/RHGhobJqcCiFvkywSi0cA3hYVF7eyC862WsJsrQ02fzX2/7dEeYCp3MocXnmphXtoHmwzoy4e24S3W0mwanuHUF9jVHvUBuHUH+I8ttlfGNs45SRMn59KRC7qwJ/QIDAQAB";


    @RequestMapping(value = "/sign/{address}", method = RequestMethod.POST)
    public String sign(@PathVariable String address, @RequestBody String message) {

        log.info("Sign request request; message:\n{}", message);

        Graph graph = MemoryGraphFactory.getInstance().openGraph();
        XDIReader xdiReader = XDIReaderRegistry.getAuto();

        // Set writer properties that control output format
        Properties xdiResultWriterParameters = new Properties();
        xdiResultWriterParameters.setProperty(XDIWriterRegistry.PARAMETER_IMPLIED, WRITE_IMPLIED ? "1" : "0");
        xdiResultWriterParameters.setProperty(XDIWriterRegistry.PARAMETER_ORDERED, WRITE_ORDERED ? "1" : "0");
        xdiResultWriterParameters.setProperty(XDIWriterRegistry.PARAMETER_INNER, WRITE_INNER ? "1" : "0");
        xdiResultWriterParameters.setProperty(XDIWriterRegistry.PARAMETER_PRETTY, WRITE_PRETTY ? "1" : "0");

        XDIWriter xdiResultWriter = XDIWriterRegistry.forFormat(OUTPUT_FORMAT, xdiResultWriterParameters);

        try {
            xdiReader.read(graph, new StringReader(message));

            // Find the context node
            ContextNode contextNode = graph.getDeepContextNode(XDI3Segment.create(address), true);
            if (contextNode == null) {
                throw new RuntimeException("No context node found at address " + address);
            }

            Signature<?, ?> signature = Signatures.createSignature(contextNode, DIGEST_ALGORITHM,
                    DIGEST_LENGTH, KEY_ALGORITHM, KEY_LENGTH, SINGLETON);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(SIGNING_KEY));
            try {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                Key k = keyFactory.generatePrivate(keySpec);

                ((KeyPairSignature) signature).sign((PrivateKey) k);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            StringWriter writer = new StringWriter();
            xdiResultWriter.write(graph, writer);

            String output = writer.getBuffer().toString();

            log.info("Signed output:\n{}", output);
            return output;

        } catch (IOException e) {
            e.printStackTrace();
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Xdi2ParseException e) {
            e.printStackTrace();
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}