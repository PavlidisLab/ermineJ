This folder contains scripts needed to create an ermineJ installation program for the Microsoft Windows platform.

The .nsi scripts found here have to be compiled with the Nullsoft Installation System (NSIS), which can be found at http://nsis.sourceforge.net/

For the .nsi scripts to be compiled successfully, this folder should contain the following files and folders:
.   - this folder contains license.txt and GNU_General_Public_License.txt
lib - a folder containing jar files
bin - a folder containing ermineJ.bat that runs the java virtual machine executing the main jar.

When compiling the JRE bundle, the script must point to a valid location where it is installed on your machine (for example, C:\j2sdk1.4.2_04\jre).

When compiling the script, instead of using the default compressor, you can choose one.  From my experience, the better the compression ratio, the longer it takes to compress (and uncompress) the files.  So if you don't have to worry about space, then just choose the default compressor (which i think is ZLIB).  If tight on space, however, here are sample results from bundling up the JRE with ermineJ:

ZLIB:    27.3 MB (33.3%), fastest
BZIP2:  23.3 MB (28.3%), slow
LZMA:  18.4 MB (22.5%), takes forever to compress on my machine

- WB


These scripts can be triggered with the following maven tasks:

nsis

nsis-jre -- to make the package with the JRE included

- PP