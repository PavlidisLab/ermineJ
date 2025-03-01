/*
 * The ermineJ project
 *
 * Copyright (c) 2011 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.erminej.gui.geneset;

import java.awt.Point;

import javax.swing.JPopupMenu;

import ubic.erminej.data.GeneSetTerm;

/**
 * Pop-up menu to display when user is browsing the table or tree.
 *
 * @author paul
 * @version $Id$
 */
public class GeneSetPanelPopupMenu extends JPopupMenu {
    private static final long serialVersionUID = 1L;
    Point popupPoint;
    GeneSetTerm selectedItem = null;

    /**
     * <p>
     * Constructor for GeneSetPanelPopupMenu.
     * </p>
     *
     * @param classID a {@link ubic.erminej.data.GeneSetTerm} object.
     */
    public GeneSetPanelPopupMenu( GeneSetTerm classID ) {
        this.selectedItem = classID;
    }

    /**
     * <p>
     * getPoint.
     * </p>
     *
     * @return a {@link java.awt.Point} object.
     */
    public Point getPoint() {
        return popupPoint;
    }

    /**
     * <p>
     * Getter for the field <code>selectedItem</code>.
     * </p>
     *
     * @return Returns the selectedItem.
     */
    public GeneSetTerm getSelectedItem() {
        return this.selectedItem;
    }

    /**
     * <p>
     * setPoint.
     * </p>
     *
     * @param point a {@link java.awt.Point} object.
     */
    public void setPoint( Point point ) {
        popupPoint = point;
    }

    /**
     * <p>
     * Setter for the field <code>selectedItem</code>.
     * </p>
     *
     * @param selectedItem The selectedItem to set.
     */
    public void setSelectedItem( GeneSetTerm selectedItem ) {
        this.selectedItem = selectedItem;
    }

}
