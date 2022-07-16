package gambaBot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.ArrayList;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {
	private String prefix = "!";
	private BlackJack game;
	private boolean playing = false;
	private int win = 0; //0 = lose, 1 = win, 2 = draw
	private int betAmount = 0;
	private String id = "";

	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String[] args = event.getMessage().getContentRaw().split(" ");
		
		// used for testing
		if (args[0].equalsIgnoreCase(prefix + "test")) {
			String id = event.getAuthor().getId();
			String credStr = "";
			int credInt = 0;

			try (Connection c = DriverManager.getConnection("jdbc:sqlite:gamba.db")) {
				Statement s = c.createStatement();
				ResultSet rs = s.executeQuery("SELECT Credits FROM User WHERE UserID = " + id + ";");
				credStr = rs.getString("Credits");
				credInt = Integer.parseInt(credStr) + 10;
				credStr = String.valueOf(credInt);

				s.execute("UPDATE User SET Credits = " + credInt + " WHERE UserID = " + id + ";");
				event.getChannel().sendMessage(credStr).queue();

			} catch (SQLException e) {
				e.printStackTrace();
			}

			// event.getChannel().sendMessage("working").queue();
		}

		// ================rock paper scissors game======================
		if (args[0].equalsIgnoreCase(prefix + "play")) {
			String id = event.getAuthor().getId();

			String[] hand = { "rock", "paper", "scissors" };
			Random generate = new Random();
			int x = generate.nextInt(hand.length);

			// rock out comes
			if (args[1].equals("rock")) {
				event.getMessage().reply(hand[x]).queue();
				if (hand[x].equals("paper")) {
					event.getChannel().sendMessage("You Lose").queue();
				} else if (hand[x].equals("scissors")) {
					event.getMessage().reply("You Win!").queue();
				} else {
					event.getMessage().reply("Draw").queue();
				}
				// paper out comes
			} else if (args[1].equals("paper")) {
				event.getMessage().reply(hand[x]).queue();
				if (hand[x].equals("scissors")) {
					event.getMessage().reply("You Lose").queue();
				} else if (hand[x].equals("rock")) {
					event.getMessage().reply("You Win!").queue();
				} else {
					event.getMessage().reply("Draw").queue();
				}
				// scissors out comes
			} else if (args[1].equals("scissors")) {
				event.getMessage().reply(hand[x]).queue();
				if (hand[x].equals("rock")) {
					event.getMessage().reply("You Lose").queue();
				} else if (hand[x].equals("paper")) {
					event.getMessage().reply("You Win!").queue();
				} else {
					event.getMessage().reply("Draw").queue();
				}
			}
		}
		// =======================================================

		// =======================blackjack=======================

		if (args[0].equalsIgnoreCase(prefix + "blackjack")) {
			game = new BlackJack();
			playing = true;
			game.setPlayerHands(event);
			event.getChannel().sendMessage("===============================").queue();
			game.setBotHands(event);
			
			//set bet amount
			try {
				if(args[1] != "") {
					String bet = args[1].toString();
					betAmount = Integer.parseInt(bet);
				}
			}catch(ArrayIndexOutOfBoundsException e) {
				System.out.println(e);
			}
		}
		
		if (args[0].equalsIgnoreCase(prefix + "hit") && playing == true) {
			game.playerHit(event);
			if (game.isPalyerBusted() == true) {
				event.getMessage().reply("Busted You Lose").queue();
				playing = false;
				win = 0;
			}

		} else if (args[0].equalsIgnoreCase(prefix + "stand") && playing == true) {

			game.botCount();
			int x = game.getBotNum();

			while (x < 19) {
				game.botHit(event);
				x = game.getBotNum();
				if (game.isBotBusted() == true) {
					event.getMessage().reply("Bot Total: " + x + " Bot Busted You Win").queue();
					playing = false;
					win = 1;
					break;
				}

			}

			if (game.isBotBusted() != true) {
				if (game.getPlayerNum() > game.getBotNum()) {
					event.getMessage().reply("Bot Total: " + x + " You Win").queue();
					playing = false;
					win = 1;
				}
				if (game.getPlayerNum() < game.getBotNum()) {
					event.getMessage().reply("Bot Total: " + x + " You Lose").queue();
					playing = false;
					win = 0;
				}
				if (game.getPlayerNum() == game.getBotNum()) {
					event.getMessage().reply("Bot Total: " + x + " Draw").queue();
					playing = false;
					win = 2;
				}
			}
			
			String id = event.getAuthor().getId();
			String credStr = ""; 
			int credInt = 0;
			
			if(betAmount != 0) {
				//connect to sqllite and update credit accordingly 
				try (Connection c = DriverManager.getConnection("jdbc:sqlite:gamba.db")){
					Statement s = c.createStatement();
					ResultSet rs = s.executeQuery("SELECT Credits FROM User WHERE UserID = " + id +";");
					credStr = rs.getString("Credits");
					credInt = Integer.parseInt(credStr);	
					if (win == 0)//lose
						credInt = credInt - betAmount;
					else if (win == 1)//win
						credInt = credInt + betAmount*2;
					
					//draw user dont gain or lose so re-use credInt
					credStr = String.valueOf(credInt);
					
					s.execute("UPDATE User SET Credits = " + credInt + " WHERE UserID = " + id +";");
					event.getChannel().sendMessage("Your new Balance: " + credStr).queue();
	
				}catch(SQLException e) {
					e.printStackTrace();
				}
			}
			
		}
		

		// ========================================================

		// ========================roulette========================
		if (args[0].equalsIgnoreCase(prefix + "roulette") && args[1] != "") {
			roulette.getValue(event);
			String str = roulette.outcome;
			StringBuilder sb = new StringBuilder();
			boolean found = false;

			for (char c : str.toCharArray()) {
				if (Character.isDigit(c)) {
					sb.append(c);
					found = true;
				} else if (found) {
					// If we already found a digit before and this char is not a digit, stop looping
					break;
				}
			}

			String x = sb.toString();
			int num;
			try {
				num = Integer.parseInt(x);
			} catch (NumberFormatException e) {
				num = 0;
			}

			if (args[1] == sb.toString()) {
				event.getChannel().sendMessage("win").queue();

			} else if (args[1].equalsIgnoreCase("even")) {
				if (num == 0) {

				} else if (num % 2 == 0) {
					event.getChannel().sendMessage("win").queue();
				}

			} else if (args[1].equalsIgnoreCase("odd")) {
				if (num == 0) {

				} else if (num % 2 != 0) {
					event.getChannel().sendMessage("win").queue();
				}

			} else if (args[1].equalsIgnoreCase("1-18")) {
				int min = 1;
				int max = 18;
				if (range(num, min, max)) {
					event.getChannel().sendMessage("win").queue();
				}

			} else if (args[1].equalsIgnoreCase("19-36")) {
				int min = 19;
				int max = 36;
				if (range(num, min, max)) {
					event.getChannel().sendMessage("win").queue();
				}

			} else if (args[1].equalsIgnoreCase("1st12")) {
				int min = 1;
				int max = 12;
				if (range(num, min, max)) {
					event.getChannel().sendMessage("win").queue();
				}
			} else if (args[1].equalsIgnoreCase("2st12")) {
				int min = 13;
				int max = 24;
				if (range(num, min, max)) {
					event.getChannel().sendMessage("win").queue();
				}

			} else if (args[1].equalsIgnoreCase("3st12")) {
				int min = 25;
				int max = 36;
				if (range(num, min, max)) {
					event.getChannel().sendMessage("win").queue();
				}

			} else if (args[1].equalsIgnoreCase("black")) {
				if (roulette.outcome.contains("black")) {
					event.getChannel().sendMessage("win").queue();
				}
			} else if (args[1].equalsIgnoreCase("red")) {
				if (roulette.outcome.contains("red")) {
					event.getChannel().sendMessage("win").queue();
				}
			} else {
				event.getChannel().sendMessage("lose").queue();
			}

		}

		// ==============================================================

		// =======================add users=============================
		if (args[0].equalsIgnoreCase(prefix + "addme")) {
			String id = event.getAuthor().getId();
			String name = event.getAuthor().getName();
			String sql = "INSERT INTO User(Userid, Name, Credits) VALUES(?,?,?)";
			try (Connection c = DriverManager.getConnection("jdbc:sqlite:gamba.db");
					PreparedStatement pstmt = c.prepareStatement(sql)) {
				pstmt.setString(1, id);
				pstmt.setString(2, name);
				pstmt.setInt(3, 100);
				pstmt.executeUpdate();
				event.getMessage().reply("You have been added. you have 100 creds").queue();
			} catch (SQLException e) {
				e.printStackTrace();
				event.getMessage().reply("You have already been added!").queue();
			}
		}
	}
	// ========================================================================

	private static boolean range(int num, int min, int max) {
		return num >= min && num <= max;
	}

}