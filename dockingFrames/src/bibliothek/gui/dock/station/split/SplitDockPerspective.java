/*
 * Bibliothek - DockingFrames
 * Library built on Java/Swing, allows the user to "drag and drop"
 * panels containing any Swing-Component the developer likes to add.
 * 
 * Copyright (C) 2010 Benjamin Sigg
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

import bibliothek.gui.DockStation;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.SplitDockStation;
import bibliothek.gui.dock.SplitDockStation.Orientation;
import bibliothek.gui.dock.perspective.Perspective;
import bibliothek.gui.dock.perspective.PerspectiveDockable;
import bibliothek.gui.dock.perspective.PerspectiveStation;
import bibliothek.gui.dock.station.stack.StackDockPerspective;
import bibliothek.gui.dock.station.support.PlaceholderMap;
import bibliothek.util.Path;
import bibliothek.util.Todo;
import bibliothek.util.Todo.Compatibility;
import bibliothek.util.Todo.Priority;
import bibliothek.util.Todo.Version;

/**
 * Represents a {@link SplitDockStation} in a {@link Perspective}.
 * @author Benjamin Sigg
 */
public class SplitDockPerspective implements PerspectiveDockable, PerspectiveStation{
	private PerspectiveStation parent;

	/** the child that is currently in fullscreen mode */
	private PerspectiveDockable fullscreen;
	
	/** the root of the tree that represents the layout */
	private final Root root = new Root();
	
	/** all the children of this station */
	private List<PerspectiveDockable> children = new ArrayList<PerspectiveDockable>();
	
	/** all the listeners of this perspective */
	private List<EntryListener> listeners = new ArrayList<EntryListener>();
	
	/**
	 * Creates a new perspective
	 */
	public SplitDockPerspective(){
		addListener( new EntryListener(){
			public void added( Entry parent, Entry child ){
				add( child );
			}
			
			public void removed( Entry parent, Entry child ){
				remove( child );
			}
			
			private void add( Entry child ){
				if( child != null ){
					if( child.asLeaf() != null ){
						children.add( child.asLeaf().getDockable() );
					}
					else{
						add( child.asNode().getChildA() );
						add( child.asNode().getChildB() );
					}
				}
			}
			
			private void remove( Entry child ){
				if( child != null ){
					if( child.asLeaf() != null ){
						children.remove( child.asLeaf().getDockable() );
					}
					else{
						remove( child.asNode().getChildA() );
						remove( child.asNode().getChildB() );
					}
				}
			}
		});
	}
	
	/**
	 * Adds a listener to this perspective.
	 * @param listener the new listener, not <code>null</code>
	 */
	public void addListener( EntryListener listener ){
		if( listener == null ){
			throw new IllegalArgumentException( "listener must not be null" );
		}
		listeners.add( listener );
	}
	
	/**
	 * Removes <code>listener</code> from this perspective
	 * @param listener the listener to remove
	 */
	public void removeListener( EntryListener listener ){
		listeners.remove( listener );
	}
	
	/**
	 * Gets an array containing all the listeners of this perspective
	 * @return all the listeners
	 */
	protected EntryListener[] listeners(){
		return listeners.toArray( new EntryListener[ listeners.size() ] );
	}
	
	/**
	 * Calls {@link EntryListener#removed(Entry, Entry)} on all listeners that are currently
	 * known to this perspective
	 * @param parent the parent from which <code>child</code> was removed
	 * @param child the child which was removed
	 */
	protected void fireRemoved( Entry parent, Entry child ){
		for( EntryListener listener : listeners() ){
			listener.removed( parent, child );
		}
	}
	
	/**
	 * Calls {@link EntryListener#added(Entry, Entry)} on all listeners that are currently 
	 * known to this perspective
	 * @param parent the parent of the new element
	 * @param child the child that was added
	 */
	protected void fireAdded( Entry parent, Entry child ){
		for( EntryListener listener : listeners() ){
			listener.added( parent, child );
		}
	}
	
	/**
	 * Reads the contents of <code>tree</code> and replaces any content of this perspective
	 * @param tree the tree that represents this perspective
	 * @param fullscreen the one child that is currently in fullscreen-mode, can be <code>null</code>
	 */
	public void read( PerspectiveSplitDockTree tree, PerspectiveDockable fullscreen ){
		root.setChild( convert( tree.getRoot() ) );
		if( fullscreen != null && !children.contains( fullscreen )){
			throw new IllegalArgumentException( "fullscreen is not a child of this station" );
		}
		this.fullscreen = fullscreen;
	}
	
