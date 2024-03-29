import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * LeagueManager - 
 * This program is used to create and manage Leagues for different sports.
 * It supports creation and deletion of leagues, adding teams or players to leagues,
 * generating and viewing fixtures and leaderboards.
 * 
 * @author Dylan King, Louise Madden, Killian Ten Bohmer, Lennart Mantel
 */
public class LeagueManager
{
	public static int loggedInUser;
	public static ArrayList<ArrayList<String>> leaderboardParticipants;
	public static ArrayList<ArrayList<Integer>> intFixtures;	
	public static ArrayList<ArrayList<Integer>> intResults;
	public static int [][] leaderBoard;

	public static void main(String[] args) throws IOException
	{
		if (login())
		{
			boolean quit =  false;
			String whatToDo = "What would you like to do?";
			String[] options = {"View existing League", "Create new League", "Edit existing League", "Quit"};
			int inputNum;
			while (!quit)
			{
				inputNum = JOptionPane.showOptionDialog(null, whatToDo, "Choose an option",
						   JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
						   options, "Quit");
				switch (inputNum)
				{
					case 0:
						viewLeagues();
						break;
					case 1:
						createLeague();
						break;
					case 2:
						editLeague();
						break;
					default:
						quit = true;
						break;
				}
				whatToDo = "Would you like to do anything else?";
			}
		}
	}

	/**
	 * Dylan King - 
	 * login - 
	 * Takes in users name and password and returns true if the information is valid.
	 * Returns false if the information is invalid.
	 * Passes information on to checkLogin for verification.
	 */
	public static boolean login() throws IOException
	{
		String username, password = "", loginPassDiag,
			   loginUserDiag = "Please enter your Username:",
			   invalid = "Invalid Credentials. ";
		boolean validLogin = false;
		username = JOptionPane.showInputDialog(null, loginUserDiag, "Enter Username");
		loginPassDiag = "Your Username is:    " + username + "\n" + loginUserDiag.replaceAll("Username","Password");
		password = JOptionPane.showInputDialog(null, loginPassDiag, "Enter Password");
		if ((username != null && password !=null) && checkLogin(username, password, invalid))
		{
			validLogin = true;
		}
		if (!validLogin)
		{
			JOptionPane.showMessageDialog(null, "Invalid Login.");
		}
		return validLogin;
	}

	/**
	 * Louise Madden - 
	 * checkLogin - 
	 * Takes the username or password, and a String to display if the submitted
	 * information is incorrect as parameters, and verifies it against the
	 * Administrators.txt file.
	 * Gives the user three attempts to login.
	 * Returns true if the parameters are valid, and false if they are invalid. 
	 */
	public static boolean checkLogin(String username,String password, String invalid) throws IOException
	{
		FileReader readAdmins = new FileReader("Administrators.txt");
		Scanner admins = new Scanner(readAdmins);
		boolean correctInfo = false;
		String userInfo = "";
		String[] userCredentials;
		for (int attempts = 0; attempts < 3 && !correctInfo; attempts++)
		{
			if (attempts > 0)
			{
				username = JOptionPane.showInputDialog(null, invalid + "Enter username again.", "Invalid credentials.");
				password = JOptionPane.showInputDialog(null, invalid + "Enter password again.", "Invalid credentials.");
				admins.close();
				readAdmins.close();
				readAdmins = new FileReader("Administrators.txt");
				admins = new Scanner(readAdmins);
			}
			while (admins.hasNext() && !correctInfo && (username != null && password != null))
			{
				userInfo = admins.nextLine();
				userCredentials = userInfo.split(",");
				if (userCredentials[1].equals(username) && userCredentials[2].equals(password))
				{
					correctInfo = true;
					loggedInUser = Integer.parseInt(userCredentials[0]);
				}
			}
		}
		admins.close();
		readAdmins.close();
		return correctInfo;
	}

	/**
	 * Louise Madden - 
	 * createLeague -
	 * Asks the user the name of the league they would like to create
	 * Checks if a league with that name already exists
	 * If it does it asks if the user still wants to create a league with this name
	 * Finds the league number based on the previous league number
	 * Adds the league number,league name and admin ID(loggedInUser) to the Leagues.txt file
	 */
	public static void createLeague() throws IOException
	{
		File leagueFile = new File("Leagues.txt");
		if (!leagueFile.exists()) 
		{
			System.out.println("Cannot find file");
		}
		else
		{
			boolean newLeague = true;
			while (newLeague)
			{
				String leagueName = JOptionPane.showInputDialog(null,
									"Enter the name of the league you wish to create");
				if (leagueName != null && leagueName != "") 
				{
					ArrayList<ArrayList<String>> leagueInfo = new ArrayList<ArrayList<String>>();
					leagueInfo.add(new ArrayList<String>());	
					leagueInfo.add(new ArrayList<String>());
					leagueInfo.add(new ArrayList<String>());
					writeToArrayList(leagueInfo, leagueFile);
					if (leagueInfo.get(1).contains(leagueName))
					{
						if (JOptionPane.showConfirmDialog(null, "This league already exists." +
							" Make a new one with the same name?", "Continue?",JOptionPane.YES_NO_OPTION) ==
							JOptionPane.NO_OPTION)
						{
							newLeague = false;
							break;
						}
					}
					int numOfLeagues = leagueInfo.get(0).size();
					int newLeagueNum = (numOfLeagues + 1);
				
					FileWriter writeLeague = new FileWriter("Leagues.txt", true);
					//amending Leagues.txt file with the new league
					PrintWriter out = new PrintWriter(writeLeague);
					out.printf("%d,", newLeagueNum);
					out.print(leagueName);
					out.printf(",%d", loggedInUser);
					out.println();
					out.close();
					writeLeague.close();
					
					newLeague = false;
				}
				else
				{
					newLeague = false;
				}
			}
		}
	}
	
