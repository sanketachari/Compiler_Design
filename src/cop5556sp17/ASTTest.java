package cop5556sp17;

import static cop5556sp17.Scanner.Kind.PLUS;
import static org.junit.Assert.assertEquals;

import cop5556sp17.AST.*;
import junit.framework.TestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;

public class ASTTest {

	static final boolean doPrint = true;

	static void show(Object s) {
		if (doPrint) {
			System.out.println(s);
		}
	}


	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IdentExpression.class, ast.getClass());
	}

	@Test
	public void testFactor1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "123";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IntLitExpression.class, ast.getClass());
	}

	@Test
	public void testBinaryExpr0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "1+abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(IntLitExpression.class, be.getE0().getClass());
		assertEquals(IdentExpression.class, be.getE1().getClass());
		assertEquals(PLUS, be.getOp().kind);
	}

    @Test
	public void testExpression0() throws IllegalCharException, IllegalNumberException, SyntaxException {

		String input = "(1+2)*3";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
	}

}
