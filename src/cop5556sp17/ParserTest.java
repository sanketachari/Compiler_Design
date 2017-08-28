package cop5556sp17;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;


public class ParserTest {

	String EXPRESSION = "false * true / abcd & 123 *56" +
		" == true * false /screenheight  % 56";

	String CHAIN_ELEM_1 = "height ("+ EXPRESSION + " , "+ EXPRESSION + " , "+ EXPRESSION
			+ " , "+ EXPRESSION + ")";

	String CHAIN_ELEM_2 = "abcd";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.factor();
	}

	@Test
	public void testFactor1() throws IllegalCharException, IllegalNumberException, SyntaxException {

		// Updated
		String input = "true false 1223 screenwidth screenheight";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);

		int len = scanner.tokens.size() - 1; // total kinds

		while (len > 0) {
			parser.factor();
			len--;
		}
	}

	@Test
	public void testExpression0() throws IllegalCharException, IllegalNumberException, SyntaxException {

		String input = "abcd * 23 + bfg * 50 != hdh * 0 - jkdfbk * 669";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);

		parser.expression();
	}

	@Test
	public void testExpression1() throws IllegalCharException, IllegalNumberException, SyntaxException {

		String input = "abcd * 23 + bfg * 50 != hdh * 0 >= jkdfbk * 669";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);

		parser.expression();
	}

	@Test
	public void testExpression2() throws IllegalCharException, IllegalNumberException, SyntaxException {

		// test when factor is (expression)
		String input = "abcd * 23 + bfg * 50 != hdh * 0 >= (abcd * 23 + bfg * 50 != hdh * 0 - jkdfbk * 669)";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);

		parser.expression();
	}

	@Test
	public void testArg() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  (3,5) ";
		Scanner scanner = new Scanner(input);
		scanner.scan();

		Parser parser = new Parser(scanner);
        parser.arg();
	}

	@Test
	public void testArg2() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = " 123( ";
		Scanner scanner = new Scanner(input);
		scanner.scan();

		Parser parser = new Parser(scanner);
		parser.arg();
	}

	@Test
	public void testArgError() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  (3,) ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		thrown.expect(Parser.SyntaxException.class);
		parser.arg();
	}

	@Test
	public void testProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "prog0 {integer \n abc}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}

	@Test
	public void testChain() throws IllegalCharException, IllegalNumberException, SyntaxException{

		String input = "abc |-> xyz -> abc";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.chain();
	}

	@Test
	public void testStrongOp() throws IllegalCharException, IllegalNumberException, SyntaxException{

		String input = "*/%&";
		Scanner scanner = new Scanner(input);
		Parser parser = new Parser(scanner.scan());

		int len = scanner.tokens.size() - 1; // total kinds

		while (len > 0) {
			parser.strongOp();
			len--;
		}
	}

	@Test
	public void testWeakOp() throws IllegalCharException, IllegalNumberException, SyntaxException{

		String input = "+-|";
		Scanner scanner = new Scanner(input);
		Parser parser = new Parser(scanner.scan());

		int len = scanner.tokens.size() - 1; // total kinds

		while (len > 0) {
			parser.weakOp();
			len--;
		}
	}

	@Test
	public void testRelOp() throws IllegalCharException, IllegalNumberException, SyntaxException{

		String input = "<><=>=!===";
		Scanner scanner = new Scanner(input);
		Parser parser = new Parser(scanner.scan());

		int len = scanner.tokens.size() - 1; // total kinds

		while (len > 0) {
			parser.relOp();
			len--;
		}
	}

	@Test
	public void testImageOp() throws IllegalCharException, IllegalNumberException, SyntaxException{

		//Updated
		String input = "width height scale height";
		Scanner scanner = new Scanner(input);
		Parser parser = new Parser(scanner.scan());

		int len = scanner.tokens.size() - 1; // total kinds

		while (len > 0) {
			parser.imageOp();
			len--;
		}
	}

	@Test
	public void testFrameOp() throws IllegalCharException, IllegalNumberException, SyntaxException{

		// Updated
		String input = "show hide move xloc yloc";
		Scanner scanner = new Scanner(input);
		Parser parser = new Parser(scanner.scan());

		int len = scanner.tokens.size() - 1; // total kinds

		while (len > 0) {
			parser.frameOp();
			len--;
		}
	}

	@Test
	public void testFilterOp() throws IllegalCharException, IllegalNumberException, SyntaxException{

		String input = "blur gray convolve";
		Scanner scanner = new Scanner(input);
		Parser parser = new Parser(scanner.scan());

		int len = scanner.tokens.size() - 1; // total kinds

		while (len > 0) {
			parser.filterOp();
			len--;
		}
	}

	@Test
	public void testArrowOp() throws IllegalCharException, IllegalNumberException, SyntaxException{

		String input = "->|->";
		Scanner scanner = new Scanner(input);
		Parser parser = new Parser(scanner.scan());

		int len = scanner.tokens.size() - 1; // total kinds

		while (len > 0) {
			parser.arrowOp();
			len--;
		}
	}

	@Test
	public void testChainElem0() throws IllegalCharException, IllegalNumberException, SyntaxException{

		String input = "intennmkgerm\nshow \nwidth\nblur";
		Scanner scanner = new Scanner(input);
		Parser parser = new Parser(scanner.scan());

		int len = scanner.tokens.size() - 1; // total kinds

		while (len > 0) {
			parser.chainElem();
			len--;
		}
	}

	@Test
	public void testChainElem1() throws IllegalCharException, IllegalNumberException, SyntaxException{

		// test frameOp arg then inside arg = (expression, expression)
		String input = "show (abcd*23 + bfg*50 != hdh*0 >= abcd*23, bfg*50 != hdh*0 - jkdfbk*669)";
		Scanner scanner = new Scanner(input);
		Parser parser = new Parser(scanner.scan());

		parser.chainElem();
	}

	@Test
	public void testChainElem2() throws IllegalCharException, IllegalNumberException, SyntaxException{

		String input = "intennmkgerm\nshow (abcd*23 + bfg*50 != hdh*0 >= (abcd*23 + bfg*50 != hdh*0 - jkdfbk*669))" +
				"\nwidth (abcd*23 + bfg*50 != hdh*0 >= (abcd*23 + bfg*50 != hdh*0 - jkdfbk*669))" +
				"\nblur (abcd*23 + bfg*50 != hdh*0 >= (abcd*23 + bfg*50 != hdh*0 - jkdfbk*669))";

		Scanner scanner = new Scanner(input);
		Parser parser = new Parser(scanner.scan());

		int len = 4; // total kinds

		while (len > 0) {
			parser.chainElem();
			len--;
		}
	}

	@Test
	public void testDec() throws IllegalCharException, IllegalNumberException, SyntaxException{

		// Updated
		String input = "integer a boolean xackn image kjbkjb frame kbkjbsakj";
		Scanner scanner = new Scanner(input);
		Parser parser = new Parser(scanner.scan());

		int len = 4; // total kinds

		while (len > 0) {
			parser.dec();
			len--;
		}
	}

	@Test
	public void testParamDec() throws IllegalCharException, IllegalNumberException, SyntaxException{

		String input = "url a boolean xackn file jhvsa integer kbackjbk";
		Scanner scanner = new Scanner(input);
		Parser parser = new Parser(scanner.scan());

		int len = 4; // total kinds

		while (len > 0) {
			parser.paramDec();
			len--;
		}
	}

	@Test
	public void testProgram1() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "prog0 { integer abcd image abcd frame abcd boolean abcd }";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}

	@Test
	public void testProgram2() throws IllegalCharException, IllegalNumberException, SyntaxException{
		//OP_SLEEP expression ;​
		String input = "prog0 { sleep "+ EXPRESSION + ";}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}

	@Test
	public void testProgram3() throws IllegalCharException, IllegalNumberException, SyntaxException{
		//whileStatement ::= KW_WHILE (​ expression )​ block
		String input = "prog0 { while (" + EXPRESSION +") {image abcd}}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}

	@Test
	public void testProgram4() throws IllegalCharException, IllegalNumberException, SyntaxException{
		//ifStatement ::= KW_IF (​ expression )​ block
		String input = "prog0 { if (" + EXPRESSION +") {image abcd}}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}

	@Test
	public void testProgram5() throws IllegalCharException, IllegalNumberException, SyntaxException{
		//statement ::= assign;
		String input = "prog0 {abcd <- " + EXPRESSION +" % 88;}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}

	@Test
	public void testProgram6() throws IllegalCharException, IllegalNumberException, SyntaxException{

		//statement ::= chain;
		String input = "prog0 {abcd -> scale;}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}

	@Test
	public void testProgram7() throws IllegalCharException, IllegalNumberException, SyntaxException{

		//statement ::= chain;
		String input = "prog0 {abcd -> "+ CHAIN_ELEM_1 + "|-> "+ CHAIN_ELEM_2 + "-> "+ CHAIN_ELEM_1+ ";}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}

	@Test
	public void testBlockChain1() throws IllegalCharException, IllegalNumberException, SyntaxException{

		//statement ::= chain;
		String input = "__ {__->_|->$0|-> show (__/_%($$TAT$T_T%$)|true*screenwidth&$|_*$!=_==_>=(z_z),_&$|_$+_0); while (__==$$!=$_/_){sleep z$_2+_3z%$;} blur -> width ($);}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.program();
	}

	@Test
	public void testIdentChain() throws IllegalCharException, IllegalNumberException, SyntaxException{

		//statement ::= chain;
		String input = "abc \n{x \n<- \n33;}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.program();
	}


}
