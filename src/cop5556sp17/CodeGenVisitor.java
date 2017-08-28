package cop5556sp17;


import java.util.ArrayList;

import cop5556sp17.AST.*;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp17.Scanner.Token;

import static cop5556sp17.AST.Type.*;

import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL          used as parameter to genPrint and genPrintTOS
	 * @param GRADE          used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {

		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	private boolean isBarrow;

	MethodVisitor mv; // visitor of method currently under construction

	private int slot = 0;
	private int globalVarCount = 0;

	/**
	 * Indicates whether genPrint and genPrintTOS should generate code.
	 */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {

		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[]{"java/lang/Runnable"});
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();


		for (ParamDec dec : params)
			dec.visit(this, mv);

		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();


		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);

		for (Dec dec : program.getB().getDecs()) {

			mv.visitLocalVariable(dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc(),
					null, startRun, endRun, dec.getSlot());
		}


		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method

		cw.visitEnd();//end of class

		//generate classfile and return it
		return cw.toByteArray();
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {

		// AssignmentStatement ∷= IdentLValue Expression
		// store value of Expression into location indicated by IdentLValue

		// if the type of elements is image, this should copy the image.
		// use PLPRuntimeImageOps.copyImage

		if (assignStatement.getVar().getDec() instanceof ParamDec) {
			mv.visitVarInsn(ALOAD, 0);
		}

		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
		assignStatement.getVar().visit(this, arg);

		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {

		/*  Visit the left expression.
			If the left Chain is a URL, generate code to invoke PLPRuntimeImageIO.readFromURL and
			leave a reference to a BufferedImage object on top of the stack.
			If the left expression is a File, generate code to invoke PLPRuntimeImageIO.readFromFile and
			leave a reference to a BufferedImage object on top of the stack.
			Otherwise generate code to leave the left object on top of the stack.
			Visit the right ChainElem and handle as given above.
			Hint:  integers, for example, could appear on either side of a BinaryChain, in one the action is load,
			the other is store.  You need to figure out a way to communicate to the IdentChain which one.
			Hint:  although some combinations have a type NONE, it is easiest to let all binary chain instances
			leave something on top of the stack.  In many cases, this value will be consumed by a parent.
			At the top level it should be popped.
		*/

		Chain e0 = binaryChain.getE0();
		e0.visit(this, true);

		if (binaryChain.getArrow().isKind(BARARROW))
			isBarrow = true;
		else
			isBarrow = false;

		ChainElem e1 = binaryChain.getE1();
		e1.visit(this, false);

		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {


		/* Visit children to generate code to leave values of arguments on stack
	 	 perform operation, leaving result on top of the stack.  Expressions should
      	   be evaluated from left to write consistent with the structure of the AST.
        */

		Expression e0 = binaryExpression.getE0();
		Expression e1 = binaryExpression.getE1();

		TypeName type = binaryExpression.getType();
		Token op = binaryExpression.getOp();

		Label l1 = new Label();
		Label l2 = new Label();


		if ((op.kind == PLUS || op.kind == MINUS || op.kind == TIMES || op.kind == DIV || op.kind == MOD
				|| op.kind == AND || op.kind == OR) &&
				type.isType(TypeName.INTEGER)) {

			e0.visit(this, arg);
			e1.visit(this, arg);

			switch (op.kind) {

				case PLUS:
					mv.visitInsn(IADD);
					break;
				case MINUS:
					mv.visitInsn(ISUB);
					break;
				case TIMES:
					mv.visitInsn(IMUL);
					break;
				case DIV:
					mv.visitInsn(IDIV);
					break;

				case MOD:
					mv.visitInsn(IREM);
					break;

				case AND:
					mv.visitInsn(IAND);
					break;

				case OR:
					mv.visitInsn(IOR);
					break;

				default:
					throw new UnsupportedOperationException("Incorrect kind for integer");
			}
		} else if (op.kind == EQUAL) {

			e0.visit(this, arg);
			e1.visit(this, arg);

			if (type.equals(TypeName.INTEGER) || type.equals(TypeName.BOOLEAN))
				mv.visitJumpInsn(IF_ICMPNE, l1);

			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);

		} else if (op.kind == NOTEQUAL) {

			e0.visit(this, arg);
			e1.visit(this, arg);

			if (type.equals(TypeName.INTEGER) || type.equals(TypeName.BOOLEAN))
				mv.visitJumpInsn(IF_ICMPEQ, l1);

			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);

		} else if ((op.kind == LT || op.kind == GT || op.kind == LE || op.kind == GE ||
				op.isKind(AND) || op.isKind(OR)) && type.isType(BOOLEAN)) {

			e0.visit(this, arg);
			e1.visit(this, arg);

			switch (op.kind) {

				case LT:
					mv.visitJumpInsn(IF_ICMPGE, l1);
					break;
				case GT:
					mv.visitJumpInsn(IF_ICMPLE, l1);
					break;
				case LE:
					mv.visitJumpInsn(IF_ICMPGT, l1);
					break;
				case GE:
					mv.visitJumpInsn(IF_ICMPLT, l1);
					break;
				case OR:
					mv.visitInsn(IOR);
					mv.visitJumpInsn(IFEQ, l1);
					break;
				case AND:
					mv.visitInsn(IAND);
					mv.visitJumpInsn(IFEQ, l1);
					break;

				default:
					throw new UnsupportedOperationException("Incorrect kind for integer or boolean");
			}

			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
		}
		else if ((op.isKind(PLUS) || op.isKind(MINUS) || op.isKind(MOD) || op.isKind(TIMES) || op.isKind(DIV))
				&& type.isType(IMAGE)) {


			switch (op.kind) {

				case PLUS:
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add",
							PLPRuntimeImageOps.addSig, false);
					break;

				case MINUS:
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub",
							PLPRuntimeImageOps.subSig, false);
					break;

				case TIMES:
					if (e0.getType().isType(TypeName.INTEGER)) {
						e1.visit(this, arg);
						e0.visit(this, arg);
					} else {
						e0.visit(this, arg);
						e1.visit(this, arg);
					}

					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul",
							PLPRuntimeImageOps.mulSig, false);
					break;

				case DIV:
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div",
							PLPRuntimeImageOps.divSig, false);
					break;

				case MOD:
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod",
							PLPRuntimeImageOps.modSig, false);
					break;

				default:
					throw new UnsupportedOperationException("Incorrect kind for image");

			}
		}else {
			throw new UnsupportedOperationException("Error occurred while generating code for binary expression");
		}

		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {


		// Decs are local variables in current scope of run method
		// Statements are executed in run method
		// Must label beginning and end of scope, and keep track of local variables,
		// their slot in the local variable array, and their range of visibility.


		// doubt: If a statement was a BinaryChain, it will have left a value on top of the stack.
		// Check for this and pop it if necessary.

		for (Dec dec : block.getDecs()) {

			dec.visit(this, arg);
		}


		for (Statement st : block.getStatements()) {

			st.visit(this, arg);

			// doubt
			if (st instanceof BinaryChain)
				mv.visitInsn(POP);
		}

		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {


		// load constant
		if (booleanLitExpression.getValue()) {
			mv.visitInsn(ICONST_1);
		} else {
			mv.visitInsn(ICONST_0);
		}

		return "Z";
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {

		if (constantExpression.getFirstToken().isKind(KW_SCREENWIDTH))
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth",
					PLPRuntimeFrame.getScreenWidthSig, false);

		else if (constantExpression.getFirstToken().isKind(KW_SCREENHEIGHT))
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight",
					PLPRuntimeFrame.getScreenHeightSig, false);

		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {


		// Assign a slot in the local variable array to this variable
		// and save it in the new slot attribute in the  Dec class.

		declaration.setSlot(slot++);

		if (declaration.getTypeName().isType(TypeName.FRAME) || declaration.getTypeName().isType(TypeName.IMAGE)) {

			//frame maps to cop5556sp17.PLPRuntimeFrame
			//image maps to java.awt.image.BufferedImage

			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, declaration.getSlot());

		}

		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {

		/* 	Assume that a reference to a BufferedImage is on top of the stack.
			Generate code to invoke the appropriate method from PLPRuntimeFilterOps.
		*/

		if (isBarrow && filterOpChain.getFirstToken().isKind(OP_GRAY)) {
			mv.visitInsn(DUP);
		} else {
			mv.visitInsn(ACONST_NULL);
		}

		switch (filterOpChain.getFirstToken().kind) {

			case OP_BLUR:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp",
						PLPRuntimeFilterOps.opSig, false);
				break;

			case OP_CONVOLVE:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp",
						PLPRuntimeFilterOps.opSig, false);
				break;

			case OP_GRAY:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp",
						PLPRuntimeFilterOps.opSig, false);
				break;

			default:
				throw new Exception("Error occurred in filterOPChain");
		}

		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {

		/* 	Assume that a reference to a PLPRuntimeFrame is on top of the stack.
			Visit the tuple elements to generate code to leave their values on top of the stack.
			Generate code to invoke the appropriate method from PLPRuntimeFrame
		*/

		frameOpChain.getArg().visit(this, arg);


		switch (frameOpChain.getFirstToken().kind) {

			case KW_SHOW:

				mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage",
						PLPRuntimeFrame.showImageDesc, false);
				break;

			case KW_HIDE:
				mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage",
						PLPRuntimeFrame.hideImageDesc, false);
				break;


			case KW_MOVE:
				mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame",
						PLPRuntimeFrame.moveFrameDesc, false);
				break;

			case KW_XLOC:

				mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal",
						PLPRuntimeFrame.getXValDesc, false);

				break;

			case KW_YLOC:

				mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal",
						PLPRuntimeFrame.getYValDesc, false);

				break;

			default:

				throw new Exception("Error occurred in FrameOpChain");
		}

		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {

		/*  Handle the ident appropriately depending on its type and whether it is on the left or right side of binary chain.
			If on the left side, load its value or reference onto the stack.
			If this IdentChain is the right side of a binary expression,
		*/

		Boolean isLeft = (Boolean) arg;
		Dec dec = identChain.getDec();

		// If on the left side, load its value or reference onto the stack.

		if (isLeft) {

			if (dec instanceof ParamDec) {

				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identChain.getFirstToken().getText(),
						Type.getTypeName(dec.getType()).getJVMTypeDesc());


				if (dec.getTypeName().isType(TypeName.URL)) {

					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL",
							PLPRuntimeImageIO.readFromURLSig, false);
				}

				// doubt : load file first and then read ?
				else if (dec.getTypeName().isType(TypeName.FILE)) {

					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile",
							PLPRuntimeImageIO.readFromFileDesc, false);
				}
			} else if (dec.getTypeName().isType(TypeName.BOOLEAN) || dec.getTypeName().isType(TypeName.INTEGER))
				mv.visitVarInsn(ILOAD, dec.getSlot());

				// doubt
			else if (dec.getTypeName().isType(TypeName.IMAGE)) {

				mv.visitVarInsn(ALOAD, dec.getSlot());

			} else if (dec.getTypeName().isType(TypeName.FRAME))

				mv.visitVarInsn(ALOAD, dec.getSlot());
		}


		// If this IdentChain is the right side of a binary expression,
		// store the item on top of the stack into a variable (if INTEGER or IMAGE),
		// or write to file (if FILE), or set as the image in the frame (if FRAME).

		else {

			if (dec instanceof ParamDec) {

				if (dec.getTypeName().isType(TypeName.BOOLEAN) || dec.getTypeName().isType(TypeName.INTEGER)) {

					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getFirstToken().getText(),
							Type.getTypeName(dec.getType()).getJVMTypeDesc());
				} else if (dec.getTypeName().isType(TypeName.FILE)) {

					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getFirstToken().getText(),
							Type.getTypeName(dec.getType()).getJVMTypeDesc());

					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",
							PLPRuntimeImageIO.writeImageDesc, false);
				}
			} else if (dec.getTypeName().isType(TypeName.BOOLEAN) || dec.getTypeName().isType(TypeName.INTEGER)) {

				mv.visitInsn(DUP);
				mv.visitVarInsn(ISTORE, dec.getSlot());
			} else if (dec.getTypeName().isType(TypeName.IMAGE)) {

				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, dec.getSlot());
			}

			// doubt: createOrSetFrame requires 2 arguments, how to load frame ?

			else if (dec.getTypeName().isType(TypeName.FRAME)) {

				mv.visitVarInsn(ALOAD, dec.getSlot());
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",
						PLPRuntimeFrame.createOrSetFrameSig, false);

				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, dec.getSlot());
			}
		}

		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {


		// IdentExpression ∷ = ident
		// load value of variable (this could be a field or a local var)

		Dec dec = identExpression.getDec();

		if (dec instanceof ParamDec) {

			mv.visitVarInsn(ALOAD, 0);

			mv.visitFieldInsn(GETFIELD, className, identExpression.getFirstToken().getText(),
					Type.getTypeName(dec.getType()).getJVMTypeDesc());

		} else if (dec.getTypeName().isType(TypeName.INTEGER) || dec.getTypeName().isType(TypeName.BOOLEAN))
			mv.visitVarInsn(ILOAD, dec.getSlot());

		else if (dec.getTypeName().isType(TypeName.IMAGE) || dec.getTypeName().isType(TypeName.FRAME))
			mv.visitVarInsn(ALOAD, dec.getSlot());

		return identExpression.getType().getJVMTypeDesc();
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {


		//store value on top of stack to this variable (which could be a field or local var)

		Dec dec = identX.getDec();

		if (dec instanceof ParamDec) {

			mv.visitFieldInsn(PUTFIELD, className, identX.getText(),
					Type.getTypeName(dec.getType()).getJVMTypeDesc());

		} else if (dec.getTypeName().isType(TypeName.BOOLEAN) || dec.getTypeName().isType(TypeName.INTEGER))

			mv.visitVarInsn(ISTORE, dec.getSlot());

			// doubt
		else if (dec.getTypeName().isType(TypeName.IMAGE)) {

			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage",
					PLPRuntimeImageOps.copyImageSig, false);

			mv.visitVarInsn(ASTORE, dec.getSlot());
		}

		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {

		Label l1 = new Label();

		Label startIf = new Label();

		ifStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFEQ, l1);
		mv.visitLabel(startIf);
		ifStatement.getB().visit(this, arg);

		mv.visitLabel(l1);

		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {

		/* 	Assume that a reference to a BufferedImage  is on top of the stack.
			Visit the tuple elements to generate code to leave their values on top of the stack.
			Generate code to invoke the appropriate method from PLPRuntimeImageOps or PLPRuntimeImageIO
		*/

		imageOpChain.getArg().visit(this, arg);

		switch (imageOpChain.getFirstToken().kind) {

			case OP_WIDTH:

				mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getWidth",
						PLPRuntimeImageOps.getWidthSig, false);
				break;

			case OP_HEIGHT:
				mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getHeight",
						PLPRuntimeImageOps.getHeightSig, false);
				break;

			case KW_SCALE:

				// doubt: set scale
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale",
						PLPRuntimeImageOps.scaleSig, false);
				break;

			default:

				throw new Exception("Error occurred in ImageOpChain");
		}

		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {

		// load constant

		mv.visitLdcInsn(intLitExpression.value);
		return "I";
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {

		// instance variable in class, initialized with values from command line arguments

		Token ident = paramDec.getIdent();
		String identName = ident.getText();
		String identDesc = paramDec.getTypeName().getJVMTypeDesc();

		paramDec.setSlot(slot++);

		Object val = null;

		if (paramDec.getTypeName().isType(TypeName.INTEGER) || paramDec.getTypeName().isType(TypeName.BOOLEAN)) {

			val = 0;
		}

		FieldVisitor fv = cw.visitField(ACC_PUBLIC, identName, identDesc, null, val);
		fv.visitEnd();

		mv.visitVarInsn(ALOAD, 0);

		if (paramDec.getTypeName().isType(TypeName.INTEGER)) {

			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(globalVarCount++);
			mv.visitInsn(AALOAD);

			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
		} else if (paramDec.getTypeName().isType(TypeName.BOOLEAN)) {

			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(globalVarCount++);
			mv.visitInsn(AALOAD);

			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
		} else if (paramDec.getTypeName().isType(TypeName.URL)) {

			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(globalVarCount++);

			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
		} else if (paramDec.getTypeName().isType(TypeName.FILE)) {

			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);

			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(globalVarCount++);
			mv.visitInsn(AALOAD);

			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
		}

		mv.visitFieldInsn(PUTFIELD, className, identName, identDesc);
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {

		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);


		/*mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		mv.visitLdcInsn("After sleep");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);*/

		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {

		for (Expression e : tuple.getExprList()) {

			e.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {

		Label l1 = new Label();
		Label l2 = new Label();

		mv.visitLabel(l1);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFEQ, l2);
		whileStatement.getB().visit(this, arg);
		mv.visitJumpInsn(GOTO, l1);
		mv.visitLabel(l2);

		return null;
	}

}
