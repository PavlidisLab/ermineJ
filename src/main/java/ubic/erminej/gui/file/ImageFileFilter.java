/*
 * The baseCode project
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
package ubic.erminej.gui.file;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import ubic.basecode.util.FileTools;

/**
 * <p>
 * ImageFileFilter class.
 * </p>
 *
 * @author Will Braynen
 * @version $Id$
 */
public class ImageFileFilter extends FileFilter {

    private String description = "image files";

    /** {@inheritDoc} */
    @Override
    public boolean accept( File f ) {

        if ( f.isDirectory() ) {
            return true;
        }

        return FileTools.hasImageExtension( f.getName() );

    } // end accept

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * <p>
     * Setter for the field <code>description</code>.
     * </p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription( String description ) {
        this.description = description;
    }
}
