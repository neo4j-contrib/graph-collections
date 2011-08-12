/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.collections.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

class List implements java.util.List<Node> {

	class ElementIterator implements Iterator<Node> {

		protected Node currentElement = underlyingNode;
		protected int index = -1;

		ElementIterator(int index) {
			if (index > -1) {
				currentElement = outer.get(index);
				this.index = index;
			}
		}

		@Override
		public boolean hasNext() {
			if (currentElement.getId() == underlyingNode.getId()) {
				return currentElement.hasRelationship(RelTypes.FIRST_ELEMENT,
						Direction.OUTGOING);
			} else {
				return currentElement.hasRelationship(RelTypes.NEXT_ELEMENT,
						Direction.OUTGOING);
			}
		}

		@Override
		public Node next() {
			if (!hasNext())
				throw new NoSuchElementException();
			if (currentElement.getId() == underlyingNode.getId()) {
				currentElement = underlyingNode.getSingleRelationship(
						RelTypes.FIRST_ELEMENT, Direction.OUTGOING)
						.getEndNode();
			} else {
				currentElement = currentElement.getSingleRelationship(
						RelTypes.NEXT_ELEMENT, Direction.OUTGOING).getEndNode();
			}
			index++;
			return currentElement;
		}

		public boolean hasPrevious() {
			if (currentElement == underlyingNode) {
				return false;
			} else {
				return currentElement.hasRelationship(RelTypes.NEXT_ELEMENT,
						Direction.INCOMING);
			}
		}
		
		public Node previous() {
			if (!hasPrevious())
				throw new NoSuchElementException();
			currentElement = currentElement.getSingleRelationship(
					RelTypes.NEXT_ELEMENT, Direction.INCOMING).getStartNode();
			index--;
			return currentElement;
		}

		@Override
		public void remove() {
			if(hasPrevious()) {
				previous();
				outer.remove(index + 1);
				
			}else{
				outer.remove(index);
				currentElement = underlyingNode;
				index = -1;
			}
		}
	}
	class ElementListIterator extends ElementIterator implements
			ListIterator<Node> {

		ElementListIterator(int index) {
			super(index);
		}

		@Override
		public void add(Node e) {
			outer.add(index, e);
		}

		@Override
		public int nextIndex() {
			return index + 1;
		}


		@Override
		public int previousIndex() {
			return index - 1;
		}

		@Override
		public void set(Node e) {
			outer.set(index, e);

		}
	}

	class NodeIterator extends ElementIterator {

		NodeIterator(int index) {
			super(index);
		}

		@Override
		public Node next() {
			return super
					.next()
					.getSingleRelationship(RelTypes.LIST_ENTRY,
							Direction.OUTGOING).getEndNode();
		}
	}

	class NodeListIterator extends ElementListIterator {

		NodeListIterator(int index) {
			super(index);
		}

		@Override
		public Node next() {
			return super
					.next()
					.getSingleRelationship(RelTypes.LIST_ENTRY,
							Direction.OUTGOING).getEndNode();
		}

		@Override
		public Node previous() {
			return super
					.previous()
					.getSingleRelationship(RelTypes.LIST_ENTRY,
							Direction.OUTGOING).getEndNode();
		}
	}

	public static enum RelTypes implements RelationshipType {
		/**
		 * A relationship which goes from the list node to the first element in
		 * the list
		 */
		FIRST_ELEMENT,

		/**
		 * A relationship which goes from the list node to the last element in
		 * the list
		 */
		LAST_ELEMENT,

		/**
		 * A relationship which points to the next entry in the list
		 */
		NEXT_ELEMENT,

		/**
		 * A relationship which points to the node added as entry in the list
		 */
		LIST_ENTRY
	}

	class ReverseElementIterator implements Iterator<Node> {

		protected Node currentElement = underlyingNode;

		@Override
		public boolean hasNext() {
			if (currentElement.getId() == underlyingNode.getId()) {
				return currentElement.hasRelationship(RelTypes.LAST_ELEMENT,
						Direction.OUTGOING);
			} else {
				return currentElement.hasRelationship(RelTypes.NEXT_ELEMENT,
						Direction.INCOMING);
			}
		}

		@Override
		public Node next() {
			if (!hasNext())
				throw new NoSuchElementException();
			if (currentElement.getId() == underlyingNode.getId()) {
				currentElement = underlyingNode.getSingleRelationship(
						RelTypes.LAST_ELEMENT, Direction.OUTGOING)
						.getEndNode();
			} else {
				currentElement = currentElement.getSingleRelationship(
						RelTypes.NEXT_ELEMENT, Direction.INCOMING)
						.getStartNode();
			}
			return currentElement;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	};

