package cop5556sp17;

import static cop5556sp17.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;

public class ScannerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testEmpty() throws Exception {
		String input = "";
		Scanner scanner = new Scanner(input);
		scanner.scan();
	}

	@Test
	public void testNewLine() throws Exception{

		String input = "\nabc\nxyz";
		Scanner scanner = new Scanner(input);
		scanner.scan();

		Scanner.Token t = scanner.nextToken();

		assertEquals(0, t.getLinePos().posInLine);
		assertEquals(1, t.getLinePos().line);

		t =  scanner.nextToken();
		assertEquals(0, t.getLinePos().posInLine);
		assertEquals(2, t.getLinePos().line);
	}

	@Test
	public void testEquals() throws Exception{

		String input = "!==";
		Scanner scanner = new Scanner(input);
		thrown.expect(IllegalCharException.class);
		scanner.scan();
	}

	@Test
	public void testInteger() throws Exception{

		String input = "123";
		Scanner scanner = new Scanner(input);
		scanner.scan();

		Scanner.Token t = scanner.nextToken();
		assertEquals(Integer.parseInt(input), t.intVal());
		assertEquals(INT_LIT,t.kind);
		assertEquals(input.length(), t.length);
	}

	@Test
	public void testComment() throws Exception {

		String input = "/*abc\nxyz\nefg */false";
		Scanner scanner = new Scanner(input);
		scanner.scan();

		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.KW_FALSE, token.kind);
		assertEquals(16, token.pos);
		String text = Scanner.Kind.KW_FALSE.getText();
		Scanner.LinePos l = new Scanner.LinePos(2, 6);
		assertEquals(l.posInLine, token.getLinePos().posInLine);
		assertEquals(text.length(), token.length);
		assertEquals(text, token.getText());

	}

	@Test
	public void testSemiConcat() throws Exception {
		// input string
		String input = ";;;";
		// create and initialize the scanner
		Scanner scanner = new Scanner(input);
		scanner.scan();
		{
			// get the first token and check its kind, position, and contents
			Scanner.Token token = scanner.nextToken();

			assertEquals(SEMI, token.kind);
			assertEquals(0, token.pos);
			Scanner.LinePos l = new Scanner.LinePos(0, 0);
			assertEquals(l.posInLine, token.getLinePos().posInLine);
			String text = SEMI.getText();
			assertEquals(text.length(), token.length);
			assertEquals(text, token.getText());
		}

		{
			// get the next token and check its kind, position, and contents
			Scanner.Token token1 = scanner.nextToken();
			//
			Scanner.LinePos l1 = new Scanner.LinePos(0, 1);
			assertEquals(l1.posInLine, token1.getLinePos().posInLine);
			assertEquals(SEMI, token1.kind);
			assertEquals(1, token1.pos);
			assertEquals(1, token1.length);
			assertEquals(SEMI.text, token1.getText());
		}

		{
			Scanner.Token token2 = scanner.nextToken();
			//
			Scanner.LinePos l2 = new Scanner.LinePos(0, 2);
			assertEquals(l2.posInLine, token2.getLinePos().posInLine);
			assertEquals(SEMI, token2.kind);
			assertEquals(2, token2.pos);
			assertEquals(1, token2.length);
			assertEquals(SEMI.text, token2.getText());
		}

		{
			// check that the scanner has inserted an EOF token at the end
			Scanner.Token token3 = scanner.nextToken();
			assertEquals(EOF, token3.kind);
		}
	}

	@Test
	public void testIntOverflowError() throws Exception {
		String input = "99999999999999999";
		Scanner scanner = new Scanner(input);
		thrown.expect(IllegalNumberException.class);
		scanner.scan();
	}

	@Test
	public void testIllegalChar() throws Exception {
		String input = "^^";
		Scanner scanner = new Scanner(input);
		thrown.expect(IllegalCharException.class);
		scanner.scan();
	}

	@Test
	public void testUnclosedComment() throws Exception {
		/* throw illegalCharException if comment remains open till the end of file*/

		String input = "/*abcd";
		Scanner scanner = new Scanner(input);
		thrown.expect(IllegalCharException.class);
		scanner.scan();
	}

	@Test
	public void testIdent() throws Exception {

		String input = "abcd)";
		Scanner scanner = new Scanner(input);
		scanner.scan();

		assertEquals(IDENT.toString(), scanner.nextToken().kind.toString());
	}

	@Test
	public void testIdent1() throws Exception {

		String input = "true abcd";
		Scanner scanner = new Scanner(input);
		scanner.scan();

		int len = scanner.tokens.size() - 1;

		while (len > 0){
			len--;
		}
	}
}