package finproject;

import finproject.Exceptions.*;

import java.util.Random;

public class Graph implements Runnable {
	private int length = 0;
	Village firstVillage;
	Village lastVillage;
	private String name;
	
	
	public String getName() {return this.name;}
	
	//constructor
	public Graph() {
		firstVillage = null;
		lastVillage = null;
	} 
	
	public Graph(String name) {
		firstVillage = null;
		lastVillage = null;
		this.name = name;
	} 
	
	public boolean isEmpty() {return length == 0;}
	public int getLength() {return length;}
	public Village getFirst() {return this.firstVillage;}
	public Village getLast() {return this.lastVillage;}
	
	public void run() { // run method for Graph		
		// stops after 5 changes
		int runCount = 0;
		while (! isEmpty() && runCount<5) {
			Random rand2 = new Random(); int nextFunction = rand2.nextInt(4);
			// 3/4 chance the government will add a new road, 1/4 chance it will restructure graph (delete road not in min span tree)
			if (nextFunction < 2) {	// chooses random villages within graph
				Random rand = new Random();
				int v = rand.nextInt(length), count=0;
				Village vill = getFirst();
				for (int i=0; i<length; i++) {
					if (count != v) {vill = vill.getNext(); count++; }
				}
				
				Village vill2 = getFirst();
				for (int i=0; i<length; i++) {
					if (count != v) {vill2 = vill2.getNext(); count++;}
				}
				
				if (!vill.equals(vill2)) {roadProposal(vill, vill2);} // if it happens upon the same village, just waits one cycle
			} else {restructure();}
			runCount++;
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				System.out.println("The system was interrupted.");
			}
		}
	} // end of run()
	
	public synchronized void restructure() {
		// finds and deletes one road that is not in the minimum spanning tree
		try {
			if (! isEmpty()) {
				Road [] minSpanTree = this.getMinSpanTree();
				Road [] allRoads = this.getAllRoads();
				Road toDelete = null;
				boolean finished = false;
				
				while (!finished) {
					for (int i=0; i<allRoads.length; i++) {
						if (!inSpanTree(minSpanTree, allRoads[i])) {toDelete = allRoads[i]; finished = true;}
					}
					finished = true;
				}
				
				if (toDelete != null) {  // toDelete will be null if no road is not in the minimum spanning tree
						toDelete.start.deleteOutRoad(toDelete);
						toDelete.end.deleteInRoad(toDelete);
						
						System.out.println("\nTo cut costs, the government has decided to restructure the road network." +
						toDelete.printRoad() + " has been deleted.\n");}
			}
		} catch (NotFoundException | GraphEmptyException | SameVillageException
				| RoadAlreadyExistsException e) {
			System.out.println(e.getMessage());
		}
	} // end of restructure()
	
	public boolean inSpanTree(Road [] minSpanTree, Road r) { // checks if a road is in the minimum spanning tree
		boolean inSpanTree = false;
		boolean finished = false;
		
		while (!finished) {
			for (int i=0; i<minSpanTree.length; i++) {
				if (r.start.getName() == minSpanTree[i].start.getName() && r.end.getName() == minSpanTree[i].end.getName()) {
					inSpanTree=true; finished=true;
				}
			} finished = true;
		}
		return inSpanTree;
	} // end of inSpanTree()
	
	public synchronized Road [] getAllRoads() { // represents all roads in graph as an array
		Road [] allRoadsLong = new Road [100];
		int numRoads = 0;
		if (! isEmpty()) {
			Village current = this.firstVillage;
			while (current != null) {
				if (!current.outgoing.isEmpty()) {
					RoadIterator ri = current.outgoing.getFirst();
					while (ri != null) {
						allRoadsLong[numRoads] = ri.getData();
						numRoads++;
						ri = ri.getNext();
					}
				}
				current = current.getNext();
			}
		}
		
		// makes new array of correct length
		Road [] allRoads = new Road [numRoads];
		for (int i=0; i<allRoads.length; i++) {
			allRoads[i] = allRoadsLong[i];
		}
		return allRoads;
	} // end of getAllRoads()
	
	public void startGnomeThreads() { // starts threads of all gnomes in the country
		if (! isEmpty()) {
			Village current = firstVillage;
			while (current != null) {
				for(int i=0; i<current.populationSize; i++) {
					Thread gnomeThread = new Thread(current.population[i]);
					gnomeThread.start();
				}
				current = current.getNext();
			}
		}
	} // end of startGnomeThreads()
			
	public synchronized boolean roadExists(Village v1, Village v2) {
		// checks if road already exists between two villages
		boolean roadExists = false, finished=false;
		if (!v1.outgoing.isEmpty()) {
			RoadIterator current = v1.outgoing.getFirst();
			while (current!=null && !finished) {
				if (current.getData().end.equals(v2)) {roadExists = true; finished=true;}
				else {current = current.getNext();}	
			}
		}
		return roadExists;
	} // end of roadExists()
	
	public synchronized void roadProposal (Village v1, Village v2) {
		try {
			// govt builds road if start road has no outgoing roads or 
			// end road has no incoming roads or
			// there is more than one intermediary village between the villages
			Random rand = new Random(); int newToll = 1+rand.nextInt(5);
			System.out.println("\nNew road proposed between village " + v1.getName() + " and village " + v2.getName());
			System.out.println("The total building cost would be: " + newToll*100);			
			
			if (roadExists(v1, v2)) {throw new RoadAlreadyExistsException();}
			if (v1.equals(v2)) {throw new SameVillageException();}
			
			boolean noOutgoing = (v1.outdegree == 0);
			boolean noIncoming = (v2.indegree == 0);
			
			if (noOutgoing) {
				System.out.println("The government has elected to build the road because village " + v1.getName() + " has no outgoing roads.\n");
				v1.connect(newToll, v2);
				return;
			}
			
			if (noIncoming) {
				System.out.println("The government has elected to build the road because village " + v2.getName() + " has no incoming roads.\n");
				v1.connect(newToll, v2);
				return;
			}
			
			boolean manyIntermediates = false;
			
			int count = 0; // counts number of spaces in travelMinExpPath string, 
						   // where 2 is a direct path, 3 is one intermediate village
			String minusStart = travelMinExpPath(v1, v2);
			while(minusStart.length()>0) {
				int spaceInd = minusStart.indexOf(" ");
				minusStart = minusStart.substring(spaceInd+1);
				count++;
			}
			if (count > 3) {manyIntermediates=true;}
			
			if (manyIntermediates) {
				System.out.println("The government has elected to build the road because there is" +
						"\nmore than one intermediate village between the two villages in the existing road structure.");
				v1.connect(newToll, v2);
			} else {
				System.out.println("Due to budget constraints, the government has elected not to build the road.\n");
			}
		} catch (SameVillageException | RoadAlreadyExistsException e) {
			System.out.println("The government says, " + e.getMessage() + "\n");
		} catch (NoIncomingRoadsException e) {  // not possible with return statement
		} catch (NoOutgoingRoadsException e) { } // not possible with return statement
	} // end of roadProposal()
	
	public synchronized void insert (Village newVillage) {
		if (isEmpty()) {
			firstVillage = newVillage;
			lastVillage = newVillage;
		}
		else {
			lastVillage.setNext(newVillage);
			newVillage.setPrev(lastVillage);
			lastVillage = newVillage;
		}
		length++;
	}
	
	public synchronized void delete(int name) throws GraphEmptyException, NotFoundException {
		if (isEmpty()) {throw new GraphEmptyException();}
		else if (getLength() == 1) {
			this.firstVillage = null;
			this.lastVillage = null; }
		else {
			Village found = find(name);
			if (found.equals(firstVillage)) {
				found.getNext().setPrev(null);
				firstVillage = found.getNext();}
			else if (found.equals(lastVillage)) {
				lastVillage = found.getPrev(); 
				lastVillage.setNext(null);}
			else {
				found.getPrev().setNext(found.getNext());
				found.getNext().setPrev(found.getPrev());
			}}
		this.length--;
	} // end of delete method
	
	public Village find(int name) throws NotFoundException, GraphEmptyException {
		// exceptions caught by MapGUI so pop-up error message can be generated
		if (! isEmpty()) {
			Village current = this.firstVillage;
			while (current != null) {					
				if (current.getName() == name) {return current;}
				current = current.getNext();
			} throw new NotFoundException();
		} else {throw new GraphEmptyException();}
	}
	
	public String travelMinExpPath(Village starting, Village end) throws NoIncomingRoadsException, NoOutgoingRoadsException, SameVillageException {
		if (starting.getName() == end.getName()) {throw new SameVillageException();}
		if (starting.outgoing.length == 0) {throw new NoOutgoingRoadsException();}
		if (end.incoming.length == 0) {throw new NoIncomingRoadsException();}

		boolean done = false;
		PriorityQ pq = new PriorityQ();
			
		pq.insert(new Node(starting, 0, null));
		while (!done && !pq.isEmpty()) {
			Node frontEntry = pq.remove();
			Village frontVertex = frontEntry.getVillage();
			if (!frontVertex.visited) {
				frontVertex.visited = true;
				
				if (frontEntry.getVillage() == starting) {
					frontEntry.pathCost = 0;
					frontVertex.prior = null;
					frontVertex.priorCost = 0;
				} else {
					frontVertex.prior = frontEntry.predecessor;
					frontVertex.priorCost = frontEntry.pathCost;
				}
				
				if (frontVertex.equals(end)) {
					done = true;
				} else {
					RoadIterator nextNeighbor = frontVertex.outgoing.firstRoad;
					if (nextNeighbor != null) {
						while (nextNeighbor != null) {
						//	System.out.println(frontEntry.getVillage().prior==null);
							//System.out.println("looking at nextNeighbor"+nextNeighbor.endVillage().getName()+" and "+(frontVertex.prior).prior.getName());
							int weightOfEdgeToNeighbor = nextNeighbor.getCost();
							if(!nextNeighbor.endVillage().visited){
								int nextCost = weightOfEdgeToNeighbor + frontEntry.pathCost;
								pq.insert(new Node(nextNeighbor.endVillage(),nextCost,frontEntry.getVillage()));
							}
							nextNeighbor=nextNeighbor.getNext();
						}
					}
				}
			}
		}
		
		Queue path = new Queue();
		String thePath = "";
		if(end.prior!=null){ // for if they don't connect even though they both have ins and outs
			path.insert(new Node(end));
			thePath += path.getLast().getVillage().getName()+" ";
		}
		Village toAdd = end.prior;
		while (toAdd != null) {
			path.insert(new Node(toAdd));
			thePath += path.getLast().getVillage().getName()+" ";
			toAdd = toAdd.prior;
		}
		System.out.println("Shortest path (from end to start) is: " + thePath);
		return thePath;
	} // end of travelMinExpPath method
	
	public Queue DepthFirstSearch(Village v,Queue white, Queue gray, Queue black) throws NotFoundException, GraphEmptyException { // perform over the entire graph
		v.color = "gray";
		System.out.println("the outgoinglength for village "+v.getName()+" is "+v.outgoing.length);
		if(v.outgoing.length!=0){
			RoadIterator ri = v.outgoing.firstRoad;
			while(ri!=null){
				Village successor = ri.endVillage();
				successor.edgeType = "tree";
				successor.predecess = ri.startVillage();
				// ri.startVillage().outgoingTopSort.insert(new Road("tree",null,successor));
				System.out.println("successor to village "+v.getName()+" is "+successor.getName());
				if(successor.color.equals("white")){
					// remove from white q and add to gray q.....white.find(successor.getName());
					System.out.println("inside if statement. successor name should still be "+successor.getName());
					System.out.println("going to delete from white q village "+white.find(successor.getName()).getName());
					Village addToGray = white.delete(white.find(successor.getName())).getVillage();
					System.out.println("added to gray is "+addToGray.getName());
					
					gray.insert(new Node(addToGray));
					//black.printGraph();
					DepthFirstSearch(successor,white,gray,black);
				}else if(successor.color.equals("black")){
					/*if(!successor.color.equals("black")){
						System.out.println("successor "+successor.getName()+"'s color is not black yet.");
						Village addToBlack = gray.delete(gray.find(successor.getName())).getVillage();
						black.insert(new Node(addToBlack));
						System.out.println("added to black q is "+black.getLast().getVillage().getName());
						addToBlack.color = "black";
					}else{*/
						System.out.println(successor.getName()+" has already been added to black");
					//}
				}else if(successor.color.equals("gray")){
					successor.edgeType = "back"; // means there's a cycle
				}
				ri = ri.getNext();
			}
		}
		if(!v.color.equals("black")){
			System.out.println("adding to black is village "+v.getName());
			gray.delete(gray.find(v.getName())).getVillage();
			black.insertLikeStack(new Node(v));
			v.color = "black";
			System.out.println("black.length is "+black.length());
			System.out.println("last thing in black q is "+black.getLast().getVillage().getName());
		} else{
			System.out.println(v.getName()+" is already added to black");
		}
		//black.printGraph();
		return black;
	}
	
	public String topologicalSort() throws NotFoundException, GraphEmptyException{
		Queue white = new Queue();
		Village addToWhite = this.firstVillage;
		int counter = 0;
		while(addToWhite!=null){ // adding all villages from the graph to the white queue, meaning they haven't been visited yet
			white.insert(new Node(addToWhite));
			addToWhite.color = "white";
			System.out.println("added to white, village "+addToWhite.getName());
			addToWhite = addToWhite.getNext();
		}
		Queue gray = new Queue(); // when they are to be visited
		Queue black = new Queue(); // when they have been dealt with
		Village addToGray = white.remove().getVillage();
		System.out.println("addToGray is "+addToGray.getName());
		gray.insert(new Node(addToGray));
		black = DepthFirstSearch(addToGray,white,gray,black);
		counter += black.length();
		while(counter!=this.length){
			System.out.println("                 ");
			Village check = firstVillage;
			//Village toTopSortAgain;
			Queue newWhite = new Queue();
			Queue newGray = new Queue();
			while(check!=null){
				if(black.find2(check.getName())==null){
					newWhite.insert(new Node(check));
				}
				check = check.getNext();
			}
			Village newAddToWhite = newWhite.remove().getVillage();
			newAddToWhite.color = "white";
			newGray.insert(new Node(newAddToWhite));
			black = DepthFirstSearch(newAddToWhite,newWhite,newGray,black);
			counter = black.length();
			System.out.println("counter is ..."+counter);
		}
		System.out.println("counter is "+counter);
		Node first = black.getFirst();
		boolean cycles = false;
		Village createdCycle = null;
		while(first!=null){
			//System.out.println("name is "+first.getVillage().getName()+" and prior village is "+first.getVillage().predecess.getName()+" with edgetype "+first.getVillage().edgeType);
			if(first.getVillage().edgeType.equals("back")){
				cycles = true;
				createdCycle = first.getVillage();
			}
			first = first.getNext();
		}
		// if there are cycles
		// start with the first thing that topSort gives you and keep traversing until you 
		String path = "";
		// start with first thing in list and check if == causeCycle, if not, add string with a " "
		// ie 1 is the causeCycle
		if(cycles){
			Node lookAt = black.getFirst();
			while(lookAt!=null){
				System.out.println("createdCycle is "+createdCycle.getName());
				if(lookAt.getVillage() == createdCycle){ // if createdCycle
					while(lookAt.getVillage()!=createdCycle.predecess){
						path += lookAt.getVillage().getName() + ",";
						lookAt = lookAt.getNext();
					}
					path += lookAt.getVillage().getName()+" ";
					//lookAt = lookAt.getNext();
				} else{ // not the thing that caused the cycle
					path += lookAt.getVillage().getName() + " ";
				}
				lookAt = lookAt.getNext();
			}
		}else{ // no cycle
			Node lookAt = black.getFirst();
			while(lookAt!=null){
				path += lookAt.getVillage().getName() + " ";
				lookAt = lookAt.getNext();
			}
			// print out as a string in order
		}
		System.out.println(path);
		return path;
		/*
		 * started with beginning of topSort
		 * firstVillage....look at its AdjList topSortIncoming... roadIterator containing edge type and end village
		 * look at its first roadIterator/incoming with and look at its edge type
		 *
		 * turns black if no successors
		 * have a white set of all the villages, an empty gray and empty black set and have a color value
		 * take the first thing in the white set, remove it and put it in the gray set
		 * look at first thing in gray set and look at it's first child and check it's status ie white or black (if any, otherwise remove and send to black), checking if anything is null
		 * if child is white, add to the gray 
		 * look at thing just added (if added to stack, then the last thing), and 
		 * stop when you don't children, look at first thing in gray thing and get child, call function with child as input if there is a child
		 * get first thing in white set and put in gray set
		 * look at first thing in gray set
		 * get child if there is one
		 * call on method with child as input
		 * child is put into gray set
		 */
		// if not all the nodes were gotten.
	}
	
	
	public Queue queueZero(Queue someQ, int num) throws NotFoundException, GraphEmptyException{
		Village vill = firstVillage;
		while(vill != null){
			if(vill.incoming.length == num){
				someQ.insert(new Node(vill));
			}
			vill = vill.getNext();
		}
		System.out.println("someQ length is"+ someQ.length());
		return someQ;
	}	// end of queueZero method
	
	public String printGraph() { // returns string representation of graph
		if (! isEmpty()) {
			String strGraph = "<br>" + this.name + "<br>";
			Village current = this.firstVillage;
			while (current != null) {
				String gnStr = "";
				for (int i=0; i<current.populationSize; i++) {
					gnStr += "g" + current.population[i].getID() + "  ";
				}
				strGraph += "<br>VILLAGE " + current.getName() + " currently holds " + current.populationSize + " gnomes ( " +
						gnStr + ")";
				strGraph += "<br>   It leads to " + current.getAdjList();
				current = current.getNext();
			} return strGraph;
		} else {System.out.println("This graph is empty."); return "";}
	} // end of printGraph()
	
	public Road[] getMinSpanTree() throws NotFoundException, GraphEmptyException, SameVillageException, RoadAlreadyExistsException {
		Proposal toBuild = new Proposal(this);
		return toBuild.getProposal();
	}
	
	public class Country {
		private String name;
		private Node start, end;
		
		public Country() {this(null);}
		public Country(Village v) {
			this.start = new Node(v);
		}
		
		public Country(Village v, Village v2) {
			this.start = new Node(v);
			this.end = new Node(v2);
		}
		
		public String getName() {return this.name;}
		public Village getStart() {return this.start.getVillage();}
		public Village getEnd() {return this.end.getVillage();}
	} // end of class country
	
}//end VillageList class
