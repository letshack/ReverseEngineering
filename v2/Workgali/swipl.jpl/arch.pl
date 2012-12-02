/* Format of the file seems to be this:
  List with single element: "element(xmi:XMI,
  ListWithXmiStuff, ListWithOurStuff" ListWithXmiStuff contains a bunch
  of XMI-specific info, but ListWithOurStuff contains our model.

  The first element in ListWithOurStuff appears to be '\n ' (in other words, a newline).

  The rest of the list consists of "element(something)" followed by another newline,
  then another element, then another newline, and so on.  The first element is
  TopLevelModel (about which
  more later), and the other elements are the aspect-specific
  stereotypes we applied to our model.

  TopLevelModel is itself a list with a single element:
     element(uml:Model,[xmi:id=<TheModelID>,name=UML
     Model],OurModelList)

  OurModelList, in turn, consists of packagedElements and
  profileApplications. The profileApplication points to the AspectJ
  profile used by the app. (Don't know if/when I'll need to add that to
  the architecture code.)

  The basic format of each element w/in uml:Model is this:
  element(<TypeOfElement>,[xmi:type=<UmlType>,xmi:id=<ElementID>,name=<El
ementName>,<other tag=value fields>],[ListOfElementsBelongingToThisElement])

  In each case, the ListOfElementsBelongingToThisElement is either [] or alternates
  between newlines and elements, starting and ending with a newline.

  When we've got a package that follows the Java convention of
  com.example.fred.packageX, "com" is the top level packagedElement
  which in turn owns the "example" packagedElement which in turn owns
  the "fred" packagedElement which in turn owns "packageX" which in turn
  owns the classes and what-not w/in that package.

  The class, in turn, owns generalizations, ownedAttributes, and
  ownedOperations.

  So what will I do with this?

  I think I'll build a list of packages, each of which has a list of
  classes and interfaces, each of which has a list of operations.

  Probably a recursive thing that grabs the three items in the element, and does something
  for each item?
  * First item:  type like xmi:XML, uml:Model, or AspectJ:Aspect
  * Second item:  List of descriptive label=value pairs
  * Third item:  List of more things�either [] or [newline, element, newline, �, element, newline]

  And then each element would be recursively processed somehow.

What do I need?

Rule 1:  Search for methods and comments relevant to the subsystem
Needed:
* element(ownedOperation,[�,name=whatever,�],[�])
Rule 2:  Abstract factory pattern classes are put together
* element(interfaceRealization, [�supplier=theOtherID�], []),
or
* element(generalization, [�,general=theOtherID,�],[])
Rule 3:  Inheritance
* same as rule 2


So what sort of data structures do I need?




  I think I'll start by implementing Rule 1 (looking for specific
  methods).

*/

%use_module(rule2, [rule2/2]).
%use_module(rule2).

buildFileName(Start, Project, End, FileName) :-
   atom_concat(Start, Project, Temp),
   atom_concat(Temp, End, FileName).


load_sgml_term(InXmiFile, OutXmiFile) :-
   % Infile = '/Users/jeff/Documents/UTD/Ex-Desktop/UmlFiles20100627/AjUmlModel.AjHotDraw.20100627.uml',

   % Infile = '/Users/jeff/Documents/UTD/Ex-Desktop/UmlFiles20100627/AjUmlModel.ShapesDemo.20100627.uml',

   % Outfile = '/Users/jeff/Desktop/AjHotDraw.plOut.write.txt',
   % Outfile = '/Users/jeff/Desktop/ShapesDemo.plOut.write.txt',


   Project = 'AjHotDraw',
   % Project = 'ShapesDemo',

%   InfileStart	    = '/Users/jeff/Documents/UTD/Ex-Desktop/UmlFiles20100627/AjUmlModel.',
%   InfileEnd        = '.20100627.uml',
   OutfileStart = '/Users/jeff/Desktop/',
   OutfileEnd   = '.plOut.write.txt',
%   OutXmiStart  = '/Users/jeff/Desktop/',
%   OutXmiEnd    = '.arch.uml',

%   buildFileName(InfileStart, Project, InfileEnd, Infile),
   buildFileName(OutfileStart, Project, OutfileEnd, Outfile),
%   buildFileName(OutXmiStart, Project, OutXmiEnd, OutXmiFile),



   load_sgml_file(InXmiFile, Term),
   open(Outfile, write, Stream),
   report(Stream, Term, 'original Term'),

   rule0(Term, Term0),
   report(Stream, Term0, 'Term after Rule 0'),
