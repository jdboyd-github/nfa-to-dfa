import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/*
 * Created by Daniel Boyd
 * Converts NFA input and prints its DFA counterpart
 * Theory of Computation Homework #3
 */
public class NFAConvert
{
	// Instance Variables
	private static ArrayList<String> states = new ArrayList<>(); 				// States
	private static ArrayList<ArrayList<String>> DFAstates = new ArrayList<>();
	private static ArrayList<String> finalStates = new ArrayList<>(); 			// Final States
	private static ArrayList<String> sigma = new ArrayList<>();
	private static String initial; 												// Initial State
	private static ArrayList<String> DFAinitial;
	private static ArrayList<String> delta = new ArrayList<>(); 				// Transitions
	private static ArrayList<Transition> transitions = new ArrayList<>();
	private static ArrayList<Transition> DFAtransitions = new ArrayList<>();

	// Driver Method
	public static void main(String[] args) throws IOException
	{
		// Checks for invalid arguments in command line prompt
		if(args.length == 0)
		{
			System.out.println("NFAConvert: no input files specified");
			return;
		}
		
		else if(args.length > 1)
		{
			System.out.println("NFAConvert: invalid usage - the program must be given one file as input");
			return;
		}
		
		String fileName = args[0];
		
		// Parsing file for NFA inputs
		parseFile(fileName);

		// Adding NFA transitions from delta list into transitions object
		getTransitions();

		// Getting DFA initial variable
		DFAinitial = getAllEmptyTransitions(initial, transitions.toArray(new Transition[transitions.size()]));
		
		// Getting DFA transitions
		DFAtransitions = getDFATransitions(DFAinitial, transitions.toArray(new Transition[transitions.size()]),
				sigma.toArray(new String[sigma.size()]));
		
		// Getting DFA states
		getStates();
		
		// Printing DFA
		print();
	}

	// If a state does not already exist, adds to DFA states
	private static void parseFile(String fileName) throws IOException
	{
		// Try Block for invalid file
		try {
			// Local Variables
			File file = new File(fileName);
			Scanner scan = new Scanner(file);
			List<String> list = new ArrayList<String>();
			int count = 0;
			
			// Moving lines from file into list
			while(scan.hasNextLine())
				list.add(scan.nextLine());
			
			// Loop to check all lines of input file
			for(int i = 0; i < list.size(); i++)
			{
				// Comment Lines
				if(list.get(i).substring(0, 1).equals("%"))
					count++;
				
				// % Q - Add to states array list
				else if(count == 1)
					states.add(list.get(i));
				
				// % Sigma - Add to sigma array list
				else if(count == 2)
					sigma.add(list.get(i));
				
				// % F - Add to finalStates array list
				else if(count == 3)
					finalStates.add(list.get(i));
				
				// % Q0 - Initial state variable
				else if(count == 4)
					initial = list.get(i);
				
				// % Delta - Add to delta array list
				else if(count == 5)
					delta.add(list.get(i));
			}
		
		// Throw error if filename not found
		} catch(FileNotFoundException e) {
			System.out.println("NFAConvert: the file '" + fileName + "' could not be opened");
			System.exit(0);
		}
	}
	
	// Creating NFA transitions from List
	private static void getTransitions()
	{
		// Looping through delta array
		for(String transition : delta)
		{
			// Splitting variables in every input line, and adding into object
			String[] arr = transition.split(" ");
			transitions.add(new Transition(arr[0], arr[2], arr[1]));
		}
	}
	
	// Finds all empty string transitions from a specific state
	private static ArrayList<String> getAllEmptyTransitions(String state, Transition[] transitions)
	{
		ArrayList<String> current = getEmptyTransition(state, transitions);
		
		// Loops through found empty state transitions for additional empty state transitions
		for(int i = 0; i < current.size(); i++)
		{
			ArrayList<String> next = getEmptyTransition(current.get(i), transitions);
			
			// If state doesn't already exist, add it
			for(int j = 0; j < next.size(); j++)
				if(!current.contains(next.get(j)))
					current.add(next.get(j));
		}
		
		return current;
	}
	
	// Finds empty string transitions from a specific state
	private static ArrayList<String> getEmptyTransition(String state, Transition[] transitions)
	{
		ArrayList<String> current = new ArrayList<>();
		current.add(state);
		
		// Looping through array of transitions to check for empty transitions
		for(int i = 0; i < transitions.length; i++)
			if(transitions[i].sigma.equals("_") && transitions[i].from.equals(state) && !current.contains(transitions[i].to))
				current.add(transitions[i].to);
		
		return current;
	}
	
	// Creating DFA transitions from NFA input
	private static ArrayList<Transition> getDFATransitions(ArrayList<String> states, Transition[] transitions, String[] sigma)
	{
		ArrayList<Transition> current = new ArrayList<>();
		
		// Looping through alphabet characters to check states
		for(int i = 0; i < sigma.length; i++)
		{
			ArrayList<String> to = getStatesFromInput(states, transitions, sigma[i]);
			
			// Empty set
			if(to.size() == 0)
				to.add("");
			
			current.add(new Transition(states, to, sigma[i]));
		}
		
		return current;
	}
	
