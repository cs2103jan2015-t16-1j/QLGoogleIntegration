download and install Java SE Development Kit 8u40 x86. http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
download and unzip/install Eclipse IDE for Java EE Developers 32bit. https://www.eclipse.org/downloads/
download and unzip Java EE 7 Full Platform/glassfish-4.1. https://glassfish.java.net/download.html

run Eclipse for Jave EE
go to Help -> Eclipse marketplace
search for glassfish
install glassfish tools for luna
restart when prompted

then go to Windows -> Preferences
go to Java -> Installed JREs
make sure there's a jdk in the list
if there's only a jre and no jdk, click on search and go to "C:\Program files (x86)\Java"

then go under window -> open perspective -> other -> Java EE
then at the bottom of the eclipse window choose Servers beside Properties and Snippets
Click on "No servers are available. Click this link to create a new server..."
Choose GlassFish 4 under GlassFish and Next
indicate both server root and java development kit
the server root should end with glassfish4/glassfish for eg. D:\glassfish4\glassfish
and the java development kit should be the jdk you just installed.
and then next and finish.

To test if it is working
download https://www.dropbox.com/s/g2srejzn7baa2c2/QLGoogleIntegration.zip?dl=0
and unzip and then import the project into eclipse

right click on the project and go to properties
Under Project Facets, ensure Java and Utility Module is ticked.
then on the right side, go under Runtime and tick GlassFish 4
then Ok

then run GoogleTest unit test and it should open a browser to login to google account and grant access.
if the browser is invoked by the unit test then it works. if not then something went wrong.