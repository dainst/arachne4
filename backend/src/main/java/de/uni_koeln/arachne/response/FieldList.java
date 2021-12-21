package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FieldList extends AbstractContent implements Iterable<String> {
	
	private transient final List<String> value;
	
	public FieldList() {
		this.value = new ArrayList<String>();
	}
	
	@XmlElementWrapper
	public List<String> getValue() {
		return this.value;
	}
	
	public void add(final String value) {
		if(!this.value.contains(value)) {
			this.value.add(value);
		}
	}
	
	public String get(final int index) {
		return this.value.get(index);
	}
	
	public void modify(final int index, final String value) {
		this.value.set(index, value);
	}
	
	public int size() {
		return this.value.size();
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		for (final String currentValue: value) {
			result.append(currentValue);
			result.append(' ');
		}
				
		return result.toString().trim();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final FieldList other = (FieldList) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else {
			if (!value.equals(other.value)) {
				return false;
			}
		}
		return true;
	}

	@Override
    public Iterator<String> iterator() {
        Iterator<String> it = new Iterator<String>() {

            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < value.size() && value.get(currentIndex) != null;
            }

            @Override
            public String next() {
                return value.get(currentIndex++);
            }

            @Override
            public void remove() {
                value.remove(currentIndex);
            }
        };
        return it;
	}
}
