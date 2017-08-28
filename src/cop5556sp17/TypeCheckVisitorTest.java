/**
 * Important to test the error cases in case the
 * AST is not being completely traversed.
 * <p>
 * Only need to test syntactically correct programs, or
 * program fragments.
 */

package cop5556sp17;

import static org.junit.Assert.*;

import cop5556sp17.AST.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.TypeCheckVisitor.TypeCheckException;

public class TypeCheckVisitorTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testAssignmentBoolLit0_0() throws Exception {
		String input = "p {\nboolean y \ny <- false;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
	}

	@Test
	public void testAssignmentBoolLitError0_0() throws Exception {

		String input = "p {\nboolean y \ny <- 3;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testAssignmentIntLitError1_0() throws Exception {

		String input = "p {\ninteger y \ny <- true;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testProgram0() throws Exception {

		String input = "prog0 integer i {}";
		Parser parser = new Parser(new Scanner(input).scan());


		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
	}

	@Test
	public void testProgram1_0() throws Exception {
		String input = "prog0  url u, integer i, boolean b, url u, file f {image im frame fr}";
		Parser parser = new Parser(new Scanner(input).scan());

		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testProgramIfStatement() throws Exception {
		//whileStatement ::= KW_WHILE (​ expression )​ block
		String input = "Integer integer Integer, url Url, file File \n{\n integer a integer b if(a==b){" +
				"sleep a+b;}\n}\n";
		Parser parser = new Parser(new Scanner(input).scan());

		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
	}

	@Test
	public void testProgramWhileStatement() throws Exception {

		String input = "Integer integer Integer, url Url, file File \n{\n integer a integer b while(a==b){" +
				"sleep a+b;}\n}\n";
		Parser parser = new Parser(new Scanner(input).scan());

		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
	}

	@Test
	public void testProgramIfWhileStatement() throws Exception {

		String input = "Integer integer Integer, url Url, file File " +
				"\n{\n integer a integer b if(a==b) { integer c sleep a+b;}" +
				" while(a!=b){c <- b;}}";

		Parser parser = new Parser(new Scanner(input).scan());

		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testBinaryChainFrameOp() throws Exception {
		//statement ::= assign;
		String input = "prog0 file File { frame f integer x integer y  while (true) \n" +
				" { xloc  ; yloc ; f -> show -> move(x,y) -> hide; } }";
		Parser parser = new Parser(new Scanner(input).scan());

		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
	}

	@Test
	public void testBinaryChainImageOp() throws Exception {

		String input = "prog0 file File { image i integer x integer y   \n" +
				" x <- screenwidth; y <- screenheight; i -> width; i -> height;}";//-> height ; i -> show -> move(x,y) -> hide;  }";
		Parser parser = new Parser(new Scanner(input).scan());

		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
	}

	@Test
	public void testBinaryChainFilterOp() throws Exception {

		String input = "prog0 file File { image i integer x integer y   \n" +
				"i -> gray -> convolve -> blur;}";//-> height ; i -> show -> move(x,y) -> hide;  }";
		Parser parser = new Parser(new Scanner(input).scan());

		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
	}

	@Test
	public void testFileFrame() throws Exception {
		//doubt:
		String input = "prog0 { frame f image i integer x integer y   \n" +
				"i -> f -> xloc;}";
		Parser parser = new Parser(new Scanner(input).scan());

		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
	}

	@Test
	public void testUrlImage() throws Exception {

		String input = "prog0 url u, file f  { frame fr image i integer x integer y   \n" +
				"u -> i -> f;}";//-> height ; i -> show -> move(x,y) -> hide;  }";
		Parser parser = new Parser(new Scanner(input).scan());

		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
	}

	@Test
	public void testExpression() throws Exception {

		String input = "prog0 url u, file f  { frame fr image i integer x integer y   \n" +
				" if (x < y ){ " +
				"while(x > y) { " +
				"		if( x <= y != (y >= x)) " +
				"			{ x <- x - y ;} " +
				"	}" +
				" }" +
				"}";
		Parser parser = new Parser(new Scanner(input).scan());

		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
	}
}
