/*
 * The baseCode project
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

/**
 * @author paul
 * @version $Id$
 */
public class GeneToGeneSetAssociation {

    public enum EvidenceCode {
        IEA, TAS, IAS
        /* todo fill in */
    }

    private EvidenceCode evidence = null;

    private Gene gene;

    private GeneSetTerm goEntry;

    public GeneToGeneSetAssociation( Gene gene, GeneSetTerm goEntry ) {
        super();
        this.gene = gene;
        this.goEntry = goEntry;
    }

    public GeneToGeneSetAssociation( Gene gene, GeneSetTerm goEntry, EvidenceCode ec ) {
        super();
        this.gene = gene;
        this.goEntry = goEntry;
        this.evidence = ec;
    }

    public EvidenceCode getEvidence() {
        return evidence;
    }

    public Gene getGene() {
        return gene;
    }

    public GeneSetTerm getGoEntry() {
        return goEntry;
    }

    public void setEvidence( EvidenceCode evidence ) {
        this.evidence = evidence;
    }

}
