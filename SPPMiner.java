// SPBMiner.cpp : Defines the entry point for the console application.
// Implemted By: Sajal Halder
// Date 21 December, 2016
package sppminer;

import java.util.*;
import java.io.*;
import java.lang.*;
import java.text.DateFormat;
import java.util.regex.Pattern;

/**
 *
 * @author Sajal
 */
public class SPPMiner {
    
     /*
     * Global Variable
     */
    public String input_file = "enron.txt";
    
    public String output_file = /*"facebookprocessed.txt";*/"sample.txt";//"enron_processed.txt";
    
    public File fp_out = new File("Output.txt");
    
    public int min_sup = 2;
    
    public int Pmax = 40;
    
    public int starttime,endtime;
   
    int minNumber = 3000;
       
    List<String> Emails = new ArrayList<String>();
    
   
    Graph SuperGraph;
    
    Graph CurrentGraph;
    
   HashMap periodicPattern = new LinkedHashMap<String,PeriodicPattern>(); 
   HashMap tempPattern = new LinkedHashMap<String,PeriodicPattern>();
    
    /*
     * Constructor that called differnt function
     */
    public SPPMiner()
    {
        // Preprocessed Data 
       //   preProcessedData();                                                 // preprocess the data and 
        //  facebookdataProcessed();
            HashCreatePatternTree();
           //CreatePatternTree();       
      
          
         System.out.println("Graph Vertex Number = "+ SuperGraph.HashVertexSets.size());
         System.out.println( "Start time = "+starttime + "  End Time = "+ endtime + " timestamp = "+(endtime-starttime));
        
    }
    
    /*
     * Create Pattern Tree 
     */
     private void HashCreatePatternTree() {
         
         try{
         
         FileReader file = new FileReader(output_file);
         BufferedReader reader = new BufferedReader(file);
         
         String str;
         str = reader.readLine();
         String b[] = str.split(" ");
         String time= b[0];
         starttime = Integer.parseInt(time);
         int count = 1; 
         CurrentGraph = new Graph();
         SuperGraph = new Graph();      
         while(str!=null)
         {
             String c[] = str.split(" ");
             if(!c[0].equals(time))
             {                                            
              //   System.out.println("Current Graph  "  + count +"\r\n\n");               
               //  HashprintGraph(CurrentGraph,"CurrentGraph.txt");                   // Print current Graph
                                                
                HashUpdatSuperGraph(CurrentGraph,Integer.parseInt(time));           // Updeate Super Graph
                System.out.println("SuperGraph = " + count);                                      // print super graph                
                HashprintGraph(SuperGraph,"SuperGraph.txt");   
                int currenttime = Integer.parseInt(time);//- starttime;
                if(count == 1)
                     {
                         tempPatternPrint();
                       // HashMiningPeriodicBehavior(currenttime,false);
                         ParsimoniousPatterns(false,Integer.parseInt(time));
                         System.out.println("Origingal Pattenr");
                         OriginalPatternPrint();
                     }
                     else
                     {
                        tempPatternPrint();
                      // HashMiningPeriodicBehavior(currenttime,false);
                        ParsimoniousPatterns(true,Integer.parseInt(time));
                        System.out.println("Origingal Pattenr");
                        OriginalPatternPrint();
                     }                 
                if(count == minNumber)
                     break;
                 Graph.deleteCurrentGraph(CurrentGraph);                        // clean current graph
                 CurrentGraph = new Graph();              
                 //HashUpdateGraphNode(str,Integer.parseInt(time)); // Update Graph node
                 FacebookHashUpdateGraphNode(str,Integer.parseInt(time));           
                  time = c[0];
                  count++;
             }
             else
             {               
                //  HashUpdateGraphNode(str,Integer.parseInt(time));
                   FacebookHashUpdateGraphNode(str,Integer.parseInt(time));                 // Update Graph Node         
             }             
             str = reader.readLine();            
         }
         endtime = Integer.parseInt(time);
         //   System.out.println("Current Graph  "  + count +"\r\n\n");               
        // HashprintGraph(CurrentGraph,"CurrentGraph.txt");                   // Print current Graph           
         HashUpdatSuperGraph(CurrentGraph,Integer.parseInt(time));      // Updeate Super Graph  
         System.out.println("SuperGraph = " + count); 
         HashprintGraph(SuperGraph,"SuperGraph.txt");
          tempPatternPrint();
         HashMiningPeriodicBehavior(false);
         tempPatternPrint();
         ParsimoniousPatterns(true);
         System.out.println("Origingal Pattenr");
         OriginalPatternPrint();
     //    ParsimoniousPatterns(true,Integer.parseInt(time));
         System.out.println("Count = " +count);
         
         }catch(Exception ex)
         {
             System.out.println("Create Pattern Tree ERROR "+ex);
         }        
    } 
      
    /*
     * Update Super graph
     * @ SuperGraph is supergraph
     * @CurrentGraph is currentgraph
     */
        
