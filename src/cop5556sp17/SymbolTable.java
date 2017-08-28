package cop5556sp17;

import cop5556sp17.AST.Dec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;


public class SymbolTable {


	HashMap<Integer, HashMap<String, Dec>> symTab;
	Stack<Integer> scopeStack;
	int currentScope;


	/** 
	 * to be called when block entered
	 */
	public void enterScope(){

		currentScope ++;
		scopeStack.push(currentScope);
		symTab.put(currentScope, new HashMap<>());
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){

		scopeStack.pop();
		currentScope --;
	}
	
	public boolean insert(String ident, Dec dec){

		if (ident == null)
			return false;

		HashMap<String, Dec> scopeMem = symTab.get(currentScope);


		if (scopeMem == null ) {
			scopeMem = new HashMap<>();
			scopeMem.put(ident, dec);
			symTab.put(currentScope, scopeMem);
			return true;
		}

		if (scopeMem.containsKey(ident))
			return false;

		scopeMem.put(ident, dec);
		return true;
	}
	
	public Dec lookup(String ident){

		if (ident == null || ident.isEmpty())
			return null;

		int scope;
		HashMap<String, Dec> scopeMembers;

		for (int index = scopeStack.size() - 1; index >= 0; index--){

			scope = scopeStack.get(index);
			scopeMembers = symTab.get(scope);

			if (scopeMembers == null)
				continue;

			if(scopeMembers.containsKey(ident))
				return symTab.get(scope).get(ident);
		}

		return null;
	}
		
	public SymbolTable() {

		symTab = new LinkedHashMap<>();
		scopeStack = new Stack<>();

		currentScope = 0;
		scopeStack.push(currentScope);
		symTab.put(currentScope, new HashMap<>());
	}


	@Override
	public String toString() {

		// doubt:

		StringBuilder content = new StringBuilder();
		int scope;
		HashMap<String, Dec> scopeMembers;

		for (int index = scopeStack.size() - 1; index >= 0; index--){

			scope = scopeStack.get(index);
			scopeMembers = symTab.get(scope);

			if (scopeMembers == null)
				continue;

			for (String ident: scopeMembers.keySet()){

				content.append("Ident: " + ident);
				content.append(" Dec: " + scopeMembers.get(ident).getType().getText());
				content.append("\n");
			}
		}

		return content.toString();
	}

}