%   convert2XMI(Term0, OutTerm),

   rule1(Term0, Term1),
   report(Stream, Term1, 'Term after Rule 1'),
   % TEMP
%   convert2XMI(Term1, OutTerm),
   rule2(Term1, Term2),
   report(Stream, Term2, 'Term after Rule 2'),

   rule2(Term2, Term2a),
   report(Stream, Term2a, 'Term after Rule 2a'),
      
   convert2XMI(Term2a, OutTerm),
% END TEMP
   report(Stream, OutTerm, 'Completed XMI Output'),
   close(Stream),

   open(OutXmiFile, write, XmiStream),
   xml_write(XmiStream, OutTerm, []),
   close(XmiStream).

/*
   Transforms the XMI:
   - Gives the uml:Model element (the element with the normal UML model)
     four package children: unsorted (classes that haven't been plugged
     into the MVC model), model, view, and controller
   - Moves the UML model elements (the former children of the
     uml:Model element) under the unsorted package. The other rules will
     move these classes to the model, view, and controller packages
   */
rule0(OldXmi, NewXmi) :-
	extractXmiTerms(OldXmi, TopTags, ModelTags, ModelKids, AspectElements),
%	% Term is a list with a single xmi:XMI element
%	OldXmi = [element('xmi:XMI', TopTags, Kids)],
%	stripNewLines(Kids, CleanKids),
% CleanKids has a uml:Model element followed by the aspect stereotypes
%	CleanKids = [element('uml:Model', ModelTags,
%	ModelKids)|AspectElements],

	/*
	Thought I'd have to fix references to packages, but packagedElement IDs don't
	  seem to be ref'd anywhere--hooray!
	*/

	  /*
	  Component diagram layout:
	  The children of uml:Model are these packagedElements:
	  - uml:Component (tags:  xmi:id, name, clientDependency="space-separated list of interfaceRealization and required interface IDs")
	  - uml:Interface for each supported and required interface (tags: xml:id and name)
	  - uml:Usage for each required interface (tags: xmi:id supplier="interface ID" client="component ID")

	  Children of uml:Component:  interfaceRealization not packageElement
	  - tags: xml:id, supplier="interface ID", client="component ID", contract="interface ID"

	  For the moment, the original model kids are now kids of the unsorted component.
	  */
	  /*
	NewModel = element('uml:Model', ModelTags,
			  [element(packagedElement, ['xmi:type'='uml:Component', 'xmi:id'='unsorted', 'name'='unsorted'], ModelKids),
			   element(packagedElement, ['xmi:type'='uml:Component', 'xmi:id'='model', 'name'='model'], []),
			   element(packagedElement, ['xmi:type'='uml:Component', 'xmi:id'='view', 'name'='view'], []),
			   element(packagedElement, ['xmi:type'='uml:Component', 'xmi:id'='controller', 'name'='controller'], [])]),
	*/

	/*
	OK for now I'm just taking the ModelKids and dropping them into unsorted.

        But can I go ahead and do some pruning, say by creating a flat list of classes?
	*/
	classesOnly(ModelKids,ModelClasses),

	NewModel = umlModel(ModelTags,
			    [component(unsorted,   [], ModelClasses),
			     component(aspect,     [], []),
			     component(model,      [], []),
			     component(view,       [], []),
			     component(controller, [], [])],
			    []),

	NewXmi = [element('xmi:XMI', TopTags, [NewModel|AspectElements])].

/*
classesOnly(List, NewList)
Given a List of elements (each of which may have kids), returns
a NewList of class elements (plus their kids).
*/
classesOnly(List, NewList) :-
	stripNewLines(List, StripList),
	classesOnlyAcc(StripList, [], NewList).

classesOnlyAcc([], NewList, NewList).

% Don't transfer non-public stuff or its kids
classesOnlyAcc([element(packagedElement, ElementTags, _) | Tail], Acc, NewList) :-
	member('visibility=private', ElementTags),
	!,
	classesOnlyAcc(Tail, Acc, NewList).
classesOnlyAcc([element(packagedElement, ElementTags, _) | Tail], Acc, NewList) :-
	member('visibility=package', ElementTags),
	!,
	classesOnlyAcc(Tail, Acc, NewList).
classesOnlyAcc([element(packagedElement, ElementTags, _) | Tail], Acc, NewList) :-
	member('visibility=protected', ElementTags),
	!,
	classesOnlyAcc(Tail, Acc, NewList).

% Packages aren't transferred to NewList, but we do need to check its
% kids as well as the Tail

