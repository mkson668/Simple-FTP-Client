

## Simple FTP client
In goal of this project is to use the Java socket related classes to create an FTP client. The program is to provide some
basic FTP client functionality through a simple text-based interface. A subset of the FTP commands in RFC 959 were
implemented. The program will provide a simple command line interface to the user. The program will read a command
typed by the user, and then interpret the command according to the command descriptions given below. Once the program
has finished processing the command it will wait for another user command.

## Configuring your environment

None

## Commands

### The following describes the commands from the user that the program accepts

based on the response will prompt for user name and password and send the user name.
if no password is needed then discard otherwise use.

Application Command	Description	FTP command to server

> user USERNAME

Sends the username to the FTP server. The user will need to pay attention to the response code to determine if the
password command must be sent. This is, typically, the first command the user will enter.

> pw PASSWORD

Sends the PASSWORD to the FTP server. For an anonymous server the user would typically enter an email address or
anonymouspassword command must be sent. This typically the second command the user will enter.

> quit

If connected, sends a QUIT to the server, and closes any established connection and then exits the program. This command
is valid at any time.


> get REMOTE

Establishes a data connection and retrieves the file indicated by REMOTE, saving it in a file of the same name on the
local machine.

> features

Requests the set of features/extensions the server supports. Your program doesn't have to do anything with the result
except print the response in the appropriate way.

> cd DIRECTORY

Changes the current working directory on the server to the directory indicated by DIRECTORY.

> dir

Establishes a data connection and retrieves a list of files in the current working directory on the server. The list is
printed to standard output.


