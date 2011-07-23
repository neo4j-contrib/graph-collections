package org.neo4j.collections.graphdb.impl;

import java.util.Collection;
import java.util.Iterator;

import org.neo4j.collections.graphdb.Node;

public class NodeCollection implements Collection<Node>{

	private final Collection<org.neo4j.graphdb.Node> nodes;
	
	public NodeCollection(Collection<org.neo4j.graphdb.Node> nodes) {
		this.nodes = nodes;
	}

	@Override
	public int size() {
		return nodes.size();
	}

	@Override
	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		org.neo4j.graphdb.Node n = ((Node)o).getNode(); 
		return nodes.contains(n);
	}

	@Override
	public Iterator<Node> iterator() {
		return new NodeIterator(nodes.iterator());
	}

	@Override
	public Object[] toArray() {
		Object[] nArray1 = nodes.toArray();
		Node[] nArray2 = new Node[nArray1.length];
		int count = 0;
		for(Object o: nArray1){
			nArray2[count] = new NodeImpl((org.neo4j.graphdb.Node)o);
			count++;
		}
		return nArray2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		Object[] nArray1 = nodes.toArray();
		if(a.length < nArray1.length){
			Node[] nArray2 = new Node[nArray1.length];
			int count = 0;
			for(Object o: nArray1){
				nArray2[count] = new NodeImpl((org.neo4j.graphdb.Node)o);
				count++;
			}
			return (T[])nArray2;
		}else{
			for(int i=0;i < a.length;i++){
				if(i < nArray1.length){
					a[i] = (T)nArray1[i];
				}else{
					a[i] = null;
				}
			}
		}
		return null;
	}

	@Override
	public boolean add(Node e) {
		return this.nodes.add(e.getNode());
	}

	@Override
	public boolean remove(Object o) {
		org.neo4j.graphdb.Node n = ((Node)o).getNode();		
		return this.nodes.remove(n);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for(Object o: c){
			if(contains(o) == false) 
				return false;
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Node> c) {
		boolean changed = false; 
		for(Node n: c){
			if(!contains(n)){
				changed = true;
				add(n);
			}
		}
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false; 
		for(Object o: c){
			if(!contains(o)){
				changed = true;
				remove(o);
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = false;
		for(Node n: this){
			if(!contains(c)){
				changed = true;
				remove(n);
			}
		}
		return changed;
	}

	@Override
	public void clear() {
		this.nodes.clear();
	}

}