classesOnlyAcc([element(packagedElement, ElementTags, ElementKids) | Tail], Acc, NewList) :-
	member('xmi:type'='uml:Package', ElementTags),
	!,
	stripNewLines(ElementKids, ElementKidsStripped),
	classesOnlyAcc(ElementKidsStripped, Acc, TempNewList),
	classesOnlyAcc(Tail, TempNewList, NewList).



% Non-packages are transferred to NewList
classesOnlyAcc([Head | Tail], Acc, NewList) :-
	append(Acc, [Head], NewAcc),
	classesOnlyAcc(Tail, NewAcc, NewList).


/*
New rule scheme
- For each main Component, search unsorted
  - For each match:
    - Remove from unsorted
    - Add interfaceRealization to Component's list
    - Add ID to Component's clientDependency tag
    - Add to new-model-kids list
- Rebuild model kids:
  - unsorted w/ new (smaller) list
  - each Component w/ updated clientDependency list and children lists
  - interfaces (including new ones)
*/

/*
--------------------------------------------------------------
rule1:  
* Aspects go into their own component.  
* Look for operations commonly found in a model, view, or controller.
--------------------------------------------------------------
*/

rule1(OldXmi, NewXmi) :-
	splitTempXmi(OldXmi, TopTags, UmlModelTags, UnsortedKids,
			      ModelClientDep, ModelKids,
			      ViewClientDep, ViewKids,
			      ControllerClientDep, ControllerKids, 
			      UmlModelInterfaces, AspectElements),
%	extractTempTerms(OldXmi, TopTags, UmlModelTags, UmlModelComponents, UmlModelInterfaces, AspectElements),
%	extractComponentTerms(UmlModelComponents,
%			      UnsortedKids,
%			      ModelClientDep, ModelKids,
%			      ViewClientDep, ViewKids,
%			      ControllerClientDep, ControllerKids),

	rule1Aspects(UnsortedKids, AspectElements, UmlModelInterfaces,
		     UnsortedKids0, NewAspectKids, InterfaceList0),

	rule1Component(model, UnsortedKids0, ModelClientDep, ModelKids, InterfaceList0,
		   UnsortedKids1, NewModelClientDep, NewModelKids, InterfaceList1),
	rule1Component(view, UnsortedKids1, ViewClientDep, ViewKids, InterfaceList1,
		  UnsortedKids2, NewViewClientDep, NewViewKids, InterfaceList2),
	rule1Component(controller, UnsortedKids2, ControllerClientDep, ControllerKids, InterfaceList2,
		       UnsortedKids3, NewControllerClientDep, NewControllerKids, InterfaceList3),
% JWK:  Should I include the aspect component in rule 0?  I probably should, although that
% will affect the splits and builds.
	buildTempXmi(NewXmi, TopTags, UmlModelTags, 
		UnsortedKids3, 
		NewAspectKids, 
		NewModelClientDep, NewModelKids, 
		NewViewClientDep, NewViewKids, 
		NewControllerClientDep, NewControllerKids,
		InterfaceList3, 
		AspectElements).
		
%	NewUmlModelComponents = [component(unsorted, [], UnsortedKids3),
%				 component(aspect, [], NewAspectKids),
%				 component(model, NewModelClientDep, NewModelKids),
%				 component(view, NewViewClientDep, NewViewKids),
%				 component(controller, NewControllerClientDep, NewControllerKids)],
%
%	NewUmlModel = umlModel(UmlModelTags, NewUmlModelComponents, InterfaceList3),
%	NewXmi = [element('xmi:XMI', TopTags, [NewUmlModel|AspectElements])].

/*
	We don't use rule1Component for aspects because they're processed a bit differently.
	If a class has an Aspect tag associated with it:
	* The class becomes an Interface w/in the model.
        * A component of the same name is added to the aspect component.
        * The internal component is linked to the Interface via
	  two ports--one on the internal component and one on the
	  aspect component.

        (More accurately, the internal-component port delegates to
	 the aspect-component port, which uses the Interface as its type.)

        Classes that aren't aspects are kept in the Unsorted list.
*/


rule1Aspects(UnsortedKids, AspectElements, InterfaceList,
	     NewUnsortedKids, NewAspectKids, NewInterfaceList) :-
	rule1AspectsAcc(UnsortedKids, AspectElements,
			[], NewUnsortedKids,
			[], NewAspectKids,
		        InterfaceList, NewInterfaceList).

rule1AspectsAcc([], _,
		NewUnsortedKids, NewUnsortedKids,
		NewAspectKids, NewAspectKids,
	        NewInterfaceList, NewInterfaceList).

