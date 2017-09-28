/*
 * The ermineJ project
 *
 * Copyright (c) 2013 University of British Columbia
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
package ubic.erminej.data;

import java.awt.Color;
import java.io.IOException;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.graphics.ColorMap;
import ubic.basecode.graphics.MatrixDisplay;

/**
 * Methods for writing a GeneSetDetails heatmap to a image file
 *
 * @author Paul
 * @version $Id$
 */
public class GeneSetDetailsImageWriter {

    /**
     * <p>
     * writePng.
     * </p>
     *
     * @param fileName a {@link java.lang.String} object.
     * @param colorMap an array of {@link java.awt.Color} objects.
     * @param includeLabels a boolean.
     * @param includeScalebar a boolean.
     * @param normalized a boolean.
     * @throws java.io.IOException if any.
     * @param geneSetDetails a {@link ubic.erminej.data.GeneSetDetails} object.
     */
    public static void writePng( GeneSetDetails geneSetDetails, String fileName, Color[] colorMap,
            boolean includeLabels, boolean includeScalebar, boolean normalized ) throws IOException {
        DoubleMatrix<Element, String> matrix = geneSetDetails.getDataMatrix();

        if ( colorMap == null ) {
            colorMap = ColorMap.BLACKBODY_COLORMAP;
        }

        MatrixDisplay<Element, String> matrixDisplay = new MatrixDisplay<>( matrix );
        matrixDisplay.setColorMap( colorMap );
        matrixDisplay.setStandardizedEnabled( normalized );
        writePng( matrixDisplay, fileName, includeLabels, includeScalebar, normalized );

    }

    /**
     * @param fileName
     * @param includeLabels
     * @param includeScalebar
     * @param normalized
     * @param matrixDisplay
     * @throws IOException
     */
    private static void writePng( MatrixDisplay<Element, String> matrixDisplay, String fileName, boolean includeLabels,
            boolean includeScalebar, boolean normalized ) throws IOException {
        matrixDisplay.saveImage( matrixDisplay.getColorMatrix(), fileName, includeLabels, includeScalebar, normalized );
    }

}