	private Entry convert( PerspectiveSplitDockTree.Key key ){
		if( key == null ){
			return null;
		}
		
		SplitDockTree<PerspectiveDockable> tree = key.getTree();
		if( tree.isDockable( key )){
			PerspectiveDockable[] dockables = tree.getDockables( key );
			PerspectiveDockable dockable = null;
			
			if( dockables.length == 1 ){
				dockable = dockables[0];
			}
			else if( dockables.length > 1 ){
				dockable = new StackDockPerspective( dockables, tree.getSelected( key ) );
			}
			
			if( dockable.getParent() != null ){
				throw new IllegalArgumentException( "dockable already has a parent" );
			}
			dockable.setParent( this );
			
			return new Leaf( dockable, tree.getPlaceholders( key ), tree.getPlaceholderMap( key ), key.getNodeId() );
		}
		if( tree.isPlaceholder( key )){
			return new Leaf( null, tree.getPlaceholders( key ), tree.getPlaceholderMap( key ), key.getNodeId() );
		}
		if( tree.isNode( key )){
			Entry childA = convert( tree.getLeft( key ));
			Entry childB = convert( tree.getRight( key ));
			Orientation orientation;
			if( tree.isHorizontal( key )){
				orientation = Orientation.HORIZONTAL;
			}
			else{
				orientation = Orientation.VERTICAL;
			}
			
			return new Node( orientation, tree.getDivider( key ), childA, childB, tree.getPlaceholders( key ), tree.getPlaceholderMap( key ), key.getNodeId() );
		}
		throw new IllegalStateException( "key does not represent any known kind of element" );
	}
	
	/**
	 * Gets the element which is in fullscreen-mode
	 * @return the maximized element, can be <code>null</code>
	 */
	public PerspectiveDockable getFullScreen(){
		return fullscreen;
	}
	
	/**
	 * Gets the root of the tree that is the layout of this station.
	 * @return the root of the tree, not <code>null</code>
	 */
	public Root getRoot(){
		return root;
	}
	
	public PerspectiveStation getParent(){
		return parent;
	}

	public Path getPlaceholder(){
		return null;
	}

	public void setParent( PerspectiveStation parent ){
		this.parent = parent;	
	}

	public PerspectiveDockable asDockable(){
		return this;
	}

	public PerspectiveStation asStation(){
		return this;
	}

	public String getFactoryID(){
		return SplitDockStationFactory.ID;
	}

	public PerspectiveDockable getDockable( int index ){
		return children.get( index );
	}

	public int getDockableCount(){
		return children.size();
	}
	
	@Todo( compatibility=Compatibility.COMPATIBLE, priority=Priority.ENHANCEMENT, target=Version.VERSION_1_1_0,
			description="implementation pending")
	public void setPlaceholders( PlaceholderMap placeholders ){
		// ignore, SplitDockStation does not support placeholder maps
	}
	
	@Todo( compatibility=Compatibility.COMPATIBLE, priority=Priority.ENHANCEMENT, target=Version.VERSION_1_1_0,
			description="implementation pending")
	public PlaceholderMap getPlaceholders(){
		return null;
	}

	/**
	 * A listener that can be added to a {@link SplitDockPerspective} and that will receive events
	 * whenever the tree of the perspective changes.
	 * @author Benjamin Sigg
	 */
	public static interface EntryListener{
		/**
		 * Called after <code>child</code> was added to <code>parent</code>. This implies that
		 * the entire subtree below <code>child</code> was added to this perspective.
		 * @param parent the parent, either a {@link Root} or a {@link Node}
		 * @param child the child, either a {@link Node} or a {@link Leaf}
		 */
		public void added( Entry parent, Entry child );
		
		/**
		 * Called after <code>child</code> was removed from <code>parent</code>. This implies that
		 * the entire subtree below <code>child</code> was removed from this perspective
		 * @param parent the parent, either a {@link Root} or a {@link Node}
		 * @param child the child, either a {@link Node} or a {@link Leaf}
		 */
		public void removed( Entry parent, Entry child );
	}
	
