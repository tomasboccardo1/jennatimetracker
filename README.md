#Jenna Timetracker
Jenna is a tool for tracking hours and effort with also integrated chatboot   
main page: [http://jennatimetracker.com](http://jennatimetracker.com/)

## Requirements:
- [Grails 2.2.4](http://www.grails.org/download) or greater
- MySql 5.x

## Configuration:
#### DB:
 see /jennatimetracker/src/groovy/customconf/CustomDataSource.groovy  
 You can configure user,password and the name of your default schema.  
 ex:  
 		username = 'project_guide'  
		password = 'pguide'  
		url = 'jdbc:mysql://localhost/project_guide'
		
#### Chatboot:
see  /jennatimetracker/src/groovy/customconf/CustomJabberBot.groovy  
You can set the user and password of your chatbot.

Run the app, it will be try to connect and access data. If fails, will be try to generate the contet of data schema.  
