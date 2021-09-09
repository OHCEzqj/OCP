package ComparingTechniques2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.IntStream;

/**
 * @author: zqj
 * @date: 2021/02/18
 * @description: 字典排序，扩展模型，降低时间开销
 */
public class FICBPL_E {
    String CoverageFile;
    int[][] CoverageMatrix;
    int TSNum=0;  //用例数量
    int STNum=0;  //组合数量
    ArrayList<Integer> SelectedTS = new ArrayList<Integer>();
    ArrayList<Integer> CandidateTS = new ArrayList<Integer>();
    int[] StCoveredNum; //每条组合被覆盖次数
    int[] OrderStCoveredNum;
    int MaxIndex=-1;
    List<Integer> maxIndexList=new ArrayList<>();
    public FICBPL_E(String CoverageFile){
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
                line = line.trim().replaceAll(" ","");
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
            this.CoverageMatrix = new int[TSNum][STNum];
            for(int i=0; i<TSNum; i++){
                char[] tempCoverage = tempAl.get(i).toCharArray();
                for (int j=0; j<tempCoverage.length;j++){
                    CoverageMatrix[i][j] = tempCoverage[j] - '0';
                }
            }

            for (int i=0;i<TSNum;i++){ CandidateTS.add(i); }

            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public int[] getSelectedTS(){
        this.getCoverageMatrix();
        StCoveredNum=new int[STNum];

        while (CandidateTS.size()>0){
            OrderStCoveredNum = StCoveredNum.clone();
            Arrays.sort(OrderStCoveredNum);
            int[] sortedIndices = IntStream.range(0, StCoveredNum.length)
                    .boxed().sorted((i, j) -> StCoveredNum[i] - StCoveredNum[j])
                    .mapToInt(ele -> ele).toArray();
            int[] q = computeQ(OrderStCoveredNum);
            int[] w ;
            int[] bestW = new int[q.length];
            for(int i=0;i<CandidateTS.size();i++){
                int test_index = CandidateTS.get(i);
                int[] oc = computeOC(CoverageMatrix[test_index], sortedIndices);
                w = computeW(oc,q);
                int tag = compareLexi(bestW,w);
                if (tag== 1){
                    maxIndexList.clear();
                    maxIndexList.add(CandidateTS.get(i));
                    bestW =w.clone();
                }else if (tag ==0){
                    maxIndexList.add(CandidateTS.get(i));
                }
            }

            MaxIndex=maxIndexList.get(new Random().nextInt(maxIndexList.size()));
            SelectedTS.add(MaxIndex);
            CandidateTS.remove((Integer) MaxIndex);
            maxIndexList.clear();
            StCoveredNum = updateStCoveredNum(StCoveredNum, CoverageMatrix[MaxIndex]);

        }
        int[] result=new int[SelectedTS.size()];
        for(int i=0;i<SelectedTS.size();i++){
            result[i]=SelectedTS.get(i);
        }
        return result;
        // return SelectedTS;

    }

    /**
     * 将occ数组划分区域，相同值在同一区域
     * @return 返回区域数组，每个取值为该区大小
     */
    public static int[] computeQ(int[] occ){
        ArrayList<Integer> qList = new ArrayList<Integer>();

        int start =0; //该区域起始值
        for(int i=1; i<occ.length; i++){//从第二个取值开始
            if (occ[i] - occ[i-1] >0){// occ已排序，所以>0和!=0一样
                qList.add(i -start) ;
                start = i;
            }
        }
        //加上最后一个
        qList.add(occ.length -start) ;

        // 输出
        int[] q=new int[qList.size()];
        for(int i=0;i<qList.size();i++){
            q[i]=qList.get(i);
        }
        return q;
    }

    /**
     * 计算该用例的权重数组
     * @param oc 该用例有序覆盖数组
     * @param q 权重分配数组
     * @return
     */
    public static  int[] computeW(int[] oc,  int[] q){
        int[] w = new int[q.length]; // 权重矩阵
        int start = 0; //该区域起始下标
        for (int i =0; i< q.length; i ++){ //对每个区域
            int partitionSize = q[i]; // 该区域长度
            for (int j = start; j < start + partitionSize; j++){
                w[i] += oc[j];
            }
            start += partitionSize;
        }
        return w;
    }

    /**
     * 依据排列规则，计算用例的有序覆盖数组，c->oc
     * @param c 原始覆盖数组
     * @param sortedIndices 转换规则
     * @return 转换后覆盖数组
     */
    public int[] computeOC(int[] c, int[] sortedIndices){

        int[] oc = new int[sortedIndices.length];
        for (int i = 0; i<sortedIndices.length; i ++ ){
            oc[i] = c[sortedIndices[i]];
        }
        return oc ;
    }

    /**
     * 比较两个数组的字典大小
     * @param a1 原数组
     * @param a2 现数组
     * @return 大小标识，0：一样大；1：a2>a1；-1:a2<a1
     */
    public int compareLexi(int[] a1, int[] a2) {
        int tag = 0;
        for (int k = 0; k < a1.length; k++) {
            if (a2[k] != a1[k]) {
                tag = a2[k] > a1[k] ? 1 : -1;
                break;
            }
        }
        return tag;
    }

    /**
     * 更新语句被覆盖次数
     * @param a 第一个数组
     * @param b 第二个数组
     * @return 相加后的数组
     */
    public int[] updateStCoveredNum(int[] a, int[] b){
        int[] c = a.clone();
        for(int i=0;i<a.length;i++){
            c[i] += b[i];
        }
        return c;
    }

    public static void main(String[] x){
        FICBPL_E ficbpl_e = new FICBPL_E("Orig_Data/CoverageMatrix/gzip/gzip_1_matrix.txt");
        for(int i : ficbpl_e.getSelectedTS()){
            System.out.print(i);
        }
    }
}
