ErmineJ installer build instructions.

mvn install antrun:run

To build the NSIS installer, you must have NSIS installed. Put makensis on your path or define it explicitly in your maven call:
  
mvn nsis:generate-project nsis:compile -Dnsis.makensis.bin="C:\Program Files\NSIS\makensis.exe"
 
 
For the izpack installer, run 

mvn izpack:izpack

(The osx installer build does not work as of Oct 2011)

For osx run

 mvn install osxappbundle:bundle