	/**
	 * Louise Madden - 
	 * checkLeagueAdmin - 
	 * Creates an ArrayList from Leagues.txt, and scans for Leagues owned by the user currently logged in.
	 * Returns an array with these Leagues.
	*/
	public static String[] checkLeagueAdmin() throws IOException
	{
		File leagueFile = new File("Leagues.txt");
		ArrayList<ArrayList<String>> leagueInfo = new ArrayList<ArrayList<String>>();
		leagueInfo.add(new ArrayList<String>());	
		leagueInfo.add(new ArrayList<String>());
		leagueInfo.add(new ArrayList<String>());
		writeToArrayList(leagueInfo, leagueFile);
		int adminID;
		String[] userLeagues = new String[leagueInfo.get(1).size()];
		int j = 0;
		for (int i = 0; i < leagueInfo.get(2).size(); i++)
		{
			adminID = (Integer.parseInt(leagueInfo.get(2).get(i)));
			if (adminID == loggedInUser)
			{
				userLeagues[j] = leagueInfo.get(0).get(i) + "," + leagueInfo.get(1).get(i);
				j++;
			}			
		}
		return userLeagues;
	}
	
	/**
	 * Louise Madden - 
	 * viewLeagues - 
	 * calls checkLeagueAdmin and uses the Array generated to let the user choose which league to view, 
	 * and then lets the view participants, fixtures, or the leaderboard for the league.
	 */
	public static void viewLeagues() throws IOException
	{
		String[] userLeagues = checkLeagueAdmin();
		String leagueToView, toView;
		boolean quit = false;
		leagueToView = "" + JOptionPane.showInputDialog(null, "Which League would you like to view?",
							"View which league?", JOptionPane.PLAIN_MESSAGE, null, userLeagues, null);
		int viewLeagueNum = Integer.parseInt(leagueToView.substring(0,1));
		int viewTypes;
		toView = "What would you like to view?";
		String[] whatToView = {"View participants", "View existing fixtures", "View leaderboard", "Cancel"};
		while (!quit)
		{
			viewTypes = JOptionPane.showOptionDialog(null, toView, "View type",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, whatToView, null);
			switch (viewTypes)
			{
				case 0:
					viewParticipants(viewLeagueNum);
					break;
				case 1:
					viewFixtures(viewLeagueNum);
					break;
				case 2:
					generateLeaderboard(viewLeagueNum);
					break;
				case 3:
					quit = true;
					break;
				default:
					quit = true;
					break;
			}
			toView = "Would you like to do anything else?";
		}
	}

	/**
	 * Louise Madden - 
	 * editLeague - 
	 * calls checkLeagueAdmin and uses the Array generated to let the user choose which league to edit, 
	 * and then lets the edit participants, fixtures, results, or the scoring scheme for the league.
	 */
	public static void editLeague() throws IOException
	{
		String[] userLeagues = checkLeagueAdmin();
		String leagueToEdit, toEdit;
		boolean quit = false;
		leagueToEdit = "" + JOptionPane.showInputDialog(null, "Which League would you like to edit?",
							"Edit which league?", JOptionPane.PLAIN_MESSAGE, null, userLeagues, null);
		int editLeagueNum = Integer.parseInt(leagueToEdit.substring(0,1));
		int editTypes;
		toEdit = "What would you like to edit?";
		String[] whatToEdit = {"Add or edit participants", "Generate new Fixtures", "Input results",
							   "Create scoring scheme", "Cancel"};
		while (!quit)
		{
			editTypes = JOptionPane.showOptionDialog(null, toEdit, "Edit type",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, whatToEdit, null);
			switch (editTypes)
			{
				case 0:
					addParticipants(editLeagueNum);
					break;
				case 1:
					generateFixtures(editLeagueNum);
					break;
				case 2:
					generateResults(editLeagueNum);
					break;
				case 3:
					setScoreScheme(editLeagueNum);
					break;
				case 4:
					quit = true;
					break;
				default:
					quit = true;
					break;
			}
			toEdit = "Would you like to do anything else?";
		}
	}
	