rule1AspectsAcc([Head|Tail], AspectElements,
		AccUnsortedKids, NewUnsortedKids,
		AccAspectKids, NewAspectKids,
	        AccInterfaceList, NewInterfaceList) :-
	% Current class is tagged as an Aspect
	Head = element(packagedElement, HeadTags, HeadKids),
	member('xmi:id'=HeadId, HeadTags),
	member(element('AspectJ:Aspect', AspectTags, _), AspectElements),
	member('base_Class'=HeadId, AspectTags),
	!,
	% Add class/aspect to InterfaceList as new Interface
	% JWK 3/27:  Added HeadKids here
	member('name'=HeadName, HeadTags),
	append(AccInterfaceList,
	       [element(packagedElement,
			['xmi:type'='uml:Interface',
			 'xmi:id'=HeadId,
			 name=HeadName],
			HeadKids)],
	      NewAccInterfaceList),

	% Add external port for AspectKids
	string_concat('extPort', HeadName, ExtPortId),
	ExtPort = element(ownedAttribute,
			  ['xmi:type'='uml:Port',
			   'xmi:id'=ExtPortId,
			   'name'=ExtPortId,
			   'type'=HeadId],
			  []),

	% Add internal port to be added to internal Component
	string_concat('port', HeadName, IntPortId),
	IntPort = element(ownedAttribute,
			  ['xmi:type'='uml:Port',
			   'xmi:id'=IntPortId,
			   'name'=IntPortId,
			   aggregation=composite],
			  []),

	% Add ownedConnector to connect the internal and external ports
	string_concat('delegate', HeadName, ConnectorId),
	string_concat('end', HeadName, IntEndId),
	string_concat('extEnd', HeadName, ExtEndId),
	Connector = element(ownedConnector,
			    ['xmi:id'=ConnectorId,
			     kind=delegation],
			    [element(end,
				     ['xmi:id'=IntEndId,
				      role=IntPortId],
				     []),
			     element(end,
				     ['xmi:id'=ExtEndId,
				      role=ExtPortId],
				     [])]),

	% Add internal Component (w/ internal port and connector) for AspectKids
	Component = component(HeadName, [], [IntPort, Connector]),

	% Update AspectKids w/ external port and internal Component
	append(AccAspectKids,
	       [ExtPort, Component],
	       NewAccAspectKids),

	% (no change to AccUnsortedKids)
	rule1AspectsAcc(Tail, AspectElements,
		AccUnsortedKids, NewUnsortedKids,
		NewAccAspectKids, NewAspectKids,
	        NewAccInterfaceList, NewInterfaceList).

rule1AspectsAcc([Head|Tail], AspectElements,
		AccUnsortedKids, NewUnsortedKids,
		AccAspectKids, NewAspectKids,
		AccInterfaceList, NewInterfaceList) :-
	% If Head isn't an aspect, add it to AccUnsortedList and process Tail
	append(AccUnsortedKids, [Head], NewAccUnsortedKids),
	rule1AspectsAcc(Tail, AspectElements,
			NewAccUnsortedKids, NewUnsortedKids,
			AccAspectKids, NewAspectKids,
			AccInterfaceList, NewInterfaceList).

	/*

  - For each match:
    - Don't add to NewUnsortedKids
    - Add ID to clientDep
    - Add interfaceRealization to NewModelKids
    - Add to NewInterfaceList

    Using the acc method, we need the four vars representing the final
    state, plus the unproc'd UnsortedKids, plus 4 acc vars.
*/
rule1Component(ComponentType, UnsortedKids, ModelClientDep, ModelKids, InterfaceList,
	   NewUnsortedKids, NewModelClientDep, NewModelKids, NewInterfaceList) :-
	rule1ComponentAcc(ComponentType, UnsortedKids, [], ModelClientDep, ModelKids, InterfaceList,
		      NewUnsortedKids, NewModelClientDep, NewModelKids, NewInterfaceList).

rule1ComponentAcc(_, [],NewUnsortedKids, NewClientDep, NewKids, NewInterfaceList,
	      NewUnsortedKids, NewClientDep, NewKids, NewInterfaceList).
rule1ComponentAcc(ComponentType, [Head|Tail], AccUnsortedKids, AccClientDep, AccKids, AccInterfaceList,
	      NewUnsortedKids, NewClientDep, NewKids, NewInterfaceList) :-
	rule1ComponentMatch(ComponentType, Head),
	!,
	addElementToComponent(Head, ComponentType, AccKids, AccInterfaceList, NewAccKids, NewAccInterfaceList),
	