      private void HashUpdatSuperGraph(Graph CurrentGraph, int time) {
        try {
          //  System.out.println("Supergraph size = "+SuperGraph.VertexSets.size());
        if (SuperGraph.HashVertexSets.size()== 0)
            {
                SuperGraph.HashVertexSets.putAll(CurrentGraph.HashVertexSets);
                System.out.println(SuperGraph.HashVertexSets.size());
                return;
            }
            String DeleteVertes = "";
            int delete =0;

           // Set set = CurrentGraph.HashVertexSets.entrySet();
            Set set = SuperGraph.HashVertexSets.entrySet();
            Iterator it = set.iterator();
            while(it.hasNext())
            {
                 Map.Entry pattern = (Map.Entry)it.next();
                 String keyv = pattern.getKey().toString();       
                 Vertex supV = (Vertex) pattern.getValue();
                 
                 if(CurrentGraph.HashVertexSets.containsKey(""+supV.vertexLabel))
                 {
                     Vertex curV = CurrentGraph.HashVertexSets.get(""+supV.vertexLabel);
               
                     if(!supV.timeContains(supV.vertexTimeset,supV.timelength,curV.vertexTimeset[0]))
                     supV.insertTime(supV, curV);               
                     
                       /* Undate Edge descriptor */
                       List <Vertex> newEdgeList = new ArrayList<Vertex>();
                       for(int m = 1; m <= curV.vertexEdgeList.size();m++)           //current grpah edge list for each vertex 
                       {
                           
                           Vertex curEdge = curV.vertexEdgeList.get(m-1);
                           int foundedge = 0;
                           for ( int n = 1; n <= supV.vertexEdgeList.size();n++)   // supergraph vertex list 
                           {
                               Vertex supEdge = supV.vertexEdgeList.get(n-1);
                               if(curEdge.vertexLabel==supEdge.vertexLabel)
                               {
                                   if(!supEdge.timeContains(supEdge.vertexTimeset,supEdge.timelength,curEdge.vertexTimeset[0]))
                                   {
                                      // System.out.println(" time = " +supEdge.vertexTimeset[supEdge.timelength-1]);
                                       
                                       if(!supEdge.timeContains(supEdge.vertexTimeset,supEdge.timelength,curV.vertexTimeset[0]))
                                       supEdge.insertTime(supEdge, curV);                                        
                                       Vertex upSupVEdge = UpdateDescriptor(curEdge,supEdge,supV.vertexLabel); //Update Edge Descriptor                                                                       
                                       supV.vertexEdgeList.set(n-1,upSupVEdge);
                                   }                                   
                                   foundedge = 1;
                                   break;
                               }
                           }
                           if(foundedge == 0)
                           {
                               newEdgeList.add(curEdge);
                           }                           
                       }                        
                        if(newEdgeList.size()>0)               
                        supV.vertexEdgeList.addAll(newEdgeList);
                        newEdgeList.clear();                     
                        SuperGraph.HashVertexSets.put(keyv, supV);   
                        CurrentGraph.HashVertexSets.remove(""+supV.vertexLabel);                    
               }
               else
                 {
                    SuperGraph.HashVertexSets.put(""+supV.vertexLabel,MinePeriodicPattern(supV,time));                    
                    if(time - supV.vertexTimeset[supV.timelength-1] >= Pmax)              // Delete supergraph node condition
                    {
                        DeleteVertes = DeleteVertes +","+""+supV.vertexLabel;                   // DeleteSupergraph label
                    }
                        
                 }
            }
            if(CurrentGraph.HashVertexSets.size()>0)
            SuperGraph.HashVertexSets.putAll(CurrentGraph.HashVertexSets); 
            String b[] = DeleteVertes.split(",");                                                // clear new list
           // for(int i = 0; i < b.length; i++)
             //   SuperGraph.HashVertexSets.remove(b[i]);              // Delete Supergrpah node
        }
        catch(Exception ex)
        {
             System.out.println("Update Super Graph Error "+ex);
        }  
        
    }
      
