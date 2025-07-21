How to run
Go to the project's root prototype/
On a terminal run the following commands:
    "mvn clean install -U"
    "mvn spring-boot:run"
You will now have access to the application acessing localhost:8080 on the browser
You have 3 starting points:
    localhost:8080/home.html where you can browser products
    localhost:8080/signup.html where you can create new useres
    localhost:8080/login.html where you can start a user session

To generate java documentation, run:
    mvn javadoc:javadoc
    That will geneate the documentation under prototype/target/site/apidocs/
    You can access it on your browser starting at prototype/target/site/apidocs/index.html