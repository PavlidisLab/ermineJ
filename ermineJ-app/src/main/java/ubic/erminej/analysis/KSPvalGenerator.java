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
package ubic.erminej.analysis;

import ubic.erminej.SettingsHolder;
import ubic.erminej.data.GeneAnnotations;

/**
 * Use the Kolmogorov-Smirnov test to evaluate the ranking of the probes.
 * 
 * @author pavlidis
 * @version $Id$
 */
public class KSPvalGenerator extends AbstractGeneSetPvalGenerator {

    public KSPvalGenerator( SettingsHolder set, GeneAnnotations an ) {
        super( set, an );
        throw new UnsupportedOperationException();
    }
}