      private Vertex UpdateDescriptor(Vertex curV, Vertex supV, int vertexLabel) {
          try{
              
              //create new descriptor
              for(int i = 0; i < supV.timelength-1; i++)
              {
                  if(curV.vertexTimeset[0]-supV.vertexTimeset[i] <= Pmax)       //define period
                  {
                      Descriptor D = new Descriptor(supV.vertexTimeset[i],curV.vertexTimeset[0],2);
                      //increase descriptor length
                      if(supV.Dlength+2 >= supV.vertexDescriptorset.length)
                      {
                           Descriptor newElements[] = new Descriptor[supV.vertexDescriptorset.length*2];
                           System.arraycopy(supV.vertexDescriptorset, 0, newElements, 0, supV.vertexDescriptorset.length);
                           supV.vertexDescriptorset = newElements;  
                      }
                      supV.Dlength++;
                      supV.vertexDescriptorset[supV.Dlength] = D;
                      supV.upheap(supV,supV.Dlength);           // add new descriptor
                  }
              }
            
              //delete descriptor
              Descriptor head = supV.DescriptorHead(supV);
                         
              while(head.period + head.last <= curV.vertexTimeset[0])
              {
                   supV.downheap(supV,1,supV.Dlength);
                  // update descriptor
                  if(head.period + head.last == curV.vertexTimeset[0])
                  {
                      Descriptor V = new Descriptor(head.start,head.period,head.last+head.period,head.support); // increase support and add last time 
                       
                       // increase supV descriptor size
                      if(supV.Dlength+2 >= supV.vertexDescriptorset.length)
                      {
                           Descriptor newElements[] = new Descriptor[supV.vertexDescriptorset.length*2];
                           System.arraycopy(supV.vertexDescriptorset, 0, newElements, 0, supV.vertexDescriptorset.length);
                           supV.vertexDescriptorset = newElements;  
                      }
                      supV.Dlength++;
                      supV.vertexDescriptorset[supV.Dlength] = V;
                      supV.upheap(supV,supV.Dlength);                         // add new descriptor                           
                  }
                  
                  //delete descriptor
                 
                      if(head.support >=min_sup)
                      {
                          HashInsert(head.start,head.period,supV.vertexLabel,head.support, vertexLabel);
                          System.out.println("("+supV.vertexLabel+","+vertexLabel+")"+"Head start = "+head.start + "  end  "+head.last + " support = "+head.support + "  ex time = "+ (head.period+head.last));
                      }                      
                                   
                  head = supV.DescriptorHead(supV);    
                  //System.out.println("("+supV.vertexLabel+","+vertexLabel+")"+"Head start = "+head.start + "  end  "+head.last + " support = "+head.support + "  ex time = "+ (head.period+head.last));
              }
           }
            catch(Exception ex)
            {
                System.out.println("Vertex Update Descriptor Error "+ex);
            }        
          return supV;  
      }  
     /*
      * Update tree node
      */
     void HashUpdateGraphNode(String info,int time)
     {
         try{
                String c[] =info.split(" ");
                int time1 = Integer.parseInt(c[0]);
                 Descriptor D = new Descriptor(time1);
                 int label_1 = Integer.parseInt(c[3]);
                 int label_2 = Integer.parseInt(c[4]);
                 
                 Vertex V1 = new Vertex(c[1],time1,label_1,D);
                 Vertex V2 = new Vertex (c[2],time1,label_2,D);
                
                 if(!CurrentGraph.HashVertexSets.containsKey(""+V1.vertexLabel))
                 {
                     CurrentGraph.HashVertexSets.put(""+V1.vertexLabel, V1);
                 }
                 
                 if(!CurrentGraph.HashVertexSets.containsKey(""+V2.vertexLabel))
                 {
                     CurrentGraph.HashVertexSets.put(""+V2.vertexLabel, V2);
                 }
                 if(label_1 > label_2)   
                 {  
             
                     HashaddEdge(new Vertex(c[1],time1,label_1,new Descriptor(time1)),new Vertex (c[2],time1,label_2,new Descriptor(time1)));              
                 }
                 else if(label_1<label_2)
                  {
                        HashaddEdge(new Vertex (c[2],time1,label_2,new Descriptor(time1)),new Vertex(c[1],time1,label_1,new Descriptor(time1)));      // Add edge V1 into V2 informaton
                 }  
                  }catch(Exception ex)
                 {
                     System.out.println("Update Graph Node ERROR "+ex);
                 }   
                 
                
     }
     
     // facebook hash graph node update
       
     /*
      * Update tree node
      */
     void FacebookHashUpdateGraphNode(String info,int time)
     {
         try{
                String c[] =info.split(" ");
                int time1 = Integer.parseInt(c[0]);
                 Descriptor D = new Descriptor(time1);
                 int label_1 = Integer.parseInt(c[1]);
                 int label_2 = Integer.parseInt(c[2]);
                 
                 Vertex V1 = new Vertex(c[1],time1,label_1,D);
                 Vertex V2 = new Vertex (c[2],time1,label_2,D);
                
                 if(!CurrentGraph.HashVertexSets.containsKey(""+V1.vertexLabel))
                 {
                     CurrentGraph.HashVertexSets.put(""+V1.vertexLabel, V1);
                 }
                 
                 if(!CurrentGraph.HashVertexSets.containsKey(""+V2.vertexLabel))
                 {
                     CurrentGraph.HashVertexSets.put(""+V2.vertexLabel, V2);
                 }
                 if(label_1 > label_2)   
                 {  
             
                     HashaddEdge(new Vertex(c[1],time1,label_1,new Descriptor(time1)),new Vertex (c[2],time1,label_2,new Descriptor(time1)));              
                 }
                 else if(label_1<label_2)
                  {
                        HashaddEdge(new Vertex (c[2],time1,label_2,new Descriptor(time1)),new Vertex(c[1],time1,label_1,new Descriptor(time1)));      // Add edge V1 into V2 informaton
                 }  
                  }catch(Exception ex)
                 {
                     System.out.println("Update Graph Node ERROR "+ex);
                 }   
                 
                
     }

