package cz.cvut.kbss.jopa.adapters;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import cz.cvut.kbss.jopa.sessions.UnitOfWorkImpl;

public class IndirectSet<E> extends IndirectCollection implements Set<E> {

	private Set<E> internalSet;

	public IndirectSet(Object owner, UnitOfWorkImpl uow, Set<E> referencedSet) {
		super(owner, uow);
		if (referencedSet == null) {
			throw new NullPointerException(
					"Null passed in as the referencedSet.");
		}
		this.internalSet = referencedSet;
	}

	public int size() {
		return internalSet.size();
	}

	public boolean isEmpty() {
		return internalSet.isEmpty();
	}

	public boolean contains(Object o) {
		return internalSet.contains(o);
	}

	public Iterator<E> iterator() {
		return internalSet.iterator();
	}

	public Object[] toArray() {
		return internalSet.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return internalSet.toArray(a);
	}

	public boolean add(E e) {
		boolean res = internalSet.add(e);
		if (res) {
			persistChange();
		}
		return res;
	}

	public boolean remove(Object o) {
		boolean res = internalSet.remove(o);
		if (res) {
			persistChange();
		}
		return res;
	}

	public boolean containsAll(Collection<?> c) {
		return internalSet.containsAll(c);
	}

	public boolean addAll(Collection<? extends E> c) {
		boolean res = internalSet.addAll(c);
		if (res) {
			persistChange();
		}
		return res;
	}

	public boolean retainAll(Collection<?> c) {
		boolean res = internalSet.retainAll(c);
		if (res) {
			persistChange();
		}
		return res;
	}

	public boolean removeAll(Collection<?> c) {
		boolean res = internalSet.removeAll(c);
		if (res) {
			persistChange();
		}
		return res;
	}

	public void clear() {
		internalSet.clear();
		persistChange();
	}
}
