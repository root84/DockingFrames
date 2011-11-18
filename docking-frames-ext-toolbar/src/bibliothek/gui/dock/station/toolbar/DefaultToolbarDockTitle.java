package bibliothek.gui.dock.station.toolbar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

import bibliothek.gui.Dockable;
import bibliothek.gui.ToolbarExtension;
import bibliothek.gui.dock.action.DockAction;
import bibliothek.gui.dock.themes.basic.action.BasicTitleViewItem;
import bibliothek.gui.dock.title.AbstractDockTitle;
import bibliothek.gui.dock.title.DockTitle;
import bibliothek.gui.dock.title.DockTitleFactory;
import bibliothek.gui.dock.title.DockTitleRequest;
import bibliothek.gui.dock.title.DockTitleVersion;

/**
 * A very simplistic implementation of a {@link DockTitle}. This particular implementation
 * shows a line with a width or height of 5 pixels and a custom color. 
 * @author Benjamin Sigg
 */
public class DefaultToolbarDockTitle extends AbstractDockTitle {
	/**
	 * Creates a new factory that creates new {@link ToolbarDockTitle}s.
	 * @param color the color of the title
	 * @return the new factory
	 */
	public static DockTitleFactory createFactory( final Color color ){
		return new DockTitleFactory(){
			@Override
			public void uninstall( DockTitleRequest request ){
				// ignore
			}
			
			@Override
			public void request( DockTitleRequest request ){
				request.answer( new DefaultToolbarDockTitle( request.getVersion(), request.getTarget(), color ) );
			}
			
			@Override
			public void install( DockTitleRequest request ){
				// ignore
			}
		};
	}
	
	private Color color;
	private Orientation orientation = Orientation.FREE_HORIZONTAL;
	
	public DefaultToolbarDockTitle( DockTitleVersion origin, Dockable dockable, Color color ){
		super( dockable, origin, true );
		this.color = color;
	}
	
	@Override
	protected BasicTitleViewItem<JComponent> createItemFor( DockAction action, Dockable dockable ){
		return dockable.getController().getActionViewConverter().createView( 
				action, ToolbarExtension.TOOLBAR_TITLE, dockable );
	}
		
	@Override
	public Dimension getPreferredSize(){
		Dimension size = super.getPreferredSize();
		return new Dimension( Math.max( 5, size.width ), Math.max( 5, size.height ));
	}
	
	@Override
	public void setActive( boolean active ){
		super.setActive( active );
		repaint();
	}
	
	@Override
	public void paintBackground( Graphics g, JComponent component ){
		g.setColor( color );
		g.fillRect( 0, 0, getWidth(), getHeight() );
		
		if( isActive() ){
			g.setColor( Color.RED );
			if( orientation.isHorizontal() ){
				g.drawLine( 1, getHeight()/2, getWidth()-1, getHeight()/2 );
			}
			else{
				g.drawLine( getWidth()/2, 1, getWidth()/2, getHeight()-1 );
			}
		}
	}
}