      private void facebookdataProcessed() {
        try{
            
            FileReader file = new FileReader("facebook-wall.txt");
            BufferedReader reader = new BufferedReader(file);
            
            BufferedWriter out = new BufferedWriter(new FileWriter("facebookprocessed.txt"));
            String str;
            String DateTime;
            String Month,SenderMail, ReceiverMail;
            int Year,Day;
            int StartDate=0,count = 1;
            while((str = reader.readLine())!=null)
            {
                System.out.println(str);
                 String b[] = str.split("\t");
                 System.out.println(b.length);
                 Date date = new Date((Long.parseLong(b[2]))*1000);
                DateFormat df2 = DateFormat.getDateInstance(DateFormat.LONG);
                DateTime = df2.format(date);
             //   System.out.println(DateTime);
                
                String c[] = DateTime.split(", ");
                 
                Year = Integer.parseInt(c[1]);
                
                String d[] = c[0].split(" ");
                 
                Month =  findMonthvalue(c[0]);
                
                Day = Integer.parseInt(d[1]);
                
            //    System.out.println("Year = "+Year + " Month = "+ Month + " Day = "+Day);   // Find year month and date
                String Date = Day+Month+Year;
                
                if(count == 1)
                StartDate = Integer.parseInt(Date);
                
                int Periodtime = dateDifference(Integer.parseInt(Date),StartDate)+1;    // find period difference from start time;
                
                out.write(Periodtime+" "+b[0]+" "+b[1]+"\r\n");
                System.out.println(Periodtime+" "+b[0]+" "+b[1]); 
                count++;           
            }
            
        }catch(Exception ex)
        {
            System.out.println("Facebook data input error "+ex);
        }
    }

    /*preprocessed Data*/
    private void preProcessedData() {
        
        
        String DateTime;
        String Month,SenderMail, ReceiverMail;
        int Year,Day;
        int StartDate=0;
        try{
            
            FileReader file = new FileReader(input_file);
            BufferedReader reader = new BufferedReader(file);
            
            BufferedWriter out = new BufferedWriter(new FileWriter(output_file));
            
            String str; int count = 0; int Email_num = 0;
            while((str = reader.readLine())!=null)
            {
                count ++;
               // if(count > Features)
                   // break;
            
                String b[] = str.split(" ");
                
                SenderMail = b[1];
                ReceiverMail = b[2];
                
                 /****************************** Date ************************/
                Date date = new Date((Long.parseLong(b[0]))*1000);
                DateFormat df2 = DateFormat.getDateInstance(DateFormat.LONG);
                DateTime = df2.format(date);
             //   System.out.println(DateTime);
                
                String c[] = DateTime.split(", ");
                 
                Year = Integer.parseInt(c[1]);
                
                String d[] = c[0].split(" ");
                 
                Month =  findMonthvalue(c[0]);
                
                Day = Integer.parseInt(d[1]);
                
            //    System.out.println("Year = "+Year + " Month = "+ Month + " Day = "+Day);   // Find year month and date
                String Date = Day+Month+Year;
                
                if(count == 1)
                StartDate = Integer.parseInt(Date);
                int Periodtime = dateDifference(Integer.parseInt(Date),StartDate)+1;    // find period difference from start time;
                /****************************** Date ********************************/
            //    System.out.println(Date);  
                String SendRecv = SenderMail +" "+ ReceiverMail;
             //   System.out.println(SenderMail + "" +ReceiverMail);
                int senderindex = Emails.indexOf(SenderMail);
                int receiverindex = Emails.indexOf(ReceiverMail);
          //      System.out.println(" sender index =" +senderindex + " revce index = " + receiverindex );
                
                if(senderindex == -1)
                {       
                    Emails.add(SenderMail);
                    senderindex= Email_num;
                    Email_num++;
                }
                if(receiverindex == -1)
                {
                    Emails.add(ReceiverMail);
                    receiverindex = Email_num;
                    Email_num++;
                }
               // out.write(Periodtime+" "+SenderMail+" "+ReceiverMail+" "+(senderindex+1)+" "+(receiverindex+1) +"\r\n");
                out.write(Periodtime+" "+(senderindex+1)+" "+(receiverindex+1) +"\r\n");
                System.out.println(Periodtime+" "+ SenderMail+" "+ReceiverMail+" "+(senderindex+1)+" "+(receiverindex+1));                  
                        
            }
            out.close();
            file.close();
            
        }catch(Exception ex)
        {
            System.out.printf("Preprocessed data error: "+ex);
        }
    }
     /*
     * Find month
     * @str is a sting of time like as month date, year
     */
    static String findMonthvalue(String str)
    {
        Pattern pat = Pattern.compile("\\s");
        String b[] = pat.split(str);
     
            if(b[0].contentEquals("January"))
                return "01";
            else if (b[0].contentEquals("February"))
                return "02";
            else if (b[0].contentEquals("March"))
                return "03";
            else if (b[0].contentEquals( "April"))
                return "04";
            else if (b[0].contentEquals( "May"))
                return "05";
            else if (b[0].contentEquals( "June"))
                return "06";
            else if (b[0].contentEquals("July"))
                return "07";
            else if (b[0].contentEquals("August"))
                return "08";
             else if (b[0].contentEquals( "Septemper"))
                return "09";
             else if (b[0].contentEquals( "October"))
                return "10"; 
             else if (b[0].contentEquals("November"))
                return "11";
             else 
                return "12"; 
             
   }
    
     // find date difference 
    
    public int dateDifference(int enddate,int startdate)
    {
        int syear = startdate%10000;
        int eyear = enddate%10000;
        startdate = startdate/10000;
        enddate = enddate/10000;
        int smonth = startdate%100;
        int emonth = enddate%100;
        int sday = startdate/100;
        int eday = enddate/100;
        Date startDate = new Date(syear-1900,smonth-1,sday);
        Date endDate = new Date(eyear-1900,emonth-1,eday);
        int difInDays = (int) ((endDate.getTime()-startDate.getTime())/(1000*60*60*24));
        return difInDays;
    }
   