	class ReverseList implements Iterable<Node> {

		@Override
		public Iterator<Node> iterator() {
			return new ReverseNodeIterator();
		}

	}

	class ReverseNodeIterator extends ReverseElementIterator {

		@Override
		public Node next() {
			return super
					.next()
					.getSingleRelationship(RelTypes.LIST_ENTRY,
							Direction.OUTGOING).getEndNode();
		}
	}

	protected Node underlyingNode;

	protected GraphDatabaseService graphDb;

	private List outer = this;

	static final String INDEX_SIZE = "index_size";

	public List(Node listNode, GraphDatabaseService graphDb) {
		this.graphDb = graphDb;
		this.underlyingNode = listNode;
	}

	@Override
	public void add(int index, Node element) {
		if (index < 0 || index >= size())
			throw new IndexOutOfBoundsException();
		Relationship currentEntryRelation = underlyingNode
				.getSingleRelationship(RelTypes.FIRST_ELEMENT,
						Direction.OUTGOING);
		Node currentEntry = currentEntryRelation.getEndNode();
		if (index == 0) {
			currentEntryRelation.delete();
			Node newEntry = graphDb.createNode();
			underlyingNode.createRelationshipTo(newEntry,
					RelTypes.FIRST_ELEMENT);
			newEntry.createRelationshipTo(currentEntry, RelTypes.NEXT_ELEMENT);
			newEntry.createRelationshipTo(element, RelTypes.LIST_ENTRY);
			underlyingNode.setProperty(INDEX_SIZE, 1);
		} else {
			for (int i = 0; i < index; i++) {
				currentEntryRelation = currentEntry.getSingleRelationship(
						RelTypes.NEXT_ELEMENT, Direction.OUTGOING);
				currentEntry = currentEntryRelation.getEndNode();
			}
			Node prevEntry = currentEntryRelation.getStartNode();
			currentEntryRelation.delete();
			Node newEntry = graphDb.createNode();
			prevEntry.createRelationshipTo(newEntry, RelTypes.NEXT_ELEMENT);
			newEntry.createRelationshipTo(currentEntry, RelTypes.NEXT_ELEMENT);
			newEntry.createRelationshipTo(element, RelTypes.LIST_ENTRY);
			int i = (Integer) underlyingNode.getProperty(INDEX_SIZE);
			underlyingNode.setProperty(INDEX_SIZE, i+1);
		}
	}

	@Override
	public boolean add(Node e) {
		Node entry = graphDb.createNode();
		entry.createRelationshipTo(e, RelTypes.LIST_ENTRY);
		if (this.isEmpty()) {
			underlyingNode.createRelationshipTo(entry, RelTypes.FIRST_ELEMENT);
			underlyingNode.createRelationshipTo(entry, RelTypes.LAST_ELEMENT);
			underlyingNode.setProperty(INDEX_SIZE, 1);
		} else {
			Relationship lastElementRelation = underlyingNode
					.getSingleRelationship(RelTypes.LAST_ELEMENT,
							Direction.OUTGOING);
			Node lastElement = lastElementRelation.getEndNode();
			lastElementRelation.delete();
			lastElement.createRelationshipTo(entry, RelTypes.NEXT_ELEMENT);
			underlyingNode.createRelationshipTo(entry, RelTypes.LAST_ELEMENT);
			int i = (Integer) underlyingNode.getProperty(INDEX_SIZE);
			underlyingNode.setProperty(INDEX_SIZE, i+1);
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Node> c) {
		for (Node n : c) {
			add(n);
		}
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Node> c) {
		if (index < 0 || index >= size())
			throw new IndexOutOfBoundsException();
		Node firstElement = null;
		Node lastElement = null;

		for (Node n : c) {
			Node element = graphDb.createNode();
			if (firstElement == null) {
				firstElement = element;
				lastElement = element;
			} else {
				lastElement
						.createRelationshipTo(element, RelTypes.NEXT_ELEMENT);
				lastElement = element;
			}
			element.createRelationshipTo(n, RelTypes.LIST_ENTRY);
		}

		if (firstElement != null) {
			Relationship currentEntryRelation = underlyingNode
					.getSingleRelationship(RelTypes.FIRST_ELEMENT,
							Direction.OUTGOING);
			Node currentEntry = currentEntryRelation.getEndNode();
			if (index == 0) {
				currentEntryRelation.delete();
				underlyingNode.createRelationshipTo(firstElement,
						RelTypes.FIRST_ELEMENT);
				lastElement.createRelationshipTo(currentEntry,
						RelTypes.NEXT_ELEMENT);
				underlyingNode.setProperty(INDEX_SIZE, size()+c.size());
			} else {
				for (int i = 0; i < index; i++) {
					currentEntryRelation = currentEntry.getSingleRelationship(
							RelTypes.NEXT_ELEMENT, Direction.OUTGOING);
					currentEntry = currentEntryRelation.getEndNode();
				}
				Node prevEntry = currentEntryRelation.getStartNode();
				currentEntryRelation.delete();
				prevEntry.createRelationshipTo(firstElement,
						RelTypes.NEXT_ELEMENT);
				lastElement.createRelationshipTo(currentEntry,
						RelTypes.NEXT_ELEMENT);
				underlyingNode.setProperty(INDEX_SIZE, size()+c.size());
			}
		}
		return true;
	}

	@Override
	public void clear() {
		for (Iterator<Node> i = this.iterator(); i.hasNext();) {
			i.next();
			i.remove();
		}
	}

	@Override
	public boolean contains(Object o) {
		if (o instanceof Node) {
			for (Node node : this) {
				if (((Node)o).getId() == node.getId()) {
					return true;
				}
			}
			return false;
		} else
			throw new ClassCastException("Supplied object is not a Node");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!this.contains(o))
				return false;
		}
		return true;
	}