/*
	% start
	Head = element(packagedElement, HeadTags, HeadKids),
	member('xmi:id'=HeadId, HeadTags),
	member(name=HeadName, HeadTags),
	% don't update AccUnsortedKids
	% Add ID to client dep
	% JWK 4/3:  Apparently we shouldn't put stuff in ClientDep after all...
	% append(AccClientDep, [HeadId], NewAccClientDep),
	% Add interfaceRealization to Kids
	string_concat(ComponentType, HeadId, IRID),
	append(AccKids,
	       [element(interfaceRealization,
		       ['xmi:id'=IRID,
			supplier=HeadId,
			client=ComponentType,
			contract=HeadId],
		       [])],
	       NewAccKids),
	% Add interface to interface list
	% JWK 3/27:  Add HeadKids here
	% JWK 3/30:  add 'visibility=public' to eliminate IllegalValueExceptions
	append(AccInterfaceList,
	       [element(packagedElement,
			['xmi:type'='uml:Interface',
			 'xmi:id'=HeadId,
			 visibility=public,			 
			 name=HeadName],
			HeadKids)],
	      NewAccInterfaceList),
	% end
	*/
%	rule1ComponentAcc(ComponentType, Tail, AccUnsortedKids, NewAccClientDep, NewAccKids, NewAccInterfaceList,
%		      NewUnsortedKids, NewClientDep, NewKids, NewInterfaceList).
	rule1ComponentAcc(ComponentType, Tail, AccUnsortedKids, AccClientDep, NewAccKids, NewAccInterfaceList,
		      NewUnsortedKids, NewClientDep, NewKids, NewInterfaceList).

% If no match, add to AccUnsortedKids but not the others
rule1ComponentAcc(ComponentType, [Head|Tail], AccUnsortedKids, AccClientDep, AccKids, AccInterfaceList,
	      NewUnsortedKids, NewClientDep, NewKids, NewInterfaceList) :-
	append(AccUnsortedKids, [Head], NewAccUnsortedKids),
	rule1ComponentAcc(ComponentType, Tail, NewAccUnsortedKids, AccClientDep, AccKids, AccInterfaceList,
		      NewUnsortedKids, NewClientDep, NewKids, NewInterfaceList).


% Rule 1: matching ownedOperation name w/ visibility=public
rule1ComponentMatch(ComponentType, element(packagedElement, _, ElementKids)) :-
	member(element(ownedOperation, OperationTags, _), ElementKids),
	member(visibility=public, OperationTags),
	member(name=OperationName, OperationTags),
	rule1operation(ComponentType, OperationName), !.

rule1operation(view, setCursor).
rule1operation(view, addMouseListener).
rule1operation(view, addKeyListener).
rule1operation(model, getDrawingArea).
rule1operation(model, locate).
rule1operation(model, isConnectable).
rule1operation(model, findConnector).
rule1operation(model, addNotify).
rule1operation(model, removeNotify).
rule1operation(controller, keyTyped).
rule1operation(controller, keyPressed).
rule1operation(controller, keyReleased).
% JWK next 1 aded 5/19/11
rule1operation(controller, keyDown).
rule1operation(controller, mousePressed).
rule1operation(controller, mouseClicked).
rule1operation(controller, mouseMoved).
rule1operation(controller, updateCursor).
% JWK next 4 added 5/19/11
rule1operation(controller, mouseMove).
rule1operation(controller, mouseDown).
rule1operation(controller, mouseDrag).
rule1operation(controller, mouseUp).

/*
--------------------------------------------------------------
Rule 2:  Subclasses go to the same component as its superclass
--------------------------------------------------------------
*/

rule2(OldXmi, NewXmi) :-
	splitTempXmi(OldXmi, TopTags, UmlModelTags, UnsortedKids0,
			      AspectKids,
			      ModelClientDep, ModelKids,
			      ViewClientDep, ViewKids,
			      ControllerClientDep, ControllerKids, 
			      InterfaceList0, AspectElements),

	rule2Component(model, UnsortedKids0, ModelClientDep, ModelKids, InterfaceList0,
		   UnsortedKids1, NewModelClientDep, NewModelKids, InterfaceList1),
	rule2Component(view, UnsortedKids1, ViewClientDep, ViewKids, InterfaceList1,
		  UnsortedKids2, NewViewClientDep, NewViewKids, InterfaceList2),
	rule2Component(controller, UnsortedKids2, ControllerClientDep, ControllerKids, InterfaceList2,
		       UnsortedKids3, NewControllerClientDep, NewControllerKids, InterfaceList3),
	buildTempXmi(NewXmi, TopTags, UmlModelTags, 
		UnsortedKids3, 
		AspectKids, 
		NewModelClientDep, NewModelKids, 
		NewViewClientDep, NewViewKids, 
		NewControllerClientDep, NewControllerKids,
		InterfaceList3, 
		AspectElements).
		

