package TreeTCP;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;


public class OCP {
    String CoverageFile;
    char[][] CoverageMatrix;
    int TSNum=0;  //用例数量
    int STNum=0;  //语句数量
    ArrayList<Integer> SelectedTS = new ArrayList<Integer>();
    TreeMap<Integer, ArrayList<Node>> CandidateTS=new TreeMap<>();
    ArrayList<Integer> TSZero = new ArrayList<Integer>();//存放覆盖率为0的用例
    int[] StCoveredNum; //每条语句是否被覆盖
    int maxIndex=-1;
    ArrayList<Node> maxCoverageNodes;
    int maxCoverage = -1;
    Node maxNode = null;
    public  OCP(String CoverageFile){
        this.CoverageFile=CoverageFile;
    }

    /**
     * 读取文件，生成CoverageMatrix矩阵
     */
    public void getCoverageMatrix(){
        try{
            BufferedReader br = new BufferedReader(new FileReader(CoverageFile));
            ArrayList<String> tempAl = new ArrayList<String>();
            String line;
            while((line = br.readLine()) != null){
                if(STNum == 0){
                    STNum = line.length();
                }else if(STNum != line.length()){
                    System.out.println("ERROR: The line from Coverage Matrix File is WORNG.\n"+line);
                    System.exit(1);
                }
                tempAl.add(line);
            }
            TSNum=tempAl.size();
            //初始化CoverageMatrix
            this.CoverageMatrix = new char[TSNum][STNum];
            for(int i=0; i<TSNum; i++){
                CoverageMatrix[i] = tempAl.get(i).toCharArray();
            }
            StCoveredNum=new int[STNum];
            for (int i=0;i<TSNum;i++){
                int tempCoverage = getCoveredNumber(CoverageMatrix[i]);
                if(tempCoverage>0){
                    if(CandidateTS.containsKey(tempCoverage)){
                        CandidateTS.get(tempCoverage).add(new Node(i,tempCoverage));
                    }else{
                        ArrayList<Node> list=new ArrayList<>();
                        list.add(new Node(i,tempCoverage));
                        CandidateTS.put(tempCoverage, list);
                    }
                }
                else { TSZero.add(i);}
            }

            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public int[] getSelectedTS() {
        this.getCoverageMatrix();

        while (SelectedTS.size() < (TSNum - TSZero.size()) ){

            do {
                maxCoverage = CandidateTS.lastKey();
                maxCoverageNodes = CandidateTS.get(maxCoverage);
                if (maxCoverage == 0){
                    for (int i=0;i<STNum;i++){StCoveredNum[i]=0;}
                    resetFlag();
                }
                updateTree(maxCoverageNodes);
            } while (maxCoverageNodes.size() == 0 && (CandidateTS.remove(maxCoverage) != null));


            maxNode=maxCoverageNodes.get(new Random().nextInt(maxCoverageNodes.size()));
            maxIndex = maxNode.index;

            SelectedTS.add(maxIndex);
            maxCoverageNodes.remove(maxNode);
            updateStCoveredNum();
            resetFlag();

        }
        SelectedTS.addAll(TSZero);
        int[] result=new int[SelectedTS.size()];
        for(int i=0;i<SelectedTS.size();i++){
            result[i]=SelectedTS.get(i);
        }
        return result;
        // return SelectedTS;

    }
    //计算该测试用例覆盖的语句数
    public int getCoveredNumber(char[] a){
        int num = 0;
        for(int i=0; i<a.length; i++){
            if(StCoveredNum[i]==0 && a[i] == '1'){
                num++;
            }
        }
        return num;
    }

    List<Integer> maxIndexList=new ArrayList<Integer>();
    /**
     *
     * @param a 传入的数组
     * @return 最大值的下标
     */
    public int selectMax(float[] a){
        maxIndexList.clear();
        float max = a[0];
        for(int i=0; i<a.length; i++){
            if(a[i] >= max){
                if (a[i] > max){
                    max = a[i];
                    maxIndexList.clear();
                }
                maxIndexList.add(i);
            }
        }

        if (maxIndexList.size() == 0){
            return  -1;
        }else {
            return maxIndexList.get(new Random().nextInt(maxIndexList.size()));
        }

    }


    /**
     * 根据被选测试用例MaxIndex，更新覆盖数组StCoveredNum
     */
    public void updateStCoveredNum(){
        for(int i=0;i<STNum;i++){
            if(CoverageMatrix[maxIndex][i]=='1'){StCoveredNum[i]=1;}
        }
    }

    private void updateTree(ArrayList<Node> maxCoverageNodes) {
        Iterator<Node> iterator=maxCoverageNodes.iterator();
        while(iterator.hasNext()){
            Node testNode = iterator.next();
            if(testNode.flag == 0){
                int coverage = getCoveredNumber(CoverageMatrix[testNode.index]);
                if(coverage != testNode.degree){
                    iterator.remove();
                    testNode.degree = coverage;
                    testNode.flag=1;
                    if(CandidateTS.containsKey(coverage)){
                        CandidateTS.get(coverage).add(testNode);
                    }else{
                        ArrayList<Node> list=new ArrayList<>();
                        list.add(testNode);
                        CandidateTS.put(coverage, list);
                    }
                }
            }
        }
    }

    public void resetFlag(){
        Iterator<Map.Entry<Integer, ArrayList<Node>>> it = CandidateTS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer,ArrayList<Node>> entry = it.next();
            for(Node node:entry.getValue()){
                node.flag=0;
            }
        }
    }


    /**
     * 用例node
     */
    class Node{
        int index;
        int flag=0; // 未更新
        int degree;

        /**
         *
         * @param index 用例下标
         * @param degree 用例覆盖值
         */
        public Node(int index,int degree){
            this.index=index;
            this.degree=degree;
        }
    }


}