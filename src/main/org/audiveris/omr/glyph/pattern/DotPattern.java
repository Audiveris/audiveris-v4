//----------------------------------------------------------------------------//
//                                                                            //
//                            D o t P a t t e r n                             //
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
package org.audiveris.omr.glyph.pattern;

import org.audiveris.omr.constant.Constant;
import org.audiveris.omr.constant.ConstantSet;

import org.audiveris.omr.glyph.Glyphs;
import org.audiveris.omr.glyph.Shape;
import org.audiveris.omr.glyph.ShapeSet;
import org.audiveris.omr.glyph.facets.Glyph;

import org.audiveris.omr.run.Orientation;

import org.audiveris.omr.sheet.Scale;
import org.audiveris.omr.sheet.SystemInfo;

import org.audiveris.omr.text.TextLine;
import org.audiveris.omr.text.TextWord;

import org.audiveris.omr.util.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.Collections;
import java.util.Set;

/**
 * Class {@code DotPattern} filters the dot glyphs to remove those
 * which are actually text dashes ('-') within sentences.
 *
 * @author Hervé Bitteur
 */
public class DotPattern
        extends GlyphPattern
{
    //~ Static fields/initializers ---------------------------------------------

    /** Specific application parameters */
    private static final Constants constants = new Constants();

    /** Usual logger utility */
    private static final Logger logger = LoggerFactory.getLogger(
            DotPattern.class);

    //~ Instance fields --------------------------------------------------------
    //
    /** Max dx from sentence end to dot. */
    private final int maxLineDx;

    /** Max dy from sentence baseline to dot. */
    private final int maxLineDy;

    //~ Constructors -----------------------------------------------------------
    //
    //------------//
    // DotPattern //
    //------------//
    /**
     * Creates a new DotPattern object.
     *
     * @param system the dedicated system
     */
    public DotPattern (SystemInfo system)
    {
        super("Dot", system);

        // Scale-dependent parameters
        maxLineDx = scale.toPixels(constants.maxLineDx);
        maxLineDy = scale.toPixels(constants.maxLineDy);
    }

    //~ Methods ----------------------------------------------------------------
    //
    //------------//
    // runPattern //
    //------------//
    /**
     * In a specified system, look for all dots that should not be kept.
     *
     * @return the number of dots deassigned
     */
    @Override
    public int runPattern ()
    {
        int nb = 0;
        String language = system.getSheet()
                .getPage()
                .getTextParam()
                .getTarget();

        for (Glyph glyph : getQuestionableDots()) {
            // Check alignment with a TextLine
            TextLine line = getEmbracingLine(glyph);

            if (line == null) {
                continue;
            }

            // Check shape
            if (!isDashLooking(glyph)) {
                continue;
            }

            // OK, assign it the character shape
            glyph.setShape(Shape.CHARACTER);

            // Insert it into line
            TextWord word = TextWord.createManualWord(glyph, "-");
            glyph.setTextWord(language, word);
            line.addWords(Collections.singleton(word));
            logger.debug("Reassign dot#{} to {}", glyph.getId(), line);

            // Counters
            nb++;
        }

        return nb;
    }

    //------------------//
    // getEmbracingLine //
    //------------------//
    /**
     * Lookup for a TextLine that embraces the provided glyph.
     *
     * @param the (dot) glyph to check
     * @return glyph the embracing line if any, otherwise null
     */
    private TextLine getEmbracingLine (Glyph glyph)
    {
        Rectangle glyphBox = glyph.getBounds();

        for (TextLine sentence : system.getSentences()) {
            Line2D baseline = sentence.getBaseline();

            // Check in abscissa: not before sentence beginning
            if ((glyphBox.x + glyphBox.width) <= baseline.getX1()) {
                continue;
            }

            // Check in abscissa: not too far after sentence end
            if ((glyphBox.x - baseline.getX2()) > maxLineDx) {
                continue;
            }

            // Check in abscissa: not overlapping any sentence word
            for (TextWord word : sentence.getWords()) {
                if (word.getBounds()
                        .intersects(glyphBox)) {
                    continue;
                }
            }

            // Check in ordinate: distance from baseline
            double dy = baseline.ptLineDist(glyph.getAreaCenter());

            if (dy > maxLineDy) {
                continue;
            }

            // This line is OK, take it
            return sentence;
        }

        // Nothing found
        return null;
    }

    //---------------------//
    // getQuestionableDots //
    //---------------------//
    /**
     * Retrieve questionable dots.
     *
     * @return the set of dots to further check
     */
    private Set<Glyph> getQuestionableDots ()
    {
        Set<Glyph> dots = Glyphs.lookupGlyphs(
                system.getGlyphs(),
                new Predicate<Glyph>()
        {
            @Override
            public boolean check (Glyph glyph)
            {
                Shape shape = glyph.getShape();

                return (shape != null)
                       && ShapeSet.Dots.contains(shape)
                       && !glyph.isManualShape() && glyph.isActive();
            }
        });

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "{} Questionable {}",
                    system.getLogPrefix(),
                    Glyphs.toString("dots", dots));
        }

        return dots;
    }

    //---------------//
    // isDashLooking //
    //---------------//
    /**
     * Check whether the provided glyph looks like a '-' character.
     *
     * @param glyph the glyph to check
     * @return true if tests are OK
     */
    private boolean isDashLooking (Glyph glyph)
    {
        return glyph.getAspect(Orientation.HORIZONTAL) >= constants.minAspect.getValue();
    }

    //~ Inner Classes ----------------------------------------------------------
    //
    //-----------//
    // Constants //
    //-----------//
    private static final class Constants
            extends ConstantSet
    {
        //~ Instance fields ----------------------------------------------------

        Constant.Ratio minAspect = new Constant.Ratio(
                2,
                "Minimum width / height ratio for a dash");

        Scale.Fraction maxLineDx = new Scale.Fraction(
                10,
                "Maximum abscissa offset from line to dash");

        Scale.Fraction maxLineDy = new Scale.Fraction(
                2,
                "Maximum ordinate offset from line to dash");

    }
}
