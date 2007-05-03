/**
 * Bibliothek - DockingFrames
 * Library built on Java/Swing, allows the user to "drag and drop"
 * panels containing any Swing-Component the developer likes to add.
 * 
 * Copyright (C) 2007 Benjamin Sigg
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Benjamin Sigg
 * benjamin_sigg@gmx.ch
 * CH - Switzerland
 */

package bibliothek.gui.dock.station.split;

import java.util.ArrayList;
import java.util.List;

import bibliothek.gui.dock.Dockable;
import bibliothek.gui.dock.station.SplitDockStation;

/**
 * A class that provides a grid for {@link Dockable Dockables}. The grid can
 * be transformed into a {@link SplitDockTree} which has values that would
 * layout the components as they are in the grid. The algorithms used in this
 * class can handle overlapping elements and holes, however results are much better
 * if there are no disturbances in the grid.<br>
 * There is also a possibility to tell the tree, where dividers should be made. 
 * @author Benjamin Sigg
 * @see #toTree()
 * @see SplitDockStation#dropTree(SplitDockTree)
 */
public class SplitDockGrid {
	/** The Dockables known to this grid */
	private List<Node> nodes = new ArrayList<Node>();
	/** The dividing lines which should appear */
	private List<Line> lines = new ArrayList<Line>();
	
	/**
	 * Adds <code>dockable</code> to the grid. The coordinates are not absolute,
	 * only the relative location and size matters.
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 * @param width the width, more than 0
	 * @param height the height, more than 0
     * @param dockables the Dockables to add
	 */
	public void addDockable( double x, double y, double width, double height, Dockable... dockables ){
		if( dockables == null )
			throw new IllegalArgumentException( "Dockable must not be null" );
		
        if( dockables.length == 0 )
            throw new IllegalArgumentException( "Dockables must at least have one element" );
        
        for( Dockable dockable : dockables )
            if( dockable == null )
                throw new IllegalArgumentException( "Entry of dockables-array is null" );
        
		Node node = new Node();
		node.dockables = new Dockable[ dockables.length ];
        System.arraycopy( dockables, 0, node.dockables, 0, dockables.length );
		node.x = x;
		node.y = y;
		node.width = width;
		node.height = height;
		nodes.add( node );
	}
	
	/**
	 * Adds a vertical dividing line.
	 * @param x the x-coordinate of the line
	 * @param y1 the y-coordinate of the first endpoint
	 * @param y2 the y-coordinate of the second endpoint
	 */
	public void addVerticalDivider( double x, double y1, double y2 ){
		Line line = new Line();
		line.horizontal = false;
		line.alpha = x;
		line.betaMin = Math.min( y1, y2 );
		line.betaMax = Math.max( y1, y2 );
		lines.add( line );
	}
	
	/**
	 * Adds a horizonal dividing line.
	 * @param x1 the x-coordinate of the first endpoint
	 * @param x2 the x-coordinate of the second endpoint
	 * @param y the y-coordinate of the line
	 */
	public void addHorizontalDivider( double x1, double x2, double y ){
		Line line = new Line();
		line.horizontal = true;
		line.alpha = y;
		line.betaMin = Math.min( x1, x2 );
		line.betaMax = Math.max( x1, x2 );
		lines.add( line );
	}
	
	/**
	 * Converts the current grid into a tree.
	 * @return the tree which represents this grid
	 * @see SplitDockStation#dropTree(SplitDockTree)
	 */
	public SplitDockTree toTree(){
		Node root = tree();
		SplitDockTree tree = new SplitDockTree();
		if( root != null ){
			SplitDockTree.Key key = root.put( tree );
			tree.root( key );
		}
		return tree;
	}
	
	/**
	 * Gets a list containing all lines of this grid.
	 * @return the list
	 */
	protected List<Line> getLines(){
		return lines;
	}
	
	/**
	 * Gets a list containing all nodes of this grid.
	 * @return the nodes
	 */
	protected List<Node> getNodes(){
		return nodes;
	}
		
