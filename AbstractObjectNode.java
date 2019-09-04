package com.example.language.attrnode;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractObjectNode extends JavaNode {
	
	private List<VarNode> varList;
	private List<MethodNode> methodList;
	private List<String> importList = new ArrayList<>();
	
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
	
	public void addMethod(MethodNode mNode) {
		methodList.add(mNode);
	}
	
	public MethodNode getMethod(String methodName) {
		String id;
		for (MethodNode node : methodList) {
			id = node.getId();
			if (methodName.equals(id.substring(id.lastIndexOf(".")
					, id.length()))) {
				return node;
			}
		}
		return null;
	}
	
	public List<MethodNode> getAllMethod() {
		return methodList;
	}
	
	public void addImport(String im) {
		importList.add(im);
	}
	
	public void addImport(String[] ims) {
		for (String s : ims) {
			importList.add(s);
		}
	}
	
	public String[] getImport() {
		return (String[]) importList.toArray();
	}
}
