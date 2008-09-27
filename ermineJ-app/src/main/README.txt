ErmineJ Windows installer build instructions.

Prerequisites: build ermineJ-help. Also in ermineJ-app run:

mvn install antrun:run

To build the NSIS installer, you must have NSIS installed. Put makensis on your path or define it explicitly in your maven call:
  
mvn nsis:generate-project nsis:compile -Dnsis.makensis.bin="C:\Program Files\NSIS\makensis.exe"
 
 