	// Allows to check for states with specific alphabet input
	private static ArrayList<String> getStatesFromInput(ArrayList<String> states, Transition[] transition, String character)
	{
		ArrayList<String> current = new ArrayList<>();
		
		// Looping through list of states
		for(int i = 0; i < states.size(); i++)
			// Looping through transition array
			for(int j = 0; j < transition.length; j++)
				// If transition matches alphabet, and it is a valid state, add to list
				if(transition[j].sigma.equals(character) && transition[j].from.equals(states.get(i))
						&& !current.contains(transition[j].to))
				{
					current.add(transition[j].to);
					addListStates(current, getAllEmptyTransitions(transition[j].to, transition));
				}
		
		return current;
	}
	
	// Checks List of states, and if its not in the array, adds it
	private static void addListStates(ArrayList<String> current, ArrayList<String> arr)
	{
		// Looping through the input array
		for(int i = 0; i < arr.size(); i++)
			if(!current.contains(arr.get(i)))
				current.add(arr.get(i));
	}
	
	// Creating DFA states from transitions
	private static void getStates()
	{
		// Looping through all transitions for DFA for states
		for(int i = 0; i < DFAtransitions.size(); i++)
		{
			// Adding all states from DFA
			addStates(DFAstates, DFAtransitions.get(i).fromList);
			addStates(DFAstates, DFAtransitions.get(i).toList);
			addTransition(DFAtransitions, getDFATransitions(DFAtransitions.get(i).toList,
					transitions.toArray(new Transition[transitions.size()]), sigma.toArray(new String[sigma.size()])));
		}
	}
	
	// If a transition does not already exist, adds to DFA transitions
	private static void addTransition(ArrayList<Transition> DFAtransitions, ArrayList<Transition> transitions)
	{
		// Looping through list of transitions
		for(int i = 0; i < transitions.size(); i++)
		{
			int j;
			
			// Sorting transitions
			Collections.sort(transitions.get(i).fromList);
			Collections.sort(transitions.get(i).toList);
			
			// Looping through DFA transitions
			for(j = 0; j < DFAtransitions.size(); j++)
			{
				// Sorting transitions
				Collections.sort(DFAtransitions.get(j).fromList);
				Collections.sort(DFAtransitions.get(j).toList);
				
				// Checking for equality, and breaking loop if they are
				if(DFAtransitions.get(j).fromList.equals(transitions.get(i).fromList) && DFAtransitions.get(j).toList.equals(transitions.get(i).toList)
						&& DFAtransitions.get(j).sigma.equals(transitions.get(i).sigma))
					break;
			}
			
			// If it gets through the entire list of DFATransitions, and found no match, add to list
			if(j == DFAtransitions.size())
				DFAtransitions.add(transitions.get(i));
		}
	}
	
	// If a state does not already exist, adds to DFA states
	private static void addStates(ArrayList<ArrayList<String>> DFAstates, ArrayList<String> states)
	{
		// Checking if state already exists
		if(!DFAstates.contains(states))
			DFAstates.add(states);
	}
	
	// Checks to see if a list of states has a final state
	private static boolean match(String[] finalStates, ArrayList<String> states)
	{
		// Loops through all states, and checks to see if any are final states
		for(int i = 0; i < states.size(); i++)
		{
			// Loops through accept states
			for(int j = 0; j < finalStates.length; j++)
				// If accept state is part of states, return true
				if(finalStates[j].equals(states.get(i)))
					return true;
		}
		
		return false;
	}
	
	// Prints DFA information created from NFA
	private static void print()
	{
		// Printing States
		System.out.println("% Q");
		
		for(int i = 0; i < DFAstates.size(); i++)
		{
			ArrayList<String> stateInDFAstates = DFAstates.get(i);
			System.out.println(toString(stateInDFAstates));
		}

		// Printing Sigma
		System.out.println("% Sigma");
		
		for(int i = 0; i < sigma.size(); i++)
			if(i != sigma.size() - 1)
				System.out.println(sigma.get(i));
			
			else
				System.out.println(sigma.get(i));
		
		// Printing Final States
		System.out.println("% F");
				
		for(int i = 0; i < DFAstates.size(); i++)
		{
			ArrayList<String> stateInDFAstates = DFAstates.get(i);
					
			if(match(finalStates.toArray(new String[finalStates.size()]), stateInDFAstates))
				System.out.println(toString(stateInDFAstates));
		}
		
		// Printing Initial State
		System.out.println("% Q0");
		System.out.println(toString(DFAinitial));

		// Printing Transitions
		System.out.println("% Delta");
		
		for(int i = 0; i < DFAtransitions.size(); i++)
			System.out.println(toString(DFAtransitions.get(i).fromList) + " " + DFAtransitions.get(i).sigma + " " +
					toString(DFAtransitions.get(i).toList));
	}
	
	// A toString() method used to help print states cleanly
	private static String toString(ArrayList<String> states)
	{
		String state = "{";
		
		// Loop to add all state variables into state string
		for(int i = 0; i < states.size(); i++)
		{
			state += states.get(i);
			
			if(i != states.size() - 1)
				state += ", ";
		}
		
		// Returning formatted string for printing in main method
		return state + "}";
	}
}

// Transition Object used to help hold information from NFA
class Transition
{
	// Instance Variables
	String from;
	String to;
	ArrayList<String> fromList;
	ArrayList<String> toList;
	String sigma;

	// Constructor from individual states
	public Transition(String from, String to, String sigma)
	{
		this.from = from;
		this.to = to;
		this.sigma = sigma;
	}

	// Constructor for List of states
	public Transition(ArrayList<String> from, ArrayList<String> to, String sigma)
	{
		this.fromList = from;
		this.toList = to;
		this.sigma = sigma;
	}
}