   private void HashprintGraph(Graph graph,String graphName) {
        
        try{
            BufferedWriter out = new BufferedWriter(new FileWriter(graphName));
            
             Set set = graph.HashVertexSets.entrySet();
            Iterator it = set.iterator();
            while(it.hasNext())
            {
                 Map.Entry pattern = (Map.Entry)it.next();
                 String keyv = pattern.toString();       
                 Vertex v = (Vertex) pattern.getValue();
              System.out.print(" Id = "+v.vertexLabel +  " Time = [" );
                out.write(" Id = "+v.vertexLabel +  " Time = [" );
                for(int p = 0; p < v.timelength; p++)
                {
                    System.out.print("  "+ v.vertexTimeset[p]);
                    out.write("  "+ v.vertexTimeset[p]);
                }
                System.out.println("]");
                out.write("]");
                out.write("Time = "+v.vertexTimeset+ " id = "+v.vertexLabel + " Descriptor = "+"\r\n");
                for(int j = 1; j <= v.Dlength;j++)
                {
                    Descriptor D = v.vertexDescriptorset[j];
                    System.out.println( "                                                                Descriptor = start   "+D.start + "  end = "+D.last + " period = "+D.period + "  support = "+D.support +" Extected time = "+(D.period+D.last));
                    out.write( "                                                                Descriptor  start=    "+D.start + "  end = "+D.last + " period = "+D.period + "  support = "+D.support+" Extected time = "+(D.period+D.last)+"\r\n");
                }
             
                for( int j =1; j<=v.vertexEdgeList.size();j++)
                {
                    Vertex v1 = v.vertexEdgeList.get(j-1);
                  //  if(v1== null) continue;
                    System.out.print("             Id = "+v1.vertexLabel +  " Time = [" );
                    for(int p = 0; p < v1.timelength; p++)
                        System.out.print("  "+ v1.vertexTimeset[p]);
                    System.out.println("]");   
                    out.write("Time = "+v1.vertexTimeset+ " id = "+v1.vertexLabel + " Descriptor = "+"\r\n");
                     for( int k = 1;  k<= v1.Dlength;k++)                   
                    {
                        Descriptor D = v1.vertexDescriptorset[k];
                        System.out.println( "                                                                                      Descriptor start =    "+D.start + "  end = "+D.last + " period = "+D.period + "  support = "+D.support+" Extected time = "+(D.period+D.last));
                        out.write( "                                                                  Descriptor start =    "+D.start + "  end = "+D.last + " period = "+D.period + "  support = "+D.support+" Extected time = "+(D.period+D.last)+"\r\n");
                    }
                
                  //  out.write("                                  Time = "+v1.vertexTimeset+ " id = "+v1.vertexLabel + " Descriptor = "+v1.vertexDescriptorset.get(0).last+"\r\n");
                }
            }
            out.close(); 
        }catch(Exception ex)
        {
            System.out.println(" Print graph error : "+ex);
        }
       
    }

  
     
    /*
     * Add edge 
     * V1 vertex that add v2 vertex info
     */
    private void HashaddEdge(Vertex V1, Vertex V2) {
        
        Vertex V = (Vertex) CurrentGraph.HashVertexSets.get(""+V1.vertexLabel);
        if(edgeContains(V2,V) != true)
        {
        V.vertexEdgeList.add(V2);
        CurrentGraph.HashVertexSets.put(""+V1.vertexLabel, V);
        }       
    }

 
    private boolean edgeContains(Vertex V2, Vertex curV) {
       
        for(int i = 1; i <= curV.vertexEdgeList.size(); i++)
        {
           if(curV.vertexEdgeList.get(i-1).vertexLabel == V2.vertexLabel)       //find matching 
           {
                  return true;
           }
        }
       return false;
    }

    

