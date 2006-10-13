//----------------------------------------------------------------------------//
//                                                                            //
//                       P a i n t i n g V i s i t o r                        //
//                                                                            //
//  Copyright (C) Herve Bitteur 2000-2006. All rights reserved.               //
//  This software is released under the terms of the GNU General Public       //
//  License. Please contact the author at herve.bitteur@laposte.net           //
//  to report bugs & suggestions.                                             //
//----------------------------------------------------------------------------//
//
package omr.score.visitor;

import omr.glyph.Glyph;
import omr.glyph.Shape;

import omr.score.Barline;
import omr.score.Clef;
import omr.score.KeySignature;
import omr.score.Measure;
import omr.score.MusicNode;
import omr.score.Score;
import static omr.score.ScoreConstants.*;
import omr.score.Slur;
import omr.score.Staff;
import omr.score.StaffNode;
import omr.score.StaffPoint;
import omr.score.System;
import omr.score.TimeSignature;
import omr.score.UnitDimension;

import omr.ui.icon.SymbolIcon;
import omr.ui.view.Zoom;

import omr.util.Logger;

import java.awt.*;

/**
 * Class <code>PaintingVisitor</code> defines for every node in Score hierarchy
 * the painting of node in the <b>Score</b> display.
 *
 * @author Herv&eacute Bitteur
 * @version $Id$
 */
public class PaintingVisitor
    implements Visitor
{
    //~ Static fields/initializers ---------------------------------------------

    private static final Logger logger = Logger.getLogger(
        PaintingVisitor.class);

    //~ Instance fields --------------------------------------------------------

    /** Graphic context */
    private final Graphics g;

    /** Display zoom */
    private final Zoom zoom;

    //~ Constructors -----------------------------------------------------------

    //-----------------//
    // PaintingVisitor //
    //-----------------//
    /**
     * Creates a new PaintingVisitor object.
     *
     * @param g Graphic context
     * @param z zoom factor
     */
    public PaintingVisitor (Graphics g,
                            Zoom     z)
    {
        this.g = g;
        this.zoom = z;
    }

    //~ Methods ----------------------------------------------------------------

    public boolean visit (Barline barline)
    {
        Shape shape = barline.getShape();

        if (shape != null) {
            // Draw the barline symbol
            barline.getStaff()
                   .paintSymbol(
                g,
                zoom,
                (SymbolIcon) shape.getIcon(),
                barline.getCenter(),
                0);
        } else {
            logger.warning("No shape for barline " + this);
        }

        return true;
    }

    public boolean visit (Clef clef)
    {
        // Draw the clef symbol
        clef.getStaff()
            .paintSymbol(
            g,
            zoom,
            (SymbolIcon) clef.getShape().getIcon(),
            clef.getCenter(),
            clef.getPitchPosition());

        return true;
    }

    public boolean visit (KeySignature keySignature)
    {
        logger.warning(
            "KeySignature to be implemented in " + getClass().getName());

        return true;
    }

    public boolean visit (Measure measure)
    {
        // Draw the measure id, if on the first staff only
        if (measure.getStaff()
                   .getStafflink() == 0) {
            Point origin = measure.getOrigin();
            g.setColor(Color.lightGray);
            g.drawString(
                Integer.toString(measure.getId()),
                zoom.scaled(origin.x + measure.getLeftX()) - 5,
                zoom.scaled(origin.y) - 15);
        }

        return true;
    }

    public boolean visit (MusicNode musicNode)
    {
        return true;
    }

    public boolean visit (Score score)
    {
        score.acceptChildren(this);

        return false;
    }

    public boolean visit (Slur slur)
    {
        slur.getArc()
            .draw(g, slur.getOrigin(), zoom);

        return true;
    }

    public boolean visit (Staff staff)
    {
        Point origin = staff.getOrigin();
        g.setColor(Color.black);

        // Draw the staff lines
        for (int i = 0; i < LINE_NB; i++) {
            // Y of this staff line
            int y = zoom.scaled(origin.y + (i * INTER_LINE));
            g.drawLine(
                zoom.scaled(origin.x),
                y,
                zoom.scaled(origin.x + staff.getWidth()),
                y);
        }

        // Draw the starting barline, if any
        if (staff.getStartingBarline() != null) {
            staff.getStartingBarline()
                 .accept(this);
        }

        return true;
    }

    public boolean visit (StaffNode staffNode)
    {
        return true;
    }

    public boolean visit (System system)
    {
        // Check whether our system is impacted)
        Rectangle clip = g.getClipBounds();

        if (!system.xIntersect(
            zoom.unscaled(clip.x),
            zoom.unscaled(clip.x + clip.width))) {
            return false;
        } else {
            UnitDimension dimension = system.getDimension();
            Point         origin = system.getOrigin();
            g.setColor(Color.lightGray);

            // Draw the system left edge
            g.drawLine(
                zoom.scaled(origin.x),
                zoom.scaled(origin.y),
                zoom.scaled(origin.x),
                zoom.scaled(origin.y + dimension.height + STAFF_HEIGHT));

            // Draw the system right edge
            g.drawLine(
                zoom.scaled(origin.x + dimension.width),
                zoom.scaled(origin.y),
                zoom.scaled(origin.x + dimension.width),
                zoom.scaled(origin.y + dimension.height + STAFF_HEIGHT));

            return true;
        }
    }

    public boolean visit (TimeSignature timeSignature)
    {
        Shape shape = timeSignature.getShape();
        Staff staff = timeSignature.getStaff();

        if (shape != null) {
            switch (shape) {
            // If this is an illegal shape, do not draw anything.
            // TBD: we could draw a special sign for this
            case NO_LEGAL_SHAPE :
                break;

            // Is it a complete (one-symbol) time signature ?
            case TIME_FOUR_FOUR :
            case TIME_TWO_TWO :
            case TIME_TWO_FOUR :
            case TIME_THREE_FOUR :
            case TIME_SIX_EIGHT :
            case COMMON_TIME :
            case CUT_TIME :
                staff.paintSymbol(
                    g,
                    zoom,
                    (SymbolIcon) shape.getIcon(),
                    timeSignature.getCenter());

                break;
            }
        } else {
            // Assume a (legal) multi-symbol signature
            for (Glyph glyph : timeSignature.getGlyphs()) {
                Shape s = glyph.getShape();

                if (s != null) {
                    StaffPoint center = timeSignature.computeGlyphCenter(glyph);
                    int        pitch = staff.unitToPitch(center.y);
                    staff.paintSymbol(
                        g,
                        zoom,
                        (SymbolIcon) s.getIcon(),
                        center,
                        pitch);
                }
            }
        }

        return true;
    }
}