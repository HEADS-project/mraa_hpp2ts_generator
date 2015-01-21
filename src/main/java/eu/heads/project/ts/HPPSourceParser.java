/*******************************************************************************
 * Copyright (c) 2014, CriativaSoft, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 *******************************************************************************/
package eu.heads.project.ts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.index.EmptyCIndex;
import org.eclipse.cdt.internal.core.parser.scanner.CharArray;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.core.runtime.CoreException;

public class HPPSourceParser {
	static boolean firstClass = true;

	
	Map<String,String> types = new HashMap<String, String>();
	Map<String, String> definedMacros = new HashMap<String, String>();
	boolean hasFunctionsOverload = false; // 2 functions with same name.
	private String defaultFileName = "SourceCode.c";
	private int options = 0;
	
	public HPPSourceParser(StringBuilder res) {
		this.res = res;
		types.put("std::string", "string");
		types.put("int", "number");
		types.put("const unsigned int", "number");
		types.put("unsigned int", "number");
		types.put("int", "number");
		types.put("uint8_t", "number");
		types.put("const unsigned uint8_t", "number");
		types.put("const uint8_t", "number");
		types.put("uint16_t", "number");
		types.put("float", "number");
		types.put("bool", "boolean");
	}
	public void parse(String sourceCode) {
		FileContent fileContent = new InternalFileContent(defaultFileName,
				new CharArray(sourceCode));
		parse(fileContent);
	}

	public void parse(File file) {
		FileContent fileContent = FileContent
				.createForExternalFileLocation(file.getPath());
		parse(fileContent);
	}

