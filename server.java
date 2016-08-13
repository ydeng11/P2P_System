

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class server {
	public static void main(String[] args) {
		if(args.length<1){
			System.out.println("please input file name\n");
			return;
		}
		else if(args.length>1){
			System.out.println(" input too many parameter ,only Receive one parameter\n");
			return;
		}
		File directory	= new File(".");
		try {
			new server(directory.getCanonicalPath(),args[0]);
		} catch (IOException e) {
			System.out.println("create server failed \n");
			e.printStackTrace();
		}
	}
	static int number;
	static int m100k=1024*100;
	int port = 8821;
	static long sum_length;
	String filePath;
	String path;
	public server(String path,String fileName){
		 Socket s = null;  
	        try {  
	        	
	            ServerSocket ss = new ServerSocket(port); 
	            this.path=path;
	            filePath = path+File.separator+fileName;
                File fi = new File(filePath); 
                sum_length=fi.length();
                System.out.println("file length :" + sum_length);
                long y=sum_length/m100k;
                if(y<5){
                	System.out.println("file should be greater than 500k");
                	return;
                }
                if(sum_length%m100k==0){
                	number=(int) y;
                }
                else{
                	number=(int) y+1;
                }
                
	            while(true){
	            	 s = ss.accept();  
		             DataInputStream dis = new DataInputStream(  
		                        new BufferedInputStream(s.getInputStream()));  
		             int id=dis.readInt();
		         
		             System.out.println("server and client "+id+" connect success");
		             new Send_file_thread(s,id).start();
		             System.out.println("server send files "+" to client "+id);  
	            }
	          
	        } catch (Exception e) { 
	        	System.out.println("Connect error or file name is not right");
	            e.printStackTrace();  
	        }  
	        finally{
	        	try {
	        		if(s!=null)
					   s.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
		
		
	}
	
	
class Send_file_thread extends Thread{
	//DataInputStream fis;
	DataOutputStream ps;
	Socket s;
	int id;
	byte[] buffer = null; 
	public Send_file_thread(Socket s,int id){
		this.id=id;
		this.s=s;
		 try {
			 ps = new DataOutputStream(s.getOutputStream());
			 ps.writeInt(number);  
             ps.flush();
             ps.writeUTF(path);  
             ps.flush();
             int index=filePath.lastIndexOf(".");
            
             if(index==-1){
            	 ps.writeUTF("none_suffix");  
                 ps.flush();
             }
             else{
            	 String d=filePath.substring(index+1);
            	 ps.writeUTF(d);  
                 ps.flush();
             }
              
			    File file = new File(filePath);  
	            FileInputStream fis = new FileInputStream(file);  
	            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);  
	            byte[] b = new byte[1024];  
	            int n;  
	            while ((n = fis.read(b)) != -1) {  
	                bos.write(b, 0, n);  
	            }  
	            fis.close();  
	            bos.close();  
	            buffer = bos.toByteArray();
	            
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
		
	}
	@Override
	public void run() {
		
	//if (fis != null) { 
		try {
			if(buffer!=null){
				
				for(int j=id;j<=number;j+=5){
					int bufferSize = 8192; 
					int skip_length=(j-1)*m100k;
					int length=m100k;
			        if(j==number){
			        	length=(int)(sum_length-(number-1)*m100k);
			        }
			        ps.writeLong(length);
			        ps.flush();
		                ps.write(buffer, skip_length, length);
		       	        ps.flush();
				}
				
				
			}
			
			
			
		} catch (Exception e) {
			try {
				ps.close();
				//fis.close();
				 s.close(); 
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		//}
		
    }
	
			
	}
		
		
		
	}

}
