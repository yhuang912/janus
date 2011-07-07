package ast;

// Reference to a symbolic or concrete character from an AstNode
public class AstCharRef {
	private char ch; // '#' means symbolic
	private int id; // a string-specific symbol ID.

	// In practice, it is the index of the character in string
	public AstCharRef() {
		ch = '#';
		id = -1;
	}

	public AstCharRef(String s, int ind) {
		ch = s.charAt(ind);
		if (ch == '#')
			id = ind;
	}

	public boolean isValid() {
		return ch != '#' || id != -1;
	}

	public boolean isSymbolic() {
		return isValid() && ch == '#';
	}

	public int getId() {
		if (!isSymbolic())
			throw new BadRequest("ID of non-symbolic AstCharRef requested");
		else
			return id;
	}

	public char getChar() {
		if (!isValid())
			throw new BadRequest("Reading uninitialized AstCharRef object");
		else
			return ch;
	}

	public static class BadRequest extends Error {
		public BadRequest(String msg) {
			super(msg);
		}
	}
}