	public void parse(FileContent fileContent) {
		String[] includePaths = new String[0];
		IScannerInfo info = new ScannerInfo(definedMacros, includePaths);
		IParserLogService log = new DefaultLogService();
		IIndex index = EmptyCIndex.INSTANCE; // or can be null
		IncludeFileContentProvider emptyIncludes = IncludeFileContentProvider
				.getEmptyFilesProvider();

		try {
			IASTTranslationUnit translationUnit = GPPLanguage.getDefault()
					.getASTTranslationUnit(fileContent, info, emptyIncludes,
							index, options, log);
			navigateTree(translationUnit, res);

		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	
	
	final StringBuilder res ;

	static List<IASTNode> nodes = new ArrayList<IASTNode>();
	
	protected void navigateTree(IASTNode node, final StringBuilder res) {
		ASTGenericVisitor ast = new ASTGenericVisitor(true) {

			@Override
			public int visit(IASTDeclaration declaration) {
				if (nodes.contains(declaration))
					return 0;
				else
					nodes.add(declaration);
				if (declaration instanceof IASTSimpleDeclaration) {
					((IASTSimpleDeclaration) declaration).getDeclSpecifier()
							.accept(this);
				} else if (declaration instanceof IASTFunctionDefinition) {
					((IASTFunctionDefinition) declaration).getDeclSpecifier()
							.accept(this);
				}

				return super.visit(declaration);
			}
			boolean enterClass =false;
			
			List<CPPASTCompositeTypeSpecifier> classes = new ArrayList<CPPASTCompositeTypeSpecifier>();

			@Override
			public int visit(IASTDeclSpecifier declSpec) {
				if (nodes.contains(declSpec))
					return 0;
				else
					nodes.add(declSpec);
				if (declSpec instanceof CPPASTCompositeTypeSpecifier) {
					CPPASTCompositeTypeSpecifier res4 = (CPPASTCompositeTypeSpecifier) declSpec;
					if (!classes.contains(declSpec)) {
						classes.add(res4);
						if (firstClass)
							firstClass=false;
						else
							res.append("\n}\n");
						
						res.append("export class " + res4.getName() + " \n");
						enterClass = true;
					}
				}
				return super.visit(declSpec);

			}

			StringBuilder tmp = new StringBuilder();
			boolean first = true;
			
			public int visit(IASTDeclarator declarator) {
				if (nodes.contains(declarator))
					return 0;
				else
					nodes.add(declarator);
				
				if (declarator instanceof CPPASTFunctionDeclarator) {
					if (enterClass){
						res.append("{\n");
						enterClass = false;
					}
					CPPASTFunctionDeclarator dec = (CPPASTFunctionDeclarator) declarator;
					if (dec.getParent() instanceof CPPASTFunctionDefinition) {
						CPPASTFunctionDefinition par = (CPPASTFunctionDefinition) dec
								.getParent();
						if (!Character.isUpperCase(par.getDeclarator()
								.getName().toString().codePointAt(0))
								&& !"~".equals(""
										+ par.getDeclarator().getName()
												.toString().charAt(0))) {
							StringBuilder res2 = new StringBuilder();

							res2.append(par.getDeclarator().getName() + "(");
							for (IASTNode n : par.getChildren()) {
								if (n instanceof CPPASTFunctionDeclarator) {
									ICPPASTParameterDeclaration[] params = ((CPPASTFunctionDeclarator) n)
											.getParameters();
									boolean firstparam = true;
									StringBuilder res1 = new StringBuilder();
									boolean error = false;
									for (ICPPASTParameterDeclaration param : params) {
										if (firstparam)
											firstparam = false;
										else
											res1.append(" , ");
										res1.append(param.getDeclarator()
												.getName() + " : ");
										if (types.containsKey(param.getDeclSpecifier().toString())){
											res1.append(types.get(param.getDeclSpecifier().toString()));											
										}else
											res1.append(param.getDeclSpecifier());
										if (param.getDeclarator().getName()
												.toString().equals("")) {
											error = true;
										}
									}

									if (error)
										res.append("//");
									res2.append(res1.toString());
								}
							}
							res2.append(")");
							if (!par.getDeclSpecifier().equals(""))
							if (types.containsKey(par.getDeclSpecifier().toString())){
								res2.append(": " + types.get(par.getDeclSpecifier().toString()));
							}else
								res2.append(": " + par.getDeclSpecifier());

							res2.append(";\n");
							if (firstClass)
								res.append("function ");
							res.append(res2.toString());
						} else if (Character.isUpperCase(par.getDeclarator()
								.getName().toString().codePointAt(0))) {
							StringBuilder res2 = new StringBuilder();
							StringBuilder res5 = new StringBuilder();

							res2.append("constructor(");
							res5.append("constructor(");
							boolean hasinit = false;
							for (IASTNode n : par.getChildren()) {
								if (n instanceof CPPASTFunctionDeclarator) {
									ICPPASTParameterDeclaration[] params = ((CPPASTFunctionDeclarator) n)
											.getParameters();
									boolean firstparam = true;
									StringBuilder res1 = new StringBuilder();
									StringBuilder res3 = new StringBuilder();

									boolean error = false;
									for (ICPPASTParameterDeclaration param : params) {
										if (param.getDeclarator().getInitializer()!=null){
											hasinit = true;
										}

										if (firstparam)
											firstparam = false;
										else{
											
											res1.append(" , ");
											if (param.getDeclarator().getInitializer()==null)
												res3.append(" , ");
										}
										
										res1.append(param.getDeclarator()
												.getName() + " : ");
										if (types.containsKey(param.getDeclSpecifier().toString())){
											res1.append(types.get(param.getDeclSpecifier().toString()));											
										}else
											res1.append(param.getDeclSpecifier());
										if (param.getDeclarator().getName()
												.toString().equals("")) {
											error = true;
										}
										if (param.getDeclarator().getInitializer()==null){
											res3.append(param.getDeclarator()
													.getName() + " : ");
											if (types.containsKey(param.getDeclSpecifier().toString())){
												res3.append(types.get(param.getDeclSpecifier().toString()));											
											}else
												res3.append(param.getDeclSpecifier());
											}
											
										}

									

									if (error)
										res.append("//");
									res2.append(res1.toString());
									if (hasinit){
										res5.append(res3.toString());
									}
								}
							}
							res2.append(")");
							if (!par.getDeclSpecifier().equals(""))
								res2.append(";\n");
							res5.append(")");
							if (!par.getDeclSpecifier().equals(""))
								res5.append(";\n");

							res.append(res2.toString());
							res.append(res5.toString());

							
							
						}

					}
					return 0;
				} else {
					{

						if (tmp.length() > 0) {
							if (firstClass){
								firstClass=false;
							}
							else
								res.append("\n}\n");
							
							res.append("export enum " + declarator.getName()
									+ "{\n");
							res.append(tmp.toString());
//							res.append("\n}\n");
							first = true;
							tmp.setLength(0);
						}
					}
					return super.visit(declarator);
				}
			}

			@Override
			public int visit(IASTEnumerator enumerator) {
				if (nodes.contains(enumerator))
					return 0;
				else
					nodes.add(enumerator);

				if (first) {
					first = false;
				} else {
					tmp.append(",\n");
				}
				tmp.append("\t" + enumerator.getName() + " = "
						+ enumerator.getValue());
				return super.visit(enumerator);
			}


		};
		node.accept(ast);

	}

}
