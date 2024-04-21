package ClientServer;

/**
 *  Written by Jakub Polacek on 13-14.4. 2024
 *  Used sample code from class as reference:
 *  github.com/logued/oop-client-server-multithreaded-2024
 */

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import com.google.gson.Gson;
import DTOs.Block;

public class Client
{
    private  DataOutputStream dataOutputStream = null;
    private  DataInputStream dataInputStream = null;
    public static void main(String[] args)
    {
        Client client = new Client();
        client.start();
    }
    public void start()
    {
        try (Socket socket = new Socket("localhost", 7420);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())))
        {
            System.out.println("Client message: The Client is running and has connected to the server");
            Scanner keyboardInput = new Scanner(System.in);
            System.out.println("Please enter a command: ");
            System.out.println("Available commands: F9, F11, F13 quit");
            String clientCommand = keyboardInput.nextLine();

            while(true)
            {
                if (clientCommand.equals("F9"))
                {
                    System.out.println("Function 9 selected - Display Block by ID");
                    System.out.println("Please input the desired ID to be found:");
                    clientCommand = keyboardInput.nextLine();
                    out.println("F9"+clientCommand);
                    String response = in.readLine();
                    Gson gsonParser = new Gson();
                    Block block = gsonParser.fromJson(response,Block.class);
                    //TODO make output nicer
                    if(block != null)
                    {
                        System.out.println(block.toString());
                    }
                    else
                    {
                        System.out.println("Invalid ID.");
                    }
                }
                else if (clientCommand.equals("F11"))
                {
                    System.out.println("Function 11 selected - Insert Block");
                    Block blockToAdd = createBlock();

                    Gson gsonParser = new Gson();
                    String blockJson = gsonParser.toJson(blockToAdd);

                    out.println("F11"+blockJson);
                    String response = in.readLine();
                    System.out.println(response);

                    //by Ruby 20/4/2024
                }
                else if (clientCommand.equals("F13")) {
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream( socket.getOutputStream());
                    // Here we call receiveFile define new for that file

                    out.println("F13");
                    System.out.println("All Images:");

                    String response = in.readLine();

                    System.out.println(response);

                    Scanner key = new Scanner(System.in); //making a new scanner since using the same one with next line can be funky

                    String imageName = key.nextLine() + ".png";

                    out.println("img" + imageName);
                    try {
                        receiveFile("clientImages/" + imageName);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    dataInputStream.close();
                    dataOutputStream.close();
                }
                else if (clientCommand.startsWith("quit"))
                {
                    out.println(clientCommand);
                    String response = in.readLine();
                    System.out.println("Client message: Response from server: \"" + response + "\"");
                    break;
                }
                else
                {
                    System.out.println("Command unknown. Try again.");
                }

                keyboardInput = new Scanner(System.in);
                System.out.println("Please enter a command: ");
                clientCommand = keyboardInput.nextLine();
            }
        }
        catch (IOException e)
        {
            System.out.println("Client message: IOException: " + e);
        }
        System.out.println("Exiting client, server may still be running.");
    }

    //Made by Ruby
    public static Block createBlock(){

        Scanner key = new Scanner(System.in);

        System.out.print("Enter the name (or enter \"Close\" to exit): ");
        String name = key.nextLine();

        Block newBlock = null;

        if(!name.equalsIgnoreCase("close")) {

            System.out.print("Enter the hardness: ");
            double hardness = key.nextDouble();

            System.out.print("Enter the blast resistance: ");
            double blastResistance = key.nextDouble();

            System.out.print("Is gravity affected? (0 = False, 1 = True): ");
            boolean gravityAffected = (key.nextInt() == 0) ? false : true;

            newBlock = new Block(name, hardness, blastResistance, gravityAffected);
        }
        return newBlock;
    }
    private void receiveFile(String fileName)
            throws Exception
    {
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);


        // DataInputStream allows us to read Java primitive types from stream e.g. readLong()
        // read the size of the file in bytes (the file length)
        long size = dataInputStream.readLong();
        System.out.println("Server: file size in bytes = " + size);


        // create a buffer to receive the incoming bytes from the socket
        byte[] buffer = new byte[4 * 1024];         // 4 kilobyte buffer

        System.out.println("Server:  Bytes remaining to be read from socket: ");

        // next, read the raw bytes in chunks (buffer size) that make up the image file
        while (size > 0 &&
                (bytes = dataInputStream.read(buffer, 0,(int)Math.min(buffer.length, size))) != -1) {

            // above, we read a number of bytes from stream to fill the buffer (if there are enough remaining)
            // - the number of bytes we must read is the smallest (min) of: the buffer length and the remaining size of the file
            //- (remember that the last chunk of data read will usually not fill the buffer)

            // Here we write the buffer data into the local file
            fileOutputStream.write(buffer, 0, bytes);

            // reduce the 'size' by the number of bytes read in.
            // 'size' represents the number of bytes remaining to be read from the socket stream.
            // We repeat this until all the bytes are dealt with and the size is reduced to zero
            size = size - bytes;
            System.out.print(size + ", ");
        }

        System.out.println("File is Received");

        System.out.println("Look in the \"clientImages\" folder to see the transferred image.");
        fileOutputStream.close();
    }
}
