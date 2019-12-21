// Amit Charran
// CS323_25
// project 9
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static class Schedule{
        int numNodes;
        int numProcs;
        int procUsed;
        int currentTime;

        boolean loop = false;


        LinkedList nodesAndEdges[];
        Node allNodes[];
        Job jobsArray[];
        Proc procArray[];

        ArrayList<Node> open; // This is a "node pointer" that acts like a listHead
        int adjMatrix[][];


        int parentCountArray[];
        int dependentCountArray[];
        int onGraphArray[];
        int totalJobTime;
        int scheduleTable[][];

        boolean unlimitedProcessor = false;

        File infile1;
        File infile2;
        File outfile1;
        File outfile2;


        public Schedule(File in1, File in2, File out1, File out2, int k){
            infile1 = in1;
            infile2 = in2;
            outfile1 = out1;
            outfile2 = out2;

            Scanner file1;
            Scanner file2;

            try{
                file1 = new Scanner(infile1);
                file2 = new Scanner(infile2);
                numProcs = file2.nextInt();
                numNodes = file1.nextInt();
                if(numProcs <= 0){
                    System.out.println("needs more than 1 processor");
                    return;
                }
                else if(numProcs > numNodes){
                    numProcs = numNodes; // So we have unlimited processor
                    unlimitedProcessor = true;
                }


                open = new ArrayList<Node>();
                adjMatrix = initializeAdjMatrix();
                jobsArray = new Job[numNodes + 1];
                procArray = new Proc[numNodes + 1];


                // Won't be able to do this until total job time
                // ScheduleTable = new int[numProcs + 1][totalJobTime + 1];

                loadMatrix();
                enterAllNodes();
                computeParentCount();
                computeDependentCount();

                //This will also initialize total job time
                constructJobArray(infile2);

                numProcs = k;
                scheduleTable = new int[numProcs][totalJobTime];
                initializeScheduleTable();

                //here is where i start a loop
                procUsed = 0;
                currentTime = 0;

                loadOpen();




                int loopchecker[] = dependentCountArray;
                boolean dependentArrayNotAll0 = true;
                while(dependentArrayNotAll0) {
                    computeParentCount();
                    computeDependentCount();
                    loadOpen();

                    if(equalArrays(loopchecker, dependentCountArray)){
                        System.out.println("there is a loop");
                        loop = true;
                        dependentArrayNotAll0 = false;
                    }

                    loopchecker = dependentCountArray;

                    int counter = 0;
                    for(int i = 0; i < dependentCountArray.length; i++){
                        if(dependentCountArray[i] == 0){
                            counter++;
                        }
                    }
                    //end loop if all dependent array values == 0
                    if(counter == dependentCountArray.length){
                        dependentArrayNotAll0 = false;
                    }
                }

//                for (int i= 0; i  < allNodes.length; i++){
//                    System.out.println(allNodes[i] + " ");
//                }


                while(currentTime < totalJobTime){
//                    for(int i = 0; i < open.size(); i++){
//                        System.out.print(open.get(i).jobID);
//                        if(i == open.size() -1){
//                            System.out.println();
//                        }
//                    }

                    updateScheduleTable();
                    currentTime++;
                }

//                for(int i = 0; i  < scheduleTable.length; i++){
//                    for (int j = 0; j < scheduleTable[i].length; j++){
//                        System.out.print(scheduleTable[i][j] + " ");
//                    }
//                    System.out.println();
//                }


                file1.close();
                file2.close();


                printToOutfile();


            }catch(FileNotFoundException e){
                System.out.println(e.getMessage());
            }
        }

        private void printToOutfile() {
//
//            System.out.print("\t -");
//          for(int i = 0; i < scheduleTable[0].length; i++){
//              System.out.print(i + "--- ");
//          }
//            System.out.println();
//            System.out.println("\t ---------------------------------------------------------------------------------------");
//
//            for(int i = 0; i < scheduleTable.length;i++){
//                System.out.print("P(" + i + ")");
//                for(int j= 0; j < scheduleTable[i].length; j++){
//                    if(scheduleTable[i][j] != -1) {
//                        System.out.print("| " + scheduleTable[i][j] + "  ");
//                    }else {
//                        System.out.print("| -");
//                    }
//                }
//                System.out.println();
//                System.out.println("\t ---------------------------------------------------------------------------------------");
//            }

            try{
                BufferedWriter writer = new BufferedWriter(new FileWriter(outfile1));


                writer.write("\t -");
                for(int i = 0; i < scheduleTable[0].length; i++){
                    writer.write(i + "--- ");
                }
                writer.write("\n");
                writer.write("\t ---------------------------------------------------------------------------------------");
                writer.write("\n");


                for(int i = 0; i < scheduleTable.length;i++){
                    writer.write("P(" + i + ")");
                    for(int j= 0; j < scheduleTable[i].length; j++){
                        if(scheduleTable[i][j] != -1) {
                            writer.write("| " + scheduleTable[i][j] + "  ");
                        }else {
                            writer.write("| -");
                        }
                    }
                    writer.write("\n");
                    writer.write("\t ---------------------------------------------------------------------------------------");
                    writer.write("\n");
                }

                writer.close();
            }catch(IOException e){
                System.out.println(e.getMessage());
            }

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outfile2));

                if(loop){
                    writer.write("There is a loop");
                }else {writer.write("I did not use this file");}

                writer.close();
            }catch (IOException e){
                System.out.println(e.getMessage());
            }


        }

        private void updateProcUsed() {
            procUsed = 0;
            for(int i = 0; i < scheduleTable.length; i++){

                if(scheduleTable[i][currentTime] == -1){
                    procUsed = i;
                   // System.out.println(procUsed + " procused:  current time" + currentTime);
                    return;
                }

            }
            procUsed = 3;
         //  System.out.println("procUsed updated to "+ procUsed);
        }

        private void initializeScheduleTable() {
            for(int i = 0; i < scheduleTable.length;i++){
                for (int j = 0; j < scheduleTable[i].length;j++){
                    scheduleTable[i][j] = -1;
                }
            }
        }

        private boolean equalArrays(int[] loopchecker, int[] dependentCountArray) {
            if(loopchecker.length == dependentCountArray.length){
                for(int i = 0; i < loopchecker.length;i++){
                    if(loopchecker[i] != dependentCountArray[i]){
                        return false;
                    }
                }
                return true;
            }

            return false;
        }


        private void loadOpen() {

            for(int i = 1; i < parentCountArray.length; i++){
                if(parentCountArray[i] == 0){
                    open.add(allNodes[i]);
                    allNodes[i].orphan = true;
                }

            }

            for(int i = 0; i < open.size(); i++){
               // System.out.print(open.get(i).jobID);
            }
         //  System.out.println();

            updateScheduleTable();
            currentTime++;



        }



        private void updateScheduleTable() {


            updateProcUsed();
            while(true){
                if(open.isEmpty()){
                    // System.out.println("h");
                    break;
                }
                if(procUsed == numProcs){
                   // System.out.println("hh");
                    break;
                }

                        Node a = open.remove(0);
                        for (int j = 0; j < a.jobTime; j++) {
                        // System.out.println( procUsed +" " +  (currentTime + j) + " JobID: " + a.jobID);
                        scheduleTable[procUsed][currentTime + j] = a.jobID;
                                }
                        updateProcUsed();


            }


        }

        private void enterAllNodes(){
            nodesAndEdges = new LinkedList[numNodes + 1];
            allNodes= new Node[numNodes + 1];

            for(int i = 0; i < allNodes.length; i++){
                allNodes[i] = new Node(i);
            }


            for(int i =0; i < adjMatrix.length; i++){
                nodesAndEdges[i] = new LinkedList();
                for(int j = 0; j < adjMatrix[i].length;j++){
                    if(adjMatrix[i][j] == 1){
                       // System.out.println(allNodes[i] + "     " + i + j);

                        nodesAndEdges[i].addList(allNodes[j]);
                    }
                    //System.out.print(adjMatrix[i][j]);
                }
               // System.out.println();

            }

            for(int i = 0; i < allNodes.length;i++){
               // System.out.println(allNodes[i]);
                //System.out.println(i + " " + nodesAndEdges[i]);
                //System.out.println(nodesAndEdges[i].returnInt());
                allNodes[i].dependentCount = nodesAndEdges[i].returnInt();
            }

            try {
                Scanner sc = new Scanner(infile2);

                int one = sc.nextInt();
                int two;

                while(sc.hasNext()){
                    one = sc.nextInt();
                    two = sc.nextInt();
                    allNodes[one].jobTime = two;
                }

                sc.close();
            }catch (FileNotFoundException e){
                System.out.println(e);
            }

            for(int i = 0; i < allNodes.length; i++){
               // System.out.println(allNodes[i]);
            }

        }


        private void constructJobArray(File in){
            Scanner sc;
            for(int i =0; i < jobsArray.length;i++){
                jobsArray[i] = new Job();
            }

            try{
                sc = new Scanner(in);
                int nodeid = sc.nextInt();
                int time;

                while(sc.hasNext()){
                    nodeid = sc.nextInt();
                    time = sc.nextInt();
                    jobsArray[nodeid].jobTime = time;
                    jobsArray[nodeid].onWhichProc = -1;
                    jobsArray[nodeid].onOpen = 0;
                    jobsArray[nodeid].parentCount = parentCountArray[nodeid];
                    jobsArray[nodeid].dependentCount = dependentCountArray[nodeid];

                    totalJobTime += time;

                }
                sc.close();

            }catch(FileNotFoundException e){
                System.out.println(e.getMessage());
            }

            for(int i = 0; i < jobsArray.length;i++){
               // System.out.println(jobsArray[i]);
            }

        }

        private void computeDependentCount() {
            // here I look through parent count array and if there is a parent I make that value in the array 1
            dependentCountArray = new int[numNodes+1];
            for(int i = 0; i < dependentCountArray.length;i++){
                dependentCountArray[i] = 0;
            }
            for(int i = 0; i < adjMatrix.length; i++){
                int hasDependentCounter = 0;
                for(int j = 0; j < adjMatrix[i].length; j++){

                    if(adjMatrix[i][j] == 1){
                        if(!allNodes[i].orphan){
                                hasDependentCounter++;
                        }
                    }

                }
                dependentCountArray[i] = hasDependentCounter;
            }

            for(int i = 0; i < dependentCountArray.length;i++){
                //System.out.println(i + " has dependent " + dependentCountArray[i]);
            }



        }

        private void computeParentCount() {
            // here I look through parent count array and if there is a parent I make that value in the array 1
            parentCountArray = new int[numNodes+1];
            for(int i = 0; i < parentCountArray.length;i++){
                parentCountArray[i] = 0;
            }
            for(int i = 0; i < adjMatrix.length; i++){
                int hasParentCounter = 0;
                for(int j = 0; j < adjMatrix[i].length; j++){
                    if(allNodes[i].orphan){
                        hasParentCounter = -1;
                        break;
                    }
                    if(adjMatrix[j][i] == 1) {
                            hasParentCounter++;
                    }
                }
                parentCountArray[i] = hasParentCounter;
            }


            for(int i = 0; i < adjMatrix.length;i++){
                for(int j =0; j < adjMatrix[i].length;j++){
                    if(adjMatrix[j][i] == 1){
                        if(allNodes[j].orphan && allNodes[i].orphan == false){
                            parentCountArray[i]--;
                        }
                    }
                }
            }




            for(int i = 0; i < parentCountArray.length; i++){
               // System.out.println(i + " has parent " +parentCountArray[i]);
            }





        }

        private void loadMatrix() {
            Scanner sc;
            try{
                sc = new Scanner(infile1);
                int one = sc.nextInt();
                int two;

                while(sc.hasNext()){
                    one = sc.nextInt();
                    two = sc.nextInt();

                    adjMatrix[one][two] = 1;
                }
                sc.close();

            }catch (FileNotFoundException e){
                //System.out.println("load matrix: FNFE");
            }

//            for(int i = 0; i < adjMatrix.length; i++){
//                for(int j =0; j < adjMatrix[i].length;j++){
//                    System.out.print(adjMatrix[i][j]);
//                }
//                System.out.println();
//            }

        }

        private int[][] initializeAdjMatrix() {
            int ans[][] = new int[numNodes + 1][ numNodes + 1];

            for(int i =0;i < ans.length; i++){
                for(int j = 0; j < ans[i].length; j++){
                    ans[i][j] = 0;
                }
            }

            return ans;
        }


        private class Proc{
            int doWhichJob;
            int timeRemain;
        }

        private class Job{
            int jobTime;
            int onWhichProc;
            int parentCount;
            int dependentCount;
            int onOpen;
            ////
            public Job(int jt, int owp, int pc, int dc,int o){
                jobTime = jt;
                onWhichProc = owp;
                parentCount = pc;
                dependentCount = dc;
                onOpen=o;
            }
            public  Job(){
                jobTime = -1;
                onWhichProc = -1;
                parentCount = -1;
                dependentCount = -1;
                onOpen= -1;
            }

            public String toString(){
                return jobTime + " " + onWhichProc + " " + onOpen+ " " + parentCount+ " " + dependentCount;
            }

        }

        private class Node{
            int jobID;
            int jobTime;
            int dependentCount;
            boolean orphan;
            Node next;

            public Node(int ji, int jt, int dc){
                jobID = ji;
                jobTime = jt;
                dependentCount = dc;
                orphan = false;
            }
            public Node(){
                jobID = -1;
                jobTime = -1;
                dependentCount = -1;
                orphan = false;
            }
            public Node(int ji){
                jobID = ji;
                jobTime = -1;
                dependentCount = -1;
                orphan = false;
            }

            public int getJobID() {
                return jobID;
            }

            public void setJobID(int jobID) {
                this.jobID = jobID;
            }

            public int getJobTime() {
                return jobTime;
            }

            public void setJobTime(int jobTime) {
                this.jobTime = jobTime;
            }

            public int getDependentCount() {
                return dependentCount;
            }

            public void setDependentCount(int dependentCount) {
                this.dependentCount = dependentCount;
            }

            public Node getNext() {
                return next;
            }

            public void setNext(Node next) {
                this.next = next;
            }
            public String toString(){
                    return jobID + " " + jobTime + " " + dependentCount;
            }

        }

        private class LinkedList{
            Node listHead;
            Node tail;

            public LinkedList(){
                listHead = new Node(-11,-1, -1);
                tail = new Node(-111,-1,-1);
                listHead.next = tail;
            }
            public void addList(Node n){
                Node m = listHead.next;
                listHead.next = n;
                n.next = m;
            }
            public boolean isEmpty(){
                return listHead.next == tail;
            }

            public Node getListHead() {
                return listHead;
            }

            public void setListHead(Node listHead) {
                this.listHead = listHead;
            }

            public int returnInt(){
                int counter = -2;

                Node itNode = listHead;
                while(itNode != null){
                    counter++;
                    itNode = itNode.next;
                }

                return counter;
            }

            public String toString(){
                String s = "";

                    Node iterativeNode = listHead.next;
                    while (iterativeNode != tail || iterativeNode != null) {
                        if(iterativeNode == null) break;
                        if(iterativeNode.jobID != -111){
                        s += iterativeNode + "-->";}
                        iterativeNode = iterativeNode.next;
                    }

                return s;
            }
        }

    }

    public static void main(String[] args) {
        File infile1 = new File(args[0]);
        File infile2 = new File(args[1]);
        File outfile1 = new File(args[2]);
        File outfile2 = new File(args[3]);
        String numProc =args[4];

        int numProccessor;

        try{
            numProccessor = Integer.parseInt(numProc);
        }catch (NumberFormatException nfe){
            System.out.println("Input is not Integer: Number Processor default to 3");
            numProccessor = 3;
        }



        Schedule s = new Schedule(infile1,infile2,outfile1,outfile2, numProccessor);
    }
}
