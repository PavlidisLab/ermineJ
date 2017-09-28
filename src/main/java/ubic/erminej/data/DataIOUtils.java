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
package ubic.erminej.data;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.dataStructure.matrix.FastRowAccessDoubleMatrix;
import ubic.basecode.io.reader.DoubleMatrixReader;
import ubic.erminej.SettingsHolder;

/**
 * <p>
 * DataIOUtils class.
 * </p>
 *
 * @author paul
 * @version $Id$
 */
public class DataIOUtils {
    /**
     * <p>
     * readDataMatrixForAnalysis.
     * </p>
     *
     * @throws java.io.IOException if any.
     * @param geneAnnots a {@link ubic.erminej.data.GeneAnnotations} object.
     * @param settings a {@link ubic.erminej.SettingsHolder} object.
     * @return a {@link ubic.basecode.dataStructure.matrix.DoubleMatrix} object.
     */
    public static DoubleMatrix<Element, String> readDataMatrixForAnalysis( GeneAnnotations geneAnnots,
            SettingsHolder settings ) throws IOException {
        DoubleMatrix<Element, String> rawData;
        DoubleMatrixReader r = new DoubleMatrixReader();

        Collection<String> usableRowNames = new HashSet<>();
        for ( Element p : geneAnnots.getProbes() ) {
            usableRowNames.add( p.getName() );
        }

        /*
         * The -2 is because the read method counts from index 0, not counting the label column. So the second column of
         * the file is index 0 as far as read() is concerned.
         */
        DoubleMatrix<String, String> omatrix = r.read( settings.getRawDataFileName(), usableRowNames,
                settings.getDataCol() - 2 );

        if ( omatrix.rows() == 0 ) {
            throw new IllegalArgumentException( "No rows were read from the file for the elements in the annotations." );
        }

        rawData = new FastRowAccessDoubleMatrix<>( omatrix.asArray() );
        rawData.setColumnNames( omatrix.getColNames() );
        for ( int i = 0; i < omatrix.rows(); i++ ) {
            String n = omatrix.getRowName( i );
            Element p = geneAnnots.findElement( n );
            assert p != null;
            rawData.setRowName( p, i );
        }
        return rawData;
    }

}
