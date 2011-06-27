/**
  Specializations of this interface each represent a different type 
  of AstNode. Meant to be used as contents of container AstNode
   all implementations are meant to be immutable classes.
   */
public interface AstNodeData
{
  public AstNode[] childNodes();
}
