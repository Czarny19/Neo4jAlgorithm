package application.model.connectivity;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class NodeConnBag<Item> implements Iterable<Item>{

	private int Size;
	private Node<Item> first;
	 
	private static class Node<Item>{
		private Item item;
	    private Node<Item> next;
	}
	 
	public NodeConnBag(){
		first = null;
	    Size = 0;
	}
	 
	public boolean isEmpty(){
		return first == null;
	}
	 
	public int size(){
		return Size;
	}
	 
	public void add(Item item){
		Node<Item> oldfirst = first;
	    first = new Node<Item>();
	    first.item = item;
	    first.next = oldfirst;
	    Size++;
	}
	 
	public Iterator<Item> iterator(){
	    return new ListIterator<Item>(first);
	}
	 
	@SuppressWarnings("hiding")
	private class ListIterator<Item> implements Iterator<Item>{
		private Node<Item> current;
	 
	    public ListIterator(Node<Item> first){
	    	current = first;
	    }
	 
	    public boolean hasNext(){
	    	return current != null;
	    }
	 
	    public Item next()
	    {
	    	if (!hasNext())
	    		throw new NoSuchElementException();
	        Item item = current.item;
	        current = current.next;
	        return item;
	    }
	}
}