	/**
	 * Transforms the grid into a tree and returns the root.
	 * @return the root, can be <code>null</code>
	 */
	protected Node tree(){
		 List<Node> nodes = new ArrayList<Node>( this.nodes );
		
		if( nodes.isEmpty() )
			return null;
		
		while( nodes.size() > 1 ){
			int size = nodes.size();
			
			int bestA = 0, bestB = 0;
			double bestDiff = Double.MAX_VALUE;
			
			for( int i = 0; i < size; i++ ){
				for( int j = i+1; j < size; j++ ){
					double diff = diff( nodes.get( i ), nodes.get( j ) );
					if( diff < bestDiff ){
						bestDiff = diff;
						bestA = i;
						bestB = j;
					}
				}
			}
			
			Node node = combine( nodes.remove( bestB ), nodes.remove( bestA ));
			nodes.add( node );
		}
		
		return nodes.get( 0 );
	}
	
	/**
	 * Creates a combination of <code>a</code> and <code>b</code>.
	 * @param a the first node
	 * @param b the second node
	 * @return a node which has <code>a</code> and <code>b</code> as children
	 */
	protected Node combine( Node a, Node b ){
		double x = Math.min( a.x, b.x );
		double y = Math.min( a.y, b.y );
		double w = Math.max( a.x + a.width, b.x + b.width ) - x;
		double h = Math.max( a.y + a.height, b.y + b.height ) - y;
		
		double max = a.x + a.width/2;
		double may = a.y + a.height/2;
		double mbx = b.x + b.width/2;
		double mby = b.y + b.height/2;
		
		double dmx = (max - mbx) * h;
		double dmy = (may - mby) * w;
		
		Node node = new Node();
		
	
		if( Math.abs( dmx ) > Math.abs( dmy )){
			node.horizontal = true;
			if( dmx > 0 ){
				node.childA = b;
				node.childB = a;
			}
			else{
				node.childA = a;
				node.childB = b;
			}
			
			double split = ((node.childA.x + node.childA.width + node.childB.x) / 2.0 - x ) / w;
			Line line = bestFittingLine( x, y, w, h, false, split );
			if( line == null )
				node.divider = split;
			else
				node.divider = (line.alpha - x) / w;
		}
		else{
			node.horizontal = false;
			if( dmy > 0 ){
				node.childA = b;
				node.childB = a;
			}
			else{
				node.childA = a;
				node.childB = b;
			}
			
			double split = ((node.childA.y + node.childA.height + node.childB.y ) / 2.0 - y ) / h;
			Line line = bestFittingLine( x, y, w, h, true, split);
			if( line == null )
				node.divider = split;
			else
				node.divider = (line.alpha - y) / h;
		}
		
		node.x = x;
		node.y = y;
		node.width = w;
		node.height = h;
		return node;
	}
	
	/**
	 * Tells whether the two nodes could be merged or not.
	 * @param a the first node
	 * @param b the second node
	 * @return how likely the two nodes can be merged, a small result indicates
	 * that merging would be a good idea.
	 */
	protected double diff( Node a, Node b ){
		double x = Math.min( a.x, b.x );
		double y = Math.min( a.y, b.y );
		double w = Math.max( a.x + a.width, b.x + b.width ) - x;
		double h = Math.max( a.y + a.height, b.y + b.height ) - y;
		
		double sizeA = a.width * a.height;
		double sizeB = b.width * b.height;
		double size = w * h;
		
		double diff = (size - sizeA - sizeB) / size;
		
		for( Line line : lines ){
			diff += penalty( x, y, w, h, line );
		}
		
		return diff;
	}
	
