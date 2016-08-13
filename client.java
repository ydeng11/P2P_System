

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;


public class client {
	List<chrunkFile> ids;
	static int number;
	static int bufferSize=8192;
	int level_port=8900;
	String dir;
	String suffix;
	static int count=0;
	static int my_count=5;
	public static void main(String[] args) {
		// create five clients
		for(int i=1;i<=my_count;i++){
			new client(i);
		}
		
	}
	 private ClientSocket cs = null; 
	 int id;
	 private String ip = "localhost"; 
	 private int port = 8821;  
	  /*
            create clients and launch the downloading and uploading thread for each of them
          */
	public client(int id){
		this.id=id;
	
		ids=new ArrayList<chrunkFile>();
		 try {  
	            if (createConnection()) {  
	                sendMessage();  
	                getMessage();
	                count++;
	                new Upload_file_thread(this).start();
	                int neighbor;
	                if(id==5){
	                	neighbor=1;
	                }
	                else{
	                	neighbor=id+1;
	                }
	                new Download_file_thread(this, neighbor).start();
	            }  
	  
	        } catch (Exception ex) {  
	            ex.printStackTrace();  
	        }  
		
		
	}

         // create the folder for each peer
 
	 public  String createDir(String destDirName) {  
	        File dir = new File(destDirName);
	        int n=-1;
	        if (dir.exists()) { 
	        	 n= JOptionPane.showConfirmDialog(null, this.id+" Folder already exists whether to overwrite", "yes", JOptionPane.YES_NO_OPTION);  
	            if (n == JOptionPane.NO_OPTION) {  
	            	int a=(int)(Math.random()*10000000); 
		            destDirName=destDirName+"_"+a; 
		            dir = new File(destDirName);
		            if(dir.exists()){
		            	 System.out.println("client "+this.id+" create directory " + destDirName + "failed,please delete "+destDirName+"\n");
		            	 throw new RuntimeException();
		            } 
		            else{
		            	dir.mkdirs();
		            }
	            }  
	        } 
	        else{
	        	dir.mkdirs();
	        }
	        if (!destDirName.endsWith(File.separator)) {  
	            destDirName = destDirName + File.separator;  
	        }  
	        
	        
	       return destDirName;
	    }  

         //get the chunk information

	 private void getMessage() {  
	        if (cs == null)  
	            return;  
	        DataInputStream inputStream = null;  
	        try {  
	            inputStream = cs.getMessageStream();  
	        } catch (Exception e) {  
	            System.out.print("client "+id+" receive message cache error\n");  
	            return;  
	        }  
	        try {  
	            String savePath;  
	            //int bufferSize = 8192*10;  
	           // byte[] buf = new byte[bufferSize];
	            number=inputStream.readInt();
	            int cc=0;
	            for(int i=id;i<=number;i+=my_count){
	            	chrunkFile c=new chrunkFile();
	            	c.id=i;
	            	ids.add(c);
	            	cc++;
	            }
	            
	            String path=inputStream.readUTF();
	            suffix=inputStream.readUTF();
	            if (!path.endsWith(File.separator)) {  
	            	path = path + File.separator;  
		        }  
	            dir=createDir(path+id);
	            if(suffix.equals("none_suffix")){
	            	suffix="";
	            }
	            else{
	            	suffix="."+suffix;
	            }
	            for(int i=0;i<cc;i++){
	            	//System.out.println("1id="+id+"  "+dir);
	            	chrunkFile c=ids.get(i);
	            	//int tempid=c.id;
	            	//savePath = dir+tempid+suffix;
	            	//System.out.println("-------------- "+i);
	            	// System.out.println("2id="+id);
	            	/*DataOutputStream fileOut = new DataOutputStream(  
		                    new BufferedOutputStream(new FileOutputStream(savePath)));*/
	            	// System.out.println("readLong ="+id);
		            long len = 0; 
		            len = inputStream.readLong(); 
		           // System.out.println("readLong ="+id+"  "+len);
		            int passedlen = 0; 
		            System.out.println("client "+id+"  receive file " +c.id+ "\n"); 
		            c.length=len;
		           // System.out.println("3id="+id);
		            /*if(inputStream==null){
		            	System.out.println("inputStream is null ");
		            }*/
		              while (passedlen<len) {  
		                 int read = 0;  
		                 //System.out.println("passedlen="+passedlen);
		                 if(passedlen+bufferSize>=len){
		                	 int temp=(int)len-passedlen;
		                	 read=inputStream.read(c.file,passedlen,(int)temp);
		                	 if(read!=temp){
		                		 passedlen += read;
		                		 continue;
		                	 }
		                	// inputStream.read(c.file,passedlen,(int)temp);
		                	 ///*read =*/ inputStream.read(c.file,passedlen,(int)temp);
		                	 //fileOut.write(buf, 0, read);
		                	 break;
		                 }
		              
		                 read =inputStream.read(c.file,passedlen,bufferSize);
		                 //read = inputStream.read(c.file,(int)passedlen,bufferSize);  
		                 if (read == -1) {  
		                    break;  
		                }  
		                 passedlen += read; 
		                 
		               // fileOut.write(buf, 0, read);
		            } 
		             /* if(id==1&&c.id==6)
				            System.out.println("byte="+c.file[0]);*/
		             
		            //fileOut.close();
		             
	            }
	            System.out.println("client "+id+" file has receive finish " + "\n");
	        
	        } catch (Exception e) {  
	            System.out.println("client "+id+" receive file error" + "\n");  
	            return;  
	        }  
	    }  
	 private void sendMessage() {  
	        if (cs == null)  
	            return;  
	        try {  
	            cs.sendMessage(id);  
	        } catch (Exception e) {  
	            System.out.print("client "+id+" send message failed!" + "\n");  
	        }  
	    }  

