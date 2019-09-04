package com.example.language.attrnode;

import java.util.HashMap;
import java.util.List;

import com.example.language.JavaDescriber.CharPosition;

public class MethodNode extends AbstractObjectNode {

	private List<VarNode> arguments;
	private List<VarNode> varList;
	private HashMap<MethodNode, CharPosition> invokeList;
	private List<String> invokeStatement;

	public void addArguments(VarNode arg) {
		arguments.add(arg);
	}
	
	public VarNode getArgument(String argName) {
		for (VarNode node : arguments) {
			if (argName.equals(node.getName())) {
				return node;
			}
		}
		return null;
	}
	
	public List<VarNode> getAllArguments() {
		return arguments;
	}
	
	public void addVariable(VarNode var) {
		varList.add(var);
	}
	
	public VarNode getVariable(String varName) {
		for (VarNode node : varList) {
			if (varName.equals(node.getName())) {
				return node;
			}
		}
		return null;
	}
	
	public List<VarNode> getAllVariables() {
		return varList;
	}
	
	public void addInvokeMethod(MethodNode methodNode, CharPosition invokePos) {
		invokeList.put(methodNode, invokePos);
	}
	
	public HashMap<MethodNode, CharPosition> getInvokeMethod() {
		return invokeList;
	}
	
	public void addInvokeStatement(String statement) {
		invokeStatement.add(statement);
	}
	
}
