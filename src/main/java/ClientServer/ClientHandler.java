package ClientServer;

import java.io.*;
import java.net.Socket;
import DAOs.BlockDaoInterface;
import DAOs.MySqlBlockDao;
import Exceptions.DaoException;

/**
 *  Written by Jakub Polacek on 13-14. and 21.4. 2024
 *  Used sample code from class as reference:
 *  github.com/logued/oop-client-server-multithreaded-2024
 *
 *  Feature 13 made by Ruby on 20.4.
 */

public class ClientHandler implements Runnable
{
    private  DataOutputStream dataOutputStream = null;
    private  DataInputStream dataInputStream = null;
    BufferedReader clientReader;
    PrintWriter clientWriter;
    Socket clientSocket;
    final int clientNumber;

    public ClientHandler(Socket clientSocket, int clientNumber)
    {
        this.clientSocket = clientSocket;
        this.clientNumber = clientNumber;
        try
        {
            this.clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            this.clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        String request;
        BlockDaoInterface IBlockDao = new MySqlBlockDao();
        try
        {
            while ((request = clientReader.readLine()) != null)
            {
                System.out.println("Server: (ClientHandler): Read command from client " + clientNumber + ": " + request);

                if (request.startsWith("F9"))
                {
                    String message = request.substring(2);
                    String blockAsJson = IBlockDao.blockToJson(Integer.parseInt(message));
                    clientWriter.println(blockAsJson);
                    System.out.println("Server message: JSON string of Block by id " + message + " sent to client.");
                }
                else if(request.startsWith("F12"))
                {
                    int idToDelete = Integer.parseInt(request.substring(3));
                    String block = IBlockDao.blockToJson(IBlockDao.deleteBlockById(idToDelete));
                    clientWriter.println(block);
                    System.out.println("Server message: Block by id " + idToDelete + " deleted.");
                }
                //by Ruby 20/4/2024
                else if(request.substring(0,3).equals("F13")){
                    // https://stackoverflow.com/a/5694398 referenced on 20/04/2024
                    File folder = new File("serverImages");
                    File[] listOfFiles = folder.listFiles();

                    String allImages = "";


                    if(listOfFiles != null) {
                        for (int i = 0; i < listOfFiles.length; i++) {
                            System.out.println(listOfFiles[i].getName());
                            if (listOfFiles[i].isFile()) {
                                allImages += listOfFiles[i].getName().substring(0, listOfFiles[i].getName().length()-4) + ((i != listOfFiles.length-1) ? " - " : "");
                            }
                        }
                    }

                    clientWriter.println(allImages);
                }
                //by Ruby 20/4/2024
                else if(request.substring(0,3).equals("img")){

                    dataInputStream = new DataInputStream(clientSocket.getInputStream());
                    dataOutputStream = new DataOutputStream( clientSocket.getOutputStream());
                    // Call SendFile Method
                    String imgPath = "serverImages/" + request.substring(3);
                    File f = new File(imgPath);
                    if(f.exists() && !f.isDirectory()) { //https://stackoverflow.com/a/1816676 on the 20/4/2024
                        try {
                            System.out.println("Successfully sent image!");
                            sendFile(imgPath);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else{
                        try {
                            System.out.println("Sent a default image since the file \"" + imgPath + "\" does not exist.");
                            sendFile("serverImages/noImage.png"); //sends default image if one does not exist
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                else if (request.startsWith("quit"))
                {
                    clientWriter.println("Sorry to see you leaving. Goodbye.");
                    System.out.println("Server message: Quit request from client, executed.");
                }
                else
                {
                    clientWriter.println("Error - invalid command");
                    System.out.println("Server message: Invalid request from client.");
                }
            }
        }
        catch (IOException | DaoException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            this.clientWriter.close();
            try
            {
                this.clientReader.close();
                this.clientSocket.close();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        System.out.println("Server: (ClientHandler): Handler for Client " + clientNumber + " is terminating .....");
    }

    /**
     * Made by Ruby
     */
    private void sendFile(String path)
            throws Exception
    {
        int bytes = 0;
        // Open the File at the specified location (path)
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);

        // send the length (in bytes) of the file to the server
        dataOutputStream.writeLong(file.length());

        // Here we break file into chunks
        byte[] buffer = new byte[4 * 1024]; // 4 kilobyte buffer

        // read bytes from file into the buffer until buffer is full or we reached end of file
        while ((bytes = fileInputStream.read(buffer))!= -1) {
            // Send the buffer contents to Server Socket, along with the count of the number of bytes
            dataOutputStream.write(buffer, 0, bytes);
            dataOutputStream.flush();   // force the data into the stream
        }
        // close the file
        fileInputStream.close();
    }
}