    /**
     * An entry in a tree, either a node or a leaf.
     * @author Benjamin Sigg
     */
    public static abstract class Entry{
    	/** the parent element of this entry */
    	private Entry parent;
    	/** the unique id of this node */
    	private long id;
    	/** placeholders that are associated with this entry */
    	private Path[] placeholders;
    	/** placeholder information of a child {@link DockStation} */
    	private PlaceholderMap placeholderMap;
    	
    	/**
    	 * Create a new entry
    	 * @param placeholders the placeholders associated with this node or leaf
    	 * @param placeholderMap placeholder information of a child {@link DockStation}
    	 * @param id the unique id of this node or -1
    	 */
    	public Entry( Path[] placeholders, PlaceholderMap placeholderMap, long id ){
    		this.placeholders = placeholders;
    		this.placeholderMap = placeholderMap;
    		this.id = id;
    	}
    	
    	/**
    	 * Tells whether <code>anchestor</code> is an anchestor of this entry.
    	 * @param anchestor the item to search
    	 * @return <code>true</code> if <code>anchestor</code> is <code>this</code> or a 
    	 * parent of <code>this</code>
    	 */
    	public boolean isAnchestor( Entry anchestor ){
    		Entry current = this;
    		while( current != null ){
    			if( current == anchestor ){
    				return true;
    			}
    			current = current.getParent();
    		}
    		return false;
    	}
    	
    	/**
    	 * Gets the owner of this node or leaf.
    	 * @return the owner, can be <code>null</code>
    	 */
    	public SplitDockPerspective getPerspective(){
    		if( parent == null ){
    			return null;
    		}
    		return parent.getPerspective();
    	}
    	
    	/**
    	 * Sets the parent of this entry.
    	 * @param parent the parent
    	 */
    	protected void setParent( Entry parent ){
			this.parent = parent;
		}
    	
    	/**
    	 * Gets the parent of this entry, is <code>null</code> for the
    	 * root entry.
    	 * @return the parent.
    	 */
    	public Entry getParent() {
			return parent;
		}
    	
    	/**
    	 * Gets the unique id of this node.
    	 * @return the unique id or -1
    	 */
    	public long getNodeId(){
			return id;
		}
    	
        /**
         * Returns <code>this</code> as leaf or <code>null</code>.
         * @return <code>this</code> or <code>null</code>
         */
        public Leaf asLeaf(){
            return null;
        }
        
        /**
         * Returns <code>this</code> as node or <code>null</code>.
         * @return <code>this</code> or <code>null</code>
         */
        public Node asNode(){
            return null;
        }
        
        /**
         * Gets all the placeholders that are associated with this entry.
         * @return the placeholders
         */
        public Path[] getPlaceholders(){
			return placeholders;
		}
        
        /**
         * Gets the placeholder information of a potential child {@link DockStation}.
         * @return the placeholder map, can be <code>null</code>
         */
        public PlaceholderMap getPlaceholderMap(){
			return placeholderMap;
		}
    }
    
    /**
     * A root in a tree.
     * @author Benjamin Sigg
     */
    public class Root extends Entry{
    	/** the one and only child of this root */
		private Entry child;
    	
    	/**
    	 * Creates the new root.
    	 */
    	public Root(){
			super( new Path[]{}, null, -1 );
		}

    	@Override
    	public SplitDockPerspective getPerspective(){
    		return SplitDockPerspective.this;
    	}
    	
    	@Override
    	protected void setParent( Entry parent ){
	    	throw new IllegalStateException( "cannot set the parent of a root" );
    	}
    	
    	/**
    	 * Gets the child of this root. 
    	 * @return the child, can be <code>null</code>
    	 */
    	public Entry getChild(){
			return child;
		}
    	
    	/**
    	 * Sets the child of this root.
    	 * @param child the child, can be <code>null</code>
    	 */
    	public void setChild( Entry child ){
    		if( child != null ){
	    		if( child.asLeaf() == null && child.asNode() == null ){
	    			throw new IllegalArgumentException( "child must either be a leaf or a node" );
	    		}
    		}
    		
    		if( this.child != null ){
    			this.child.setParent( null );
    			fireRemoved( this, this.child );
    		}
    		
			this.child = child;
			
			if( this.child != null ){
				this.child.setParent( this );
				fireAdded( this, this.child );
			}
		}
    }

