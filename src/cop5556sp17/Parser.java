package cop5556sp17;

import cop5556sp17.AST.*;
import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}

	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when
	 * the Parser is finished.
	 */
	@SuppressWarnings("serial")
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	HashSet<Kind> type = new HashSet<>();
	HashSet<Kind> typeParam = new HashSet<>();
	HashSet<Kind> typeDec = new HashSet<>();
	HashSet<Kind> Op = new HashSet<>();
	HashSet<Kind> relOp = new HashSet<>();
	HashSet<Kind> weakOp = new HashSet<>();
	HashSet<Kind> strongOp = new HashSet<>();
	HashSet<Kind> filterOp = new HashSet<>();
	HashSet<Kind> imageOp = new HashSet<>();
	HashSet<Kind> frameOp = new HashSet<>();

	Parser(Scanner scanner) {

		this.scanner = scanner;
		t = scanner.nextToken();

		firstSetOp();
		firstSetType();
		firstSetTypeParam();
		firstSetTypeDec();
		firstSetRelOp();
		firstSetWeakOp();
		firstSetStrongOp();
		firstSetFilterOp();
		firstSetFrameOp();
		firstSetImageOp();
	}

	private void firstSetOp(){

		Op.add(LT); Op.add(LE); Op.add(GT); Op.add(GE); Op.add(EQUAL); Op.add(NOTEQUAL);
		Op.add(PLUS); Op.add(MINUS); Op.add(OR);
		Op.add(AND); Op.add(MOD); Op.add(TIMES); Op.add(DIV);

	}

	private void firstSetType(){

		type.add(KW_INTEGER); type.add(KW_IMAGE); type.add(KW_BOOLEAN);
		type.add(KW_FRAME);type.add(KW_URL);type.add(KW_FRAME);
	}

	private void firstSetTypeDec(){

		typeDec.add(KW_INTEGER); typeDec.add(KW_IMAGE); typeDec.add(KW_BOOLEAN);
		typeDec.add(KW_FRAME);
	}

	private void firstSetTypeParam(){

		typeParam.add(KW_INTEGER); typeParam.add(KW_FILE); typeParam.add(KW_URL);
		typeParam.add(KW_BOOLEAN);
	}

	private void firstSetRelOp(){
		relOp.add(LT); relOp.add(LE); relOp.add(GT); relOp.add(GE); relOp.add(EQUAL); relOp.add(NOTEQUAL);
	}

	private void firstSetWeakOp(){
		weakOp.add(PLUS); weakOp.add(MINUS); weakOp.add(OR);
	}

	private void firstSetStrongOp(){
		strongOp.add(AND); strongOp.add(MOD); strongOp.add(TIMES); strongOp.add(DIV);
	}

	private void firstSetFilterOp(){
		filterOp.add(OP_GRAY); filterOp.add(OP_BLUR); filterOp.add(OP_CONVOLVE);
	}

	private void firstSetImageOp(){
		imageOp.add(OP_WIDTH); imageOp.add(OP_HEIGHT); imageOp.add(KW_SCALE);
	}

	private void firstSetFrameOp(){
		frameOp.add(KW_SHOW); frameOp.add(KW_HIDE); frameOp.add(KW_MOVE); frameOp.add(KW_XLOC); frameOp.add(KW_YLOC);
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 *
	 * @throws SyntaxException
	 */
	ASTNode parse() throws SyntaxException {

		Program p = program();
		matchEOF();

		return p;
	}

	Expression expression() throws SyntaxException {

		Token firstToken = t;
		Expression ex0 = term();

		while (true)

			switch (t.kind) {

				case LT:
				case LE:
				case GT:
				case GE:
				case EQUAL:
				case NOTEQUAL:
					ex0 = new BinaryExpression(firstToken, ex0, relOp(), term());
					break;

				default:
					return ex0;
			}
	}

	Expression term() throws SyntaxException {

		Token firstToken = t;
		Expression ex0 = elem();

		while (true) {

			switch (t.kind) {

				case PLUS:
				case MINUS:
				case OR:
					ex0 = new BinaryExpression(firstToken, ex0, weakOp(), elem());
					break;

				default:
					return ex0;
			}
		}
	}

	Expression elem() throws SyntaxException {

		Token firstToken = t;
		Expression ex0 = factor();

		while (true) {

			switch (t.kind) {

				case TIMES:
				case DIV:
				case AND:
				case MOD:
					ex0 = new BinaryExpression(firstToken, ex0, strongOp(), factor());
					break;

				default:
					return ex0;
			}
		}
	}

	Expression factor() throws SyntaxException {

		Expression ex0;

		switch (t.kind) {

			case IDENT:
				ex0 =  new IdentExpression(consume());
				break;

			case INT_LIT:
				ex0 =  new IntLitExpression(consume());
				break;

			case KW_TRUE:
			case KW_FALSE:
				ex0 =   new BooleanLitExpression(consume());
				break;

			case KW_SCREENWIDTH:
			case KW_SCREENHEIGHT:
				ex0 =  new ConstantExpression(consume());
				break;

			case LPAREN: {
				consume();
				ex0 = expression();
				match(RPAREN);
			}
			break;
			default:
				//you will want to provide a more useful error message
				throw new SyntaxException("Illegal factor");
		}

		return ex0;
	}

	SleepStatement sleepStatement() throws SyntaxException{

		Token firstToken = t;
		match(OP_SLEEP);
		Expression exp = expression();
		match(SEMI);

		return new SleepStatement(firstToken, exp);
	}

	Statement statement() throws SyntaxException {

		//statement ::= OP_SLEEP expression ;​ | whileStatement | ifStatement | chain ;​ | assign ;

		Statement statement;

		switch (t.kind) {

			case OP_SLEEP:
				return sleepStatement();

			case KW_WHILE:
				return whileStatement();

			case KW_IF:
				return ifStatement();

			case IDENT:

				switch (scanner.peek().kind) {

					case ASSIGN:
						statement = assign();
						match(SEMI);
						return statement;

					case ARROW:
					case BARARROW:

						Chain chain = chain();
						match(SEMI);
						return chain;

					default:
						throw new SyntaxException("Illegal statement");
				}
		}

		if (filterOp.contains(t.kind) || frameOp.contains(t.kind) || imageOp.contains(t.kind)) {
			Chain chain = chain();
			match(SEMI);
			return chain;
		}
		else
			throw new SyntaxException("Illegal statement");
	}

	WhileStatement whileStatement() throws SyntaxException {

		//whileStatement ::= KW_WHILE (​ expression )​ block

		Token firstToken = t;
		match(KW_WHILE);
		match(LPAREN);
		Expression exp  = expression();
		match(RPAREN);
		return new WhileStatement(firstToken, exp, block());
	}

	IfStatement ifStatement() throws SyntaxException {

		//ifStatement ::= KW_IF (​ expression )​ block

		Token firstToken = t;
		match(KW_IF);
		match(LPAREN);
		Expression exp  = expression();
		match(RPAREN);
		return new IfStatement(firstToken, exp, block());
	}

	Token arrowOp() throws SyntaxException {

		//arrowOp ∷= ARROW | BARARROW

		switch (t.kind) {

			case ARROW:
				return consume();

			case BARARROW:
				return consume();

			default:
				throw new SyntaxException("Illegal Arrowop");
		}
	}

	IdentChain identChain() throws SyntaxException{

		return new IdentChain(match(IDENT));
	}

	Chain chain() throws SyntaxException {

		//chainElem arrowOp chainElem ( arrowOp chainElem)*
		//Chain ∷= ChainElem | BinaryChain

		Token current = t;
		Chain chain;

		if (t.isKind(IDENT)){

			chain = new IdentChain(consume());

		}
		else if (filterOp.contains(t.kind)){

			chain = filterOp();

		}
		else if (frameOp.contains(t.kind)){

			chain = frameOp();
		}
		else if (imageOp.contains(t.kind)){

			chain = imageOp();
		}
		else
			throw new SyntaxException("Illegal Chain");

		if (t.isKind(ARROW) || t.isKind(BARARROW)) {

			while (t.isKind(ARROW) || t.isKind(BARARROW)) {

				Token op = arrowOp();
				chain = new BinaryChain(current, chain, op, chainElem());
			}
		}

		return chain;
	}

	AssignmentStatement assign() throws SyntaxException {

		Token firstToken = consume();
		match(ASSIGN);
		IdentLValue identLValue = new IdentLValue(firstToken);
		Expression ex = expression();
		return new AssignmentStatement(firstToken, identLValue, ex);
	}

	Token weakOp() throws SyntaxException {

		if (weakOp.contains(t.kind))
			return consume();
		else
			//you will want to provide a more useful error message
			throw new SyntaxException("Illegal weak Op");
	}

	Token relOp() throws SyntaxException {

		if (relOp.contains(t.kind))
			return consume();
		else
			//you will want to provide a more useful error message
			throw new SyntaxException("Illegal rel Op");
	}

	Token strongOp() throws SyntaxException {

		if (strongOp.contains(t.kind))
			return consume();
		else
			//you will want to provide a more useful error message
			throw new SyntaxException("Illegal strong op");
	}

	Block block() throws SyntaxException {


		ArrayList<Dec> dec = new ArrayList<>();
		ArrayList<Statement> statements= new ArrayList<>();
		Token firstToken = t;

		match(LBRACE);

		while (true) {

			switch (t.kind) {

				case KW_INTEGER:
				case KW_BOOLEAN:
				case KW_IMAGE:
				case KW_FRAME:
					dec.add(dec());
					break;

				case IDENT:
				case OP_SLEEP:
				case KW_WHILE:
				case KW_IF:
				case ASSIGN:
				case KW_SHOW:
				case KW_HIDE:
				case KW_MOVE:
				case KW_XLOC:
				case KW_YLOC:
				case OP_BLUR:
				case OP_GRAY:
				case OP_CONVOLVE:
				case OP_WIDTH:
				case OP_HEIGHT:
				case KW_SCALE:
					statements.add(statement());
					break;

				default:
					match(RBRACE);
					return new Block(firstToken, dec, statements);
			}
		}

	}

	Program program() throws SyntaxException {

		Token firstToken = consume();
		ArrayList<ParamDec> paramDecList =  new ArrayList<>();
		Block block;

		if (t.isKind(LBRACE)){
			block= block();
		}
		else{
			paramDecList.add(paramDec());

			while (t.isKind(COMMA)) {
				consume();
				paramDecList.add(paramDec());
			}

			block = block();
		}

		return new Program(firstToken, paramDecList, block);
	}

	ParamDec paramDec() throws SyntaxException {

		Token firstToken = typeParam();
		Token ident = t;
		match(IDENT);

		return new ParamDec(firstToken, ident);
	}

	Dec dec() throws SyntaxException {

		Token firstToken = typeDec();
		Token ident = t;
		match(IDENT);

		return new Dec(firstToken, ident);
	}

	ChainElem chainElem() throws SyntaxException {

		if (t.isKind(IDENT))
			return identChain();

		else if (filterOp.contains(t.kind))
			return filterOp();

		else if (frameOp.contains(t.kind))
			return frameOp();

		else if (imageOp.contains(t.kind))
			return imageOp();

		else
			throw new SyntaxException("Illegal dec");

	}

	FrameOpChain frameOp() throws SyntaxException {

		Token current = t;

		switch (t.kind) {

			case KW_SHOW:
			case KW_HIDE:
			case KW_MOVE:
			case KW_XLOC:
			case KW_YLOC:
				consume();
				break;

			default:
				throw new SyntaxException("Illegal dec");
		}

		return new FrameOpChain(current, arg());
	}

	FilterOpChain filterOp() throws SyntaxException {

		//FilterOpChain ∷= filterOp arg

		Token current = t;

		switch (t.kind) {

			case OP_BLUR:
			case OP_GRAY:
			case OP_CONVOLVE:
				consume();
				break;

			default:
				throw new SyntaxException("Illegal dec");
		}

		return new FilterOpChain(current, arg());
	}

	ImageOpChain imageOp() throws SyntaxException {

		Token current = t;

		switch (t.kind) {

			case OP_WIDTH:
			case OP_HEIGHT:
			case KW_SCALE:
				consume();
				break;

			default:
				throw new SyntaxException("Illegal dec");
		}

		return new ImageOpChain(current, arg());
	}

	Token type() throws SyntaxException{

		Token current = t;

		if (type.contains(t.kind)) {
			consume();
			return current;
		}
		else
			throw new SyntaxException("Illegal type for token: " + t.getText());
	}

	Token typeParam() throws SyntaxException{

		Token current = t;

		if (typeParam.contains(t.kind)) {
			consume();
			return current;
		}
		else
			throw new SyntaxException("Illegal type for token: " + t.getText());
	}

	Token typeDec() throws SyntaxException{

		Token current = t;

		if (typeDec.contains(t.kind)) {
			consume();
			return current;
		}
		else
			throw new SyntaxException("Illegal type for token: " + t.getText());
	}

	Tuple arg() throws SyntaxException {

		Token firstToken = t;
		List<Expression> argList = new ArrayList<>();

		switch (t.kind) {

			case LPAREN:
				consume();
				argList.add(expression());

				while (t.isKind(COMMA)) {
					consume();
					argList.add(expression());
				}
				match(RPAREN);
				break;

			default:
				//throw new SyntaxException("Illegal arg");
		}
		return new Tuple(firstToken, argList);
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 *
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {

		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * <p>
	 * Precondition: kind != EOF
	 *
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {

		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.getText() +  " kind: " + t.kind + " expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * <p>
	 * * Precondition: for all given kinds, kind != EOF
	 *
	 * @param kinds list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {

		for (Kind kind : kinds) {

			if (t.isKind(kind)) {
				consume();
				return t;
			}
		}

		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * Precondition: t.kind != EOF
	 *
	 * @return
	 */
	private Token consume() throws SyntaxException {

		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
