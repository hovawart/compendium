package com.nobodyelses.data.model;

import java.util.List;

import com.maintainer.data.model.EntityImpl;
import com.maintainer.util.Utils;

@SuppressWarnings("serial")
public class CodeEntityImpl extends EntityImpl implements Comparable<CodeEntityImpl> {
    protected String name;
    protected String description;
    protected long sequence;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean validate(List<String> errors) {
        if (Utils.isEmpty(name)) {
            errors.add("A code name is required.");
        }

        return errors.isEmpty();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(CodeEntityImpl o) {
        return name.compareTo(o.getName());
    }
}
