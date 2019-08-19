import java.io.FileReader;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;


public class Regression extends JFrame{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ArrayList<Double> X;
    ArrayList<Double> Y;
    
    public Regression(String fileName,String type,String title,String x,String y,String u){
      super("Regression Calculator");
      this.X = new ArrayList<Double>();
      this.Y = new ArrayList<Double>();
      this.readFile(fileName);
      XYDataset dataset = createDataset(type,u);
      JFreeChart chart = ChartFactory.createScatterPlot(title,x,y, dataset);
      XYPlot plot = (XYPlot) chart.getPlot();
      XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
      renderer.setSeriesLinesVisible(0, false);
      renderer.setSeriesShapesVisible(0, true);
      renderer.setSeriesLinesVisible(1, true);
      renderer.setSeriesShapesVisible(1, false);
      renderer.setSeriesPaint(0, Color.RED);
      renderer.setSeriesPaint(1, Color.BLUE);
      renderer.setSeriesPaint(2,Color.WHITE);
      plot.setRenderer(renderer);
      plot.setBackgroundPaint(new Color(255,228,196));
      ChartPanel panel = new ChartPanel(chart);
      setContentPane(panel);
      }

      public static void main (String[] args) {
        String fileName = args[0];
        String type;
        String title;
        String x;
        String y;
        String u;
        if (args.length>1)
          type = args[1];
        else{
          type = "0";
        }
        if (args.length>2)
          title = args[2];
        else{
           title = "Regression" ;
        }
        if (args.length>3)
          x = args[3]; 
        else{
          x = "X-axis" ;
        }
        if (args.length>4)
          y = args[4]; 
        else{
          y = "Y-axis" ;
        }
        if (args.length>5)
          u = args[5]; 
        else{
          u = "Data";
        }
      SwingUtilities.invokeLater(() -> {
        Regression regr = new Regression(fileName,type,title,x,y,u);
        regr.setSize(800, 400);
        regr.setLocationRelativeTo(null);
        regr.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        regr.setVisible(true);
      });
    }

    private static void error() {
      System.err.println("File Not Found");
      System.exit(1);
    }

    private void readFile(String fileName) {
      try (Scanner scanner = new Scanner(new FileReader(fileName))){
       while (scanner.hasNextLine()) {
        String s = scanner.nextLine();
        String[] arr = s.split("\\t");
        this.X.add(Double.parseDouble(arr[0]));
        this.Y.add(Double.parseDouble(arr[1]));
       } 
      } catch(FileNotFoundException e){
        error();
      }
    }

    private XYDataset createDataset(String type,String u) { 
      XYSeriesCollection dataset = new XYSeriesCollection();
      XYSeries series1 = new XYSeries(u);
      for(int i=0; i<this.X.size();i++){
        series1.add(this.X.get(i),this.Y.get(i));
      }
      dataset.addSeries(series1);
      XYSeries series2 = new XYSeries("Regression Function");
      double[] rf;
      switch(type){
        default: // choose best
          double[] lin = reg(this.X,this.Y,"1");
          double[] pow = reg(this.X,log(this.Y),"2");
          double[] ln = reg(log(this.X),this.Y,"3");
          if (lin[3]== Math.max(lin[3],Math.max(pow[3],ln[3]))){
            rf=lin;
            double f = this.X.get(0);
            double l =  this.X.get(this.X.size()-1);
            series2.add(f,(f*rf[0])+ rf[1]);
            series2.add(l,(l*rf[0])+ rf[1]);
            series2.setKey("Regression Function: " + "y = "+ String.format("%1.2f",rf[0]) + "x + " + String.format("%1.2f",rf[1]));
            dataset.addSeries(series2);
          }
          else if(pow[3]== Math.max(lin[3],Math.max(pow[3],ln[3]))){
            rf=pow;
            rf[1]= Math.pow(Math.E,rf[1]);
            rf[0]= pow[0];
            for (int i=0; i<this.X.size();i++){
              series2.add((double)this.X.get(i), rf[1]*Math.pow(Math.E,rf[0]*this.X.get(i)));
            }
            series2.setKey("Regression Function: " + "y = " + String.format("%1.2f",rf[1]) + "e ^" + String.format("%1.2f",rf[0])+ "x");
            dataset.addSeries(series2);
          }
          else {
            rf=ln;
            for (int i=0; i<this.X.size();i++){
             series2.add((double)this.X.get(i), rf[0]*Math.log(this.X.get(i)) + rf[1]); 
            } 
            series2.setKey("Regression Function: " + "y = "+ String.format("%1.2f",rf[0]) + "ln(x) + " + String.format("%1.2f",rf[1]));
            dataset.addSeries(series2);
          }
          break;
        case "1": // y = mx + b
          double f = this.X.get(0);
          double l =  this.X.get(this.X.size()-1);
          rf =  reg(this.X,this.Y,type);
          series2.add(f,(f*rf[0])+ rf[1]);
          series2.add(l,(l*rf[0])+ rf[1]);
          series2.setKey("Regression Function: " + "y = "+ String.format("%1.2f",rf[0]) + "x + " + String.format("%1.2f",rf[1]));
          dataset.addSeries(series2);
          break;
        case "2": // y = c10^kx
          rf =  reg(this.X,log(this.Y),type);
          rf[1]= Math.pow(Math.E,rf[1]);
          for (int i=0; i<this.X.size();i++){
            series2.add((double)this.X.get(i), rf[1]*Math.pow(Math.E,rf[0]*this.X.get(i)));
          }
          series2.setKey("Regression Function: " + "y = " + String.format("%1.2f",rf[1]) + "e ^" + String.format("%1.2f",rf[0])+ "x");
          dataset.addSeries(series2);
          break;
        case "3": // y = aln(x) + b 
          rf = reg(log(this.X),this.Y,type);
          for (int i=0; i<this.X.size();i++){
           series2.add((double)this.X.get(i), rf[0]*Math.log(this.X.get(i)) + rf[1]); 
          } 
          series2.setKey("Regression Function: " + "y = "+ String.format("%1.2f",rf[0]) + "ln(x) + " + String.format("%1.2f",rf[1]));
          dataset.addSeries(series2);
          break;
      }
      XYSeries series3 = new XYSeries("R2: " + String.format("%1.2f", rf[3]) + " RSME: " + String.format("%1.2f", rf[2]) );
      dataset.addSeries(series3);
      return dataset;
    }

