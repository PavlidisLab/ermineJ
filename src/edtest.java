import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import classScore.classPvalRun;

public class edtest {
   public static void main(String args[]) {

      String pbPvalFile = "age.welch.pvals.highexpression.forerminej.txt";
      String affyGoFile = "MG-U74Av2.go.txt";
      String goNameFile = "goNames.txt";
      String destinFile = "q-100-01-true.txt";
      String ugFile = "MG-U74Av2.ug.txt";
      String method;
      classPvalRun test;
      int iters;

      for (int i = 1; i <= 10; i++) {
         iters = i * 1000;
         method = "false";
         String number = (new Integer(i)).toString();
         destinFile = method + "-mean-" + number + "log4";
         /*
          if(i==1){
             method = "false";
             destinFile = "f-mean-4-01";
          }
          else if(i==2){
             iters =5000;
             method = "false";
             destinFile = "f-mean-5-01";
          }
          else if(i==3){
             iters =6000;
             method = "false";
             destinFile = "f-mean-6-01";
          }
          else if(i==4){
             iters =2000;
             method = "false";
             destinFile = "f-mean-2-01";
          }
          else if(i==5){
             iters =2000;
             method = "true";
             destinFile = "t-mean-2-01";
          }
          else if(i==6){
             iters =3000;
             method = "true";
             destinFile = "t-mean-3-01";
          }
          else if(i==7){
             iters =4000;
             method = "true";
             destinFile = "t-mean-4-01";
          }
          else if(i==8){
             iters =5000;
             method = "true";
             destinFile = "t-mean-5-01";
          }
          else if(i==9){
             iters =6000;
             method = "true";
             destinFile = "t-mean-6-01";
          }
          else{
             iters =7000;
             method = "true";
             destinFile = "t-mean-7-01";
          }*/

         if (!isURL(pbPvalFile)) {
            pbPvalFile = getCanonical(pbPvalFile);
         } else {
            ;
         }
         if (!isURL(affyGoFile)) {
            affyGoFile = getCanonical(affyGoFile);
         } else {
            ;
         }
         if (!isURL(destinFile)) {
            destinFile = getCanonical(destinFile);
         } else {
            ;
         }
         if (!isURL(goNameFile)) {
            goNameFile = getCanonical(goNameFile);
         } else {
            ;
         }
         if (!isURL(ugFile)) {
            ugFile = getCanonical(ugFile);
         } else {
            ;
         }

         // test = new classScore.class_pvals(pbPvalFile,affyGoFile,goNameFile,destinFile,ugFile,"MEAN_METHOD", 100, 2,iters, 50, 5.0, 0.0001, method);
         //test.class_pval_generator();
      }
   }

   protected static String getCanonical(String in) {
      if (in == null || in.length() == 0) {
         return in;
      }
      File outFile = new File(in);
      try {
         return outFile.getCanonicalPath();
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   protected static boolean isURL(String filename) {
      try {
         URL url = new URL(filename);
      } catch (MalformedURLException e) {
         return false;
      }
      return true;
   }
}
