package ComparingTechniques2;

import jdk.nashorn.internal.ir.IfNode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * @author: zqj
 * @date: 2021/05/15
 * @description: implement Lingming Zhang ICSE'13 paper.
 */
public class UnifyGreedy {
    String CoverageFile;
    char[][] CoverageMatrix;
    int TSNum=0;  //用例数量
    int STNum=0;  //语句数量
    ArrayList<Integer> SelectedTS = new ArrayList<Integer>();
    ArrayList<Integer> CandidateTS = new ArrayList<Integer>();
    double[] STProb; //每条语句检测缺陷概率
    double [] TSValue; //每个用例的评估值
    int maxIndex=-1;
    double p = 0.65;
    public  UnifyGreedy(String CoverageFile){
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
            TSValue=new double[TSNum];
            for(int i=0; i<TSNum; i++){
                CoverageMatrix[i] = tempAl.get(i).toCharArray();
                CandidateTS.add(i);
            }

            STProb=new double[STNum];
            for (int j=0; j< STNum; j++){
                STProb[j] = 1;
            }


            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public int[] getSelectedTS() {
        this.getCoverageMatrix();

        while (CandidateTS.size()>0){
//            System.out.println(SelectedTS.size());
            for(int i=0;i<CandidateTS.size();i++){
                int testIndex = CandidateTS.get(i);
                for (int j=0; j< STNum; j++){
                    if (CoverageMatrix[testIndex][j] == '1'){
                        TSValue[testIndex] += STProb[j];
                    }
                }

            }
            maxIndex=selectMax(TSValue);
            SelectedTS.add(maxIndex);
            CandidateTS.remove((Integer) maxIndex);
            TSValue[maxIndex] = -1;
            updateSTProb();

        }
        int[] result=new int[SelectedTS.size()];
        for(int i=0;i<SelectedTS.size();i++){
            result[i]=SelectedTS.get(i);
        }
        return result;
        // return SelectedTS;

    }

    public int selectMax(double[] a){
        List<Integer> maxIndex=new ArrayList<Integer>();
        int index = -1;
        double max = -1;
        for(int i=0; i<a.length; i++){
            if(a[i] >= max){
                if (a[i] > max){
                    max = a[i];
                    maxIndex.clear();
                }
                maxIndex.add(i);
            }
        }
        Collections.shuffle(maxIndex);
        index = maxIndex.get(0);
//        if (max == 0){
//            index = -1;
//        }else {
//            Collections.shuffle(maxIndex);
//            index = maxIndex.get(0);
//        }
        return index;
    }


    public void updateSTProb(){
        for(int i=0;i<STNum;i++){
            if(CoverageMatrix[maxIndex][i] == '1'){
                STProb[i] = STProb[i] * (1-p);
            }
        }
    }

    public static void main(String[] args){
        String path = "/Users/quanjunzhang/Desktop/TCP2021/RTCP_DATA/Orig_Data/CoverageMatrix/ant_v2/branch_matrix.txt";
        UnifyGreedy unifyGreedy=new UnifyGreedy(path);
        for (int temp :unifyGreedy.getSelectedTS()){
            System.out.print(temp + ", ");
        }

    }
}