	/**
	 * Louise Madden - 
	 * deleteLeague - 
	 * Gives a list of the existing leagues that can be deleted
	 * Removes the chosen league from the multidimentional ArrayList leagueInfo
	 * Rewrites the amended leagues into the Leagues.txt file
	 * Deletes the files with information regarding the leagues
	 */
	public static void deleteLeague() throws IOException
	{
		File leagueFile = new File("Leagues.txt");
		if (!leagueFile.exists()) 
		{
			System.out.println("Cannot find file");
		}
		else
		{
			ArrayList<ArrayList<String>> leagueInfo = new ArrayList<ArrayList<String>>();
			leagueInfo.add(new ArrayList<String>());	
			leagueInfo.add(new ArrayList<String>());
			leagueInfo.add(new ArrayList<String>());
			writeToArrayList(leagueInfo, leagueFile);
			String[] options = new String[leagueInfo.get(1).size()];
			for (int i = 0; i < leagueInfo.get(1).size(); i++)
			{
				options[i] = leagueInfo.get(1).get(i);
			}
			int leagueIndex = JOptionPane.showOptionDialog(null, "What league would you like to delete?",
							  "Choose an option", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
							  null, options, "Quit");
			String leagueNum = leagueInfo.get(0).get(leagueIndex);
			//League chosen now to delete
			leagueInfo.get(0).remove(leagueIndex);
			leagueInfo.get(1).remove(leagueIndex);
			leagueInfo.get(2).remove(leagueIndex);
			
			
			FileWriter fixedLeagues = new FileWriter("leagues.txt");
			PrintWriter printLeagues = new PrintWriter(fixedLeagues);
			for (int i = 0; i < leagueInfo.get(0).size(); i++)
			{
				printLeagues.print(leagueInfo.get(0).get(i) + ",");
				printLeagues.print(leagueInfo.get(1).get(i) + ",");
				printLeagues.println(leagueInfo.get(2).get(i));
			}
			printLeagues.close();
			fixedLeagues.close();
			
			String[] filesToDelete = {leagueNum + "_participants.txt", leagueNum + "_fixtures.txt",
									  leagueNum + "_results.txt", leagueNum + "_pointscheme.txt"};
			
			for (int i = 0; i <	 filesToDelete.length; i++)
			{
				File deleteFile = new File(filesToDelete[i]);
				if (!(deleteFile.exists()))
				{
					JOptionPane.showMessageDialog(null, deleteFile.getName() + " does not exist");
				}
				else if (deleteFile.delete())
				{
					JOptionPane.showMessageDialog(null, deleteFile.getName() + " has been deleted");
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Operation to delete file failed.");
				}
			}
		}
	}