	/**
	 * Searches the line that divides the rectangle <code>x, y, width, height</code>
	 * best.
	 * @param x the x-coordinate of the rectangle
	 * @param y the y-coordinate of the rectangle
	 * @param w the width of the rectangle
	 * @param h the height of the rectangle
	 * @param horizontal whether the line should be horizontal or not
	 * @param split the preferred value of {@link Line#alpha}.
	 * @return a line or <code>null</code>
	 */
	protected Line bestFittingLine( double x, double y, double w, double h, boolean horizontal, double split ){
		Line bestLine = null;
		double best = Double.MAX_VALUE;
		
		for( Line line : lines ){
			if( line.horizontal != horizontal )
				continue;
			
			double max, min, diff, penalty;
			
			if( line.horizontal ){
				if( y > line.alpha || y + h < line.alpha )
					continue;
				
				if( x + w < line.betaMin || x > line.betaMax )
					continue;
				
				min = Math.min( x, line.betaMin );
				max = Math.max( x+w, line.betaMax );
				diff = max - min - Math.min( line.betaMax - line.betaMin, w );
				penalty = diff / (max - min);
				penalty *= (1+Math.abs( split - line.alpha )/h);
			}
			else{
				if( x > line.alpha || x + w < line.alpha )
					continue;
				
				if( y + h < line.betaMin || y > line.betaMax )
					continue;
				
				min = Math.min( y, line.betaMin );
				max = Math.max( y+h, line.betaMax );
				diff = max - min - Math.min( line.betaMax - line.betaMin, h );
				penalty = diff / (max - min);
				penalty *= (1+Math.abs( split - line.alpha )/w);
			}
			
			if( penalty < 0.25 && penalty < best ){
				best = penalty;
				bestLine = line;
			}
		}
		
		return bestLine;
	}
	
	/**
	 * Used by {@link #diff(SplitDockGrid.Node, SplitDockGrid.Node) diff}
	 * to calculate add a penalty if a line hits a rectangle. 
	 * @param x the x-coordinate of the rectangle
	 * @param y the y-coordinate of the rectangle
	 * @param w the width of the rectangle
	 * @param h the height of the rectangle
	 * @param line the line which may hit the rectangle
	 * @return the penalty, a value that will be added to the result of <code>diff</code>.
	 */
	protected double penalty( double x, double y, double w, double h, Line line ){
		double max, min, diff;
		
		if( line.horizontal ){
			if( y > line.alpha || y + h < line.alpha )
				return 0;
			
			if( x + w < line.betaMin || x > line.betaMax )
				return 0;
			
			min = Math.min( x, line.betaMin );
			max = Math.max( x+w, line.betaMax );
			diff = max - min - Math.min( line.betaMax - line.betaMin, w );
		}
		else{
			if( x > line.alpha || x + w < line.alpha )
				return 0;
			
			if( y + h < line.betaMin || y > line.betaMax )
				return 0;
			
			min = Math.min( y, line.betaMin );
			max = Math.max( y+h, line.betaMax );
			diff = max - min - Math.min( line.betaMax - line.betaMin, h );
		}
		
		return diff / (max - min);
	}
	
	/**
	 * Represents a dividing line in the grid.
	 * @author Benjamin Sigg
	 */
	protected class Line{
		/** whether this line is horizontal or not */
		public boolean horizontal;
		/** the coordinate which is always the same on the line */
		public double alpha;
		/** the end with the smaller coordinate */
		public double betaMin;
		/** the end with the higher coordinate */
		public double betaMax;
	}
	
	/**
	 * Represents a node in the tree which will be built.
	 * @author Benjamin Sigg
	 */
	protected class Node{
		/** the x-coordinate */
		public double x;
		/** the y-coordinate */
		public double y;
		/** the width of this rectangle */
		public double width;
		/** the height of this rectangle */
		public double height;
		/** the first child of this node */
		public Node childA;
		/** the second child of this node */
		public Node childB;
		/** the location of the divider */
		public double divider;
		/** whether the children of this node are laied out horizontally or not */
		public boolean horizontal;
		/** the elements represtented by this leaf */
		public Dockable[] dockables;
	
		/**
		 * Writes the contents of this node into <code>tree</code>.
		 * @param tree the tree to write into
		 * @return the key of the node
		 */
		public SplitDockTree.Key put( SplitDockTree tree ){
			if( dockables != null )
				return tree.put( dockables );
			else if( horizontal ){
				return tree.horizontal( childA.put( tree ), childB.put( tree ), divider );
			}
			else{
				return tree.vertical( childA.put( tree ), childB.put( tree ), divider );
			}
		}
	}
}
