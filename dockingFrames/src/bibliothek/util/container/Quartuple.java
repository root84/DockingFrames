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
 
/*
 * Created on 07.12.2004
 */
package bibliothek.util.container;

/**
 * @author Benjamin Sigg
 */
public class Quartuple<A,B,C,D> extends Triple<A,B,C>{
	private D d;
	
	public Quartuple(){
	}
	
	public Quartuple( A a, B b, C c, D d ){
		super( a, b, c );
		this.d = d;
	}

	public void setD( D d ){
		this.d = d;
	}
	
	public D getD(){
		return d;
	}
	
    @Override
	public Quartuple<A, B, C, D> clone(){
		return new Quartuple<A, B, C, D>( getA(), getB(), getC(), d );
	}
	
    @Override
	public boolean equals( Object o ){
		if( o instanceof Quartuple ){
			Quartuple s = (Quartuple)o;
			return super.equals( o ) && ( 
				(s.d == null && d == null) ||
				s.d.equals( d ));
		}
		return false;
	}
	
    @Override
	public int hashCode(){
		return super.hashCode() ^ (d == null ? 0 : d.hashCode());
	}
	
    @Override
	public String toString(){
		return getClass().getName() + "[a=" + getA() + ", b=" + getB() + 
			", c=" + getC() + ", d=" + d + "]";
	}
}
