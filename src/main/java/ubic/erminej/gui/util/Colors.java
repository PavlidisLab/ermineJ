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
 * <p>
 * Colors class.
 * </p>
 *
 * @author pavlidis
 * @version $Id$
 */
public class Colors {
    /** Constant <code>goParent</code> */
    static public final Color goParent = Color.LIGHT_GRAY;
    /** Constant <code>goChild</code> */
    static public final Color goChild = Color.YELLOW;

    /** Constant <code>LIGHTRED1</code> */
    static public final Color LIGHTRED1 = new Color( 240, 100, 100 ); // dark
    /** Constant <code>LIGHTRED2</code> */
    static public final Color LIGHTRED2 = new Color( 240, 140, 140 );
    /** Constant <code>LIGHTRED3</code> */
    static public final Color LIGHTRED3 = new Color( 240, 180, 180 );
    /** Constant <code>LIGHTRED4</code> */
    static public final Color LIGHTRED4 = new Color( 240, 200, 200 );
    /** Constant <code>LIGHTRED5</code> */
    static public final Color LIGHTRED5 = new Color( 240, 220, 220 ); // light

    /** Constant <code>GREY1</code> */
    static public final Color GREY1 = new Color( 100, 100, 100 ); // dark
    /** Constant <code>GREY2</code> */
    public static final Color GREY2 = new Color( 140, 140, 140 );
    /** Constant <code>GREY3</code> */
    public static final Color GREY3 = new Color( 189, 180, 180 );
    /** Constant <code>GREY4</code> */
    public static final Color GREY4 = new Color( 200, 200, 200 );
    /** Constant <code>GREY5</code> */
    public static final Color GREY5 = new Color( 220, 220, 220 ); // light

    /** Constant <code>LIGHTGREEN1</code> */
    public static final Color LIGHTGREEN1 = new Color( 140, 240, 140 ); // dark
    /** Constant <code>LIGHTGREEN2</code> */
    public static final Color LIGHTGREEN2 = new Color( 160, 240, 160 );
    /** Constant <code>LIGHTGREEN3</code> */
    public static final Color LIGHTGREEN3 = new Color( 180, 240, 180 );
    /** Constant <code>LIGHTGREEN4</code> */
    public static final Color LIGHTGREEN4 = new Color( 200, 240, 200 );
    /** Constant <code>LIGHTGREEN5</code> */
    public static final Color LIGHTGREEN5 = new Color( 220, 240, 220 ); // light

    /** Constant <code>LIGHTBLUE1</code> */
    static public final Color LIGHTBLUE1 = new Color( 100, 100, 240 ); // dark
    /** Constant <code>LIGHTBLUE2</code> */
    static public final Color LIGHTBLUE2 = new Color( 140, 140, 240 );
    /** Constant <code>LIGHTBLUE3</code> */
    static public final Color LIGHTBLUE3 = new Color( 180, 180, 240 );
    /** Constant <code>LIGHTBLUE4</code> */
    static public final Color LIGHTBLUE4 = new Color( 200, 200, 240 );
    /** Constant <code>LIGHTBLUE5</code> */
    static public final Color LIGHTBLUE5 = new Color( 220, 220, 240 ); // light

    /** Constant <code>PINK</code> */
    public static final Color PINK = new Color( 220, 160, 220 );
    /** Constant <code>LIGHTYELLOW</code> */
    public static final Color LIGHTYELLOW = new Color( 255, 239, 142 );

    /**
     * <p>
     * chooseBackgroundColorForPvalue.
     * </p>
     *
     * @param pvalCorr a double.
     * @return a {@link java.awt.Color} object.
     */
    public static Color chooseBackgroundColorForPvalue( double pvalCorr ) {
        if ( pvalCorr > 0.1 ) return Color.WHITE;
        if ( pvalCorr > 0.05 ) return Colors.LIGHTGREEN5;
        if ( pvalCorr > 0.01 ) return Colors.LIGHTGREEN4;
        if ( pvalCorr > 0.001 ) return Colors.LIGHTGREEN3;
        return Colors.LIGHTGREEN2;
    }

    /**
     * <p>
     * chooseColorForMultifunctionalityEffect.
     * </p>
     *
     * @param value a {@link ubic.erminej.data.GeneSetResult} object.
     * @return a {@link java.awt.Color} object.
     */
    public static Color chooseColorForMultifunctionalityEffect( GeneSetResult value ) {

        Integer step = value.getMultifunctionalityCorrectedRankDelta();

        if ( step == null ) return null;

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