       private void HashMiningPeriodicBehavior(int time,boolean filestatus ) {
        try
        {
        BufferedWriter out = new BufferedWriter(new FileWriter("periodic.txt",filestatus));       
     
            Set set = SuperGraph.HashVertexSets.entrySet();
            Iterator it = set.iterator();
          
            while(it.hasNext())
            {
                Map.Entry pattern = (Map.Entry)it.next();           
                String key = pattern.getKey().toString(); 
                 Vertex supV = (Vertex)pattern.getValue();                       // Supergraph Vertex
        
                for(int j =supV.vertexEdgeList.size(); j>=1; j--)                  // Supergraph Connected Edge 
                {
                    Vertex Edge = supV.vertexEdgeList.get(j-1);                     // find edge vertex

                    for(int k = Edge.Dlength; k >=1; k--)
                        {
                            Descriptor VD = Edge.vertexDescriptorset[k];              // Vertex Descritor remove
                             if((VD.last+VD.period) <=  time)                      // find periodic pattern 
                                     {
                                         if(VD.support >= min_sup)                        // check support 
                                         {
                                            // System.out.println("                                                  Edge Periodic behavior "+ Edge.vertexLabel + "    start = "+ VD.start + " End = "+VD.last + " Support = "+ VD.support);
                                              out.write("                                                  Edge Periodic behavior "+ Edge.vertexLabel + "    Start = "+ VD.start + " End = "+VD.last + " Period =  "+ VD.period + " Support = "+ VD.support+"\r\n");
                                              HashInsert(VD.start,VD.period,Edge.vertexLabel,VD.support,supV.vertexLabel);
                                         }
                                         Edge.DeleteDescriptor(Edge,VD);            //delete edge node descriptor   
                                    }               
                        }  

               } 
                SuperGraph.HashVertexSets.put(key, supV);     
        }    
        out.close();
        }
        catch(Exception ex)
        {
            System.out.println("Periodic Behavior Miing Error "+ex);
        }
    }
        private Vertex MinePeriodicPattern(Vertex supV,int time) {
            
             for(int j =supV.vertexEdgeList.size(); j>=1; j--)                  // Supergraph Connected Edge 
                {
                    Vertex Edge = supV.vertexEdgeList.get(j-1);                     // find edge vertex

                    for(int k = Edge.Dlength; k >=1; k--)
                        {
                            Descriptor VD = Edge.vertexDescriptorset[k];              // Vertex Descritor remove
                             if((VD.last+VD.period) <=  time)                      // find periodic pattern 
                                     {
                                         if(VD.support >= min_sup)                        // check support 
                                         {
                                            // System.out.println("                                                  Edge Periodic behavior "+ Edge.vertexLabel + "    start = "+ VD.start + " End = "+VD.last + " Support = "+ VD.support);
                                             //out.write("                                                  Edge Periodic behavior "+ Edge.vertexLabel + "    Start = "+ VD.start + " End = "+VD.last + " Period =  "+ VD.period + " Support = "+ VD.support+"\r\n");
                                              HashInsert(VD.start,VD.period,Edge.vertexLabel,VD.support,supV.vertexLabel);
                                         }
                                           Edge.DeleteDescriptor(Edge,VD);            //delete edge node descriptor
                                     }               
                        }  
               } 
             return supV;
        
    }
        private void HashMiningPeriodicBehavior(boolean filestatus ) {
        try
        {
        BufferedWriter out = new BufferedWriter(new FileWriter("periodic.txt",filestatus));       
     
            Set set = SuperGraph.HashVertexSets.entrySet();
            Iterator it = set.iterator();
          
            while(it.hasNext())
            {
                Map.Entry pattern = (Map.Entry)it.next();           
                String key = pattern.getKey().toString(); 
                 Vertex supV = (Vertex)pattern.getValue();                       // Supergraph Vertex
        
                for(int j =supV.vertexEdgeList.size(); j>=1; j--)                  // Supergraph Connected Edge 
                {
                    Vertex Edge = supV.vertexEdgeList.get(j-1);                     // find edge vertex

                    for(int k = Edge.Dlength; k >=1; k--)
                        {
                            Descriptor VD = Edge.vertexDescriptorset[k];              // Vertex Descritor remove
                          //   if((VD.last+VD.period) <=  time)                      // find periodic pattern 
                                     {
                                         if(VD.support >= min_sup)                        // check support 
                                         {
                                            // System.out.println("                                                  Edge Periodic behavior "+ Edge.vertexLabel + "    start = "+ VD.start + " End = "+VD.last + " Support = "+ VD.support);
                                             out.write("                                                  Edge Periodic behavior "+ Edge.vertexLabel + "    Start = "+ VD.start + " End = "+VD.last + " Period =  "+ VD.period + " Support = "+ VD.support+"\r\n");
                                              HashInsert(VD.start,VD.period,Edge.vertexLabel,VD.support,supV.vertexLabel);
                                         }
                                          Edge.DeleteDescriptor(Edge,VD);            //delete edge node descriptor  
                                    }               
                        }  
               } 
                SuperGraph.HashVertexSets.put(key, supV);         
        }   
        out.close();
        }
        catch(Exception ex)
        {
            System.out.println("Periodic Behavior Miing Error "+ex);
        }
    }

    private void HashInsert(int start, int period, int vertexLabel, int support) {
        int s = start;
        String Pattern = ""+vertexLabel;
      //  for(int i = support; i >=min_sup; i-- )
        {
            PPattern P = new PPattern(support,Pattern);
            String hashkey = s+"-"+period;
            if(tempPattern.containsKey(hashkey))
            {
                PeriodicPattern V = (PeriodicPattern)tempPattern.get(hashkey);
                V.insert(V.Periodic,P);
                tempPattern.put(hashkey, V);               
            }
            else
            {
                tempPattern.put(hashkey,P);
            }
            s = s+period;
        }
    }

    private void HashInsert(int start, int period, int vertexLabel0, int support,int vertexLabel) {    
        
        String Pattern ="("+ vertexLabel0+","+vertexLabel+")";
   //     for(int i = support; i >=min_sup; i-- )
        {
            PPattern P = new PPattern(support,Pattern);
            String hashkey = start+"-"+period;
            if(tempPattern.containsKey(hashkey))
            {
                PeriodicPattern V = (PeriodicPattern)tempPattern.get(hashkey);
                V.insert(V.Periodic,P);
                tempPattern.put(hashkey, V);               
            }
            else
            {
                PeriodicPattern V =  new PeriodicPattern(P);
                tempPattern.put(hashkey,V);
            }
          //  s = s+period;
        }
            
        
    }
    
