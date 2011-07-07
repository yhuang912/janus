package ast;

import java.util.HashMap;
import java.util.LinkedList;

// package private access
class MinMaxRedundancy {
	public static enum Result {
		needboth, small1, small2, same
	}

	public static class HashKey {
		public int hcode;

		public HashKey(AstNode n) {
			hcode = getHashCode(n);
		}

		private static int combine(int a, int b) {
			if (a >= (1 << 30))
				a = (a << 1) + 1;
			else
				a <<= 1;
			return a ^ b;
		}

		public int getHashCode(AstNode node) {
			if (node.getType() != AstAddNode.class)
				return super.hashCode();
			AstNode addchild[] = node.children();
			int rv = (new Integer(addchild.length)).hashCode();
			for (int i = 0; i < addchild.length - 1; ++i)
				rv = combine(rv, addchild[i].hashCode());
			AstNode lastchild = addchild[addchild.length - 1];
			if (lastchild.getType() != AstValueNode.class)
				rv = combine(rv, lastchild.hashCode());
			return rv;
		}

		public int hashCode() {
			return hcode;
		}

		public boolean equals(Object o) {
			if (o.getClass() != HashKey.class)
				return false;
			return hcode == ((HashKey) o).hcode;
		}
	}

	/*
	 * Checks if either of the two nodes make the other redundant as a child of
	 * the same Min/Max node. Right now, works only on Add nodes. NOTE: HashKey
	 * needs to be updated whenever this one is.
	 */
	public static Result checkRedundant(AstNode node1, AstNode node2) {
		// Ideally, we want a deep-comparison of canonized versions, but
		// here we do something simpler: shallow-comparison of unsorted versions
		if (node1.getType() != AstAddNode.class
				|| node2.getType() != AstAddNode.class)
			return Result.needboth;

		AstNode addchild1[] = node1.children();
		AstNode addchild2[] = node2.children();

		// Looking for a very specific pattern here
		if (addchild1.length != addchild2.length)
			return Result.needboth;

		int k, len = addchild1.length;
		for (k = 0; k < len - 1; ++k)
			if (!addchild1[k].equals(addchild2[k]))
				break;
		if (k < len - 1)
			return Result.needboth;
		// everything's a match, we can omit one of them
		if (addchild1[k].equals(addchild2[k]))
			return Result.same;

		AstNode last1 = addchild1[len - 1], last2 = addchild2[len - 1];
		if (last1.getType() != AstValueNode.class
				|| last2.getType() != AstValueNode.class)
			return Result.needboth;

		// only possible difference is in const, bigger one has to go
		int v1 = ((AstValueNode) last1.getData()).getValue();
		int v2 = ((AstValueNode) last2.getData()).getValue();
		if (v1 > v2)
			return Result.small2;
		else
			return Result.small1;
	}

	public static AstNode[] dumpToArray(
			HashMap<HashKey, LinkedList<AstNode>> hmap, int len) {
		java.util.Iterator<java.util.Map.Entry<HashKey, LinkedList<AstNode>>> it = hmap
				.entrySet().iterator();
		int j = 0;
		AstNode[] dest = new AstNode[len];
		while (it.hasNext()) {
			java.util.ListIterator<AstNode> src = it.next().getValue()
					.listIterator();
			while (src.hasNext()) {
				AstNode t = src.next();
				if (t != null)
					dest[j++] = t;
			}
		}
		return dest;
	}

}