rule2Component(ComponentType, UnsortedKids, ModelClientDep, ModelKids, InterfaceList,
	   NewUnsortedKids, NewModelClientDep, NewModelKids, NewInterfaceList) :-
	rule2ComponentAcc(ComponentType, UnsortedKids, [], ModelClientDep, ModelKids, InterfaceList,
		      NewUnsortedKids, NewModelClientDep, NewModelKids, NewInterfaceList).
		      

rule2ComponentAcc(_, [],NewUnsortedKids, NewClientDep, NewKids, NewInterfaceList,
	      NewUnsortedKids, NewClientDep, NewKids, NewInterfaceList).
rule2ComponentAcc(ComponentType, [Head|Tail], AccUnsortedKids, AccClientDep, AccKids, AccInterfaceList,
	      NewUnsortedKids, NewClientDep, NewKids, NewInterfaceList) :-
	rule2ComponentMatch(Head, AccKids, ComponentType),
	!,
	addElementToComponent(Head, ComponentType, AccKids, AccInterfaceList, NewAccKids, NewAccInterfaceList),
	rule2ComponentAcc(ComponentType, Tail, AccUnsortedKids, AccClientDep, NewAccKids, NewAccInterfaceList,
		      NewUnsortedKids, NewClientDep, NewKids, NewInterfaceList).

% If no match, add to AccUnsortedKids but not the others
rule2ComponentAcc(ComponentType, [Head|Tail], AccUnsortedKids, AccClientDep, AccKids, AccInterfaceList,
	      NewUnsortedKids, NewClientDep, NewKids, NewInterfaceList) :-
	append(AccUnsortedKids, [Head], NewAccUnsortedKids),
	rule2ComponentAcc(ComponentType, Tail, NewAccUnsortedKids, AccClientDep, AccKids, AccInterfaceList,
		      NewUnsortedKids, NewClientDep, NewKids, NewInterfaceList).
		     

% Match if the Element is a subclass of an element in ComponentKids
rule2ComponentMatch(Element, ComponentKids, ComponentType) :-
% BOOKMARK
	Element = element(packagedElement, _, ElementKids),
	member(element(generalization, GenTags, _), ElementKids),
	member(general=SuperClassId, GenTags),
	
	member(element(interfaceRealization, SuperClassTags, _), ComponentKids),
	member(contract=SuperClassId, SuperClassTags).	
	


/*
XMI conversion rules
*/

convert2XMI(Working, Xmi) :-
	accXmi(Working, [], Xmi).

accXmi([], Xmi, Xmi).
%element(packagedElement, ListOfTags, ListOfKids)
%with
%component(ComponentName, SpaceSeparatedClientDependencies, ListOfKids)
accXmi([component(ComponentName, ClientDependencies, ListOfKids)|Tail], Acc, Xmi) :-
	!, XmiTags = ['xmi:type'='uml:Component', 'xmi:id'=ComponentName, name=ComponentName,
		   clientDependency=ClientDependencies],
	convert2XMI(ListOfKids, XmiListOfKids),
	append(Acc,
	       [element(packagedElement, XmiTags, XmiListOfKids)],
	       NewAcc),
	accXmi(Tail, NewAcc, Xmi).

%element('uml:Model', ListOfModelTags, ListOfComponentsAndInterfaces)
%with:
%umlModel(ListOfModelTags, ListOfComponents, ListOfInterfaces)
accXmi([umlModel(ListOfModelTags, ListOfComponents, ListOfInterfaces)|Tail], Acc, Xmi) :-
	!, append(ListOfComponents, ListOfInterfaces, ListOfKids),
	convert2XMI(ListOfKids, XmiListOfKids),
	append(Acc,
	       [element('uml:Model', ListOfModelTags, XmiListOfKids)],
	       NewAcc),
	accXmi(Tail, NewAcc, Xmi).

% Regular elements: Kids may need to be converted
accXmi([element(ElementType, ListOfTags, ListOfKids)|Tail], Acc, Xmi) :-
	!, convert2XMI(ListOfKids, XmiListOfKids),
	append(Acc,
	       [element(ElementType, ListOfTags, XmiListOfKids)],
	       NewAcc),
	accXmi(Tail, NewAcc, Xmi).


accXmi([Head|Tail], Acc, Xmi) :-
	append(Acc, [Head], NewAcc),
	accXmi(Tail, NewAcc, Xmi).


