//----------------------------------------------------------------------------//
//                                                                            //
//                                  P a g e                                   //
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
package org.audiveris.omr.score.entity;

import org.audiveris.omr.run.FilterDescriptor;

import org.audiveris.omr.score.Score;
import org.audiveris.omr.score.visitor.ScoreVisitor;

import org.audiveris.omr.sheet.Scale;
import org.audiveris.omr.sheet.Sheet;

import org.audiveris.omr.step.StepException;

import org.audiveris.omr.util.LiveParam;
import org.audiveris.omr.util.TreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.RenderedImage;
import java.util.List;

/**
 * Class {@code Page} represents a page in the score hierarchy,
 * and corresponds to a {@link Sheet} with its specific scale, skew,
 * dimension, etc.
 * Page instances compose a {@link Score}.
 *
 * @author Hervé Bitteur
 */
public class Page
        extends PageNode
{
    //~ Static fields/initializers ---------------------------------------------

    /** Usual logger utility */
    private static final Logger logger = LoggerFactory.getLogger(Page.class);

    //~ Instance fields --------------------------------------------------------
    /** Index of page, counted from 1, in the image file. */
    private final int index;

    /** Page ID. */
    private final String id;

    /** Related sheet. */
    private Sheet sheet;

    /** Page global scale. */
    private Scale scale;

    /** ScorePart list for the page. */
    private List<ScorePart> partList;

    /** Number of measures in this page. */
    private Integer measureCount;

    /** Progression of measure id within this page. */
    private Integer deltaMeasureId;

    /** Param for pixel filter. */
    private final LiveParam<FilterDescriptor> filterContext;

    /** Param for text language. */
    private final LiveParam<String> textContext;

    //~ Constructors -----------------------------------------------------------
    //------//
    // Page //
    //------//
    /**
     * Creates a new Page object.
     *
     * @param score the containing score
     * @param index page initial index in the containing image file, counted
     *              from 1.
     */
    public Page (Score score,
                 int index,
                 RenderedImage image)
            throws StepException
    {
        super(score);
        this.index = index;

        if (score.isMultiPage()) {
            id = score.getRadix() + "#" + index;
        } else {
            id = score.getRadix();
        }

        filterContext = new LiveParam<>(score.getFilterParam());
        textContext = new LiveParam<>(score.getTextParam());

        sheet = new Sheet(this, image);
    }

    //~ Methods ----------------------------------------------------------------
    //----------------//
    // getFilterParam //
    //----------------//
    public LiveParam<FilterDescriptor> getFilterParam ()
    {
        return filterContext;
    }

    //--------------//
    // getTextParam //
    //--------------//
    public LiveParam<String> getTextParam ()
    {
        return textContext;
    }

    //--------//
    // accept //
    //--------//
    @Override
    public boolean accept (ScoreVisitor visitor)
    {
        return visitor.visit(this);
    }

    //---------------------//
    // computeMeasureCount //
    //---------------------//
    /**
     * Compute the number of (vertical) measures in the page.
     */
    public void computeMeasureCount ()
    {
        int count = 0;

        for (TreeNode sn : getSystems()) {
            ScoreSystem system = (ScoreSystem) sn;
            count += system.getFirstPart()
                    .getMeasures()
                    .size();
        }

        measureCount = count;
    }

    //-------------------//
    // dumpMeasureCounts //
    //-------------------//
    /**
     * Log the detailed number of measures in the score.
     */
    public void dumpMeasureCounts ()
    {
        int count = 0;
        StringBuilder sb = new StringBuilder();

        for (TreeNode node : getSystems()) {
            ScoreSystem sys = (ScoreSystem) node;
            SystemPart part = sys.getLastPart();

            if (sb.length() > 0) {
                sb.append(", ");
            }

            sb.append(part.getMeasures().size())
                    .append(" in ")
                    .append(sys.getInfo().idString());
            count += part.getMeasures()
                    .size();
        }

        StringBuilder msg = new StringBuilder();
        msg.append(count);
        msg.append(" raw measure");

        if (count > 1) {
            msg.append('s');
        }

        msg.append(": [")
                .append(sb)
                .append("]");

        logger.info("{}{}", sheet.getLogPrefix(), msg.toString());
    }

    //-------------------//
    // getDeltaMeasureId //
    //-------------------//
    /**
     * Report the progression of measure IDs within this page.
     *
     * @return the deltaMeasureId
     */
    public Integer getDeltaMeasureId ()
    {
        return deltaMeasureId;
    }

    //--------------//
    // getDimension //
    //--------------//
    /**
     * Report the dimension of the sheet/page.
     *
     * @return the page/sheet dimension in pixels
     */
    public Dimension getDimension ()
    {
        return sheet.getDimension();
    }

    //----------------//
    // getFirstSystem //
    //----------------//
    /**
     * Report the first system in the page.
     *
     * @return the first system
     */
    public ScoreSystem getFirstSystem ()
    {
        if (children.isEmpty()) {
            return null;
        } else {
            return (ScoreSystem) children.get(0);
        }
    }

    //-------//
    // getId //
    //-------//
    /**
     * @return the id
     */
    public String getId ()
    {
        return id;
    }

    //----------//
    // getIndex //
    //----------//
    /**
     * @return the page index
     */
    public int getIndex ()
    {
        return index;
    }

    //---------------//
    // getLastSystem //
    //---------------//
    /**
     * Report the last system in the page.
     *
     * @return the last system
     */
    public ScoreSystem getLastSystem ()
    {
        if (children.isEmpty()) {
            return null;
        } else {
            return (ScoreSystem) children.get(children.size() - 1);
        }
    }

    //--------------------//
    // getMeanStaffHeight //
    //--------------------//
    /**
     * Report the mean staff height based on page interline.
     * This should be refined per system, if not per staff
     *
     * @return the page-based average value of staff heights
     */
    public int getMeanStaffHeight ()
    {
        return (Score.LINE_NB - 1) * scale.getInterline();
    }

    //-----------------//
    // getMeasureCount //
    //-----------------//
    /**
     * Report the number of (vertical) measures in this page.
     *
     * @return the number of page measures
     */
    public int getMeasureCount ()
    {
        return measureCount;
    }

    //-------------//
    // getPartList //
    //-------------//
    /**
     * Report the global list of parts.
     *
     * @return partList the list of parts
     */
    public List<ScorePart> getPartList ()
    {
        return partList;
    }

    //---------------------//
    // getPrecedingInScore //
    //---------------------//
    /**
     * Report the preceding page of this one within the score.
     *
     * @return the preceding page, or null if none
     */
    public Page getPrecedingInScore ()
    {
        return (Page) getPreviousSibling();
    }

    //----------//
    // getScale //
    //----------//
    /**
     * Report the scale of the page.
     *
     * @return the page scale (basically: number of pixels for main interline)
     */
    @Override
    public Scale getScale ()
    {
        return scale;
    }

    //----------//
    // getSheet //
    //----------//
    /**
     * Report the related sheet entity.
     *
     * @return the related sheet, or null if none
     */
    public Sheet getSheet ()
    {
        return sheet;
    }

    //---------------//
    // getSystemById //
    //---------------//
    /**
     * Report the system for which id is provided.
     *
     * @param id id of desired system
     * @return the desired system
     */
    public ScoreSystem getSystemById (int id)
    {
        return (ScoreSystem) getSystems()
                .get(id - 1);
    }

    //------------//
    // getSystems //
    //------------//
    /**
     * Report the collection of systems in that score.
     *
     * @return the systems
     */
    public List<TreeNode> getSystems ()
    {
        return getChildren();
    }

    //--------------//
    // resetSystems //
    //--------------//
    /**
     * Reset the systems collection of a score entity.
     */
    public void resetSystems ()
    {
        // Discard systems
        getSystems()
                .clear();

        // Discard partlists
        if (partList != null) {
            partList.clear();
        }
    }

    //-------------------//
    // setDeltaMeasureId //
    //-------------------//
    /**
     * Assign the progression of measure IDs within this page.
     *
     * @param deltaMeasureId the deltaMeasureId to set
     */
    public void setDeltaMeasureId (Integer deltaMeasureId)
    {
        this.deltaMeasureId = deltaMeasureId;
    }

    //-------------//
    // setPartList //
    //-------------//
    /**
     * Assign a part list valid for the page.
     *
     * @param partList the list of parts
     */
    public void setPartList (List<ScorePart> partList)
    {
        this.partList = partList;
    }

    //----------//
    // setScale //
    //----------//
    /**
     * Assign proper scale for this page.
     *
     * @param scale the general scale for the page
     */
    public void setScale (Scale scale)
    {
        this.scale = scale;
    }

    //----------//
    // setSheet //
    //----------//
    /**
     * Register the name of the corresponding sheet entity.
     *
     * @param sheet the related sheet entity
     */
    public void setSheet (Sheet sheet)
    {
        this.sheet = sheet;
    }

    //----------//
    // systemAt //
    //----------//
    /**
     * Retrieve which system contains the provided point.
     *
     * @param point the point in the <b>SHEET</b> display
     * @return the nearest system.
     */
    public ScoreSystem systemAt (Point point)
    {
        return getSheet()
                .getSystemOf(point)
                .getScoreSystem();
    }

    //----------//
    // toString //
    //----------//
    @Override
    public String toString ()
    {
        return "{Page " + id + "}";
    }
}
