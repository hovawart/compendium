package com.nobodyelses.data.model;

import com.maintainer.data.model.Autocreate;
import com.maintainer.data.model.EntityImpl;
import com.maintainer.data.model.Resource;

@Resource(name="files")
@Autocreate(id="name")
public class File extends EntityImpl implements Comparable<File> {
    private String name;
    private String content;
    private Long size;

    protected File() {}

    public File(final String name, final String content) {
        this.name = name;
        this.content = content;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setContents(final String contents) {
        this.content = contents;
    }

    public String getContent() {
        return this.content;
    }

    public void setSize(final Long size) {
        this.size = size;
    }

    public Long getSize() {
        return size;
    }

    @Override
    public int compareTo(final File other) {
        return this.name.compareTo(other.name);
    }
}