/*
Tool that extracts the following information from an Xmi term: (real XMI,
not the temporary format we use when working the other rules)
- The attributes of the xmi:XMI element (TopTags)
- The attributes of the uml:Model element (ModelTags)
- The children of the uml:Model element (ModelKids)
- Other xmi:XMI children, usually aspect stereotypes (AspectElements)

This is used by Rule 0 because it still has the original XMI, not
the temporary format used by Rule 1+

*/
extractXmiTerms(Xmi, TopTags, ModelTags, ModelKids, AspectElements) :-
	% Term is a list with a single xmi:XMI element
	Xmi = [element('xmi:XMI', TopTags, Kids)],
	stripNewLines(Kids, CleanKids),
	% CleanKids has a uml:Model element followed by the aspect stereotypes
	CleanKids = [element('uml:Model', ModelTags, ModelKids)|AspectElements].
	

/*
--------------------------------------------------------------
Split the temporary XMI into its components.  Used by Rule 1+.
--------------------------------------------------------------
*/
% This version (without an Aspect component) is used by Rule 1.
splitTempXmi(Xmi, TopTags, UmlModelTags, UnsortedKids,
		      ModelClientDep, ModelKids,
		      ViewClientDep, ViewKids,
		      ControllerClientDep, ControllerKids, 
		      UmlModelInterfaces, AspectElements) :-

	extractTempTerms(Xmi, TopTags, UmlModelTags, UmlModelComponents, UmlModelInterfaces, AspectElements),
	extractComponentTerms(UmlModelComponents,
			      UnsortedKids,
			      ModelClientDep, ModelKids,
			      ViewClientDep, ViewKids,
			      ControllerClientDep, ControllerKids).

% This version (with Aspect component) is used by Rule 2
splitTempXmi(Xmi, TopTags, UmlModelTags, UnsortedKids,
              AspectKids,
		      ModelClientDep, ModelKids,
		      ViewClientDep, ViewKids,
		      ControllerClientDep, ControllerKids, 
		      UmlModelInterfaces, AspectElements) :-

	extractTempTerms(Xmi, TopTags, UmlModelTags, UmlModelComponents, UmlModelInterfaces, AspectElements),
	extractComponentTerms(UmlModelComponents,
			      UnsortedKids,
			      AspectKids,
			      ModelClientDep, ModelKids,
			      ViewClientDep, ViewKids,
			      ControllerClientDep, ControllerKids).


extractTempTerms(Xmi, TopTags, ModelTags, ModelComponents, ModelInterfaces, AspectElements) :-
	% Term is a list with a single xmi:XMI element
	Xmi = [element('xmi:XMI', TopTags, Kids)],
	stripNewLines(Kids, CleanKids),
	% CleanKids has a uml:Model element followed by the aspect stereotypes
	CleanKids = [umlModel(ModelTags, ModelComponents, ModelInterfaces)|AspectElements].

% Extracts the necessary component parms (no Aspect)
extractComponentTerms(UmlModelComponents,
		      UnsortedKids,
		      ModelClientDep, ModelKids,
		      ViewClientDep, ViewKids,
		      ControllerClientDep, ControllerKids) :-
	member(component(unsorted, _, UnsortedKids), UmlModelComponents),
	member(component(model, ModelClientDep, ModelKids), UmlModelComponents),
	member(component(view, ViewClientDep, ViewKids), UmlModelComponents),
	member(component(controller, ControllerClientDep, ControllerKids), UmlModelComponents).

% Extracts necessary component parms (including Aspect)
extractComponentTerms(UmlModelComponents, 
		      UnsortedKids,
		      AspectKids,
		      ModelClientDep, ModelKids,
		      ViewClientDep, ViewKids,
		      ControllerClientDep, ControllerKids) :-
	member(component(aspect, _, AspectKids), UmlModelComponents),
	extractComponentTerms(UmlModelComponents,
		      UnsortedKids,
		      ModelClientDep, ModelKids,
		      ViewClientDep, ViewKids,
		      ControllerClientDep, ControllerKids).
