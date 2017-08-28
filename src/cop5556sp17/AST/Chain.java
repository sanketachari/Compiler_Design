package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.*;


public abstract class Chain extends Statement {

	private TypeName type;
	
	public Chain(Token firstToken) {
		super(firstToken);
	}

	public TypeName getType() {
		return type;
	}

	public void setType(TypeName type) {
		this.type = type;
	}
}
