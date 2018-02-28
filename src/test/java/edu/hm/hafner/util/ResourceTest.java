package edu.hm.hafner.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.google.errorprone.annotations.MustBeClosed;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Base class for tests that need to read resource files from disk. Provides several useful methods that simplify
 * reading of resources from disk.
 *
 * @author Ullrich Hafner
 */
public abstract class ResourceTest {
    /**
     * Reads the contents of the desired resource. The rules for searching resources associated with this test class are
     * implemented by the defining {@linkplain ClassLoader class loader} of this test class.  This method delegates to
     * this object's class loader.  If this object was loaded by the bootstrap class loader, the method delegates to
     * {@link ClassLoader#getSystemResource}.
     * <p>
     * Before delegation, an absolute resource name is constructed from the given resource name using this algorithm:
     * <p>
     * <ul>
     *     <li> If the {@code name} begins with a {@code '/'} (<tt>'&#92;u002f'</tt>), then the absolute name of the
     * resource is the portion of the {@code name} following the {@code '/'}.</li>
     *     <li> Otherwise, the absolute name is of the following form:
     *     <blockquote> {@code modified_package_name/name} </blockquote>
     *     <p> Where the {@code modified_package_name} is the package name of this object with {@code '/'}
     *     substituted for {@code '.'} (<tt>'&#92;u002e'</tt>).</li>
     *  </ul>
     *
     * @param fileName
     *         name of the desired resource
     *
     * @return the content represented by a byte array
     */
    protected byte[] readAllBytes(final String fileName) {
        try {
            return Files.readAllBytes(getPath(fileName));
        }
        catch (IOException | URISyntaxException e) {
            throw new AssertionError("Can't read resource " + fileName, e);
        }
    }

    @SuppressFBWarnings("UI_INHERITANCE_UNSAFE_GETRESOURCE")
    private Path getPath(final String name) throws URISyntaxException {
        URL resource = getClass().getResource(name);
        if (resource == null) {
            throw new AssertionError("Can't find resource " + name);
        }
        return Paths.get(resource.toURI());
    }

    /**
     * Read all lines from the desired resource as a {@code Stream}, i.e. this method populates lazily as the stream is
     * consumed. <p> Bytes from the resource are decoded into characters using UTF-8 and the same line terminators as
     * specified by {@link Files#readAllLines(Path, Charset)} are supported.</p>
     *
     * @param fileName
     *         name of the desired resource
     *
     * @return the content represented as a {@link Stream} of lines
     */
    @MustBeClosed
    protected Stream<String> asStream(final String fileName) {
        return asStream(fileName, StandardCharsets.UTF_8);
    }

    /**
     * Read all lines from the desired resource as a {@code Stream}, i.e. this method populates lazily as the stream is
     * consumed. <p> Bytes from the resource are decoded into characters using the specified charset and the same line
     * terminators as specified by {@link Files#readAllLines(Path, Charset)} are supported.</p>
     *
     * @param fileName
     *         name of the desired resource
     * @param charset
     *         the charset to use for decoding
     *
     * @return the content represented as a {@link Stream} of lines
     */
    @MustBeClosed
    protected Stream<String> asStream(final String fileName, final Charset charset) {
        try {
            return Files.lines(getPath(fileName), charset);
        }
        catch (IOException | URISyntaxException e) {
            throw new AssertionError("Can't read resource " + fileName, e);
        }
    }

    /**
     * Finds a resource with the given name and returns an input stream with UTF-8 decoding.
     *
     * @param fileName
     *         name of the desired resource
     *
     * @return the content represented as an {@link InputStream}
     */
    protected InputStream asInputStream(final String fileName) {
        InputStream stream = getTestResourceClass().getResourceAsStream(fileName);

        if (stream == null) {
            throw new AssertionError("Can't find resource " + fileName);
        }

        return stream;
    }

    /**
     * Returns the class that should be used to read the resource files of a test.
     *
     * @return default value is the actual test class
     */
    protected Class<?> getTestResourceClass() {
        return getClass();
    }

    /**
     * Finds a resource with the given name and returns the content (decoded with UTF-8) as String.
     *
     * @param fileName
     *         name of the desired resource
     *
     * @return the content represented as a {@link Stream} of lines
     */
    protected String toString(final String fileName) {
        return new String(readAllBytes(fileName), StandardCharsets.UTF_8);
    }

    /**
     * Read all lines from the specified text String as a {@code Stream}.
     *
     * @param text
     *         the text to return as {@link Stream} of lines
     *
     * @return the content represented by a byte array
     */
    @SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed"})
    protected Stream<String> getTextLinesAsStream(final String text) {
        return new BufferedReader(new StringReader(text)).lines();
    }
}
