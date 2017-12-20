import java.io.DataInputStream;  
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;  
import java.net.Socket;  
import java.util.HashMap;  
import java.util.Iterator;  
import java.util.Map;  
  
public class Server {  
  
    private Map<String, Socket> sockets = new HashMap<String, Socket>();  
      
    /** 
     * @param args 
     * @throws IOException  
     */  
    public static void main(String[] args) throws IOException {  
            new Server().execute();  
    }  
      
    public void execute() throws IOException{  
        ServerSocket ss = null;  
        try {  
            ss = new ServerSocket(1234);  
            System.out.println("server is running");  
        } catch (IOException e1) {  
            e1.printStackTrace();  
        }  
        Socket s = null;   
        String clientName = null;
        try {
	        while(true){  
	            try {  
	                s = ss.accept();
	                DataInputStream dis = null;
	                try {  
	                     dis = new DataInputStream(s.getInputStream());  
	                } catch (IOException e) {  
	                    e.printStackTrace();  
	                }  
	              
	                clientName = dis.readUTF();  
	                sockets.put(clientName, s);  
	//               sendMessageFeedback(clientName);  
	                System.out.println(clientName + " gets connection!");  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }  
	            ReadAndWriteThread t = new ReadAndWriteThread(s);  
	            t.setName(clientName);
	            new Thread(t).start();  
	//            ss.close();
	            }
	        }finally {
	    		ss.close();
	       	} 
 }  
      
//    private void sendMessageFeedback(String clientName) {  
//
//                Socket s = sockets.get(clientName);  
//                try {  
//                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());  
//                    dos.writeUTF("this is "+clientName); 
//                    dos.flush();
//                } catch (IOException e) {  
//                    e.printStackTrace();  
//                }  
//            }  
//         
     
      
    class ReadAndWriteThread implements Runnable{  
        private DataInputStream dis = null;    
        private String name;
        private FileOutputStream fos;
        public ReadAndWriteThread(Socket s){  
            try {  
                dis = new DataInputStream(s.getInputStream());  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
        public void setName(String name)
        {
            this.name = name;
        }
        @Override  
        public void run() {  
            String str = "";  
            while(true){  
                try {  
                    str = dis.readUTF();  
                    System.out.println(str);
                    if(str.equals("exit")){                   	
                    	System.out.println(name + " exit");
                    	break;
                    }
                    String[] index = str.split(" ");
                    
                    if(index[1].equals("message")||index[0].equals("block")){
                        dealWithMessageInput(str,name);  
                    }
                    else if(index[1].equals("file")) {
                    	if(index[0].equals("broadcast")){
                    		fos = new FileOutputStream(new File(index[2].substring(index[2].lastIndexOf("/")+1))); 
                    	}
                    	else if(index[0].equals("unicast")){
                    		fos = new FileOutputStream(new File(index[3].substring(index[3].lastIndexOf("/")+1))); 
                    	}
                        int count = 0;  
                        while (count == 0) {  
                            count = dis.available();  
                        }  
                        byte[] b = new byte[count];  
                        dis.read(b);  
                        fos.write(b,0,count);
                        fos.flush();
                        dealWithFileInput(str,name);
//                        byte[] sendBytes = new byte[8192];
//                        int read = 0;
//                        while(true) {
//                            read = dis.read(sendBytes);
//                            if(read == -1)
//                                break;
//                            fos.write(sendBytes, 0, read);
//                            fos.flush();
////                            if(dis.available()==0)
////                            	break;
//                        dealWithFileInput(str);
//                         }

                  }
                    } catch (IOException e) {  
                  e.printStackTrace();  
                 }  
             }  
        }  
      
      
    private void dealWithMessageInput(String str, String name){  
        	
        	String[] index = str.split(" ");
        	String mode = index[0];
    	
        	if(mode.equals("broadcast")&&index[1].equals("message")){
            		System.out.println(name+" "+str);
            		sendMessageToAllClients(str,name);
        	}
    
        	if(mode.equals("unicast")&&index[1].equals("message")){
        		    System.out.println(name+" unicast a message to "+index[2]);
            		String target = index[2];
            		sendMessageToSingleClient(target,str);
        	}    	
    
        	if(mode.equals("block")){
        		    System.out.println(name+" block "+index[1]);
            		String blockName = index[1];
            		sendMessageToClientsExceptOne(blockName,str,name);
        	}
    	
    }
    
    private void dealWithFileInput(String str, String name) {

          String[] index = str.split(" ");
          String mode = index[0];
         
          if(mode.equals("broadcast")&&index[1].equals("file")){
        	  System.out.println(name+" broadcast file" + index[2].substring(index[2].lastIndexOf("/")+1));
              if(sockets != null && sockets.size() > 0) {  
                  for (String key : sockets.keySet()) {  
        				if(key.equals(name))
          					continue;
                        Socket s = sockets.get(key);  
                        try {  
                            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
                            dos.writeUTF(str + " " + key);                            
                            dos.flush();
                          	byte[] bytes = new byte[2048];
                          	
                            File inputFile = new File(index[2].substring(index[2].lastIndexOf("/")+1));
                           InputStream is = new FileInputStream(inputFile);
                           int len = 0;  
                           while ((len = is.read(bytes)) != -1) {  
                                 dos.write(bytes, 0, len);  
                                 dos.flush();
//                                 if(is.available()==0)
//                                 	break;
                           }
                           is.close();
//                            byte[] sendBytes = new byte[8192];
//                            int read = 0;
//                           while(true) {
//                               
//                             if(read == -1)
//                                 break;
//                               read = dis.read(sendBytes);
 //                             dos.write(byte,0,count);  
//                               dos.flush();
//                            	  System.out.println(sendBytes+"11");
//
//                            }
                            } catch (IOException e) {  
                                e.printStackTrace();  
                            }  
                        }  
                    }  
               }
          
          if(mode.equals("unicast")&&index[1].equals("file")) {
               Socket s = sockets.get(index[2]);  
               System.out.println(name+" unicast file " +index[3].substring(index[3].lastIndexOf("/")+1)+ " to " + index[2]);
               DataOutputStream dos = null;  
               try {  
                   if(s != null) {  
                       dos = new DataOutputStream(s.getOutputStream()); 
                       dos.writeUTF(str);                             
                       dos.flush();
                      byte[] bytes = new byte[2048];  
                      File inputFile = new File(index[3].substring(index[3].lastIndexOf("/")+1));
                      InputStream is = new FileInputStream(inputFile);
                      int len = 0;  
                      while ((len = is.read(bytes)) != -1) {  
                            dos.write(bytes, 0, len);  
                            dos.flush();
//                            if(is.available()==0)
//                            	break;
//                       dos = new DataOutputStream(s.getOutputStream());  
//                       dos.writeUTF(str);  
//                       dos.flush();
//                       byte[] sendBytes = new byte[8192];
//                       int read = 0;
//                      while(true) {
//                          
//                        if(read == -1)
//                            break;
//                          read = dis.read(sendBytes);
//                          dos.write(sendBytes,0,read);  
//                          dos.flush();
//                       	  System.out.println(read);
//                       	  System.out.println("1");
                      }
                      is.close();
                   }  
               } catch (IOException e) {  
                       e.printStackTrace();  
                } 
          }
    }


    
	private void sendMessageToAllClients(String str, String name){
		if(sockets != null && sockets.size() > 0){  
		  for (String key : sockets.keySet()) {   
				if(key.equals(name))
  					continue;
	            Socket s = sockets.get(key);  
	            try {  
	                DataOutputStream dos = new DataOutputStream(s.getOutputStream());  
	                dos.writeUTF(str);  
	                dos.flush();
	                } catch (IOException e) {  
	                    e.printStackTrace();  
	                }  
	            }  
	        }  
       }  
	
	private void sendMessageToSingleClient(String name,String str){
		Socket s = sockets.get(name);  
        DataOutputStream dos = null;  
        try {  
            if(s != null){  
                dos = new DataOutputStream(s.getOutputStream());  
                dos.writeUTF(str);  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
		
	}
	
      	private void sendMessageToClientsExceptOne(String clientName,String str,String name) {
      		if(sockets != null && sockets.size() > 0) {  
      			for (Iterator<String> iterator = sockets.keySet().iterator(); iterator.hasNext();) {  
      				String key = (String) iterator.next();  
      				if(key.equals(clientName))
      					continue;
      				if(key.equals(name))
      					continue;
      	            	Socket s = sockets.get(key);  
      	                try {  
      	                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());  
      	                    dos.writeUTF(str);  
      	                } catch (IOException e) {  
      	                    e.printStackTrace();  
      	                }  
      	            }  
      	         }  
      	      } 
          }
	  }
 
