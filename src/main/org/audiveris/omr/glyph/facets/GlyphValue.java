//----------------------------------------------------------------------------//
//                                                                            //
//                            G l y p h V a l u e                             //
//                                                                            //
//----------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//
// Copyright © Hervé Bitteur and others 2000-2017. All rights reserved.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//----------------------------------------------------------------------------//
// </editor-fold>
package org.audiveris.omr.glyph.facets;

import org.audiveris.omr.glyph.Shape;

import org.audiveris.omr.lag.Section;

import java.util.SortedSet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class {@code GlyphValue} is used to map a Glyph with its XML
 * representation as handled by JAXB, to allow the decoupling between
 * in-memory layout and XML layout.
 *
 * @author Hervé Bitteur
 */
@XmlRootElement(name = "glyph")
public class GlyphValue
{
    //~ Instance fields --------------------------------------------------------

    /** Id */
    @XmlAttribute(name = "id")
    final int id;

    /** Interline */
    @XmlAttribute(name = "interline")
    final int interline;

    /** Shape */
    @XmlAttribute(name = "shape")
    final Shape shape;

    /** Stem Number */
    @XmlElement(name = "stem-number")
    final int stemNumber;

    /** With Ledger */
    @XmlElement(name = "with-ledger")
    final boolean withLedger;

    /** Pitch Position */
    @XmlElement(name = "pitch-position")
    final double pitchPosition;

    /** Member sections */
    @XmlElement(name = "section")
    final SortedSet<Section> members;

    //~ Constructors -----------------------------------------------------------
    //------------//
    // GlyphValue //
    //------------//
    /**
     * Create a new GlyphValue object from scratch
     *
     * @param shape
     * @param interline
     * @param id
     * @param stemNumber
     * @param withLedger
     * @param pitchPosition
     * @param members
     */
    public GlyphValue (Shape shape,
                       int interline,
                       int id,
                       int stemNumber,
                       boolean withLedger,
                       double pitchPosition,
                       SortedSet<Section> members)
    {
        this.shape = shape;
        this.interline = interline;
        this.id = id;
        this.stemNumber = stemNumber;
        this.withLedger = withLedger;
        this.pitchPosition = pitchPosition;
        this.members = members;
    }

    /**
     * Create a new GlyphValue object from a real Glyph
     *
     * @param glyph the real glyph
     */
    public GlyphValue (Glyph glyph)
    {
        this(
                (glyph.getShape() != null) ? glyph.getShape().getPhysicalShape()
                : null,
                glyph.getInterline(),
                glyph.getId(),
                glyph.getStemNumber(),
                glyph.isWithLedger(),
                glyph.getPitchPosition(),
                glyph.getMembers());
    }

    //------------//
    // GlyphValue //
    //------------//
    /**
     * No-arg constructor needed for JAXB, using dummy values
     */
    protected GlyphValue ()
    {
        this.shape = null;
        this.interline = 0;
        this.id = 0;
        this.stemNumber = 0;
        this.withLedger = false;
        this.pitchPosition = 0;
        this.members = null;
    }
}
