
import java.lang.System;
import java.io.IOException;
import java.io.*;
import java.net.*;
import java.util.Scanner;

//
// This is an implementation of a simplified version of a command 
// line ftp client. The program always takes two arguments
//


public class CSftp
{
	//static final int MAX_LEN = 255;
	static final int ARG_CNT = 2;
	static private Scanner sc = new Scanner(System.in);

	public static void main(String [] args)
	{
		//byte cmdString[] = new byte[MAX_LEN];
		// Get command line arguments and connected to FTP
		// If the arguments are invalid or there aren't enough of them
		// then exit.

		if (args.length != ARG_CNT) {
			System.out.print("Usage: cmd ServerAddress ServerPort\n");
			return;
		}

		String hostname = args[0];
		int port = Integer.parseInt(args[1]);
		String fromServer;
		Boolean loggedIn = false;


		try {
            Socket csftpSocket = new Socket(hostname, port);
			PrintWriter out = new PrintWriter(csftpSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(csftpSocket.getInputStream()));

			//TODO: maybe check for code in assn doc
			System.out.println(in.readLine());

			while (true) {

				System.out.print("csftp> ");
				String input = safeScan(sc,csftpSocket, out, in);
				String[] inputArr = splitTrimString(input);

				String cmd = inputArr[0];
				if (cmd.equals("user")) {
					if (!checkWrongArgs(input, 2)) {
						System.out.println("0x002 Incorrect number of arguments");
						continue;
					}

					String username = inputArr[1];
					System.out.println("--> " + "USER " + username);
					out.println("USER " + username + "\r\n");

					fromServer = in.readLine();
					System.out.println("<-- " + fromServer);

				} else if (cmd.equals("pw")) {
					if (!checkWrongArgs(input, 2)) {
						System.out.println("0x002 Incorrect number of arguments");
						continue;
					}

					String password = inputArr[1];
					System.out.println("--> " + "PASS " + password);
					out.println("PASS " + password + "\r\n");

					fromServer = in.readLine();
					System.out.println("<-- " + fromServer);

				} else if (cmd.equals("quit")) {
					if (!checkWrongArgs(input, 1)) {
						System.out.println("0x002 Incorrect number of arguments");
						continue;
					}
					String quit = inputArr[0];
					System.out.println("--> QUIT");
					out.println("QUIT\r\n");

					fromServer = in.readLine();
					System.out.println("<-- " + fromServer);
					csftpSocket.close();
					try {
						out.close();
						in.close();
					} catch (IOException err) {
						System.out.println(err);
					}
					System.exit(0);

				} else if (cmd.equals("get")) {
					if (!checkWrongArgs(input, 2)) {
						System.out.println("0x002 Incorrect number of arguments");
						continue;
					}

					out.println("TYPE I");
					System.out.println("--> TYPE I");

					fromServer = in.readLine();
					System.out.println("<-- " + fromServer);
					if (!checkWrongArgs(input, 2)) {
						System.out.println("0x002 Incorrect number of arguments");
						continue;
					}
					String remote = inputArr[1];
					out.println("PASV\r\n");
					System.out.println("--> PASV");

					fromServer = in.readLine();
					System.out.println("<-- " + fromServer);

					boolean enteredPASV = fromServer.substring(0, 3).equals("227");
					String[] ipAndPortStr = new String[6];
					if (enteredPASV) {
						String[] ipAndPort;
						int openBracket = fromServer.indexOf("(");
						int closeBracket = fromServer.indexOf(")");
						String numbers = fromServer.substring(openBracket + 1, closeBracket);
						ipAndPort = numbers.split(",");
						int counter = 0;
						for (String s : ipAndPort) {
							ipAndPortStr[counter] = s;
							counter++;
						}
					} else {
                        System.out.println("0x3A2 Data transfer connection to " + hostname + " on port "
                                + port + " failed to open.");
                        continue;
					}
					String ip = "";
					for (int i = 0; i < 3; i++) {
						ip = ip.concat(ipAndPortStr[i]);
						ip = ip.concat(".");
					}
					ip = ip.concat(ipAndPortStr[3]);

					int dataSocketHost = Integer.parseInt(ipAndPortStr[4])*256 + Integer.parseInt(ipAndPortStr[5]);
					Socket dataSocket = new Socket(ip, dataSocketHost);

					DataInputStream dataIn = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));

					out.println("RETR " + remote + "\r\n");

					System.out.println("--> " + "RETR " + remote);

					fromServer = in.readLine();
					System.out.println("<-- " + fromServer);

					byte[] buffer = new byte[dataIn.available()];
					dataIn.read(buffer);

					int slashIndex = remote.lastIndexOf("/");
					String fileName = remote.substring(slashIndex + 1);

					try (FileOutputStream fos = new FileOutputStream(fileName)) {
						fos.write(buffer);
					} catch (Exception err) {
						System.out.println("0x3A7 Data transfer connection I/O error, closing data connection.");
                        try {
                            dataSocket.close();
                            dataIn.close();
                            continue;
                        } catch (Exception e2) {
                            continue;
                        }
					}

					dataSocket.close();
					dataIn.close();
				} else if (cmd.equals("features")) {
					if (!checkWrongArgs(input, 1)) {
						System.out.println("0x002 Incorrect number of arguments");
						continue;
					}

					out.println("FEAT\r\n");
					System.out.println("--> FEAT");

					fromServer = in.readLine();
					System.out.println("<-- " + fromServer);

					String feats;
					while (!(feats = in.readLine()).contains("211 End")) {
							System.out.println("<-- " + feats);
					}
					System.out.println("<-- " + feats);

				} else if (cmd.equals("cd")) {
					if (!checkWrongArgs(input, 2)) {
						System.out.println("0x002 Incorrect number of arguments");
						continue;
					}

					String path = inputArr[1];

					out.println("CWD " + path + "\r\n");
					fromServer = in.readLine();
					System.out.println("<-- " + fromServer);

				} else if (cmd.equals("dir")) {
					if (!checkWrongArgs(input, 1)) {
						System.out.println("0x002 Incorrect number of arguments");
						continue;
					}

					out.println("TYPE A");
					System.out.println("--> TYPE A");

					fromServer = in.readLine();
					System.out.println("<-- " + fromServer);

					out.println("PASV\r\n");
					fromServer = in.readLine();
					System.out.println("<-- " + fromServer);

					boolean enteredPASV = fromServer.substring(0, 3).equals("227");
					String[] ipAndPortStr = new String[6];
					if (enteredPASV) {
						String[] ipAndPort;
						int openBracket = fromServer.indexOf("(");
						int closeBracket = fromServer.indexOf(")");
						String numbers = fromServer.substring(openBracket + 1, closeBracket);
						ipAndPort = numbers.split(",");
						int counter = 0;
						for (String s : ipAndPort) {
							ipAndPortStr[counter] = s;
							counter++;
						}
					} else {
                        System.out.println("0x3A2 Data transfer connection to " + hostname + " on port "
                                + port + " failed to open.");
                        continue;
					}
					String ip = "";
					for (int i = 0; i < 3; i++) {
						ip = ip.concat(ipAndPortStr[i]);
						ip = ip.concat(".");
					}
					ip = ip.concat(ipAndPortStr[3]);

					int dataSocketHost = Integer.parseInt(ipAndPortStr[4])*256 + Integer.parseInt(ipAndPortStr[5]);
					Socket dataSocket = new Socket(ip, dataSocketHost);

					BufferedReader dataReaderIn = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
					out.println("LIST\r\n");
					fromServer = in.readLine();
					System.out.println("<-- " + fromServer);

					String directory;
					try {
                        while ((directory = dataReaderIn.readLine()) != null) {
                            System.out.println("<-- " + directory);
                        }
                    } catch (Exception e) {
                        System.out.println("0x3A7 Data transfer connection I/O error, closing data connection.");
                        try {
                            dataSocket.close();
                            dataReaderIn.close();
                            continue;
                        } catch (Exception e1) {
                            continue;
                        }
                    }

				} else {
					System.out.println("0x001 Invalid command");
				}

			}
		} catch (IOException exception) {
			System.err.println("0xFFFD Control connection I/O error, closing control connection.");
            System.exit(1);

		}
	}

	private static String[] splitTrimString(String input) {
		String[] inputArr = input.split(" ", 2);
		for (int i = 0; i < inputArr.length; i++) {
			inputArr[i] = inputArr[i].replace("\t", "").trim();
			//inputArr[i].replaceAll(" +", " ");
		}
		return inputArr;
	}

	private static boolean checkWrongArgs(String s, int number) {
		boolean firstChar = (s.indexOf(" ") == 0 || s.indexOf("\t") == 0);
		String[] a = s.split("[ |\t][/|[0-9]|a-zA-Z]");
		int lng = a.length;
		if (firstChar) {
			lng--;
		}
		return lng == number;
	}

    private static String safeScan(Scanner sc, Socket s, PrintWriter p, BufferedReader b) {
        try {
            return sc.nextLine();
        } catch (Exception e) {
            System.out.print("0xFFFE Input error while reading commands, terminating.");
            try {
                s.close();
                p.close();
                b.close();
            } catch (IOException err) {
                System.out.println(err);
            }
            System.exit(1);
            return "";
        }
    }
}


