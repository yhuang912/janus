package ast;

import java.util.WeakHashMap;

public class AstVisitedMap<V> {
	private WeakHashMap<AstNode, V> v;

	public AstVisitedMap() {
		v = new WeakHashMap<AstNode, V>();
	}

	public boolean isVisited(AstNode n) {
		return v.containsKey(n);
	}

	public V valueAt(AstNode n) {
		return v.get(n);
	}

	public void visit(AstNode n, V value) {
		v.put(n, value);
	}

	public void clear() {
		v.clear();
	}

	public int size() {
		return v.size();
	}

}
