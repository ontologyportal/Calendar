Calendar
========

Calendar is a knowledge-based calendar application in Java which uses the power of the [SUMO](http://www.adampease.org/OP/) ontology to represent calendar events.

Note: This application is still in the alpha development stage.

Resolving conflicting events
----------------------------

Each calendar event belongs to a SUMO Process type which not only represents the time and place of the event but also the rules under which two events may conflict.
For example, the calendar may show you attending a meeting in Barcelona, but also attending a birthday party at friend's house. If the calendar knows your friend's house is in Germany,
SUMO has the rules to show that Barcelona and Germany are separate locations and you can't attend events in two separate places at the same time. Using
[assumption-based argumentation](http://www.doc.ic.ac.uk/%7Eft/publications.html), the calendar shows the argument for you being in Barcelona and the argument for
you being at your friend's house and why they contradict. This lets you better see how to resolve the conflicting events.