    private static double avg(ArrayList<Double> d){
      double sum=0;
      for (int i = 0; i< d.size();i++){
        sum+=d.get(i);
      }
      return (sum/d.size());
    }

    private static double var(ArrayList<Double> d,double avg){
        double sum=0;
        for (int i = 0; i< d.size();i++){
          sum += Math.pow((d.get(i)-avg),2);
        }
        return sum;
    }

    private double coVar(ArrayList<Double> x,ArrayList<Double> y,double avgX,double avgY){
      double sum=0;
      for (int i = 0; i< x.size();i++){
        sum += (x.get(i) - avgX)*(y.get(i)-avgY);
      }
      return sum;
    }

    private ArrayList<Double> log(ArrayList<Double> d){
      ArrayList<Double> e= new ArrayList<Double>();
      for (int i = 0; i< d.size();i++){
        if (d.get(i)== 0.0){
          e.add(Math.log(0.0001));
        }
        else{
        e.add(Math.log(d.get(i)));
        }
      }
      return e;
    }

    private double[] reg(ArrayList<Double> x,ArrayList<Double> y,String type){
      double avgX = avg(x);
      double avgY = avg(y);
      double varX = var(x,avgX);
      double coVar = coVar(x,y,avgX,avgY);
      double m = coVar/varX;
      double b = avgY-(avgX*m);
      double rmse = RMSE(avgY,m,b,type);
      double r2 = R2(avgY,m,b,type);
      return new double[] {m,b,rmse,r2};
    }

    private double RMSE(double avgY,double m, double b,String type){
      double sum=0;
      if (type.equals("1")){
          for (int i = 0; i< this.Y.size();i++){
            sum+=Math.pow((this.Y.get(i)-((this.X.get(i)*m)+b)),2);
          }
          sum= sum/this.X.size();
          return Math.sqrt(sum);
      }
      else if(type.equals("2")){
          double c = Math.pow(Math.E,b);
          for (int i = 0; i< this.Y.size();i++){
            sum+=Math.pow(this.Y.get(i) - (c*Math.pow(Math.E,m*this.X.get(i))),2);
          }
          sum= sum/this.X.size();
          return Math.sqrt(sum);
      }
      else{
          for (int i = 0; i< this.Y.size();i++){
            sum+=Math.pow(this.Y.get(i) - (m*Math.log(this.X.get(i)) + b) ,2);
          }
          sum= sum/this.X.size();
          return Math.sqrt(sum);
      }
    }

    private double R2(double avgY, double m, double b,String type){
      double SSt = var(this.Y,avgY);
      double SSr=0;
      if (type.equals("1")){
          for (int i = 0; i< this.Y.size();i++){
            SSr+=Math.pow((this.Y.get(i)-((this.X.get(i)*m)+b)),2);
          }
          return 1 - (SSr/SSt) ; 
      }
      else if(type.equals("2")){
          double c = Math.pow(Math.E,b);
          for (int i = 0; i< this.Y.size();i++){
            SSr+=Math.pow(this.Y.get(i) - (c*Math.pow(Math.E,m*this.X.get(i))),2);
          }
          return 1 - (SSr/SSt) ; 
      }
      else{
          for (int i = 0; i< this.Y.size();i++){

            SSr+=Math.pow(this.Y.get(i) - (m*Math.log(this.X.get(i)) + b) ,2);
          }
          return 1 - (SSr/SSt) ; 
      }
    }
} 