	public void delete() {
		clear();
		underlyingNode.removeProperty(INDEX_SIZE);
		underlyingNode.delete();
	}

	@Override
	public Node get(int index) {
		if (index < 0 || index >= size())
			throw new IndexOutOfBoundsException();
		Iterator<Node> it = iterator();
		for (int i = 0; i < this.size(); i++) {
			Node n = it.next();
			if (i == index)
				return n;
		}
		throw new IndexOutOfBoundsException();
	}

	/**
	 * Returns the underlying node representing this timeline.
	 * 
	 * @return The underlying node representing this timeline
	 */
	public Node getUnderlyingNode() {
		return underlyingNode;
	}

	@Override
	public int indexOf(Object o) {
		if (o instanceof Node) {
			int i = 0;
			for (Node node : this) {
				if (((Node)o).getId() == node.getId()) {
					return i;
				}
				i++;
			}
			return -1;
		} else
			throw new ClassCastException("Supplied object is not a Node");
	}

	@Override
	public boolean isEmpty() {
		return !underlyingNode.hasRelationship(RelTypes.FIRST_ELEMENT,
				Direction.OUTGOING);
	}

	@Override
	public Iterator<Node> iterator() {
		return new NodeIterator(-1);
	}

	@Override
	public int lastIndexOf(Object o) {
		if (o instanceof Node) {
			int i = size() - 1 ;
			for (Node node : new ReverseList()) {
				if (((Node)o).getId() == node.getId()) {
					return i;
				}
				i--;
			}
			return -1;
		} else
			throw new ClassCastException("Supplied object is not a Node");
	}

	@Override
	public ListIterator<Node> listIterator() {
		return new NodeListIterator(-1);
	}

	@Override
	public ListIterator<Node> listIterator(int index) {
		if (index < 0 || index >= size())
			throw new IndexOutOfBoundsException();
		return new NodeListIterator(index);
	}

