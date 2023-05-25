package pt.unl.fct.di.hyflexchain.util.collections;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class InsertionOrderedMap<K, V> extends AbstractMap<K, V>
{
	protected final Map<K, V> map;

	protected final List<Entry<K, V>> list;

	protected List<Entry<K, V>> viewList;
	
	protected List<V> viewValuesList;

	protected Set<Entry<K, V>> viewEntrySet;

	/**
	 * 
	 */
	public InsertionOrderedMap(
			Supplier<Map<K, V>> mapSupplier,
			Supplier<List<Map.Entry<K, V>>> listSupplier
		)
	{
		this.map = mapSupplier.get();
		this.list = listSupplier.get();
	}

	/**
	 * Create a new Map with an initial capacity
	 * using an underlying hash map and a linked list.
	 */
	public InsertionOrderedMap(int initialCapacity)
	{
		this.map = new HashMap<>(initialCapacity);
		this.list = new LinkedList<>();
	}

	@Override
	public void clear() {
		this.map.clear();
		this.list.clear();
	}

	@Override
	public int size() {
		return this.map.size();
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.map.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return this.map.get(key);
	}

	@Override
	public V put(K key, V value)
	{
		V old = this.map.put(key, value);
		
		if (old != null)
			this.list.remove(Map.entry(key, old));

		this.list.add(Map.entry(key, value));

		return old;
	}

	@Override
	public V remove(Object key) {
		V res = this.map.remove(key);

		if (res != null)
			this.list.remove(Map.entry(key, res));

		return res;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		if (this.viewEntrySet == null)
			this.viewEntrySet = new EntrySetOverMap();
		
		return this.viewEntrySet;
	}

	/**
	 * Return a view of this map as an unmodifiable list.
	 * @return A view of this map as an unmodifiable list.
	 */
	public List<Entry<K, V>> asList()
	{
		if (this.viewList == null)
			this.viewList = Collections.unmodifiableList(new OrderedViewList());

		return this.viewList;
	}

	/**
	 * Return a view of this map values as an unmodifiable list.
	 * @return A view of this map values as an unmodifiable list.
	 */
	public List<V> valuesList()
	{
		if (this.viewValuesList == null)
			this.viewValuesList = Collections.unmodifiableList(new OrderedViewValuesList());

		return this.viewValuesList;
	}

	protected class EntrySetOverMap extends AbstractSet<Entry<K, V>>
	{

		@Override
		public int size() {
			return asList().size();
		}

		@Override
		public boolean isEmpty() {
			return asList().isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return asList().contains(o);
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return asList().iterator();
		}

		@Override
		public Object[] toArray() {
			return asList().toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return asList().toArray(a);
		}

		@Override
		public boolean add(Entry<K, V> e) {
			return InsertionOrderedMap.this.put(e.getKey(), e.getValue()) == null;
		}

		@Override
		public boolean remove(Object o) {
			if (InsertionOrderedMap.this.map.entrySet().remove(o))
				return InsertionOrderedMap.this.list.remove(o);
				
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return asList().containsAll(c);
		}

		@Override
		public void clear() {
			InsertionOrderedMap.this.clear();
		}
		
	}

	/**
	 * An unmodifiable ordered view list of the elements of this map. 
	 */
	protected class OrderedViewList implements List<Entry<K, V>>
	{

		@Override
		public int size() {
			return InsertionOrderedMap.this.size();
		}

		@Override
		public boolean isEmpty() {
			return InsertionOrderedMap.this.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return InsertionOrderedMap.this.list.contains(o);
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return InsertionOrderedMap.this.list.iterator();
		}

		@Override
		public Object[] toArray() {
			return InsertionOrderedMap.this.list.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return InsertionOrderedMap.this.list.toArray(a);
		}

		@Override
		public boolean add(Entry<K, V> e) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'add'");
		}

		@Override
		public boolean remove(Object o) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'remove'");
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return InsertionOrderedMap.this.list.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends Entry<K, V>> c) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'addAll'");
		}

		@Override
		public boolean addAll(int index, Collection<? extends Entry<K, V>> c) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'addAll'");
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'removeAll'");
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'retainAll'");
		}

		@Override
		public void clear() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'clear'");
		}

		@Override
		public Entry<K, V> get(int index) {
			return InsertionOrderedMap.this.list.get(index);
		}

		@Override
		public Entry<K, V> set(int index, Entry<K, V> element) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'set'");
		}

		@Override
		public void add(int index, Entry<K, V> element) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'add'");
		}

		@Override
		public Entry<K, V> remove(int index) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'remove'");
		}

		@Override
		public int indexOf(Object o) {
			return InsertionOrderedMap.this.list.indexOf(o);
		}

		@Override
		public int lastIndexOf(Object o) {
			return InsertionOrderedMap.this.list.lastIndexOf(o);
		}

		@Override
		public ListIterator<Entry<K, V>> listIterator() {
			return InsertionOrderedMap.this.list.listIterator();
		}

		@Override
		public ListIterator<Entry<K, V>> listIterator(int index) {
			return InsertionOrderedMap.this.list.listIterator(index);
		}

		@Override
		public List<Entry<K, V>> subList(int fromIndex, int toIndex) {
			return InsertionOrderedMap.this.list.subList(fromIndex, toIndex);
		}
	}

	protected class OrderedViewValuesList extends AbstractList<V>
	{
		@Override
		public int size() {
			return InsertionOrderedMap.this.size();
		}

		@Override
		public boolean isEmpty() {
			return InsertionOrderedMap.this.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return InsertionOrderedMap.this.list.contains(o);
		}

		@Override
		public Iterator<V> iterator() {
			return InsertionOrderedMap.this.map.values().iterator();
		}

		@Override
		public Object[] toArray() {
			return InsertionOrderedMap.this.map.values().toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return InsertionOrderedMap.this.map.values().toArray(a);
		}

		@Override
		public boolean add(V e) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'add'");
		}

		@Override
		public boolean remove(Object o) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'remove'");
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return InsertionOrderedMap.this.map.values().containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends V> c) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'addAll'");
		}

		@Override
		public boolean addAll(int index, Collection<? extends V> c) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'addAll'");
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'removeAll'");
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'retainAll'");
		}

		@Override
		public void clear() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'clear'");
		}

		@Override
		public V get(int index) {
			var entry = InsertionOrderedMap.this.list.get(index);

			if (entry == null)
				return null;

			return entry.getValue();
		}

		@Override
		public V set(int index, V element) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'set'");
		}

		@Override
		public void add(int index, V element) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'add'");
		}

		@Override
		public V remove(int index) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'remove'");
		}

		@Override
		public int indexOf(Object o) {
			return super.indexOf(o);
		}

		@Override
		public int lastIndexOf(Object o) {
			return super.lastIndexOf(o);
		}

		@Override
		public ListIterator<V> listIterator() {
			return new ListItr(InsertionOrderedMap.this.asList().listIterator());
		}

		@Override
		public ListIterator<V> listIterator(int index) {
			return new ListItr(InsertionOrderedMap.this.asList().listIterator(index));
		}

		@Override
		public List<V> subList(int fromIndex, int toIndex) {
			return super.subList(fromIndex, toIndex);
		}

		private class ListItr implements ListIterator<V>
		{
			private final ListIterator<Entry<K, V>> entriesIt;

			/**
			 * @param entriesIt
			 */
			public ListItr(ListIterator<Entry<K, V>> entriesIt) {
				this.entriesIt = entriesIt;
			}

			@Override
			public boolean hasNext() {
				return this.entriesIt.hasNext();
			}

			@Override
			public V next() {
				return this.entriesIt.next().getValue();
			}

			@Override
			public boolean hasPrevious() {
				return this.entriesIt.hasPrevious();
			}

			@Override
			public V previous() {
				return this.entriesIt.previous().getValue();
			}

			@Override
			public int nextIndex() {
				return this.entriesIt.nextIndex();
			}

			@Override
			public int previousIndex() {
				return this.entriesIt.previousIndex();
			}

			@Override
			public void remove() {
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException("Unimplemented method 'remove'");
			}

			@Override
			public void set(V e) {
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException("Unimplemented method 'set'");
			}

			@Override
			public void add(V e) {
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException("Unimplemented method 'add'");
			}
			
		}
	}

	
}