    private void ParsimoniousPatterns(boolean status,int time)
    {
          
        try{
            UpdatePattern();
            
       /*     BufferedWriter out = new BufferedWriter(new FileWriter("Parsimonious pattern",status));
            
            Map<String, PeriodicPattern> sortedMap = new TreeMap<String, PeriodicPattern>(periodicPattern);
            
           // Set set = sortedMap.entrySet();
            Set set = periodicPattern.entrySet();
            Iterator it = set.iterator();
            List< PPattern> DPattern = new ArrayList<PPattern>();
            while(it.hasNext())
            {
                Map.Entry pattern = (Map.Entry)it.next();           
                String key = pattern.getKey().toString();                       //Current Key
                String b[]=key.split("-");                                      //Split Key
             
                int period = Integer.parseInt(b[1]);                            //Current Pattern period
                int start = Integer.parseInt(b[0]);                             // Current Pattern start position
               
                PeriodicPattern curP =  (PeriodicPattern) pattern.getValue();   // Current Patterns at position period and start
                
                for(int i =1; i <=  curP.Periodic.size(); i++)
                {
                    PPattern Pat = curP.Periodic.get(i-1);
                   if((start+(period*(Pat.support))) < time)
                    {
                     //  if((subsumtion(sortedMap,start,period,Pat.Pattern,Pat.support)== false) && (subsumtionIntoSamePeriodStart(curP,Pat)==false))
                               //   if(subsumtionIntoSamePeriodStart(curP,Pat)==false)                    
                        {
                             out.write("Pattern =   "+Pat.Pattern);
                             out.write( "  time " +time +"  Period  = " +b[1]+" Starting position = " + b[0] + "  Support = "+Pat.support +"\r\n");
                             curP.detete(curP,Pat); 
                        }
                    }
                }
                periodicPattern.put(key,curP);                
            }
            out.close();
           //periodicPattern.clear();    */        
        }
        catch(Exception ex)
        {
            System.out.println("Parsimonious pattern error");
        }        
    }
    private void OriginalPatternPrint()
    {
         System.out.println("periodic pattern = " +periodicPattern.size());
         Set set = periodicPattern.entrySet();
         Iterator it = set.iterator();
         while(it.hasNext())
         {
             Map.Entry pattern = (Map.Entry)it.next();
             String key = pattern.getKey().toString();
             String b[] = key.split("-");
             PeriodicPattern P = (PeriodicPattern)pattern.getValue();
             System.out.println(P.Periodic.size());
              for(int i = 1; i <=P.Periodic.size();i++)
                 {
                     PPattern pat = (PPattern)P.Periodic.get(i-1);
                    System.out.println("Original Pattern = "+pat.Pattern + "   suppoert = "+pat.support + " start = "+b[0] + " period = "+b[1]);
                 
                 }
         }
       //  System.out.println("ok");
    }
             
    private void tempPatternPrint()
    {
     //   System.out.println("Temp pattern = " +tempPattern.size());
         Set set = tempPattern.entrySet();
         Iterator it = set.iterator();
         while(it.hasNext())
         {
             Map.Entry pattern = (Map.Entry)it.next();
             String key = pattern.getKey().toString();
             String b[] = key.split("-");
             PeriodicPattern P = (PeriodicPattern)pattern.getValue();
             System.out.println(P.Periodic.size());
              for(int i = 1; i <=P.Periodic.size();i++)
                 {
                     PPattern pat = (PPattern)P.Periodic.get(i-1);
                     System.out.println("Temp Pattern = "+pat.Pattern + "   suppoert = "+pat.support + " start = "+b[0] + " period = "+b[1]);
                 }
         }
      //   System.out.println("ok");
       
    }
    
