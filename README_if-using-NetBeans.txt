If using NetBeans IDE (3.6), you have to mount the /src subdirectory, not this directory!

Otherwise you'll get the following errors if you try to run the classScoreGUI class in NetBeans, and the classScoreGUI class also won't have a green run/play icon next to the class name:

~~~~~~~~~~~
java.lang.NoClassDefFoundError: src/classScore/classScoreGUI (wrong name: classScore/classScoreGUI)
        at java.lang.ClassLoader.defineClass0(Native Method)
        at java.lang.ClassLoader.defineClass(ClassLoader.java:537)
        at java.security.SecureClassLoader.defineClass(SecureClassLoader.java:123)
        at java.net.URLClassLoader.defineClass(URLClassLoader.java:251)
        at java.net.URLClassLoader.access$100(URLClassLoader.java:55)
        at java.net.URLClassLoader$1.run(URLClassLoader.java:194)
        at java.security.AccessController.doPrivileged(Native Method)
        at java.net.URLClassLoader.findClass(URLClassLoader.java:187)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:289)
        at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:274)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:235)
        at java.lang.ClassLoader.loadClassInternal(ClassLoader.java:302)
Exception in thread "main" 
~~~~~~~~~~~

