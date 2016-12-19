/*
 * @(#)ParentAtom.java
 *
 * $Date: 2014-03-13 04:15:48 -0400 (Thu, 13 Mar 2014) $
 *
 * Copyright (c) 2011 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * https://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */
package com.bric.qt.io;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.TreeNode;

import com.bric.io.GuardedInputStream;
import com.bric.io.GuardedOutputStream;

public class ParentAtom extends Atom {
	Vector<Atom> children = new Vector<Atom>();
	String id;
	
	public ParentAtom(String id) {
		super(null);
		this.id = id;
	}
	
	public void add(Atom a) {
		children.add(a);
		a.parent = this;
	}
	
	public ParentAtom(Atom parent,String id,GuardedInputStream in) throws IOException {
		super(parent);
		this.id = id;
		while(in.isAtLimit()==false) {
			children.add(AtomFactory.read(this,in));
		}
	}

	public Enumeration<Atom> children() {
		return children.elements();
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public TreeNode getChildAt(int childIndex) {
		return children.get(childIndex);
	}

	public int getChildCount() {
		return children.size();
	}

	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	public boolean isLeaf() {
		return children.size()==0;
	}

	@Override
	protected long getSize() {
		long sum = 8;
		for(int a = 0; a<children.size(); a++) {
			Atom atom = children.get(a);
			sum += atom.getSize();
		}
		return sum;
	}

	@Override
	protected String getIdentifier() {
		return id;
	}

	@Override
	protected void writeContents(GuardedOutputStream out) throws IOException {
		for(int a = 0; a<children.size(); a++) {
			Atom atom = children.get(a);
			atom.write(out);
		}
	}
}