	/**
	 * Dylan King - 
	 * addParticipants - 
	 * Takes the number of the chosen league as a parameter, and makes or edits the
	 * participants file prefixed with that number. Takes in particpant names from
	 * the user and stores them in an ArrayList before writing them to file.
	 */
	public static void addParticipants(int leagueNum) throws IOException
	{
		String fileName = leagueNum + "_participants.txt", name = "";
		File playersOrTeams = new File(fileName);
		boolean quit = false, foundPart, blank, emptyName;
		ArrayList<ArrayList<String>> participants = new ArrayList<ArrayList<String>>();
		participants.add(new ArrayList<String>());
		participants.add(new ArrayList<String>());
		int i = 1;
		if (playersOrTeams.exists() && playersOrTeams.length() != 0)
		{
			JOptionPane.showMessageDialog(null, "File already exists. Importing existing teams.",
										"File found", JOptionPane.INFORMATION_MESSAGE);
			writeToArrayList(participants, playersOrTeams);
			i = participants.get(0).size() + 1;
		}
		String toQuit = "Do you want to add more participants?";
		while (!quit && name != null)
		{
			name = JOptionPane.showInputDialog(null, "Enter team or player name:");
			foundPart = participants.get(1).contains(name);
			blank = name == null;
			emptyName = false;
			if(!blank)
			{
				emptyName = name.equals("");
			}
			if (!blank && !foundPart && !emptyName)
			{
				participants.get(0).add(String.valueOf(i));
				participants.get(1).add(name);
				i++;
				toQuit = "Do you want to add more participants?";
			}
			else if (!blank && foundPart && !emptyName)
			{
				toQuit = name + " is already in this league. " + toQuit;
			}
			else if (emptyName)
			{
				toQuit = "You entered an empty name. Enter again?";
			}
			if ((!blank || emptyName) && JOptionPane.showConfirmDialog(null, toQuit,
				"Continue?",JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			{
				quit = true;
			}
		}
		FileWriter writeParts = new FileWriter(playersOrTeams);
		PrintWriter out = new PrintWriter(playersOrTeams);
		for (int j = 0; j < participants.get(0).size(); j++)
		{
			out.println(participants.get(0).get(j) + "," + participants.get(1).get(j));
		}
		out.close();
		writeParts.close();
	}

	/**
	 * Dylan King - 
	 * viewParticipants - 
	 * Takes in the league number as a parameter, and displays the participants of that league.
	 */
	public static void viewParticipants(int leagueNum) throws IOException
	{
		String result;
		String fileName =  leagueNum + "_participants.txt";
		File partFile = new File(fileName);
		if (!partFile.exists())
		{
			result = "File not found.";
		}
		else
		{
			ArrayList<ArrayList<String>> participants = new ArrayList<ArrayList<String>>();
			participants.add(new ArrayList<String>());
			participants.add(new ArrayList<String>());
			writeToArrayList(participants, partFile);
			result = "Teams in this League:\n";
			for (int i = 0; i < participants.get(0).size(); i++)
			{
				result += "Team #" + participants.get(0).get(i) + ":\t" +
						participants.get(1).get(i) + "\n";
			}
		}
		JTextArea output = new JTextArea(result);
		JOptionPane.showMessageDialog(null, output, "Result", JOptionPane.INFORMATION_MESSAGE, null);
	}

	/**
	 * Dylan King and Louise Madden - 
	 * writeToArrayList - 
	 * Takes in an String ArralyList and a file, and reads the file into the ArrayList.
	 * Does not return since ArrayLists are pass-by-reference.
	 */
	public static void writeToArrayList(ArrayList<ArrayList<String>> aList, File fileToRead) throws IOException
	{
		FileReader fileBeingRead = new FileReader(fileToRead);
		Scanner in = new Scanner(fileBeingRead);
		String[] lineElements;
		while (in.hasNext())
		{
			lineElements = (in.nextLine()).split(",");
			for (int i = 0; i < aList.size(); i++)
			{
				aList.get(i).add(lineElements[i]);
			}
		}
		in.close();
		fileBeingRead.close();
	}

	/**
	 * Dylan King - 
	 * writeToArrayList - 
	 * Takes in an Integer ArralyList and a file, and reads the file into the ArrayList.
	 * Does not return since ArrayLists are pass-by-reference.
	 */
	public static void writeToIntArrayList(ArrayList<ArrayList<Integer>> aList, File fileToRead) throws IOException
	{
		FileReader fileBeingRead = new FileReader(fileToRead);
		Scanner in = new Scanner(fileBeingRead);
		String[] lineElements;
		while (in.hasNext())
		{
			lineElements = (in.nextLine()).split(",");
			for (int i = 0; i < aList.size(); i++)
			{
				aList.get(i).add(Integer.parseInt(lineElements[i]));
			}
		}
		in.close();
		fileBeingRead.close();
	}

	/**
	 * Dylan King - 
	 * generateFixtures -  
	 * Takes in the number of teams in a league and the league number as parameters,
	 * and geerates fixtures and stores them in a fixtures file for the league.
	 */
	public static void generateFixtures(int leagueNum) throws IOException
	{
		int teamCount, totalRounds, matchesPerRound, roundNum, matchCount,
			matchNum, homeTeamNum, awayTeamNum, even, odd;
		String fixtureAsText;
		File participantsFile = new File(leagueNum + "_participants.txt");
		if (participantsFile.exists())
		{
			ArrayList<ArrayList<String>> participants = new ArrayList<ArrayList<String>>();
			participants.add(new ArrayList<String>());
			participants.add(new ArrayList<String>());
			writeToArrayList(participants, participantsFile);
			teamCount = participants.get(0).size();
			ArrayList<ArrayList<String>> fixtures = new ArrayList<ArrayList<String>>();
			fixtures.add(new ArrayList<String>());
			fixtures.add(new ArrayList<String>());
			fixtures.add(new ArrayList<String>());
			ArrayList<ArrayList<String>> revisedFixtures = new ArrayList<ArrayList<String>>();
			revisedFixtures.add(new ArrayList<String>());
			revisedFixtures.add(new ArrayList<String>());
			revisedFixtures.add(new ArrayList<String>());
			if (teamCount % 2 != 0)
			{
				teamCount++;
			}
			totalRounds = teamCount - 1;
			matchesPerRound = teamCount / 2;
			for (roundNum = 0, matchCount = 0; roundNum < totalRounds; roundNum++)
			{
				for (matchNum = 0; matchNum < matchesPerRound; matchNum++, matchCount++)
				{
					homeTeamNum = (roundNum + matchNum) % (totalRounds);
					awayTeamNum = (totalRounds - matchNum + roundNum) % (totalRounds);
					if (matchNum == 0)
					{
						awayTeamNum = totalRounds;
					}
					fixtures.get(0).add("" + (matchCount + 1));
					fixtures.get(1).add("" + (homeTeamNum + 1));
					fixtures.get(2).add("" + (awayTeamNum + 1));
				}
			}
			even = 0;
			odd = matchesPerRound;
			for (int j = 0; j < fixtures.size(); j++)
			{
				for (int i = 0; i < fixtures.get(j).size(); i++)
				{
					if (j == 0)
					{
						revisedFixtures.get(j).add(fixtures.get(j).get(i));
					}
					else
					{
						if (i % 2 == 0)
						{
							revisedFixtures.get(j).add(fixtures.get(j).get(even++));
						}
						else
						{
							revisedFixtures.get(j).add(fixtures.get(j).get(odd++));
						}
					}
				}
				even = 0;
				odd = matchesPerRound;
			}
			fixtures = revisedFixtures;
			for (matchCount = 0; matchCount < fixtures.get(0).size(); matchCount++)
			{
				if (matchCount % 2 != 0)
				{
					fixtureAsText = fixtures.get(1).get(matchCount);
					fixtures.get(1).set(matchCount, fixtures.get(2).get(matchCount));
					fixtures.get(2).set(matchCount, fixtureAsText);

				}
			}
			FileWriter writeFixtures = new FileWriter(leagueNum + "_fixtures.txt");
			PrintWriter fixturesOut = new PrintWriter(writeFixtures);
			for (int i = 0; i < fixtures.get(0).size(); i ++)
			{
				fixturesOut.println(fixtures.get(0).get(i) + "," +
									fixtures.get(1).get(i) + "," + fixtures.get(2).get(i));
			}
			fixturesOut.close();
			writeFixtures.close();
			if (JOptionPane.showConfirmDialog(null, "Fixtures generated. Would you like to view them?",
				"Fixtures generated", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) ==
				JOptionPane.YES_OPTION)
			{
				viewFixtures(leagueNum);
			}
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Error: No participants found. Please add participants to this league.",
										  "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Dylan King - 
	 * viewFixtures - 
	 * Takes in the league number and the number of teams, and displays the fixtures for the league.
	 * If there are an odd number of teams, the output says the matches against the last team are byes.
	 */
	public static void viewFixtures(int leagueNum) throws IOException
	{
		boolean extraTeam = false;
		File participantsFile = new File(leagueNum + "_participants.txt");
		if (participantsFile.exists())
		{
			ArrayList<ArrayList<String>> participants = new ArrayList<ArrayList<String>>();
			participants.add(new ArrayList<String>());
			participants.add(new ArrayList<String>());
			writeToArrayList(participants, participantsFile);
			int teamCount = participants.get(0).size();
			if (teamCount % 2 != 0)
			{
				extraTeam = true;
			}
			File fixturesFile = new File(leagueNum + "_fixtures.txt");
			if (fixturesFile.exists())
			{
				ArrayList<ArrayList<String>> fixtures = new ArrayList<ArrayList<String>>();
				fixtures.add(new ArrayList<String>());
				fixtures.add(new ArrayList<String>());
				fixtures.add(new ArrayList<String>());
				writeToArrayList(fixtures, fixturesFile);
				String output;
				int i;
				output = "The current fixtures for this league are:\n";
				for (i = 0; i < fixtures.get(0).size(); i++)
				{
					output += "Fixture #" + fixtures.get(0).get(i) + ":\t" +
							fixtures.get(1).get(i) +  " v " + fixtures.get(2).get(i);
					if ((i + 1) % 3 == 0)
					{
						output += "\n";
					}
					else
					{
						output += "\t";
					}
				}
				if (extraTeam)
				{
					output += "\n Since you had " + teamCount + " teams at the outset (unveven number), " +
							"fixtures against team number " + (teamCount + 1) + " are byes.";
				}
				JTextArea result = new JTextArea(output);
				JOptionPane.showMessageDialog(null, result, "Current Fixtures.", JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Error: No fixtures found. Please generate " +
				"fixtures for this league.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Error: No participants found. Please add participants to this league.",
			"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Dylan King - 
	 * Takes in the league Number as a parameter and creates a results file for each
	 * fixture from user input. It scans the fixtures and participants files and allows
	 * the user to select each fixture individually, and enter the results, and stores
	 * the results in the results file.
	 */
	public static void generateResults(int leagueNum) throws IOException
	{
		File participantsFile = new File(leagueNum + "_participants.txt");
		File fixturesFile = new File(leagueNum + "_fixtures.txt");
		File resultsFile = new File(leagueNum + "_results.txt");
		int outputSelection, teamOne, teamTwo;
		String fixtureChosen, homeTeam, awayTeam, homeScore, awayScore, outputString, byeTeam;
		String[] prettyFixtures;
		boolean quit = false;
		ArrayList<ArrayList<String>> participants = new ArrayList<ArrayList<String>>();
		participants.add(new ArrayList<String>());
		participants.add(new ArrayList<String>());
		writeToArrayList(participants, participantsFile);
		ArrayList<ArrayList<String>> fixtures = new ArrayList<ArrayList<String>>();
		fixtures.add(new ArrayList<String>());
		fixtures.add(new ArrayList<String>());
		fixtures.add(new ArrayList<String>());
		writeToArrayList(fixtures, fixturesFile);
		ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
		results.add(new ArrayList<String>());
		results.add(new ArrayList<String>());
		results.add(new ArrayList<String>());
		prettyFixtures = new String[fixtures.get(0).size()];
		byeTeam = "Bye Team";
		for (int i = 0; i < prettyFixtures.length; i++)
		{
			teamOne = Integer.parseInt(fixtures.get(1).get(i)) - 1;
			teamTwo = Integer.parseInt(fixtures.get(2).get(i)) - 1;
			if (teamOne > participants.get(0).size() - 1)
			{
				homeTeam = byeTeam;
				awayTeam = participants.get(1).get(teamTwo);
			}
			else if (teamTwo > participants.get(0).size() - 1)
			{
				homeTeam = participants.get(1).get(teamOne);
				awayTeam = byeTeam;
			}
			else
			{
				homeTeam = participants.get(1).get(teamOne);
				awayTeam = participants.get(1).get(teamTwo);
			}
			prettyFixtures[i] = "Fixture #" + fixtures.get(0).get(i) + ": " + homeTeam + " v " + awayTeam;
		}
		while (!quit)
		{
			fixtureChosen = "" + JOptionPane.showInputDialog(null, "Please select a fixture to input results for.",
							"Select a fixture", JOptionPane.PLAIN_MESSAGE, null, prettyFixtures, null);
			if (!fixtureChosen.equals("null"))
			{
				fixtureChosen = fixtureChosen.substring(9, 10);
				homeScore = JOptionPane.showInputDialog(null, "Enter the score of the Home Team.", "Enter Score",
							JOptionPane.PLAIN_MESSAGE);
				if (homeScore != null)
				{
					awayScore = JOptionPane.showInputDialog(null, "Enter the score of the Away Team.", "Enter Score",
								JOptionPane.PLAIN_MESSAGE);
					if(awayScore != null)
					{
						if ((homeScore.matches("[0-9]+") && awayScore.matches("[0-9]+"))
							 && !results.get(0).contains(fixtureChosen))
						{
							results.get(0).add(fixtureChosen);
							results.get(1).add(homeScore);
							results.get(2).add(awayScore);
							outputString = "Result recorded. Continue?";
						}
						else if (results.get(0).contains(fixtureChosen))
						{
							outputString = "Results already recorded for this fixture. Pick again?";
						}
						else
						{
							outputString = "Invalid input. Must be a whole number. Try again?";
						}
						outputSelection = JOptionPane.showConfirmDialog(null, outputString, "Result",
										  JOptionPane.YES_NO_OPTION);
						if (outputSelection == JOptionPane.NO_OPTION)
						{
							FileWriter writeResults = new FileWriter(resultsFile);
							PrintWriter out = new PrintWriter(resultsFile);
							for (int i = 0; i < results.get(0).size(); i++)
							{
								out.println(results.get(0).get(i) + "," + results.get(1).get(i)
											+ "," + results.get(2).get(i));
							}
							out.close();
							writeResults.close();
							quit = true;
						}
					}
					else
					{
						quit = true;
					}
				}
				else
				{
					quit = true;
				}
			}
			else
			{
				quit = true;
			}
		}
	}

	/**
	 * Killian Ten Bohmer - 
	 * setScoreScheme - 
	 * Takes in the lague number as a parameter and sets the pounts
	 * given in the even of a win, draw or loss. 
	 */
	public static void setScoreScheme(int leagueNum) throws IOException
	{
		
		String winScore = "", loseScore = "", drawScore = "";
		File results = new File(leagueNum + "_pointscheme.txt") ;
		if (results.exists())
		{
			JOptionPane.showMessageDialog(null, "File already exists");
		}
		else
		{
			boolean valid = false;
			while (!valid)
			{
				winScore = JOptionPane.showInputDialog(null, "Enter points for winning team",
						   "Enter points", JOptionPane.QUESTION_MESSAGE);
				drawScore = JOptionPane.showInputDialog(null, "Enter points for a draw.",
							"Enter points", JOptionPane.QUESTION_MESSAGE);
				loseScore = JOptionPane.showInputDialog(null, "Enter points for losing team",
							"Enter points", JOptionPane.QUESTION_MESSAGE);
				if (winScore.matches("[0-9]+") && loseScore.matches("[0-9]+") && drawScore.matches("[0-9]+"))
				{
					valid = true;
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Invalid input. Please enter integers.",
												  "Invalid input.", JOptionPane.ERROR_MESSAGE);
				}
			}
			FileWriter aFileWriter = new FileWriter(results);
			PrintWriter out = new PrintWriter(aFileWriter);
			out.print(winScore + "," + drawScore + "," + loseScore);
			out.close();
			aFileWriter.close();
		}
	}
	
	/**
	 * Lennart Mantel and Dylan King - 
	 * generateLeaderboard - 
	 * The "main" method for leaderboard generation. Takes in th league number
	 * and generates a leaderboard using the other leaderboard methods.
	 */
	public static void generateLeaderboard(int leagueNum) throws IOException
	{
		boolean textIntoArray; 
		textIntoArray = inputTextFiles(leagueNum);
		if (!textIntoArray)
		{
			JOptionPane.showMessageDialog(null, "Files have not been found.",
			"Files not found", JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			emptyLeaderboardFrame();
			calculateScores(leagueNum);
			orderLeaderBoard();
			displayLeaderboard();
		}
	}
	
	/**
	 * Louise Madden and Dylan King -
	 * inputTextFiles -
	 * The text files are passed into arrays for teams, fixtures and outcomes,
	 * ready to be used for calculations for the leaderboard.
	 */
	public static boolean inputTextFiles(int leagueNum) throws IOException
	{
		String participantsFile = leagueNum + "_participants.txt";
		String fixturesFile = leagueNum + "_fixtures.txt"; //text file names
		String outcomesFile = leagueNum + "_results.txt";

		File participantsInput = new File(participantsFile);
		File fixturesInput = new File(fixturesFile);
		File outcomesInput = new File(outcomesFile);

		leaderboardParticipants = new ArrayList<ArrayList<String>>();
		leaderboardParticipants.add(new ArrayList<String>());
		leaderboardParticipants.add(new ArrayList<String>());

		intFixtures = new ArrayList<ArrayList<Integer>>();
		intFixtures.add(new ArrayList<Integer>());
		intFixtures.add(new ArrayList<Integer>());
		intFixtures.add(new ArrayList<Integer>());

		intResults = new ArrayList<ArrayList<Integer>>();
		intResults.add(new ArrayList<Integer>());
		intResults.add(new ArrayList<Integer>());
		intResults.add(new ArrayList<Integer>());

		if (participantsInput.exists() && fixturesInput.exists() && outcomesInput.exists())
		{
			writeToArrayList(leaderboardParticipants, participantsInput);
			writeToIntArrayList(intFixtures, fixturesInput);
			writeToIntArrayList(intResults, outcomesInput);
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Dylan King and Lennart Mantel - 
	 * emptyLeaderboardFrame -
	 * Creates an empty leaderboard the appropriate size for the amount of teams there are.
	 */
	public static void emptyLeaderboardFrame()
	{ 
		int rows = leaderboardParticipants.get(0).size();
		int columns = 14;  
		leaderBoard = new int[rows][columns];
		for (int i = 0; i < leaderBoard.length; i++)
		{
			leaderBoard[i][0] = Integer.parseInt(leaderboardParticipants.get(0).get(i));
		}
	}	 

	/**
	 * Dylan King -
	 * calculateScores -
	 * A win, draw or loss is added for each team who played and
	 * points are awarded as such and added to the leaderboard
	 */
	public static void calculateScores(int leagueNum) throws IOException
	{
		File scoresFile = new File(leagueNum + "_pointscheme.txt");
		Scanner pointScan = new Scanner(scoresFile);
		String[] scoresStr = (pointScan.nextLine()).split(",");
		pointScan.close();
		int winPoints, losePoints, drawPoints;
		winPoints = Integer.parseInt(scoresStr[0]);
		drawPoints = Integer.parseInt(scoresStr[1]);
		losePoints = Integer.parseInt(scoresStr[2]);
		int fixtureNumber, homeTeamScore, awayTeamScore, homeTeamNumber, awayTeamNumber;
		int position;
		for (int i = 0; i < intResults.get(0).size(); i++)  
		{
			fixtureNumber = intResults.get(0).get(i); //explain this? double get 
			homeTeamScore = intResults.get(1).get(i);
			awayTeamScore = intResults.get(2).get(i);
			position = intFixtures.get(0).indexOf(fixtureNumber);
			homeTeamNumber = intFixtures.get(1).get(position);
			awayTeamNumber = intFixtures.get(2).get(position);
			if (homeTeamScore == awayTeamScore)
			{
				recordResultForHomeTeam(homeTeamNumber,0,1,0,homeTeamScore,awayTeamScore,drawPoints);
				recordResultForAwayTeam(awayTeamNumber,0,1,0,homeTeamScore,awayTeamScore,drawPoints);
			}  
			else if (homeTeamScore > awayTeamScore)
			{
				recordResultForHomeTeam(homeTeamNumber,1,0,0,homeTeamScore,awayTeamScore,winPoints);
				recordResultForAwayTeam(awayTeamNumber,0,0,1,homeTeamScore,awayTeamScore,losePoints);
			}  
			else
			{
				recordResultForHomeTeam(homeTeamNumber,0,0,1,homeTeamScore,awayTeamScore,losePoints);
				recordResultForAwayTeam(awayTeamNumber,1,0,0,homeTeamScore,awayTeamScore,winPoints);
			}    
		}
	}	 

	/**
	 * Dylan King - 
	 * recordResultForHomeTeam - 
	 * Takes in the home teams number, score, and the away teams score,
	 * and calculates all relavant stats for the leaderboard. 
	 */
	public static void recordResultForHomeTeam(int homeTeamNum, int wins, int draws, int losses, 
												int homeTeamScore, int awayTeamScore, int points)
	{
		leaderBoard[homeTeamNum-1][1]++;        								// gamesPlayed
		leaderBoard[homeTeamNum-1][2] += wins;      							// homeWin
		leaderBoard[homeTeamNum-1][3] += draws;      							// homeDraw
		leaderBoard[homeTeamNum-1][4] += losses;								// homeLoss
		leaderBoard[homeTeamNum-1][5] += homeTeamScore;    						// homeTeamScore
		leaderBoard[homeTeamNum-1][6] += awayTeamScore;    						// awayTeamScore
		int goalDif = homeTeamScore - awayTeamScore;
		if (goalDif < 0)
		{
			goalDif *= -1;
		}
		leaderBoard[homeTeamNum-1][12] += goalDif;    							// goalDifference
		leaderBoard[homeTeamNum-1][13] += points;    							// points
	}

	/**
	 * Dylan King - 
	 * recordResultForAwayTeam - 
	 * Takes in the away teams number, score, and the home teams score,
	 * and calculates all relavant stats for the leaderboard. 
	 */
	public static void recordResultForAwayTeam(int awayTeamNum, int wins, int draws, int losses, 
												int homeTeamScore, int awayTeamScore, int points)
	{
		leaderBoard[awayTeamNum-1][1]++;        								// gamesPlayed
		leaderBoard[awayTeamNum-1][7] += wins;      							// awayWin
		leaderBoard[awayTeamNum-1][8] += draws;      							// awayDraw
		leaderBoard[awayTeamNum-1][9] += losses;      							// awayLoss
		leaderBoard[awayTeamNum-1][10] += awayTeamScore;    					// awayTeamScore
		leaderBoard[awayTeamNum-1][11] += homeTeamScore;    					// homeTeamScore
		int goalDif = homeTeamScore - awayTeamScore;
		if (goalDif < 0)
		{
			goalDif *= -1;
		}
		leaderBoard[awayTeamNum-1][12] += (goalDif);    						// goalDifference
		leaderBoard[awayTeamNum-1][13] += points;    							// points  
	}	

	/**
	 * Louise Madden - 
	 * orderLeaderBoard - 
	 * Organises the leaderboard based on the total ponts of each team, in descending order.
	 */
	public static void orderLeaderBoard()
	{
		int [][] tempBoard = new int[leaderBoard.length][leaderBoard[0].length];
		boolean finished = false;
		while (!finished) 
		{
			finished = true;
			for (int i = 0; i < leaderBoard.length - 1; i++) 
			{
				if (leaderBoard[i][13] < leaderBoard[i + 1][13])
				{
					for (int j = 0; j < leaderBoard[i].length; j++) 
					{
						tempBoard[i][j] = leaderBoard[i][j];
						leaderBoard[i][j] = leaderBoard[i + 1][j];
						leaderBoard[i + 1][j] = tempBoard[i][j];
					}
					finished = false;
				}
			}
		}
	}

	/**
	 * Dylan King and Lennart Mantel
	 * displayLeaderBoard - 
	 * Takes all previous leaderboard information and displays it in a JOPtionPane Message Dialog.
	 */
	public static void displayLeaderboard()
	{
		String output;
		int aTeamNumber;
		String aTeamName, formatStringTeamName;
		String longestTeamName = leaderboardParticipants.get(1).get(0);
		int longestTeamNameLength = longestTeamName.length();

		for (int i = 1; i < leaderboardParticipants.get(1).size(); i++)
		{
			longestTeamName = leaderboardParticipants.get(1).get(i);  
			if (longestTeamNameLength < longestTeamName.length())
			{
				longestTeamNameLength = longestTeamName.length();
			}
		}
		formatStringTeamName = "%-" + (longestTeamNameLength + 15) + "s";
		output = String.format(formatStringTeamName, "Team Name");
		output += String.format("\t\tGP\tHW\tHD\tHL\tGF\tGA\tAW\tAD\tAL\tGF\tGA\tGD\tTP\n"); 
		for (int i = 0; i < leaderBoard.length; i++)
		{
			aTeamNumber = leaderBoard[i][0];
			aTeamName = leaderboardParticipants.get(1).get(aTeamNumber - 1);
			output += String.format(formatStringTeamName + "\t\t", aTeamName);
			output += String.format("%4d\t", leaderBoard[i][1]);
			output += String.format("%4d\t", leaderBoard[i][2]);
			output += String.format("%4d\t", leaderBoard[i][3]);
			output += String.format("%4d\t", leaderBoard[i][4]);
			output += String.format("%4d\t", leaderBoard[i][5]);
			output += String.format("%4d\t", leaderBoard[i][6]);
			output += String.format("%4d\t", leaderBoard[i][7]);
			output += String.format("%4d\t", leaderBoard[i][8]);
			output += String.format("%4d\t", leaderBoard[i][9]);
			output += String.format("%4d\t", leaderBoard[i][10]);
			output += String.format("%4d\t", leaderBoard[i][11]);
			output += String.format("%5d\t", leaderBoard[i][12]);
			output += String.format("%5d\t", leaderBoard[i][13]);
			output += "\n";
		}
		JTextArea result = new JTextArea(output);
		JOptionPane.showMessageDialog(null, result, "Current Leaderboard", JOptionPane.INFORMATION_MESSAGE);
	} 
}