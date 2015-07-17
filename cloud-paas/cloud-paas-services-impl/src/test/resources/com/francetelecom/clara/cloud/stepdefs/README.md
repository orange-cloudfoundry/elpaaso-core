This directory contains executable specifications for the elpaaso product. They play the role of acceptance tests, non regression test and also living documentation.

Useful references:

* [overview of cucumber bindings and cucumber-jvm apidocs/source/example pointers](http://cukes.info/platforms.html)
* [nice cucumber-jvm intro](http://fr.slideshare.net/alan_parkinson/test-automation-with-cucumberjvm)
* [step definition in cucumber-jvm java syntax](http://cukes.info/step-definitions.html#datatables-java)
* [cucumber-jvm IDE support](https://github.com/cucumber/cucumber-jvm/wiki/IDE-support)
    * intellij
        * [intellij cucumber support](http://www.jetbrains.com/idea/webhelp/cucumber.html) + search usage on stepdeds + rename on table headers
        * [screenshots of intellij support](http://blog.jetbrains.com/idea/2012/11/cucumber-for-java-and-groovy-in-intellij-idea-12/)
        * [known issues and pending improvements in intellij](http://youtrack.jetbrains.com/issues/IDEA?q=%23{Cucumber+JVM}+)
        * [discussion forums around cucumber](http://devnet.jetbrains.com/search.jspa?resultTypes=DOCUMENT&resultTypes=MESSAGE&resultTypes=COMMUNITY&resultTypes=TASK&resultTypes=PROJECT&resultTypes=SOCIAL_GROUP&resultTypes=COMMENT&peopleEnabled=false&communityID=2054&q=cucumber)
        * [source code](https://github.com/JetBrains/intellij-plugins/tree/master/cucumber-java)
* [cucumber-jvm jenkins plugin](https://github.com/masterthought/jenkins-cucumber-jvm-reports-plugin-java)
* cucumber ruby resources:
    * [guerkins intro and ruby bindings in cucumber](https://github.com/cucumber/cucumber/wiki/Feature-Introduction)
    * [cucumber dogfooding specs](https://relishapp.com/cucumber/cucumber/docs/background)
    * [nice cucumber workflow tutorial](https://github.com/cucumber/cucumber/wiki/Cucumber-Backgrounder)

* Weird cucumber-spring issues w.r.t; dirty context

<code>
<!--
    don't use the <jdbc:embedded-database id="datasource" type="HSQL"/> syntax as it lack the scope attribute necessary
    for cucumber DirtiesContext, see https://github.com/cucumber/cucumber-jvm/issues/453

    Still use embeded db facilities suplied by spring (this one shuts down by default mem hsql db when datasource bean is destroyed)

    Drawback is that P3Spy drviver is not yet configured
-->
    <bean id="datasource" scope="cucumber-glue" class="org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactoryBean" >
        <property name="databaseType" value="HSQL"/>
    </bean>

<!-- workaround for https://github.com/cucumber/cucumber-jvm/issues/600 -->
    <import resource="classpath:cucumber/runtime/java/spring/cucumber-glue.xml"/>
</code>