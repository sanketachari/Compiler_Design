package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.*;

public abstract class Expression extends ASTNode {

	private TypeName type;

	protected Expression(Token firstToken) {
		super(firstToken);
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

	public TypeName getType() {
		return type;
	}

	public void setType(TypeName type) {
		this.type = type;
	}
}
