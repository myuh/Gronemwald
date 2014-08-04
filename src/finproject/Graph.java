package finproject;

import finproject.Exceptions.*;

import java.util.Random;

public class Graph implements Runnable {
	private int length = 0;
	Village firstVillage;
	Village lastVillage;
	String name;
	
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
		if (! isEmpty()) {
			Village current = firstVillage;
			while (current!=null) {
				for(int i=0; i<current.populationSize; i++) {
					Thread gnomeThread = new Thread(current.population[i]);
					gnomeThread.start();
				}
				current = current.getNext();
			}
		}
		
		
		/*
		int villCount=0;
		while (! isEmpty() && villCount < 3) { // chooses random village within graph
				Random rand = new Random();
				int v = rand.nextInt(length), count=0;
				Village vill = getFirst();
				for (int i=0; i<length; i++) {
					if (count != v) {count++; vill = vill.getNext();}
				}
				System.out.println("start village " + vill.getName());
				
				Village vill2 = getFirst();
				for (int i=0; i<length; i++) {
					if (count != v) {count++; vill2 = vill2.getNext();}
				}
				System.out.println("end village is " + vill2.getName());
				
				try {
					System.out.println("New road proposed between village " + vill.getName() + " and village " + vill2.getName());
					roadProposal(vill, vill2);
					villCount++;
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					System.out.println("The system was interrupted.");
				}
				
		}*/
	} // end of run()
			
	public boolean roadExists(Village v1, Village v2) {
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
			Random rand = new Random(); int newToll = 1+rand.nextInt(5);
			System.out.println("New road proposed between village " + v1.getName() + " and village " + v2.getName());
			System.out.println("The total building cost would be: " + newToll*100);
			System.out.println("The government has elected to build the road.");
			v1.connect(1, v2);
		} catch (SameVillageException e) {
			System.out.println("The government says, " + e.getMessage());
		} catch (RoadAlreadyExistsException e) {
			System.out.println("The government says, " + e.getMessage());
		}
	}
	
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

	/*
	//Just.....forget this method.............creates deep copy of this graph
	public void createProposal() throws SameVillageException, RoadAlreadyExistsException, NotFoundException, GraphEmptyException {
		//create proposal instance; WILL NOT BE UPDATED IF ROADS ARE BUILT INTO ACTUAL GRAPH
		proposal = new ProposalGraph();
	}
	
	public void clearProposal() {
		//essentially same as createProposal, but here for clarity
		//Use after finishing finding min tree for existing proposal
		proposal = new ProposalGraph();
	}
	
	//To be used with deep copy....don't use this
	public void addOopsProposal(Village a, Village b, int cost) throws NotFoundException, GraphEmptyException, SameVillageException, RoadAlreadyExistsException {
		//Assumes a village with no roads out of it has been created in the Graph
		Village proposalA = proposal.find(a.getName());
		Village proposalB = proposal.find(b.getName());
		Road newRoad = proposalA.connect(cost,  proposalB);
		newRoad.built = false;
		
	}*/
	
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
		System.out.println("Found the shortest path!");
		System.out.println("Path (from end to start) is: " + thePath);
		return thePath;
	} // end of travelMinExpPath method
	
	public Queue travelTopSort() throws NotFoundException, GraphEmptyException{ // need to check for cycles?
		Queue q = new Queue();
		Village a;
		RoadIterator b;
		queueZero(q,0); // adds villages with incoming roads of 0
		Queue pathToTake = new Queue();
		int[][] checkCycle = new int[this.length+1][this.length+1];
		System.out.println("is it empty? "+q.isEmpty());
		while(!q.isEmpty()){
			a = q.remove().getVillage();
			checkCycle[a.getName()][a.getName()] = a.getName();
			System.out.println("a has been removed from q. q length is now "+q.length());
			//System.out.println("hello?");
			pathToTake.insert(new Node(a)); //+= a.getName() + " ";
			System.out.println("a.name is "+pathToTake.getFirst().getVillage().getName()+" and adjacent length is "+a.outgoing.length);
			// for each adjacent village to village a, decrease each indegree and if it equals 0, add to queue
			if(a.outgoing.length != 0){
				b = a.outgoing.firstRoad;
				while(b != null){
					System.out.println("b's indegree is "+b.endVillage().indegree);
					b.endVillage().indegree--;
					System.out.println("b's indegree is nowww "+b.endVillage().indegree);
					if( b.endVillage().indegree  == 0 ){
						System.out.println("about to insert...");
						q.insert(new Node(b.endVillage()));
						System.out.println("added to the q is village "+b.endVillage().getName() +" with indegree "+b.endVillage().indegree);
					}
					b = b.getNext();
				} // end of while
			} // end of if
		} // end of while
		// System.out.println("pathToTake is "+pathToTake);
		pathToTake.printQueue();
		return pathToTake;
		
	} // end of travelTopSort method
	
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
					gnStr += current.population[i].getID() + " ";
				}
				strGraph += "<br>VILLAGE " + current.getName() + " currently holds " + current.populationSize + " gnomes ( " +
						gnStr + ")";
				strGraph += "<br>   It leads to " + current.getAdjList();
				current = current.getNext();
			} return strGraph;
		} else {System.out.println("This graph is empty."); return "";}
	} // end of printGraph()
	
}//end VillageList class
