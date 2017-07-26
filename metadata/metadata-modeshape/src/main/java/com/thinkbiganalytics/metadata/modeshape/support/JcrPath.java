/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.support;

/*-
 * #%L
 * thinkbig-metadata-modeshape
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Really just a generic path implementation but currently being used in the JCR utilities.
 */
public class JcrPath implements Path {

    private final List<String> elements;
    private final boolean absolute;

    public JcrPath(boolean absolute, List<String> elements) {
        this.elements = Collections.unmodifiableList(elements);
        this.absolute = absolute;
    }

    public JcrPath(boolean absolute, String element) {
        this.elements = Collections.singletonList(element);
        this.absolute = absolute;
    }

    public static Path get(String first, String... more) {
        ArrayList<String> list = new ArrayList<>();
        boolean absolute = first.trim().startsWith("/");
        String[] firstElems = first.split("/");

        for (int idx = (absolute ? 1 : 0); idx < firstElems.length; idx++) {
            if (StringUtils.isNotBlank(firstElems[idx])) {
                list.add(firstElems[idx]);
            }
        }

        for (String another : more) {
            String[] anotherElems = another.split("/");

            for (String elem : anotherElems) {
                if (StringUtils.isNotBlank(elem)) {
                    list.add(elem);
                }
            }
        }

        return new JcrPath(absolute, list);
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#getFileSystem()
     */
    @Override
    public FileSystem getFileSystem() {
        return null;
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#isAbsolute()
     */
    @Override
    public boolean isAbsolute() {
        return this.absolute;
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#getRoot()
     */
    @Override
    public Path getRoot() {
        return this.absolute ? new JcrPath(true, Collections.emptyList()) : null;
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#getFileName()
     */
    @Override
    public Path getFileName() {
        return this.elements.size() > 0 ? new JcrPath(false, this.elements.get(this.elements.size() - 1)) : null;
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#getParent()
     */
    @Override
    public Path getParent() {
        if (this.elements.size() > 1) {
            return new JcrPath(this.absolute, this.elements.subList(0, this.elements.size() - 1));
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#getNameCount()
     */
    @Override
    public int getNameCount() {
        return this.elements.size();
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#getName(int)
     */
    @Override
    public Path getName(int index) {
        if (index < 0 || index > this.elements.size() - 1) {
            throw new IllegalArgumentException("Index is out of bounds: " + index);
        } else if (index == 0) {
            return new JcrPath(this.isAbsolute(), this.elements.get(0));
        } else {
            return new JcrPath(false, this.elements.get(index));
        }
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#subpath(int, int)
     */
    @Override
    public Path subpath(int beginIndex, int endIndex) {
        if (beginIndex < 0 || beginIndex > this.elements.size() - 1 || endIndex < beginIndex || endIndex > this.elements.size()) {
            throw new IllegalArgumentException("Invalid indices bounds: " + beginIndex + ", " + endIndex);
        } else {
            return new JcrPath(this.isAbsolute() && beginIndex == 0, this.elements.subList(beginIndex, endIndex));
        }
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#startsWith(java.nio.file.Path)
     */
    @Override
    public boolean startsWith(Path other) {
        if (other.isAbsolute() != isAbsolute()) {
            return false;
        } else if (other.getNameCount() > getNameCount()) {
            return false;
        } else {
            for (int idx = 0; idx < other.getNameCount(); idx++) {
                Path otherElem = other.getName(idx);

                if (otherElem.getFileName().equals(this.elements.get(idx))) {
                    return false;
                }
            }

            return true;
        }
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#startsWith(java.lang.String)
     */
    @Override
    public boolean startsWith(String other) {
        Path otherPath = get(other);
        return startsWith(otherPath);
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#endsWith(java.nio.file.Path)
     */
    @Override
    public boolean endsWith(Path other) {
        int offset = getNameCount() - other.getNameCount();

        if (offset < 0) {
            return false;
        } else {
            for (int idx = getNameCount() - 1; idx - offset >= 0; idx--) {
                Path otherElem = other.getName(idx - offset);

                if (otherElem.getFileName().equals(this.elements.get(idx))) {
                    return false;
                }
            }

            return true;
        }
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#endsWith(java.lang.String)
     */
    @Override
    public boolean endsWith(String other) {
        Path otherPath = get(other);
        return startsWith(otherPath);
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#normalize()
     */
    @Override
    public Path normalize() {
        return this;
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#resolve(java.nio.file.Path)
     */
    @Override
    public Path resolve(Path other) {
        if (other.isAbsolute()) {
            return other;
        } else if (other.getNameCount() == 0) {
            return this;
        } else {
            List<String> elements = new ArrayList<>(this.elements);
            for (int idx = 0; idx < other.getNameCount(); idx++) {
                elements.add(other.getName(idx).toString());
            }
            return new JcrPath(this.absolute, elements);
        }
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#resolve(java.lang.String)
     */
    @Override
    public Path resolve(String other) {
        Path otherPath = get(other);
        return resolve(otherPath);
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#resolveSibling(java.nio.file.Path)
     */
    @Override
    public Path resolveSibling(Path other) {
        if (other.isAbsolute()) {
            return other;
        } else if (other.getNameCount() == 0) {
            return this;
        } else {
            return getParent().resolve(other);
        }
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#resolveSibling(java.lang.String)
     */
    @Override
    public Path resolveSibling(String other) {
        Path otherPath = get(other);
        return resolveSibling(otherPath);
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#relativize(java.nio.file.Path)
     */
    @Override
    public Path relativize(Path other) {
        if (other.isAbsolute() != isAbsolute() || other.getNameCount() < getNameCount()) {
            return other;
        } else if (other.getNameCount() == 0) {
            return this;
        } else {
            int idx = 0;
            for (; idx < getNameCount(); idx++) {
                if (!other.getName(idx).equals(getName(idx))) {
                    return other;
                }
            }
            return other.subpath(idx - 1, other.getNameCount());
        }
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#toUri()
     */
    @Override
    public URI toUri() {
        return URI.create(toString());
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#toAbsolutePath()
     */
    @Override
    public Path toAbsolutePath() {
        // TODO Support a root node?
        return get("/").resolve(this);
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#toRealPath(java.nio.file.LinkOption[])
     */
    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return this;
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#toFile()
     */
    @Override
    public File toFile() {
        return new File(toUri());
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#register(java.nio.file.WatchService, java.nio.file.WatchEvent.Kind[], java.nio.file.WatchEvent.Modifier[])
     */
    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("Register not supported");
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#register(java.nio.file.WatchService, java.nio.file.WatchEvent.Kind[])
     */
    @Override
    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException("Register not supported");
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#iterator()
     */
    @Override
    public Iterator<Path> iterator() {
        return this.elements.stream().map(e -> new JcrPath(false, e)).collect(Collectors.<Path>toList()).iterator();
    }

    /* (non-Javadoc)
     * @see java.nio.file.Path#compareTo(java.nio.file.Path)
     */
    @Override
    public int compareTo(Path other) {
        int len = Math.min(other.getNameCount(), getNameCount());

        for (int idx = 0; idx < len; idx++) {
            int value = other.getName(idx).toString().compareTo(getName(idx).toString());
            if (value != 0) {
                return value;
            }
        }

        return Integer.compare(other.getNameCount(), getNameCount());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Path) {
            Path other = (Path) obj;
            if (isAbsolute() == other.isAbsolute()) {
                return compareTo(other) == 0;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Boolean.hashCode(this.absolute)
               + this.elements.stream()
                   .mapToInt(String::hashCode)
                   .reduce((h, e) -> h + e)
                   .orElse(0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder bldr = new StringBuilder();
        if (this.absolute) {
            bldr.append("/");
        }
        if (this.elements.size() > 0) {
            bldr.append(this.elements.get(0));
        }

        for (int idx = 1; idx < this.elements.size(); idx++) {
            bldr.append("/").append(this.elements.get(idx));
        }

        return bldr.toString();
    }
}