     private void UpdatePattern() {
       //  System.out.println("Temp priodic Pattern Number = "+tempPattern.size());
         Set set = tempPattern.entrySet();                                      // temp pattern set 
         Iterator it = set.iterator();
         while(it.hasNext())
         {
             Map.Entry pattern = (Map.Entry)it.next();
             String key = pattern.getKey().toString();                          // temp pattern key
             PeriodicPattern P = (PeriodicPattern)pattern.getValue();           // temp pattern
          //   System.out.println("Temp pattern length = " +P.Periodic.size());   // temp pattern size
            
                 if(periodicPattern.containsKey(key))                           // if contains in original periodic pattern 
                 {
                     PeriodicPattern PreP = (PeriodicPattern)periodicPattern.get(key);   // find final pattern 
                //     System.out.println(" Prep length = "+PreP.Periodic.size());
                      for(int j = 1; j <=P.Periodic.size();j++)                 //foe each temp pattern               
                     {
                         PPattern cur = P.Periodic.get(j-1);                    // get current one 
                         int found = 0;
                         for(int i = PreP.Periodic.size(); i >=1; i--)
                         {
                             PPattern prePattern = PreP.Periodic.get(i-1);
                             if(patternEqual(cur.Pattern,prePattern.Pattern) && cur.support > prePattern.support)  //perivious pattern also exist in current pattern
                             {
                                 //PreP.Periodic.set(i-1, cur); 
                                // found = 1;break;
                                 PreP.Periodic.remove(i-i);
                             }
                         }
                        // if(found == 0)
                         {
                            PreP.Periodic.add(cur);
                         }                    //set update Pattern
                     }
                      periodicPattern.put(key,PreP);  
                     //  System.out.println(" Prep after length = "+PreP.Periodic.size());
                 }
                 else
                 {
                     periodicPattern.put(key, P);
                     System.out.println(P.Periodic.get(0).Pattern);
                 } 
            
           //  System.out.println("Ok");
         }
         tempPattern.clear();  
     //   OriginalPatternPrint();         
    }
     private void ParsimoniousPatterns(boolean status)
    {
        try{
            
            UpdatePattern(); // Update pattern
            BufferedWriter out = new BufferedWriter(new FileWriter("Parsimonious pattern",status));
            
            Map<String, PeriodicPattern> sortedMap = new TreeMap<String, PeriodicPattern>(periodicPattern);
            
            Set set = sortedMap.entrySet();
            Iterator it = set.iterator();
          
            while(it.hasNext())
            {
                Map.Entry pattern = (Map.Entry)it.next();           
                String key = pattern.getKey().toString();                       //Current Key
                String b[]=key.split("-");                                      //Split Key
             
                int period = Integer.parseInt(b[1]);                            //Current Pattern period
                int start = Integer.parseInt(b[0]);                             // Current Pattern start position
               
                PeriodicPattern curP =  (PeriodicPattern) pattern.getValue();   // Current Patterns at position period and start
              
               for(int i = curP.Periodic.size(); i >=1; i--)
                {
                    PPattern Pat = curP.Periodic.get(i-1);
                   
                     //  if((subsumtion(sortedMap,start,period,Pat.Pattern,Pat.support)== false) && (subsumtionIntoSamePeriodStart(curP,Pat)==false))
                               //   if(subsumtionIntoSamePeriodStart(curP,Pat)==false)                    
                        {
                             out.write("Pattern =   "+Pat.Pattern);
                             out.write( "    Period  = " +b[1]+" Starting position = " + b[0] + "  Support = "+Pat.support +"\r\n");
                             curP.Periodic.remove(i-1);  
                        }                    
                }     
            }
            out.close();
           //periodicPattern.clear();
            
        }
        catch(Exception ex)
        {
            System.out.println("Parsimonious pattern error");
        }
        
    }

    // subsumtion 
    private boolean subsumtion(Map<String, PeriodicPattern> sortedMap, int start, int period, String Pattern, int support) {
        
        try{
                for(int i = 1; i <= start;i++)
                {
                    for( int j = 1; j <= period;j++)
                    {
                       
                        String searchkey = i+"-"+j;
                        
                        if(sortedMap.containsKey(searchkey))
                        {
                            PeriodicPattern PP  = (PeriodicPattern) sortedMap.get(searchkey);
                            
                          //  System.out.println(PP.Periodic.)
                            
                            Iterator it = PP.Periodic.iterator();
                            while(it.hasNext())
                            {
                                PPattern prePat = (PPattern)it.next();
                                if((patternEqual(prePat.Pattern,Pattern)) && prePat.support > support)
                                    return true;
                            }                           
                        }
                    }
                  }                    
                   
        }catch(Exception ex)
        {
            System.out.println("Subsumption Error");
        }
        return false;               
    }
    private boolean patternEqual(String Pattern, String Pattern0) {
        String b[] = Pattern0.split(";");
        for(int i = 0; i < b.length;i++)
        {
            if(!Pattern.contains(b[i]))
            {
                return false;
            }
        }
        return true;
    }
    private boolean subsumtionIntoSamePeriodStart(PeriodicPattern curP, PPattern Pat) {
        Iterator It = curP.Periodic.iterator();
        
        while(It.hasNext())
        {
            PPattern P = (PPattern)It.next();
            if(patternEqual(P.Pattern,Pat.Pattern) && P.support > Pat.support)
            {
               // System.out.println("Okkkk");
                return true;
            }
            
        }
        return false;
    }
     // Memory Uses funciton
    public static float MemoryUsed()
    {
        long total = Runtime.getRuntime().totalMemory();
        long used  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("Total memory = "+total/(1024*1024) +" MB   Used Memory  = "+used/(1024*1024) +" MB");
        return total/(1024*1024);
    }
    
    // time contains
    private boolean timeContains(int[] vertexTimeset, int time) {
        for(int i = 0; i < Pmax;i++)
            if(vertexTimeset[i]== time)
                return true;
        return false;
    }
     /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        float Privious_Memory = MemoryUsed();
        
        long startTime = System.currentTimeMillis();
        
         //Get the jvm heap size.
        long heapSize = Runtime.getRuntime().totalMemory()/(1024*1024);
         
        //Print the jvm heap size.
        System.out.println("Heap Size = " + heapSize);
        
        // Call Constructor
        new SPPMiner();
        
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Total Time = "+totalTime/1000);
        
        float EndUsed_Memory = MemoryUsed();
      //  System.out.print("Total Memory Used = "+ (EndUsed_Memory - Privious_Memory));        
        
    }
}