         // create connection between peers

	 private boolean createConnection() {  
	        cs = new ClientSocket(ip, port);  
	        try {  
	            cs.CreateConnection();  
	            if(id!=-1)
	            System.out.print("client "+id+" connect to server successfully!"+"\n");  
	            return true;  
	        } catch (Exception e) {  
	            System.out.print("client "+id+" connect server failed!"+"\n");  
	            return false;  
	        }  
	  
	    } 
 class chrunkFile{
	  public int id;
	  public long length;
	  public byte[] file;
	  public chrunkFile(){
		  length=-1;
		  id=-1;
		  file=new byte[1024*100];
	  }
	  
 }

//uploading thread
class Upload_file_thread extends Thread{
	client cf;
	public Upload_file_thread(client cf){
		 this.cf=cf;
		
	}
		@Override
		public void run() {
			while(true){
            	if(cf.count==5){
            		break;
            	}
            	else{
            		try {
						sleep(20);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            }
			System.out.println("client id="+id+" upload start\n");
			Socket s = null;
			try {
				ServerSocket ss = new ServerSocket(cf.level_port+cf.id); //listening port
				while(true){
					
					 s = ss.accept();
					 DataInputStream dis = new DataInputStream(  
		                        new BufferedInputStream(s.getInputStream()));  
					 int length=dis.readInt();
					 if(length>=cf.number){
						 dis.close();
						 break;
					 }
					 List<Integer> have_ids=new ArrayList<Integer>();
					 DataOutputStream  ps = new DataOutputStream(s.getOutputStream());
					 int up_id=-1;
					 String idsstring="";
					 String idsstring1="";
					 for(int j=0;j<length;j++){
						 Integer m=dis.readInt();
						 have_ids.add(m);
						 idsstring+=m+" ";
					 }
					
					 
					 chrunkFile c=null;
					 synchronized(client.class){
						 for(chrunkFile k:cf.ids){
							 if(!have_ids.contains(k.id)){
								 up_id=k.id;
								 c=k;
							 }
						 }
						/* for(chrunkFile k:cf.ids){
							 idsstring1+=k.id+" ";
						 }*/
					 }
					 //System.out.println("upload client "+id+" has files:"+idsstring1);
					 if(up_id==-1){
						 ps.writeInt(up_id);
						 ps.flush();
						 ps.close();
						// sleep(50);
						 continue;
					 }
					 int sendid;
					 if(cf.id==1)sendid=my_count;
					 else sendid=cf.id-1;
					 System.out.println("upload client "+cf.id+" receive request, download client "+sendid+" has file:"+idsstring+" upload client "+cf.id+" send "+up_id+"th file\n");
					 ps.writeInt(up_id);
					 ps.flush();
					/* DataInputStream fis = new DataInputStream(  
					         new BufferedInputStream(new FileInputStream(cf.dir+up_id+suffix)));*/
					// int bufferSize = 8192*10; 
					// File temp=new File(cf.dir+up_id+suffix);
					 //ps.writeLong(temp.length());
					 ps.writeLong(c.length);
					 ps.flush();
				    // byte[] buffer = new byte[bufferSize];
				     int len=0;
				     while(len<c.length){
				    	 if(len+bufferSize>=c.length){
				    		 int temp=(int)c.length-len;
				    		 ps.write(c.file, len, temp);
				    		 break;
				    	 }
				    	 ps.write(c.file, len, bufferSize);
				    	 len+=bufferSize;
				     }
				    /* while((len=fis.read(buffer))!=-1){
				    	* ps.write(buffer, 0, len);
				     }*/
				      ps.flush();
				      ps.close();
				     // fis.close();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally{
				if(s!=null){
					try {
						s.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			
			
		}
		
	}
//downloading thread
class Download_file_thread extends Thread{
	client cf;
	private ClientSocket cs = null;
	int num_id;
	DataOutputStream out = null; 
	public Download_file_thread(client cf,int num_id){
		this.cf=cf;
		this.num_id=num_id;
	}
		@Override
		public void run() {
			
			/**while(true){
				try {
					Socket	 sc=new Socket(ip,num_id+cf.level_port);
					break;
				} catch (Exception e) {
					try {
						sleep(1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}	
			    
			}*/

			while(true){
            	if(cf.count==my_count){
            		break;
            	}
            	else{
            		try {
						sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            }
			 
			 /* try {
				cs=new ClientSocket(cf.ip, cf.level_port+num_id);
				cs.CreateConnection();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			cs=new ClientSocket(cf.ip, cf.level_port+num_id); //downloading port
			while(true){
				
				
				 try {  
					 System.out.println("client id="+id+" download start\n");
					  
					  try {
						 
						  cs.CreateConnection();
					} catch (Exception e) {
						 try {
							 
							 if(cs!=null)
					        	  cs.shutDownConnection();
								sleep(10);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
					}
					 
					  out = new DataOutputStream(cs.socket.getOutputStream());  
			          out.writeInt(cf.ids.size());
			          out.flush(); 
			          String idsstring="";
			          for(chrunkFile k:cf.ids){
			        	  idsstring+=k.id+" ";
			        	  out.writeInt(k.id);
				          out.flush();
			          }
			          //System.out.println("client id="+id+" download start1\n");
			          System.out.println("download client "+cf.id+" send request to upload client "+num_id+", download client "+cf.id+" has file:"+idsstring+"\n");
			          DataInputStream inputStream = cs.getMessageStream();
			          int get_id=inputStream.readInt();
			          //System.out.println("client id="+id+" download startm="+get_id);
			          if(get_id==-1){
			        	  sleep(100);
			        	  if(cs!=null)
			        	  cs.shutDownConnection();
			        	  continue;
			          }
			          //System.out.println("client id="+id+" download start2\n");
			          long len=0;
			          chrunkFile c=new chrunkFile();
			        //  int bufferSize = 8192*10;  
			          //byte[] buf = new byte[bufferSize];
			          len = inputStream.readLong(); 
			          //String savePath=cf.dir+get_id+cf.suffix;
			         /* DataOutputStream fileOut = new DataOutputStream(  
			                    new BufferedOutputStream(new FileOutputStream(savePath))); */
			          c.id=get_id;
			           int passedlen = 0; 
			           c.length=len;
			           //System.out.println("download id="+id+"  length="+len);
			              while (passedlen<len) {  
			                 int read = 0; 
			                 if(passedlen+bufferSize>=len){
			                	 int temp=(int)len-passedlen;
			                	 read=inputStream.read(c.file,passedlen,temp);
			                	 if(read!=temp){
			                		 passedlen += read; 
			                		 continue;
			                	 }
			                	
			                	 break;
			                 }
			                 read = inputStream.read(c.file,passedlen,bufferSize);  
			                 if (read == -1) {  
			                    break;  
			                }  
			                passedlen += read; 
			                //fileOut.write(buf, 0, read);
			            }  
			            System.out.println("download client "+cf.id+"  get file the "+get_id+"th file from "+"upload client "+num_id+ "\n");
			            //fileOut.close(); 
			            synchronized(client.class){
			            	cf.ids.add(c);
						 }
			            
			            if(cf.ids.size()>=cf.number){
			            	cs.CreateConnection();
					        out = new DataOutputStream(cs.socket.getOutputStream()); 
			            	out.writeInt(cf.ids.size());
					        out.flush(); 
					        out.close();
					        if(cs!=null)
					        	  cs.shutDownConnection();
			            	break;
			            }
			            if(cs!=null)
				        	  cs.shutDownConnection();
			            
			        } catch (Exception e) { 
			        	 try {
							sleep(10);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
			          System.out.print("download client "+id+"  download file failed!"+ "\n");  
			            
			        }  
			}
			//System.out.println("----------------------------------------------");
			String pa=cf.dir+cf.id+"_all"+cf.suffix;
			
			 try {
				DataOutputStream fileOut = new DataOutputStream(  
				            new BufferedOutputStream(new FileOutputStream(pa)));
				for(int i=1;i<=cf.number;i++){
					 /*DataInputStream fis = new DataInputStream(  
					         new BufferedInputStream(new FileInputStream(cf.dir+i+suffix)));*/
					
					// int bufferSize = 8192*10;
					for(chrunkFile m:ids){
						if(m.id==i){
							//System.out.println("id="+m.id+"  "+"  i="+i+"  length="+(int)m.length);
							fileOut.write(m.file,0,(int)m.length);
							break;
						}
					}
					/* byte[] buffer = new byte[bufferSize];
				     int len=-1;
				     while((len=fis.read(buffer))!=-1){
				    	 fileOut.write(buffer, 0, len);
				     }
				     fis.close();*/
				}
				fileOut.close(); 
			} catch (Exception e) {
				
				e.printStackTrace();
			} 
			
		}
		
	}
//client socket used to connect with each other
class ClientSocket {
        private String ip;  
		  
		    private int port;  
		  
		    private Socket socket = null;  
		  
		    DataOutputStream out = null;  
		  
		    DataInputStream getMessageStream = null;  
		  
		    public ClientSocket(String ip, int port) {  
		        this.ip = ip;  
		        this.port = port;  
		    }  
		  
		    
		    public void CreateConnection() throws Exception {  
		        try {  
		            socket = new Socket(ip, port);  
		        } catch (Exception e) {  
		            //e.printStackTrace();  
		            if (socket != null)  
		                socket.close();  
		           // throw e;  
		        } finally {  
		        	
		        }  
		    }  
		  
		    public void sendMessage(int id) throws Exception {  
		        try {  
		            out = new DataOutputStream(socket.getOutputStream());  
		            out.writeInt(id);
		            out.flush();  
		           
		        } catch (Exception e) {  
		            e.printStackTrace();  
		            if (out != null)  
		                out.close();  
		            throw e;  
		        } finally {  
		        }  
		    }  
		  
		    public DataInputStream getMessageStream() throws Exception {  
		        try {  
		            getMessageStream = new DataInputStream(new BufferedInputStream(  
		                    socket.getInputStream()));  
		            return getMessageStream;  
		        } catch (Exception e) {  
		            e.printStackTrace();  
		            if (getMessageStream != null)  
		                getMessageStream.close();  
		            throw e;  
		        } finally {  
		        }  
		    }  
		  
		    public void shutDownConnection() {  
		        try {  
		            if (out != null)  
		                out.close();  
		            if (getMessageStream != null)  
		                getMessageStream.close();  
		            if (socket != null)  
		                socket.close();  
		        } catch (Exception e) {  
		  
		        }  
		    }  
}
}