	@Override
	public Node remove(int index) {
		if (index < 0 || index >= size())
			throw new IndexOutOfBoundsException();
		Relationship currentEntryRelation = underlyingNode
				.getSingleRelationship(RelTypes.FIRST_ELEMENT,
						Direction.OUTGOING);
		Node currentEntry = currentEntryRelation.getEndNode();
		Node currentNode = currentEntry.getSingleRelationship(
				RelTypes.LIST_ENTRY, Direction.OUTGOING).getEndNode();
		Relationship lastEntryRelation = underlyingNode.getSingleRelationship(
				RelTypes.LAST_ELEMENT, Direction.OUTGOING);
		Node lastEntry = lastEntryRelation.getEndNode();
		if (index == 0) {
			currentEntryRelation.delete();
			if (currentEntry.getId() == lastEntry.getId()) {
				lastEntryRelation.delete();
				Relationship entryRelation = currentEntry
						.getSingleRelationship(RelTypes.LIST_ENTRY,
								Direction.OUTGOING);
				entryRelation.delete();
				currentEntry.delete();
				underlyingNode.setProperty(INDEX_SIZE, 0);

			} else {
				Relationship nextEntryRelation = currentEntry
						.getSingleRelationship(RelTypes.NEXT_ELEMENT,
								Direction.OUTGOING);
				Node nextEntry = nextEntryRelation.getEndNode();
				nextEntryRelation.delete();
				Relationship entryRelation = currentEntry
						.getSingleRelationship(RelTypes.LIST_ENTRY,
								Direction.OUTGOING);
				entryRelation.delete();
				currentEntry.delete();
				underlyingNode.createRelationshipTo(nextEntry,
						RelTypes.FIRST_ELEMENT);
				int i = (Integer) underlyingNode.getProperty(INDEX_SIZE);
				underlyingNode.setProperty(INDEX_SIZE, i-1);
			}
		} else {
			for (int i = 0; i < index; i++) {
				currentEntryRelation = currentEntry.getSingleRelationship(
						RelTypes.NEXT_ELEMENT, Direction.OUTGOING);
				currentEntry = currentEntryRelation.getEndNode();
			}
			currentNode = currentEntry.getSingleRelationship(
					RelTypes.LIST_ENTRY, Direction.OUTGOING).getEndNode();
			Node prevEntry = currentEntryRelation.getStartNode();
			currentEntryRelation.delete();
			if (currentEntry.getId() == lastEntry.getId()) {
				lastEntryRelation.delete();
				underlyingNode.createRelationshipTo(prevEntry,
						RelTypes.LAST_ELEMENT);
				Relationship entryRelation = currentEntry
						.getSingleRelationship(RelTypes.LIST_ENTRY,
								Direction.OUTGOING);
				entryRelation.delete();
				currentEntry.delete();
				int i = (Integer) underlyingNode.getProperty(INDEX_SIZE);
				underlyingNode.setProperty(INDEX_SIZE, i-1);
			} else {
				Relationship nextEntryRelation = currentEntry
						.getSingleRelationship(RelTypes.NEXT_ELEMENT,
								Direction.OUTGOING);
				Node nextEntry = nextEntryRelation.getEndNode();
				nextEntryRelation.delete();
				Relationship entryRelation = currentEntry
						.getSingleRelationship(RelTypes.LIST_ENTRY,
								Direction.OUTGOING);
				entryRelation.delete();
				currentEntry.delete();
				prevEntry
						.createRelationshipTo(nextEntry, RelTypes.NEXT_ELEMENT);
				int i = (Integer) underlyingNode.getProperty(INDEX_SIZE);
				underlyingNode.setProperty(INDEX_SIZE, i-1);
			}
		}
		return currentNode;
	}

	@Override
	public boolean remove(Object o) {
		int idx = indexOf(o);
		if (idx >= 0) {
			remove(idx);
			return true;
		} else
			return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Object o : c) {
			if (o instanceof Node) {
				changed = true;
				remove(o);
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = false;
		for (Iterator<Node> i = this.iterator(); i.hasNext();) {
			if (!c.contains(i.next())) {
				changed = true;
				i.remove();
			}
		}
		return changed;
	}

	@Override
	public Node set(int index, Node element) {
		if (index < 0 || index >= size())
			throw new IndexOutOfBoundsException();
		Node currentEntry = underlyingNode.getSingleRelationship(
				RelTypes.FIRST_ELEMENT, Direction.OUTGOING).getEndNode();
		for (int i = 0; i < index; i++) {
			currentEntry = currentEntry.getSingleRelationship(
					RelTypes.NEXT_ELEMENT, Direction.OUTGOING).getEndNode();
		}
		currentEntry.getSingleRelationship(RelTypes.LIST_ENTRY,
				Direction.OUTGOING).delete();
		currentEntry.createRelationshipTo(element, RelTypes.LIST_ENTRY);
		return element;
	}

	@Override
	public int size() {
		if (underlyingNode.hasProperty(INDEX_SIZE)) {
			return (Integer) underlyingNode.getProperty(INDEX_SIZE);
		} else {
			underlyingNode.setProperty(INDEX_SIZE, 0);
			return 0;
		}
	}

	@Override
	public java.util.List<Node> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		Node[] nodeArray = new Node[this.size()];
		for (int i = 0; i < size(); i++) {
			nodeArray[i] = this.get(i);
		}
		return nodeArray;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		if (a instanceof Node[]) {
			if (a.length < this.size()) {
				return (T[]) toArray();
			} else {
				for (int i = 0; i < a.length; i++) {
					if (i <= this.size()) {
						a[i] = (T) this.get(i);
					} else {
						a[i] = null;
					}
				}
				return a;
			}
		} else
			throw new ArrayStoreException();
	}

}
