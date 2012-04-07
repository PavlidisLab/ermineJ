/*
 * The ermineJ project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.erminej.gui.util;

import java.awt.Color;

import ubic.erminej.data.GeneSetResult;

/**
 * @author pavlidis
 * @version $Id$
 */
public class Colors {
    static public final Color goParent = Color.LIGHT_GRAY;
    static public final Color goChild = Color.YELLOW;

    static public final Color LIGHTRED1 = new Color( 240, 100, 100 ); // dark
    static public final Color LIGHTRED2 = new Color( 240, 140, 140 );
    static public final Color LIGHTRED3 = new Color( 240, 180, 180 );
    static public final Color LIGHTRED4 = new Color( 240, 200, 200 );
    static public final Color LIGHTRED5 = new Color( 240, 220, 220 ); // light

    static public final Color GREY1 = new Color( 100, 100, 100 ); // dark
    public static final Color GREY2 = new Color( 140, 140, 140 );
    public static final Color GREY3 = new Color( 189, 180, 180 );
    public static final Color GREY4 = new Color( 200, 200, 200 );
    public static final Color GREY5 = new Color( 220, 220, 220 ); // light

    public static final Color LIGHTGREEN1 = new Color( 140, 240, 140 ); // dark
    public static final Color LIGHTGREEN2 = new Color( 160, 240, 160 );
    public static final Color LIGHTGREEN3 = new Color( 180, 240, 180 );
    public static final Color LIGHTGREEN4 = new Color( 200, 240, 200 );
    public static final Color LIGHTGREEN5 = new Color( 220, 240, 220 ); // light

    static public final Color LIGHTBLUE1 = new Color( 100, 100, 240 ); // dark
    static public final Color LIGHTBLUE2 = new Color( 140, 140, 240 );
    static public final Color LIGHTBLUE3 = new Color( 180, 180, 240 );
    static public final Color LIGHTBLUE4 = new Color( 200, 200, 240 );
    static public final Color LIGHTBLUE5 = new Color( 220, 220, 240 ); // light

    public static final Color PINK = new Color( 220, 160, 220 );
    public static final Color LIGHTYELLOW = new Color( 255, 239, 142 );

    /**
     * @param pvalCorr
     * @return
     */
    public static Color chooseBackgroundColorForPvalue( double pvalCorr ) {
        if ( pvalCorr > 0.1 ) return Color.WHITE;
        if ( pvalCorr > 0.05 ) return Colors.LIGHTGREEN5;
        if ( pvalCorr > 0.01 ) return Colors.LIGHTGREEN4;
        if ( pvalCorr > 0.001 ) return Colors.LIGHTGREEN3;
        return Colors.LIGHTGREEN2;
    }

    public static Color chooseColorForMultifunctionalityEffect( GeneSetResult value ) {

        int step = value.getMultifunctionalityCorrectedRankDelta();

        if ( step > 100 ) {
            return Color.RED;
        }
        if ( step > 20 ) {
            return Color.RED.darker();
        }
        if ( step > 10 ) {
            return Colors.LIGHTRED2;
        }
        if ( step > 0 ) {
            return Colors.LIGHTYELLOW.darker();
        }
        return Colors.LIGHTGREEN1;
    }
}
