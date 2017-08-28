package cop5556sp17;

import cop5556sp17.AST.*;

import java.util.HashSet;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;

import static cop5556sp17.AST.Type.*;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	HashSet<Kind> relOp;
	HashSet<Kind> boolRes;
	HashSet<Kind> frameOp;
	HashSet<Kind> imageOp;
	HashSet<Kind> filterOp;

	public TypeCheckVisitor() {

		relOp = new HashSet<>();
		relOp.add(LT);
		relOp.add(LE);
		relOp.add(GT);
		relOp.add(GE);
		relOp.add(EQUAL);
		relOp.add(NOTEQUAL);


		boolRes = new HashSet<>();

		boolRes.add(LT);
		boolRes.add(LE);
		boolRes.add(GT);
		boolRes.add(GE);
		boolRes.add(EQUAL);
		boolRes.add(NOTEQUAL);

		frameOp = new HashSet<>();
		frameOp.add(KW_SHOW);
		frameOp.add(KW_HIDE);
		frameOp.add(KW_MOVE);
		frameOp.add(KW_XLOC);
		frameOp.add(KW_YLOC);

		imageOp = new HashSet<>();
		imageOp.add(OP_WIDTH);
		imageOp.add(OP_HEIGHT);
		imageOp.add(KW_SCALE);

		filterOp = new HashSet<>();
		filterOp.add(OP_GRAY);
		filterOp.add(OP_BLUR);
		filterOp.add(OP_CONVOLVE);
	}

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {

		Chain e0 = binaryChain.getE0();
		e0.visit(this, arg);

		ChainElem e1 = binaryChain.getE1();
		e1.visit(this, arg);

		TypeName type1 = e0.getType();
		TypeName type2 = e1.getType();

		Token op = binaryChain.getArrow();
		Token firstToken = e1.getFirstToken();

		if ((type1.isType(URL) || type1.isType(FILE)) && type2.isType(IMAGE) && op.isKind(ARROW)) {

			binaryChain.setType(IMAGE);
			return null;

		} else if (type1.isType(FRAME) && op.isKind(ARROW) && e1 instanceof FrameOpChain && frameOp.contains(firstToken.kind)) {

			if (firstToken.isKind(KW_XLOC) || firstToken.isKind(KW_YLOC))
				binaryChain.setType(INTEGER);
			else
				binaryChain.setType(FRAME);

			return null;

		} else if (type1.isType(IMAGE)) {


			if (op.isKind(ARROW)) {

				if (type2.isType(FRAME)) {

					binaryChain.setType(FRAME);
					return null;
				}

				if ((e1 instanceof ImageOpChain && firstToken.isKind(KW_SCALE))
						|| (e1 instanceof IdentChain && type2.isType(IMAGE))) {   // doubt : condition delete ?

					binaryChain.setType(IMAGE);
					return null;
				}

				if (e1 instanceof ImageOpChain && (firstToken.isKind(OP_WIDTH) || firstToken.isKind(OP_HEIGHT))) {

					binaryChain.setType(INTEGER);
					return null;
				}

				if (type2.isType(FILE)) {

					binaryChain.setType(NONE);
					return null;
				}
			}

			if ((op.isKind(ARROW) || op.isKind(BARARROW)) && e1 instanceof FilterOpChain
					&& filterOp.contains(firstToken.kind)) {

				binaryChain.setType(IMAGE);
				return null;
			}


		} else if (type1.isType(INTEGER) && op.isKind(ARROW) && e1 instanceof IdentChain && type2.isType(INTEGER)){

			binaryChain.setType(INTEGER);
			return null;
		}

		throw new TypeCheckException("Wrong Binary Chain");
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {

		Expression e0 = binaryExpression.getE0();
		e0.visit(this, arg);

		Expression e1 = binaryExpression.getE1();
		e1.visit(this, arg);

		TypeName type1 = e0.getType();
		TypeName type2 = e1.getType();

		Token op = binaryExpression.getOp();

		if (boolRes.contains(op.kind) || (type1.isType(BOOLEAN) && type2.isType(BOOLEAN) && (op.isKind(AND) || op.isKind(OR)))) {

			binaryExpression.setType(BOOLEAN);

			if ((op.isKind(EQUAL) || op.isKind(NOTEQUAL)) && type1.isType(type2))
				return null;

			if (type1.isType(INTEGER) && type2.isType(INTEGER)) {

				switch (op.kind) {

					case LT:
					case LE:
					case GT:
					case GE:
						break;

					default:
						throw new TypeCheckException("Wrong Binary Expression");
				}

				return null;

			} else if (type1.isType(BOOLEAN) && type2.isType(BOOLEAN)) {

				switch (op.kind) {

					case LT:
					case LE:
					case GT:
					case GE:
					case AND:
					case OR:
						break;

					default:
						throw new TypeCheckException("Wrong Binary Expression");
				}

				return null;

			} else {

				throw new TypeCheckException("Wrong Binary Expression, Type 1: " + type1.toString() + " Type 2: " + type2.toString());
			}
		}

		if (type1.isType(IMAGE) || type2.isType(IMAGE))
			binaryExpression.setType(IMAGE);

		else if (type1.isType(INTEGER) && type2.isType(INTEGER))
			binaryExpression.setType(INTEGER);
		else
			throw new TypeCheckException("Wrong Binary Expression, Type 1: " + type1.toString() +
					" Type 2: " + type2.toString());

		if (binaryExpression.getType().isType(INTEGER)){

			switch (binaryExpression.getOp().kind) {

				case PLUS:
				case MINUS:
				case TIMES:
				case DIV:
				case AND:
				case MOD:
				case OR:
					break;

				default:
					throw new TypeCheckException("Wrong Binary Expression, wrong operator");
			}

		}

		else if (type1.isType(IMAGE) && type2.isType(IMAGE)){

			switch (binaryExpression.getOp().kind) {

				case PLUS:
				case MINUS:
					break;

				default:
					throw new TypeCheckException("Wrong Binary Expression, wrong operator");
			}
		}

		else if (type1.isType(IMAGE) && type2.isType(INTEGER)){

			switch (binaryExpression.getOp().kind) {

				case MOD:
				case TIMES:
				case DIV:
					break;

				default:
					throw new TypeCheckException("Wrong Binary Expression, wrong operator");
			}
		}
		else if (type1.isType(INTEGER) && type2.isType(IMAGE)){

			switch (binaryExpression.getOp().kind) {

				case TIMES:
					break;

				default:
					throw new TypeCheckException("Wrong Binary Expression, wrong operator");
			}
		}
		else
			throw new TypeCheckException("Wrong Binary Expression, wrong operator");


		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {

		symtab.enterScope();

		for (Dec dec : block.getDecs()) {

			dec.visit(this, arg);
		}

		for (Statement st : block.getStatements()) {

			st.visit(this, arg);
		}

		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {

		booleanLitExpression.setType(BOOLEAN);

		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {

		filterOpChain.getArg().visit(this, arg);

		if (filterOpChain.getArg().getExprList().size() == 0)
			filterOpChain.setType(IMAGE);

		else
			throw new TypeCheckException("Wrong FilterOpChain");

		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {

		Token frameOp = frameOpChain.getFirstToken();
		frameOpChain.getArg().visit(this, arg);
		int argSize = frameOpChain.getArg().getExprList().size();

		//doubt:

		if ((frameOp.isKind(KW_SHOW) || frameOp.isKind(KW_HIDE)) && argSize == 0)
			frameOpChain.setType(NONE);

		else if ((frameOp.isKind(KW_XLOC) || frameOp.isKind(KW_YLOC)) && argSize == 0)
			frameOpChain.setType(INTEGER);

		else if (frameOp.isKind(KW_MOVE) && argSize == 2)
			frameOpChain.setType(NONE);

		else
			throw new TypeCheckException(" Wrong FrameOpChain ");

		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {

		Token ident = identChain.getFirstToken();

		Dec dec = symtab.lookup(ident.getText());

		if (dec == null)
			throw new TypeCheckException("Wrong Ident Chain due to: " + ident.getText());

		identChain.setType(dec.getTypeName());
		ident.setType(dec.getTypeName());
		identChain.setDec(dec);
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {

		// condition: ident has been declared and is visible in the current scope
		// IdentExpression.type <- ident.type
		// IdentExpression.dec <- Dec of ident

		Token ident = identExpression.getFirstToken();
		Dec dec = symtab.lookup(ident.getText());

		if (dec == null)
			throw new TypeCheckException("Wrong Ident Chain");

		//identExpression.setType(ident.getType());
		identExpression.setType(dec.getTypeName());
		identExpression.setDec(dec);

		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {

		ifStatement.getE().visit(this, arg);
		ifStatement.getB().visit(this, arg);

		if (!ifStatement.getE().getType().isType(BOOLEAN))
			throw new TypeCheckException("Wrong IfStatement");

		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {

		intLitExpression.setType(INTEGER);

		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {

		sleepStatement.getE().visit(this, arg);

		if (!sleepStatement.getE().getType().isType(INTEGER))
			throw new TypeCheckException("Wrong SleepStatement");

		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {

		whileStatement.getE().visit(this, arg);
		whileStatement.getB().visit(this, arg);

		if (!whileStatement.getE().getType().isType(BOOLEAN))
			throw new TypeCheckException("Wrong WhileStatement");

		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {

		Token ident = declaration.getIdent();
		ident.setType(Type.getTypeName(declaration.getType()));
		declaration.setTypeName(Type.getTypeName(declaration.getType()));

		if (!symtab.insert(declaration.getIdent().getText(), declaration))
			throw new TypeCheckException("Wrong Dec");

		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {

		for (ParamDec pd : program.getParams()) {

			pd.visit(this, arg);
		}

		program.getB().visit(this, arg);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {


		assignStatement.getE().visit(this, arg);
		assignStatement.getVar().visit(this, arg);

		Dec dec = symtab.lookup(assignStatement.getVar().getFirstToken().getText());

		if (dec == null || !dec.getTypeName().isType(assignStatement.getE().getType()))
			throw new TypeCheckException("Wrong AssignmentStatement");

		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {

		// to do: condition: ident has been declared and is visible in the current scope

		Dec dec = symtab.lookup(identX.getText());

		if (dec == null )
			throw new TypeCheckException("Wrong IdentLValue");

		identX.setDec(dec);

		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {

		Token ident = paramDec.getIdent();
		ident.setType(Type.getTypeName(paramDec.getType()));
		paramDec.setTypeName(Type.getTypeName(paramDec.getType()));

		if (!symtab.insert(paramDec.getIdent().getText(), paramDec))
			throw new TypeCheckException("Wrong ParamDec");

		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {

		constantExpression.setType(INTEGER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {

		imageOpChain.getArg().visit(this, arg);
		int argSize = imageOpChain.getArg().getExprList().size();

		Token imageOp = imageOpChain.getFirstToken();

		if ((imageOp.isKind(OP_WIDTH) || imageOp.isKind(OP_HEIGHT)) && argSize == 0)
			imageOpChain.setType(INTEGER);

		else if (imageOp.isKind(KW_SCALE) && argSize == 1)
			imageOpChain.setType(IMAGE);

		else
			throw new TypeCheckException(" Wrong ImageOpChain");

		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {

		for (Expression e : tuple.getExprList()) {

			e.visit(this, arg);

			if (!e.getType().isType(INTEGER))
				throw new TypeCheckException("Wrong Tuple");
		}

		return null;
	}

}