    /**
     * A node in a tree.
     * @author Benjamin Sigg
     */
    public static class Node extends Entry{
        /** whether the node is horizontal or vertical */
        private Orientation orientation;
        /** the location of the divider */
        private double divider;
        /** the top or left child */
        private Entry childA;
        /** the bottom or right child */
        private Entry childB;
        
        /**
         * Creates a new node.
         * @param orientation whether this node is horizontal or vertical
         * @param divider the location of the divider
         * @param childA the left or top child
         * @param childB the right or bottom child
         * @param placeholders placeholders associated with this node
         * @param placeholderMap placeholder information of a child {@link DockStation}
         * @param id the unique identifier of this node or -1
         */
        public Node( Orientation orientation, double divider, Entry childA, Entry childB, Path[] placeholders, PlaceholderMap placeholderMap, long id ){
        	super( placeholders, placeholderMap, id );
            this.orientation = orientation;
            this.divider = divider;
            
            setChildA( childA );
            setChildB( childB );
        }
        
        @Override
        public Node asNode() {
            return this;
        }
        
        /**
         * Tells whether this node is horizontal or vertical.
         * @return the orientation
         */
        public Orientation getOrientation() {
            return orientation;
        }
        
        /**
         * The location of the divider.
         * @return a value between 0 and 1
         */
        public double getDivider() {
            return divider;
        }
        
        /**
         * Gets the left or top child.
         * @return the left or top child
         */
        public Entry getChildA() {
            return childA;
        }
        
        /**
         * Sets the left or top child of this node.
         * @param childA the new child, can be <code>null</code>
         */
        public void setChildA( Entry childA ){
        	if( childA != null ){
	    		if( childA.asLeaf() == null && childA.asNode() == null ){
	    			throw new IllegalArgumentException( "child must either be a leaf or a node" );
	    		}
    		}
        	
        	if( childA != null && isAnchestor( childA )){
        		throw new IllegalArgumentException( "cannot build a cycle" );
        	}
        	
        	SplitDockPerspective perspective = getPerspective();
        	
        	if( this.childA != null ){
        		this.childA.setParent( null );
        		if( perspective != null ){
        			perspective.fireRemoved( this, this.childA );
        		}
        		
        	}
			this.childA = childA;
			if( this.childA != null ){
				this.childA.setParent( this );
				if( perspective != null ){
					perspective.fireAdded( this, this.childA );
				}
			}
		}
        
        /**
         * Gets the right or bottom child.
         * @return the right or bottom child
         */
        public Entry getChildB() {
            return childB;
        }
        

        /**
         * Sets the right or bottom child of this node.
         * @param childB the new child, can be <code>null</code>
         */
        public void setChildB( Entry childB ){
        	if( childB != null ){
	    		if( childB.asLeaf() == null && childB.asNode() == null ){
	    			throw new IllegalArgumentException( "child must either be a leaf or a node" );
	    		}
    		}
        	
        	if( childB != null && isAnchestor( childB )){
        		throw new IllegalArgumentException( "cannot build a cycle" );
        	}
        	
        	SplitDockPerspective perspective = getPerspective();
        	
        	if( this.childB != null ){
        		this.childB.setParent( null );
        		if( perspective != null ){
        			perspective.fireRemoved( this, this.childB );
        		}
        		
        	}
			this.childB = childB;
			if( this.childB != null ){
				this.childB.setParent( this );
				if( perspective != null ){
					perspective.fireAdded( this, this.childB );
				}
			}
		}
    }
    
    /**
     * A leaf in a tree, describes one {@link Dockable}.
     * @author Benjamin Sigg
     */
    public static class Leaf extends Entry{
        /** the element represented by this leaf */
        private PerspectiveDockable dockable;
        
        /**
         * Creates a new leaf
         * @param dockable the element that is represented by this leaf
         * @param placeholders placeholders associated with this leaf
         * @param placeholderMap placeholder information of a child {@link DockStation}
         * @param nodeId the unique identifier of this node, can be -1
         */
        public Leaf( PerspectiveDockable dockable, Path[] placeholders, PlaceholderMap placeholderMap, long nodeId ){
        	super( placeholders, placeholderMap, nodeId );
            this.dockable = dockable;
        }
        
        @Override
        public Leaf asLeaf() {
            return this;
        }
        
        /**
         * Gets the element which is represented by this leaf.
         * @return the element
         */
        public PerspectiveDockable getDockable(){
			return dockable;
		}
    }
}