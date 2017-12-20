import java.net.*;
import java.io.*;


public class Client {  
        public static void main(String[] args) throws UnknownHostException, IOException{  
            new Client().execute(args[0]);  
        }  
          
        public void execute(String name) {  
            try {  
                Socket s = new Socket("127.0.0.1",1234);  
//              start the reading thread, read from socket
                RecvThread recv = new RecvThread(s);  
                new Thread(recv).start();  
//              start the writing thread, write the information to the socket
                SendThread send = new SendThread(s);  
                send.setName(name);
                new Thread(send).start();  
            } catch (UnknownHostException e) {  
                e.printStackTrace();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
            
       }  
        
        class RecvThread implements Runnable {  
            private DataInputStream dis = null;  
            private FileOutputStream fos;
            public RecvThread(Socket s){  
                try {  
                    dis = new DataInputStream(s.getInputStream());  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
              
            @Override  
            public void run() {  
                String str = "" ;  
                
                while(true){  
                    try {  
                        //read form socket 
                        str = dis.readUTF();
                        String index[] = str.split(" ");
                        if(index[0].equals("block")){
                            System.out.println("received message:" + index[2]);  
                        }
                        else if(index[1].equals("message")&&index[0].equals("broadcast")){
                        	System.out.println("received message:" + index[2]);
                        }
                        else if(index[1].equals("message")&&index[0].equals("unicast")){
                        	System.out.println("received message:" + index[3]);
                        }
                        
                        else if(index[1].equals("file")&&index[0].equals("broadcast")) {                          
                          System.out.println("received file:" + index[2].substring(index[2].lastIndexOf("/")+1));  
                          fos = new FileOutputStream(new File("../"+index[3]+"/"+index[2].substring(index[2].lastIndexOf("/")+1)));        
                          int count = 0;  
                          while (count == 0) {  
                              count = dis.available();  
                          }  
                          byte[] b = new byte[count];  
                          dis.read(b);  
                          fos.write(b,0,count);
                          fos.flush();
//                          byte[] sendBytes = new byte[1024];
//                          int read = 0;
//                          while(true) {
//                              read = dis.read(sendBytes);
//                              if(read == -1)
//                                  break;
//                              fos.write(sendBytes, 0, read);
//                              fos.flush();
//                              if(dis.available()==0)
//                              	break;
 //                         }
                        }
                        
                        else if(index[1].equals("file")&&index[0].equals("unicast")){
                            System.out.println("received file:" + index[3].substring(index[3].lastIndexOf("/")+1));  
                            fos = new FileOutputStream(new File("../"+index[2]+"/"+index[3].substring(index[3].lastIndexOf("/")+1)));  
                            int count = 0;  
                            while (count == 0) {  
                                count = dis.available();  
                            }  
                            byte[] b = new byte[count];  
                            dis.read(b);  
                            fos.write(b,0,count);
                            fos.flush();
//                            byte[] sendBytes = new byte[1024];
//                            int read = 0;
//                            while(true) {
//                                read = dis.read(sendBytes);
//                                if(read == -1)
//                                    break;
//                                fos.write(sendBytes, 0, read);
//                                fos.flush();
////                                if(dis.available()==0)
////                                	break;
//                            }
                        }
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                }  
            }  
        }  
          
        class SendThread implements Runnable{  
            private DataOutputStream dos;  
            private String name;
            public SendThread(Socket s){  
                try {  
                    dos = new DataOutputStream(s.getOutputStream()); 
                  

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
                //BufferedReader brName = new BufferedReader(new InputStreamReader(System.in));
                try {
				//	String name = brName.readLine();
					dos.writeUTF(name);
					dos.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                DataOutputStream temp = dos;
                while(true) {  
                    System.out.println("please enter a command as a string:");  
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));                      
                    dos = temp;
                    try {  
                        str = br.readLine();  
                        if(str.equals("exit")) {
                            dos.writeUTF(str);  
                            dos.flush();
                        	break;
                        }
                        String[] index = str.split(" ");
                        if(index[1].equals("message")||index[0].equals("block")) {
                        
                        dos.writeUTF(str);  
                        dos.flush();

                        }                     
                        if(index[1].equals("file")&&index[0].equals("broadcast")){
                        	String fileName = index[2];
                          dos.writeUTF(str);  
                          dos.flush();
                        	byte[] bytes = new byte[2048];  
                           File inputFile = new File(fileName);
                          InputStream is = new FileInputStream(inputFile);
                          int len = 0;  
                          while ((len = is.read(bytes)) != -1) {  
                                dos.write(bytes, 0, len);  
                                dos.flush();
//                                if(is.available()==0)
//                                	break;
                            }  
                          is.close();
//                          dos.close();
                        }
                        
                        if(index[1].equals("file")&&index[0].equals("unicast")){
                        	String fileName = index[3];
                            dos.writeUTF(str);  
                            dos.flush();
                          	byte[] bytes = new byte[2048];  
                            File inputFile = new File(fileName);
                            InputStream is = new FileInputStream(inputFile);
                            int len = 0;  
                            while ((len = is.read(bytes)) != -1) {  
                                  dos.write(bytes, 0, len);  
                                  dos.flush();
//                                  if(is.available()==0)
//                                  	break;
                              }  
                            is.close();
//                            dos.close();
                        }
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                }                 
            }

        }  
}  
