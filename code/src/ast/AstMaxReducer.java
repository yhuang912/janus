package ast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import ast.MinMaxRedundancy.HashKey;
import ast.MinMaxRedundancy.Result;

public class AstMaxReducer {
	private AstReducer reducerMain;

	public AstMaxReducer(AstReducer r) {
		reducerMain = r;
	}

	public boolean reduce(AstNode node, AstReducer.ReduceInfo nodeinfo) {
		boolean repeat = false;
		// scount = Symbol Count, curmax = Current Maximum of AstValueNode
		int scount = 0, curmax = Integer.MIN_VALUE;
		nodeinfo.upperLim = nodeinfo.lowerLim = Integer.MIN_VALUE;
		AstNode[] children = node.children();
		for (int i = 0; i < children.length; ++i) {
			reducerMain.reduce(children[i]);
			// if(children[i].getType()==node.getType()) return true;
		}
		for (int i = 0; i < children.length; ++i) {
			if (children[i].getType() != AstValueNode.class)
				scount++;
			else {
				int v = ((AstValueNode) children[i].getData()).getValue();
				if (curmax < v)
					curmax = v;
			}

			AstReducer.ReduceInfo childinfo = reducerMain.reduce(children[i]);
			int temphi = childinfo.upperLim;
			int templo = childinfo.lowerLim;
			if (nodeinfo.upperLim < temphi)
				nodeinfo.upperLim = temphi;
			if (nodeinfo.lowerLim < templo)
				nodeinfo.lowerLim = templo;
		}
		boolean hasconst = children.length > scount
				&& curmax > nodeinfo.lowerLim;
		scount = 0;
		AstNode lastSym = null;
		for (int i = 0; i < children.length; ++i)
			if (reducerMain.reduce(children[i]).upperLim > nodeinfo.lowerLim
					&& children[i].getType() != AstValueNode.class) {
				lastSym = children[i];
				scount++;
			}
		if (scount == 0) {
			node.setData(new AstValueNode(curmax));
			nodeinfo.lowerLim = nodeinfo.upperLim = curmax;
			return false;
		}
		if (scount == 1 && !hasconst) {
			node.setData(lastSym.getData());
			nodeinfo.hasConst = reducerMain.reduce(lastSym).hasConst;
			return true;
		}

		AstNode newchildren[] = new AstNode[scount + (hasconst ? 1 : 0)];
		for (int i = 0, j = 0; i < children.length; ++i)
			if (reducerMain.reduce(children[i]).upperLim > nodeinfo.lowerLim
					&& children[i].getType() != AstValueNode.class)
				newchildren[j++] = children[i];

		if (hasconst)
			newchildren[scount] = AstValueNode.create(curmax);
		nodeinfo.hasConst = hasconst;
		node.setData(new AstMaxNode(newchildren));
		repeat = exRemoveRedundantMaxChildren(node, nodeinfo) || repeat;
		repeat = factorConstAdd(node) || repeat;
		return repeat;
	}

	// counterpart of the same method in AstMaxReducer
	private boolean checkRedundantAndAdd(LinkedList<AstNode> nodelist,
			AstNode newnode) {
		ListIterator<AstNode> it = nodelist.listIterator(0);
		while (it.hasNext()) {
			AstNode childi = it.next();
			if (childi == null)
				continue;
			MinMaxRedundancy.Result r = MinMaxRedundancy.checkRedundant(childi,
					newnode);
			if (r == MinMaxRedundancy.Result.same
					|| r == MinMaxRedundancy.Result.small2)
				return false;
			else if (r == MinMaxRedundancy.Result.small1) {
				it.set(null);
				return true;
			}
		}
		nodelist.add(newnode);
		return false;
	}

	// max(add(x,1),add(x,2),3) --> max(add(x,2),3)
	private boolean exRemoveRedundantMaxChildren(AstNode node,
			AstReducer.ReduceInfo nodeinfo) {
		if (node.getType() != AstMaxNode.class)
			return false;
		AstNode maxchild[] = node.children();

		int i, nullcount = 0, oldcount = -1;
		HashMap<MinMaxRedundancy.HashKey, LinkedList<AstNode>> hmap = new HashMap<MinMaxRedundancy.HashKey, LinkedList<AstNode>>();

		for (i = 0; i < maxchild.length; ++i) {
			MinMaxRedundancy.HashKey rk = new MinMaxRedundancy.HashKey(
					maxchild[i]);
			if (hmap.containsKey(rk))
				if (checkRedundantAndAdd(hmap.get(rk), maxchild[i]))
					nullcount++;
				else {
					LinkedList<AstNode> t = new LinkedList<AstNode>();
					t.add(maxchild[i]);
					hmap.put(rk, t);
				}
		}
		if (maxchild.length == hmap.size())
			return false;
		if (nullcount == 0)
			return false;

		AstNode newmaxchild[] = MinMaxRedundancy.dumpToArray(hmap,
				maxchild.length - nullcount);
		node.setData(new AstMaxNode(newmaxchild));

		return true;
	}

	// may change Max node to Add node
	// counterpart (i.e. copy-paste) of the same method in AstMinReducer
	// merge candidate
	private boolean factorConstAdd(AstNode node) {
		if (node.getType() != AstMaxNode.class)
			return false;
		AstNode[] maxchild = node.children();
		int factored = Integer.MIN_VALUE;
		for (int i = 0; i < maxchild.length; ++i) {
			if (maxchild[i].getType() != AstAddNode.class)
				return false;
			AstNode[] addchild = maxchild[i].children();
			if (addchild[addchild.length - 1].getType() != AstValueNode.class)
				return false;
			int v = ((AstValueNode) addchild[addchild.length - 1].getData())
					.getValue();
			if (v > factored)
				factored = v;
		}
		if (factored == 0)
			return false;
		/*
		 * AstPrinter.print(node,System.err); System.err.println();
		 */

		// start changing things
		AstNode[] newmaxchild = new AstNode[maxchild.length];
		for (int i = 0; i < maxchild.length; ++i) {
			AstNode[] addchild = maxchild[i].children();
			int v = ((AstValueNode) addchild[addchild.length - 1].getData())
					.getValue();
			int j;
			AstNode[] newaddchild = new AstNode[addchild.length
					- (v == factored ? 1 : 0)];
			for (j = 0; j < addchild.length - 1; ++j)
				newaddchild[j] = addchild[j];
			if (j < newaddchild.length)
				newaddchild[j] = AstValueNode.create(v - factored);
			if (newaddchild.length == 1)
				newmaxchild[i] = newaddchild[0];
			else
				newmaxchild[i] = AstAddNode.create(newaddchild);
		}
		AstNode[] factadd = new AstNode[2];
		factadd[0] = AstMaxNode.create(newmaxchild);
		factadd[1] = AstValueNode.create(factored);
		node.setData(new AstAddNode(factadd));
		/*
		 * AstPrinter.print(node,System.err); System.err.println("\n-----");
		 */
		return true;
	}
}
