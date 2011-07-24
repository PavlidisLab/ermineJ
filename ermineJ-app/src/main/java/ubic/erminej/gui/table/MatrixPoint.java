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
package ubic.erminej.gui.table;

import java.awt.Point;

/**
 * @author paul
 * @version $Id$
 */
public class MatrixPoint extends Point implements Comparable<MatrixPoint> {

    private java.lang.Double value = java.lang.Double.NaN;

    public MatrixPoint( int x, int y, double value ) {
        super( x, y );
        this.value = value;
    }

    public java.lang.Double getValue() {
        return value;
    }

    @Override
    public int compareTo( MatrixPoint o ) {
        // sort so large values are on the top.
        return -this.getValue().compareTo( o.getValue() );
    }

}