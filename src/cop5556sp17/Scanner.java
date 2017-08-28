package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;

import cop5556sp17.AST.Type.*;

public class Scanner {

	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;
	int line = 0, posNewLine = 0;
	HashMap<String, Kind> reserved;

	Scanner(String chars) {

		this.chars = chars;
		tokens = new ArrayList<>();
		reserved = new HashMap<>();


		for (Kind kind : Kind.values()) {

			reserved.put(kind.getText(), kind);
		}
	}

	public enum State {

		START, IN_DIGIT, IN_IDENT, AFTER_EQ
	}

	/**
	 * Kind enum
	 */
	public enum Kind {

		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"),
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"),
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"),
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"),
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"),
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="),
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"),
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"),
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"),
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"),
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"),
		KW_SCALE("scale"), EOF("eof");

		final String text;

		Kind(String text) {
			this.text = text;
		}

		String getText() {
			return text;
		}
	}

	/**
	 * Thrown by Scanner when an illegal character is encountered
	 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}

	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
		public IllegalNumberException(String message) {
			super(message);
		}
	}

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {

		public final int line;
		public final int posInLine;

		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}


	public class Token {

		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;
		private LinePos linePos;
		private TypeName type;


		//returns the text of this Token
		public String getText() {

			if (length == 0)
				return null;

			return chars.substring(pos, pos + length);
		}

		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos() {

			return this.linePos;
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;

			this.linePos = new LinePos(line, pos - posNewLine);
		}

		/**
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 *
		 * @return int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException {

			return Integer.parseInt(chars.substring(pos, pos + length));
		}

		public boolean isKind(Kind kind) {

			return this.kind == kind;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + pos;
			return result;
		}

		@Override
		public boolean equals(Object obj) {

			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Token)) {
				return false;
			}
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (kind != other.kind) {
				return false;
			}
			if (length != other.length) {
				return false;
			}
			if (pos != other.pos) {
				return false;
			}
			return true;
		}

		private Scanner getOuterType() {
			return Scanner.this;
		}

		public TypeName getType() {
			return type;
		}

		public void setType(TypeName type) {
			this.type = type;
		}
	}


	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 *
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException, Parser.SyntaxException {

		int pos = 0;

		int length = chars.length();
		State state = State.START;
		int startPos = 0;
		int ch = pos;

		while (pos <= length) {

			switch (state) {

				case START: {

					pos = skipWhiteSpace(pos);
					ch = pos < length ? chars.charAt(pos) : -1;
					startPos = pos;
					switch (ch) {

						case -1: {
							tokens.add(new Token(Kind.EOF, pos, 0));
							pos++;
						}
						break;
						case '\n': {
							line++;
							pos++;
							posNewLine = pos;
						}
						break;
						case '+': {
							tokens.add(new Token(Kind.PLUS, startPos, 1));
							pos++;
						}
						break;
						case '-': {

							if (pos + 1 < length && chars.charAt(pos + 1) == '>') {
								pos++;
								tokens.add(new Token(Kind.ARROW, startPos, 2));
							} else
								tokens.add(new Token(Kind.MINUS, startPos, 1));

							pos++;
						}
						break;
						case '*': {
							tokens.add(new Token(Kind.TIMES, startPos, 1));
							pos++;
						}
						break;
						case '&': {
							tokens.add(new Token(Kind.AND, startPos, 1));
							pos++;
						}
						break;
						case '%': {
							tokens.add(new Token(Kind.MOD, startPos, 1));
							pos++;
						}
						break;
						case '/': {

							if (pos + 1 < length && chars.charAt(pos + 1) == '*') {

								int tempPos = pos + 2;

								while (tempPos + 1 < length) {

									if (chars.substring(tempPos, tempPos + 2).equals("*/")) {
										pos = tempPos + 2;
										break;
									} else if (chars.charAt(tempPos) == '\n') {
										tempPos++;
										line++;
										posNewLine = tempPos;
									} else
										tempPos++;
								}

								if (tempPos + 1 == length) {

									pos = tempPos + 1;
									throw new IllegalCharException(
											"Unclosed comment" + " at pos " + pos);
								}
							} else {
								tokens.add(new Token(Kind.DIV, startPos, 1));
								pos++;
							}
						}
						break;
						case '<': {

							if (pos + 1 < length && chars.charAt(pos + 1) == '-') {
								pos++;
								tokens.add(new Token(Kind.ASSIGN, startPos, 2));
							} else if (pos + 1 < length && chars.charAt(pos + 1) == '=') {
								pos++;
								tokens.add(new Token(Kind.LE, startPos, 2));
							} else
								tokens.add(new Token(Kind.LT, startPos, 1));

							pos++;
						}
						break;
						case '>': {

							if (pos + 1 < length && chars.charAt(pos + 1) == '=') {
								pos++;
								tokens.add(new Token(Kind.GE, startPos, 2));
							} else
								tokens.add(new Token(Kind.GT, startPos, 1));

							pos++;
						}
						break;
						case '|': {

							if (pos + 2 < length && chars.charAt(pos + 1) == '-'
									&& chars.charAt(pos + 2) == '>') {
								tokens.add(new Token(Kind.BARARROW, startPos, 3));
								pos++;
								pos++;
							} else
								tokens.add(new Token(Kind.OR, startPos, 1));

							pos++;
						}
						break;
						case '!': {

							if (pos + 1 < length && chars.charAt(pos + 1) == '=') {
								pos++;
								tokens.add(new Token(Kind.NOTEQUAL, startPos, 2));

							} else
								tokens.add(new Token(Kind.NOT, startPos, 1));

							pos++;
						}
						break;
						case '(': {
							tokens.add(new Token(Kind.LPAREN, startPos, 1));
							pos++;
						}
						break;
						case ')': {
							tokens.add(new Token(Kind.RPAREN, startPos, 1));
							pos++;
						}
						break;
						case '{': {
							tokens.add(new Token(Kind.LBRACE, startPos, 1));
							pos++;
						}
						break;
						case '}': {
							tokens.add(new Token(Kind.RBRACE, startPos, 1));
							pos++;
						}
						break;
						case ',': {
							tokens.add(new Token(Kind.COMMA, startPos, 1));
							pos++;
						}
						break;
						case ';': {
							tokens.add(new Token(Kind.SEMI, startPos, 1));
							pos++;
						}
						break;
						case '=': {
							state = State.AFTER_EQ;
							pos++;
						}
						break;
						case '0': {
							tokens.add(new Token(Kind.INT_LIT, startPos, 1));
							pos++;
						}
						break;

						default: {
							if (Character.isDigit(ch)) {
								state = State.IN_DIGIT;
								pos++;
							} else if (Character.isJavaIdentifierStart(ch)) {
								state = State.IN_IDENT;
								pos++;
							} else {
								throw new IllegalCharException(
										"illegal char " + (char) ch + " at pos " + pos);
							}
						}
					}
				}
				break;

				case IN_DIGIT: {

					if (pos < length && Character.isDigit(chars.charAt(pos))) {
						pos++;
					} else {

						String s = chars.substring(startPos, pos);

						try {

							Integer.parseInt(s);
							tokens.add(new Token(Kind.INT_LIT, startPos, pos - startPos));
							state = State.START;

						} catch (NumberFormatException nf) {

							throw new IllegalNumberException("illegal number " + s + " starting position: " + startPos
									+ " to position:" + pos);
						}
					}
				}
				break;

				case IN_IDENT: {

					if (pos < length && Character.isJavaIdentifierPart(chars.charAt(pos))) {

						pos++;
					} else if (reserved.containsKey(chars.substring(startPos, pos))) {

						//Add code for reserved words
						tokens.add(new Token(reserved.get(chars.substring(startPos, pos)), startPos, pos - startPos));
						state = State.START;


					} else {

						tokens.add(new Token(Kind.IDENT, startPos, pos - startPos));
						state = State.START;
					}
				}
				break;

				case AFTER_EQ: {

					if (pos == length || chars.charAt(pos) != '=') {
						throw new IllegalCharException(
								"illegal char " + (char) ch + " at pos " + pos);
					} else {
						pos++;
						tokens.add(new Token(Kind.EQUAL, startPos, pos - startPos));
						state = State.START;
					}
				}
				break;

				default:
					assert false;
			}
		}

		return this;
	}


	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..
	 */
	public Token nextToken() {

		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}

	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);
	}


	/**
	 * Returns a LinePos object containing the line and position in line of the
	 * given token.
	 * <p>
	 * Line numbers start counting at 0
	 *
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {

		return t.getLinePos();
	}


	public int skipWhiteSpace(int pos) {

		while (pos < chars.length() && chars.charAt(pos) != '\n' && Character.isWhitespace(chars.charAt(pos)))
			pos++;

		return pos;
	}
}
