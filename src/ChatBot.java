import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatBot extends JFrame{
	
	//contains possible English words
	private HashSet<String> wordContainer;
	// artist as key and the music
	// played by the artist as value
	private HashMap<String, ArrayList<String>> artistBank;
	// music as key and artist as value
	private HashMap<String, String> musicBank;
	
	// to clarify the user's intent
	// whether what the user types
	// means artist or music 
	private boolean mus;
	private boolean art;
	
	// when there exist words
	// both in artist and music list
	private boolean musOrArt;
	
	// true when it is time for the user
	// to type exactly what music 
	// the user wants to hear; false
	// otherwise
	private boolean musicDecided;
	
	// true when it is time for the user
	// to specify which artist the user
	// wants; false otherwise 
	private boolean chooseArtist;
	
	// true if the user types
	// without any error; false
	// otherwise
	private boolean firstStep;
	
	// chosen artist to show
	// the list of music played
	// by him or her
	private String artist;
	
	// the list of music the user chooses
	private String[] musicChoice;
	// the list of artist the user chooses
	private String[] artistChoice;
	
	//Typing Area:
	private JTextField txtEnter = new JTextField();

	//Chat Area:
	private JTextArea txtChat = new JTextArea();
	
	public ChatBot() throws IOException{
		// words relating to the type or genre of musi
		String[] musicTypeOrGenre = {"Alternative", "Anime", "Blues", "Children's Music"
				 ,"Classical", "Comedy", "Commercial", "Country", "Dance", "Disney", "Easy Listening"
				 ,"Electronic", "Enka", "French Pop", "German Folk", "German Pop", "Fitness", "Workout", 
				 "Hip-Hop", "Rap", "Holiday", "Indie Pop", "Industrial", "Inspirational", "Instrumental",
				 "J-Pop", "Jazz", "K-Pop", "Karaoke", "Kayokyoku", "Latin", "New Age", "Opera", "Pop", 
				 "R&B", "Reggae", "Rock", "Soundtrack", "Tex-Mex"}; 
		// words relating to music
		String[] musicWords = {"music", "melody", "opera", "piece", "rap", "rock", "singing", "soul",
				"tune"};
		// words relating to playing music
		String[] actionWords ={"play", "perform", "open up", "listen", "hear"};
		wordContainer = readFiles("words.txt");
		// create music and artist bank
		createMusAndArtBank();
		// Frame Attributes
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(600,600);
		this.setVisible(true);
		this.setResizable(false);
		this.setLayout(null);
		this.setTitle("JAVA BOT");
		this.setTitle("AI ROBOT for MUSIC");
		
		// txtEnter Attributes:
		txtEnter.setLocation(2, 540);
		txtEnter.setSize(590, 30);
		txtEnter.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent argO){
				String uText = txtEnter.getText();
				txtChat.append("You: " + uText + "\n");
				ArrayList<String> words  = getWords(uText);
				// assume that among words the user types, only nouns, 
				// which might have meaning relating to the title of music,
				// the name of artist, the action verb for playing music, or
				// words relevant to music, are picked.
				
				// first step
				if(!firstStep){
					// check if the user's text has any spelling error
					if(!checkValidWord(words)){
							botSay("AI bot does not understand. Please check the spelling.");
					// check if the user's text contains a word relevant to
					// music to understand the user's intent.
					}else if(checkWordBank(musicWords, words) > 0 || checkWordBank(musicTypeOrGenre, words) > 0 
							|| checkWordBank(actionWords, words) > 0){
						// if the user's intent is clear
						botSay("What music or artist are you looking for?");
						firstStep = !firstStep;
					}
				// second step
				}else{
					// the user has decided 
					// which music to play
					if(musicDecided){
						// the user types nothing.
						if(uText.length() == 0  || uText.length() > 10){
							botSay("You must type something."); 
						// 
						}else if((int)uText.charAt(0) < 49 || (int)uText.charAt(0) > 57){
							botSay("You must pick the number from the list.");		
						}else if((((int)uText.charAt(0)) - 49) < 1 ||
								musicChoice[(((int)uText.charAt(0)) - 49)] == null){
							botSay("Choose again.");
						}else{
							char choice = uText.charAt(0);
							int pos = ((int)choice) - 49;
							botSay("Give me a few seconds to play " + musicChoice[pos]);
						}
					// the user has decided 
					// the artist 
					}else if(chooseArtist){
						if(uText.length() == 0){
							botSay("You must type something."); 
						}else if((int)uText.charAt(0) < 49 || (int)uText.charAt(0) > 57 ){
							botSay("You must pick the number from the list.");		
						}else if((((int)uText.charAt(0)) - 49) < 1 ||
								artistChoice[(((int)uText.charAt(0)) - 49)] == null){
							botSay("Choose again.");
						}else{
							char choice = uText.charAt(0);
							int pos = ((int)choice) - 49;
							artist = artistChoice[pos];
							// display the list of music played
							// by the chosen artist
							displayMusicList();
						}
					}else{
						// just in case what the user types
						// is in both the artist name bank
						// and the music title bank
						if(!musOrArt){
							artistChoice = checkMusicOrArtist(uText.toLowerCase(), artistBank.keySet());
							musicChoice = checkMusicOrArtist(uText.toLowerCase(), musicBank.keySet());
							if(artistChoice[0] == null && musicChoice[0] == null){
								String guessArt = findSimilarWord(artistBank.keySet(), uText.toLowerCase());
								String guessMus = findSimilarWord(musicBank.keySet(), uText.toLowerCase());
								if(guessArt != null){
									artistChoice = checkMusicOrArtist(guessArt.toLowerCase(), artistBank.keySet());
									botSay("Do you mean " + guessArt + "?");
								}else if(guessMus != null){
									musicChoice = checkMusicOrArtist(guessMus.toLowerCase(),musicBank.keySet());
									botSay("Do you mean " + guessMus + "?");
								// no word similar to what the user types.
								}else{
									botSay("Please check the spelling.");
									return;
								}
							}
							if(musOrArt){
								musicChoice[0] = "1";
								artistChoice[0] = "0"; 
								musOrArt = !musOrArt;
								chooseArtist = !chooseArtist;
							}
							musOrArt = !musOrArt;
							if(artistChoice[0] != null && musicChoice[0] != null){
								botSay("Do you mean artist or music?");
								return;
							}else if(artistChoice[0] == null){
								mus = !mus;
							}else {
								art = !art;
							}
							// there exists either artist or 
							// music that matches the user's text
							execution();
						}else{
							// after the chatbot asks for the clear intent
							// between music title and artist's name, get the user 
							// input which is either music 
							if("music".equals(uText.toLowerCase())){
								mus = !mus;
							}else if("artist".equals(uText.toLowerCase())){
								art = !art;
							}else{
								botSay("You should type either artist or music.");
								return;
							}
							execution();
						}
					}
				}
				txtEnter.setText("");
			}
		});
		
		// txtChar Attributes:
		txtChat.setLocation(20,0);
		txtChat.setSize(560, 510);
		
		//Add  Items to Frame
		add(txtEnter);
		add(txtChat);
	}
	
	
	// method that returns a string of which the first character
	// indicates the percent of how approximate the user's text
	// and the word in the bank are. (yet to be used)
	private String findSimilarWord(Iterable<String> container, String input){
		int max = 0;
		String closest = "";
		for(String str : container){
			int count = 0;
			String word = "";
			if(str.length() >= input.length()){
				for(int i = 0; i < input.length(); i++){
					word += str.charAt(i);
					count += input.charAt(i) == str.charAt(i) ? 1 : 0;
				}
			}
			// 75% approximation
			if(max < count && count >= (input.length()/2 + ((input.length()/2)/2))){
				max = count;
				closest = word;
			}
		}
		// return null if there is no word approximate enough to 
		// 
		return max == 0 ? null : closest;
	}
	
	// method that displays list and
	// updates internal states to
	// take next step
	public void execution(){
		// the user chooses artist
		if(art){
				// there is more than one artist 
				// whose name matches what the
				// user types
				if(artistChoice[1] != null){
					getList(artistChoice);
					chooseArtist = !chooseArtist;
				// only one artist matching
				// the user's text
				}else {
					artist = artistChoice[0];
					displayMusicList();
			}
		// the user chooses music
		}else{
				if(musicChoice[1] != null){
					getList(musicChoice);
					musicDecided = !musicDecided;
				}else{
					botSay("Give me a few seconds to play " + musicChoice[0]);
				}
		}
	}
	
	// method that shows the list of music or artist
	// that has the user's text in common
	private void getList(String[] choice){
		String ans =  "There is more than one artist or music matching what you have typed. \n"
				+ "Choose the number from the list. ";
		String list = "";
		boolean stop = false;
		int index = 0;
		while(!stop){
			if(index < choice.length && choice[index] != null){
				list += (index+1) + ". " + choice[index] + "\n";
				index++;
			}else{
				stop = !stop;
			}
		}	
		botSay(ans + "\n" + list);
	}
	
	// method that displays the list of music 
	// if there is more than one music matching
	// the user's text or played by the artist
	private void displayMusicList(){
		String list = "";
		ArrayList<String> musicList = artistBank.get(artist);
		musicChoice = new String[musicList.size()];
		musicDecided = !musicDecided;
		for(int i = 0; i < musicList.size(); i++){
			String m = musicList.get(i);
			musicChoice[i] = m;
			list += (i+1) + ". " + m + "\n";
		}
		botSay("Choose the number from the list of music played by "
				 + artist + ": \n" + list);
	}
	
	// method that returns string array which
	// have the music or artist that matches
	// the user's text 
	private String[] checkMusicOrArtist(String text, Set<String> bank){
		int index = 0;
		String[] result = new String[100];
		for(String str : bank){
				if(str.toLowerCase().contains(text) && index < bank.size()){
					result[index] = str;
					index++;
				}
		}
		return result;	
	}
	
	// method that returns the number of words from
	// the user's text that are relevant to playing
	// music.
	private int checkWordBank(String[] container, ArrayList<String> text){
		int count = 0;
		for(String txt : text){
			for(String word : container){
				if(word.toLowerCase().equals(txt))
					count++;
			}
		}
		return count;
	}

	// method that reads the file and make a word bank
	// to check the validity of user's text
	private HashSet<String> readFiles(String fileName) throws IOException{
		HashSet<String> result = new HashSet<>();
		Path path = Paths.get(fileName);
        byte[] readBytes = Files.readAllBytes(path);
        String wordListContents = new String(readBytes, "UTF-8");
        String[] words = wordListContents.split("\n");
        for(String str : words){
        	result.add(str.toLowerCase());
        }
        return result;
	}
	
	// method that checks whether what the user types
	// has some spelling mistakes or unknown words
	private boolean checkValidWord(ArrayList<String> words){
		for(String str : words){
			boolean test = false;
			for(String w : wordContainer){
				if(w.equalsIgnoreCase(str)){
					test = !test;
				}
			}
			if(!test){
				return false;
			}
		}
		return true;
	}
	
	private void createTextFile()throws IOException{
		artistBank = new HashMap<String, ArrayList<String>>();
		Scanner input = new Scanner(new File("artists.txt"));
		String currentName = "";
		while(input.hasNextLine()){
			Scanner line = new Scanner(input.nextLine());
			String temp = line.next();
			if(temp.equals("•")){
				String name = copyRemainder(line);
				artistBank.put(name, new ArrayList<String>());
				currentName = name;
			}else{
				String song = temp + copyRemainder(line);
				artistBank.get(currentName).add(song);
			}
		}
	}
	
	// method that creates the maps for music and artist
	private void createMusAndArtBank() throws IOException{
		artistBank = new HashMap<String, ArrayList<String>>();
		Scanner input = new Scanner(new File("artists.txt"));
		String currentName = "";
		//while(input.hasNextLine()){
		//	Scanner line = new Scanner(input.nextLine());
		//	String temp = line.next();
		//	if(temp.equals("•")){
		//		String name = copyRemainder(line);
		//		artistBank.put(name, new ArrayList<String>());
		//		currentName = name;
		//	}else{
		//		String song = temp + copyRemainder(line);
		//		artistBank.get(currentName).add(song);
		//	}
		//	line.close();
		//}
		while(input.hasNextLine()){
			String line = input.nextLine();
			char header = line.charAt(0);
			if(header == '•'){
				int pos = 1;
				boolean test = true;
				while(test){
					if(line.charAt(pos+1) == ' '){
						pos++;
					}else{
						test = !test;
					}
				}
				String artist = line.substring(pos+1);
				currentName = artist;
				artistBank.put(artist, new ArrayList<String>());
			}else{
				String song = line;
				artistBank.get(currentName).add(song);
			}
		}
		flipKeyAndVal();
		input.close();
	}
	
	// method that copies the music title
	// followed by the artist's name until
	// the file is fully read or the next
	// artist appears
	private String copyRemainder(Scanner input){
		String result = "";
		while(input.hasNext()){
			result += input.next();
		}
		return result;	
	}
	
	// method that by copying the map, which has artist's name as key
	// and music title as value, create a new map
	// that has music title as key and artist's name as value
	private void flipKeyAndVal(){
		musicBank = new HashMap<String, String>();
		for(String key : artistBank.keySet()){
			for(String m : artistBank.get(key)){
				musicBank.put(m, key);
			}
		}
	}
	
	// method that takes what the user typed and split
	// it into separate words
	private ArrayList<String> getWords(String userText){
		ArrayList<String> result = new ArrayList<String>();
		// split into sentences 
	    String simple = "[.?!]";      
	    String[] splitString = userText.split(simple);
	    // split each sentence into words
	    for(String str : splitString){
	    	String sentence = str;
	    	String word = "";
	    	for(int i = 0; i < sentence.length(); i++){
	    	    if(sentence.charAt(i) == ' '){
	    	    	result.add(word);
	    	    	word = "";
	    	    }else{
	    	    	word += sentence.charAt(i);
	    	    }
	    	}
	    	result.add(word);
	    	word = "";
	    }
	    return result;
	}
	
	// method that types AI's answer
	public void botSay(String s){
		txtChat.append("AI: " + s + "\n");
	}
	
	public static void main(String [] args) throws IOException{
		new ChatBot();
	}
	
}