/*
--------------------------------------------------------------
Add an element to one of the components.  Used by Rule1+.
--------------------------------------------------------------
*/
addElementToComponent(NewElement, ComponentType, Kids, InterfaceList, NewKids, NewInterfaceList) :-

	NewElement = element(packagedElement, NewElementTags, NewElementKids),
	member('xmi:id'=NewElementId, NewElementTags),
	member(name=NewElementName, NewElementTags),
	% don't update AccUnsortedKids
	% Add ID to client dep
	% JWK 4/3:  Apparently we shouldn't put stuff in ClientDep after all...
	% append(AccClientDep, [HeadId], NewAccClientDep),
	% Add interfaceRealization to Kids
	string_concat(ComponentType, NewElementId, IRID),
	append(Kids,
	       [element(interfaceRealization,
		       ['xmi:id'=IRID,
			supplier=NewElementId,
			client=ComponentType,
			contract=NewElementId],
		       [])],
	       NewKids),
	% Add interface to interface list
	% JWK 3/27:  Add HeadKids here
	% JWK 3/30:  add 'visibility=public' to eliminate IllegalValueExceptions
	append(InterfaceList,
	       [element(packagedElement,
			['xmi:type'='uml:Interface',
			 'xmi:id'=NewElementId,
			 visibility=public,			 
			 name=NewElementName],
			NewElementKids)],
	      NewInterfaceList).




/*
--------------------------------------------------------------
Builds the temporary XMI at the end of each rule.  Used by Rule 1+.
--------------------------------------------------------------
*/
buildTempXmi(NewXmi, TopTags, UmlModelTags, 
		UnsortedKids, 
		AspectKids, 
		ModelClientDep, ModelKids, 
		ViewClientDep, ViewKids, 
		ControllerClientDep, ControllerKids,
		InterfaceList, 
		AspectElements) :-
		
	NewUmlModelComponents = [component(unsorted, [], UnsortedKids),
				 component(aspect, [], AspectKids),
				 component(model, ModelClientDep, ModelKids),
				 component(view, ViewClientDep, ViewKids),
				 component(controller, ControllerClientDep, ControllerKids)],

	NewUmlModel = umlModel(UmlModelTags, NewUmlModelComponents, InterfaceList),
	NewXmi = [element('xmi:XMI', TopTags, [NewUmlModel|AspectElements])].
	


% stripNewLines removes the newline chars from a list
stripNewLines([],[]).
stripNewLines([Head|Rest], NewList) :-
	isNewline(Head),
	!,
	stripNewLines(Rest, NewList).
stripNewLines([Head|Rest], NewList) :-
	stripNewLines(Rest, NewRest),
	NewList = [Head|NewRest].

% X:  XMI terms loaded by load_sgml_term(X).
% L:  List of packages within X.
/*
build_element_list(X,L, Stream) :-
	% drill down into X until we find our list
	X = [element(_,_,ListWithOurStuff)],
	ListWithOurStuff = [_,TopLevelModel | _],
	TopLevelModel = [element(_,_,OurModelList)],
	write(Stream,'OurModelList:'), nl(Stream),
	write(Stream,OurModelList),nl(Stream).
*/

%report(_,_, _) :-
report(Stream,Term, Description) :-
	atom_concat('start of ',Description, StartDesc),
	atom_concat('end of ',Description, EndDesc),
	write(Stream,StartDesc), nl(Stream),
	write(Stream,Term), nl(Stream),
       	write(Stream,EndDesc), nl(Stream).
%       	true.

splitElement(Element, First, Second, Third) :-
	Element = element(First, Second, Third).

/*
	I think I need:
	* one or more clauses to parse an element, and
	* several clauses to process the third-parm list.

	For now, I think parsing an element requires:
	* Remembering the first parm (the type),
	* Extracting the element name and ID from the second-parm list, and
	* Processing the third-parm list.

	If the third-parm list is null, return what you've got.
        If the third-parm list starts w/ something other than
	an element, skip it and process the tail.
	If the third-parm list starts w/ an element, parse it.

	*/

parseElement(Stream, Element) :-
	Element = element(First, Second, Third),
	write(Stream, First), nl(Stream),
	write(Stream, Second), nl(Stream),
	processList(Stream, Third).

processList(Stream, []) :-
	write(Stream,'empty list'), nl(Stream).

processList(Stream, [Head|Tail]) :-
	% I need something here that matches newline followed by X blanks
	isNewline(Head),
	processList(Stream, Tail).

processList(Stream, [Head|Tail]) :-
	Head=element(_,_,_),
	parseElement(Stream, Head),
	processList(Stream, Tail).

/*
	True if the atom includes newline.
*/
isNewline(Term) :-
	atom(Term),
	sub_atom(Term, _, _, _, '\n').


reportFirstSecondThird(Stream, MainDesc, First, Second, Third) :-
	atom_concat('First - ',MainDesc, FirstDesc),
	atom_concat('Second - ',MainDesc, SecondDesc),
	atom_concat('Third - ',MainDesc, ThirdDesc),
	report(Stream, First, FirstDesc),
	report(Stream, Second, SecondDesc),
	report(Stream, Third, ThirdDesc).
